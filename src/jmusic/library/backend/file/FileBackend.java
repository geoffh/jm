package jmusic.library.backend.file;

import jmusic.library.LibraryException;
import jmusic.library.LibraryItem;
import jmusic.library.backend.Backend;
import jmusic.util.ProgressListener;
import org.farng.mp3.AbstractMP3Tag;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.AbstractID3v2Frame;
import org.farng.mp3.id3.FrameBodyAPIC;
import org.farng.mp3.id3.ID3v2_4;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class FileBackend implements Backend {
    public static final String sUriScheme = "file";
    private static final String sAudioFileSuffix = ".mp3";
    private static final long sAPICEncodingISO88591 = 0;
    private static final long sAPICEncodingUTF8 = 3;
    
    private final String mRootUri;
    private final Logger mLogger = Logger.getLogger( FileBackend.class.getName() );
    
    public FileBackend( String inRootUri ) {
        mRootUri = inRootUri;
    }
    
    @Override
    public boolean canHandleUri( String inUri ) {
        boolean canHandle = false;
        try {
            URI theUri = new URI( inUri );
            if ( ! sUriScheme.equals( theUri.getScheme() ) ) {
                return false;
            }
            File theRootFile = uriToFile( mRootUri );
            File theFile = uriToFile( inUri );
            while ( theFile != null ) {
                if ( theFile.equals( theRootFile ) ) {
                    canHandle = true;
                    break;
                }
                theFile = theFile.getParentFile();
            }
        } catch( Exception theException ) {
            mLogger.throwing( "FileBackend", "canHandle", theException );
        }
        return canHandle;
    }

    @Override
    public void fixTrack( String inTrackUri, LibraryItem inItem )
        throws LibraryException {
        if ( inItem.isEmpty() ) {
            return;
        }
        try {
            MP3File theFile = new MP3File( uriToFile( inTrackUri ) );
            AbstractMP3Tag theTag = new ID3v2_4();
            boolean needsSave = false;
            String theValue = inItem.getArtistName();
            if ( theValue != null ) {
                theValue = theValue.trim();
                if ( theValue.length() > 0 ) {
                    needsSave = true;
                    theTag.setLeadArtist( theValue );
                }
            }
            theValue = inItem.getAlbumName();
            if ( theValue != null ) {
                theValue = theValue.trim();
                if ( theValue.length() > 0 ) {
                    needsSave = true;
                    theTag.setAlbumTitle( theValue );
                }
            }
            theValue = inItem.getTitle();
            if ( theValue != null ) {
                theValue = theValue.trim();
                if ( theValue.length() > 0 ) {
                    needsSave = true;
                    theTag.setSongTitle( theValue );
                }
            }
            Integer theTrackNumber = inItem.getTrackNumber();
            if ( theTrackNumber != null && theTrackNumber > 0 ) {
                needsSave = true;
                theTag.setTrackNumberOnAlbum( String.valueOf(  theTrackNumber ) );
            }
            if ( needsSave ) {
                theFile.setID3v2Tag( theTag );
                save( theFile );
            }
        } catch( URISyntaxException | IOException | TagException theException ) {
            mLogger.throwing( "FileBackend", "update", theException );
            throw new LibraryException( theException );
        }
    }

    @Override
    public InputStream getThumbnailInputStream( String inUri ) throws LibraryException {
        try {
            File theFile = uriToFile( inUri );
            if ( ! theFile.exists() || ! theFile.isFile() ) {
                return null;
            }
            AbstractID3v2Frame theFrame = getAPIC( new MP3File( theFile ) );
            FrameBodyAPIC theFrameBody = ( FrameBodyAPIC )theFrame.getBody();
            byte[] theData = ( byte[] ) theFrameBody.getObject( "Picture Data" );
            Long theEncoding = ( Long )theFrameBody.getObject( "Text Encoding" );
            String theMimeType = ( String )theFrameBody.getObject( "MIME Type" );
            InputStream theStream;
            if ( sAPICEncodingUTF8 == theEncoding || "image/png".equalsIgnoreCase( theMimeType ) ) {
                theStream = new ByteArrayInputStream( theData, 1, theData.length - 1 );
            } else {
               theStream = new ByteArrayInputStream( theData );
            }
            return new BufferedInputStream( theStream );
        } catch ( Exception theException ) {
            mLogger.throwing(  "FileBackend", "getInputStream", theException );
            throw new LibraryException( theException );
        }
    }

    @Override
    public InputStream getTrackInputStream( String inTrackUri, ProgressListener inListener )
        throws LibraryException {
        try {
            return new BufferedInputStream( new FileInputStream( uriToFile( inTrackUri ) ) );
        } catch( URISyntaxException | FileNotFoundException theException ) {
            mLogger.throwing(  "FileBackend", "getInputStream", theException );
            throw new LibraryException( theException );
        }
    }
    
    @Override
    public LibraryItem getTrack( String inTrackUri )
            throws LibraryException {
        try {
            File theFile = uriToFile( inTrackUri );
            LibraryItem theItem = getTrack( theFile );
            theItem.putAll( getTrackProperties( theFile ) );
            return theItem;
        } catch( URISyntaxException theException ) {
            throw new LibraryException( theException );
        }
    }
    
    @Override
    public void importTrack( Backend inSourceBackend, String inSourceTrackUri, LibraryItem inItem, ProgressListener inListener  )
        throws LibraryException {
        try {
            File theTmpFile =
                File.createTempFile(
                    String.valueOf( System.currentTimeMillis() ),
                    sAudioFileSuffix );
            Files.copy(
                inSourceBackend.getTrackInputStream( inSourceTrackUri, inListener ),
                theTmpFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING );
            updateTrack( fileToUri( theTmpFile ), inItem );
            File theTargetFile = createUniqueTargetFile( getTrackProperties( theTmpFile ) );
            theTargetFile.getParentFile().mkdirs();
            theTmpFile.renameTo( theTargetFile );
        } catch( Exception theException ) {
            mLogger.throwing( "FileBackend", "import", theException );
            throw new LibraryException( theException );
        }
    }

    @Override
    public boolean isWriteable() { return true; }

    @Override
    public Map< String, LibraryItem > listTracks() throws LibraryException {
        final Map< String, LibraryItem > theTracks = new HashMap<>();
        try {
            URI theUri = new URI( mRootUri );
            Files.walkFileTree( new File( theUri ).toPath(),
                new FileVisitor< Path >() {
                    @Override
                    public FileVisitResult postVisitDirectory( Path inDir, IOException inException ) {
                        return FileVisitResult.CONTINUE;
                    }
                        
                    @Override
                    public FileVisitResult preVisitDirectory( Path inFile, BasicFileAttributes inAttrs )
                            throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                        
                    @Override
                    public FileVisitResult visitFile( Path inFile, BasicFileAttributes inAttrs )
                            throws IOException {
                        if ( isAudioFile( inFile ) ) {
                            LibraryItem theTrack = getTrack( inFile.toFile() );
                            theTracks.put( theTrack.getUri(), theTrack );
                        }
                        return FileVisitResult.CONTINUE;
                    }
                        
                    @Override
                    public FileVisitResult visitFileFailed( Path inFile, IOException inException )
                            throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                } );
        } catch( URISyntaxException | IOException theException ) {
            mLogger.throwing( "FileBackend", "listTracks", theException );
            throw new LibraryException( theException );
        }
        return theTracks;
    }
    
    @Override
    public void updateTrack( String inTrackUri, LibraryItem inItem )
        throws LibraryException {
        if ( inItem.isEmpty() ) {
            return;
        }
        try {
            File theOriginalFile = uriToFile( inTrackUri );
            MP3File theFile = new MP3File( theOriginalFile );
            AbstractMP3Tag theTag = getTag( theFile );
            boolean needsSave = false;
            String theArtist = inItem.getArtistName();
            if ( theArtist != null ) {
                theArtist = theArtist.trim();
                if ( theArtist.length() > 0 && ! theArtist.equals( theTag.getLeadArtist() ) ) {
                    needsSave = true;
                    theTag.setLeadArtist( theArtist );
                }
            }
            String theAlbum = inItem.getAlbumName();
            if ( theAlbum != null ) {
                theAlbum = theAlbum.trim();
                if ( theAlbum.length() > 0 && ! theAlbum.equals( theTag.getAlbumTitle() ) ) {
                    needsSave = true;
                    theTag.setAlbumTitle( theAlbum );
                }
            }
            String theTitle = inItem.getTitle();
            if ( theTitle != null ) {
                theTitle = theTitle.trim();
                if ( theTitle.length() > 0 && ! theTitle.equals( theTag.getSongTitle() ) ) {
                    needsSave = true;
                    theTag.setSongTitle( theTitle );
                }
            }
            Integer theTrackNumber = inItem.getTrackNumber();
            if ( theTrackNumber != null && theTrackNumber > 0 ) {
                String theNumber = String.valueOf( theTrackNumber );
                if ( ! theNumber.equals( theTag.getTrackNumberOnAlbum() ) ) {
                    needsSave = true;
                    theTag.setTrackNumberOnAlbum( String.valueOf( theTrackNumber ) );
                }
            }
            if ( needsSave ) {
                theFile.setID3v2Tag( theTag );
                save( theFile );
                relocateFile( theOriginalFile, theArtist, theAlbum, theTitle );
            }
        } catch( URISyntaxException | IOException | TagException theException ) {
            mLogger.throwing( "FileBackend", "update", theException );
            throw new LibraryException( theException );
        }
    }
    
    private String createAlbumUri( String inArtist, String inAlbum ) {
        return createArtistUri( inArtist ) + "/" + inAlbum;
    }
    
    private String createArtistUri( String inArtist ) {
        return mRootUri + "/" + inArtist;
    }

    private File createUniqueTargetFile( LibraryItem inItem ) throws URISyntaxException {
        String theFileName =
            mRootUri +
                File.separator +
                inItem.getArtistName() +
                File.separator +
                inItem.getAlbumName() +
                File.separator +
                inItem.getTitle();
        File theTargetFile;
        String theIndex = "";
        int theCounter = 1;
        while ( true ) {
            String theTargetUri = theFileName + theIndex + sAudioFileSuffix;
            theTargetFile = uriToFile( theTargetUri );
            if ( ! theTargetFile.exists() ) {
                break;
            }
            theIndex = String.valueOf( theCounter );
            ++ theCounter;
        }
        return theTargetFile;
    }
    
    private String fileToUri( File inFile ) {
        return inFile.toURI().toString();
    }
    
    private LibraryItem getTrack( File inFile ) {
        LibraryItem theItem = new LibraryItem();
        theItem.setLastModified( inFile.lastModified() );
        theItem.setUri( fileToUri( inFile ) );
        return theItem;
    }
    
    private AbstractMP3Tag getTag( MP3File inFile ) throws LibraryException {
        boolean isV2 = inFile.hasID3v2Tag();
        AbstractMP3Tag theTag = isV2 ?
            inFile.getID3v2Tag() : inFile.getID3v1Tag();
        if ( theTag == null ) {
            LibraryException theException =
                new LibraryException( "Error: Failed to get ID3 tag for file '" +
                    inFile.getMp3file() .getAbsolutePath() + "'" );
            theException.setErrorCode( LibraryException.ErrorCode.NoTag );
            throw theException;
        }
        return theTag;
    }

    private LibraryItem getTrackProperties( File inFile ) throws LibraryException {
        LibraryItem theItem = new LibraryItem();
        try {
            MP3File theFile = new MP3File( inFile );
            theFile.seekMP3Frame();
            AbstractMP3Tag theTag = getTag( theFile );
            String theArtist = theTag.getLeadArtist();
            if ( theArtist == null || theArtist.length() == 0 ) {
                theArtist = LibraryItem.sUnknown;
            }
            theItem.setArtistName( theArtist );
            theItem.setArtistUri( createArtistUri( theArtist ) );
            String theAlbumTitle = theTag.getAlbumTitle();
            if ( theAlbumTitle == null || theAlbumTitle.length() == 0 ) {
                theAlbumTitle = LibraryItem.sUnknown;
            }
            theItem.setAlbumName( theAlbumTitle );
            theItem.setAlbumUri( createAlbumUri( theArtist, theAlbumTitle ) );
            int theBitRate = theFile.getBitRate();
            theItem.setBitRate( theBitRate );
            int theDuration = ( int )( ( ( inFile.length() / 1024 ) * 8 ) / theBitRate);
            theItem.setDuration( theDuration );
            String theTitle = theTag.getSongTitle();
            if ( theTitle == null || theTitle.length() == 0 ) {
                theTitle = LibraryItem.sUnknown;
            }
            theItem.setSize( inFile.length() );
            theItem.setTitle( theTitle );
            String theTrackNumber = null;
            if ( theFile.hasID3v2Tag() ) {
                theTrackNumber = theTag.getTrackNumberOnAlbum();
                if ( theTrackNumber != null && theTrackNumber.length() > 0 ) {
                    if ( "0".equals(  theTrackNumber ) ) {
                        theTrackNumber = null;
                    } else {
                        // Some track numbers are list as #trackNumber/#trackCount
                        int theIndex = theTrackNumber.indexOf( "/" );
                        if ( theIndex > -1 ) {
                            theTrackNumber = theTrackNumber.substring( 0, theIndex );
                        }
                    }
                }
            }
            theItem.setTrackNumber( theTrackNumber != null && theTrackNumber.length() > 0 ?
                Integer.valueOf( theTrackNumber ) : LibraryItem.sTrackNumberUnknown );
            theItem.setHasThumbnail( getAPIC( theFile ) != null );
        } catch( IOException | TagException | UnsupportedOperationException  theException ) {
            throw new LibraryException( theException );
        }
        return theItem;
    }

    private AbstractID3v2Frame getAPIC( MP3File inFile ) {
        if ( ! inFile.hasID3v2Tag() ) {
            return null;
        }
        AbstractID3v2 theTag = inFile.getID3v2Tag();
        Iterator theIterator = theTag.getFrameIterator();
        AbstractID3v2Frame theFrame = null;
        while ( theIterator.hasNext() ) {
            theFrame = ( AbstractID3v2Frame )theIterator.next();
            if ( theFrame.getIdentifier().startsWith( "APIC" ) ) {
                break;
            }
            theFrame = null;
        }
        return theFrame;
    }
    
    private boolean isAudioFile( File inFile ) {
        return inFile.getName().toLowerCase().endsWith( sAudioFileSuffix );
    }
    
    private boolean isAudioFile( Path inPath ) {
        return isAudioFile( inPath.toFile() );
    }

    private void relocateFile( File inFile, String inArtist, String inAlbum, String inTitle ) throws URISyntaxException {
        if ( inArtist == null || inAlbum == null || inTitle == null ) {
            return;
        }
        File theFile = uriToFile( mRootUri + File.separator + inArtist + File.separator + inAlbum + File.separator + inTitle + sAudioFileSuffix );
        if ( theFile.exists() ) {
            return;
        }
        theFile.getParentFile().mkdirs();
        inFile.renameTo( theFile );
    }

    private void save( MP3File inFile ) throws IOException, TagException {
        File theFile = inFile.getMp3file();
        File theBackupFile = new File( theFile.getPath().concat( ".bak" ) );
        Files.copy( theFile.toPath(), new FileOutputStream( theBackupFile ) );
        inFile.save( theFile );
        theBackupFile.delete();
    }

    private File uriToFile( String inUri ) throws URISyntaxException {
        return new File( new URI( inUri.replace( " ", "%20" ).replace("?", "%3F") ).getPath() );
    }
}