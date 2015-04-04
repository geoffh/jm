package jmusic.library.backend.cd;

import jmusic.cdpoll.CDPoll;
import jmusic.cdpoll.CDPollFactory;
import jmusic.cdpoll.CDPollListener;
import jmusic.library.LibraryException;
import jmusic.library.LibraryItem;
import jmusic.library.backend.Backend;
import jmusic.library.backend.file.FileBackend;
import jmusic.util.JMusicProcess;
import jmusic.util.ProgressListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class CDBackend implements Backend, CDPollListener {
    public static final String sUriScheme = "cd";
    private static final String sTocFile = ".TOC.plist";

    private final Object mLock = new Object();
    private String mRootUri;
    private final CDPoll mCDPoll = CDPollFactory.getCDPoll();
    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    public CDBackend() {
        mCDPoll.addListener( this );
        mCDPoll.poll();
    }

    @Override
    public boolean canHandleUri( String inUri ) {
        return inUri.startsWith( sUriScheme );
    }

    @Override
    public void cdEjected() {
        synchronized( mLock ) {
            mRootUri = null;
        }
    }

    @Override
    public void cdInserted( String inUri ) {
        synchronized( mLock ) {
            mRootUri = inUri;
        }
    }

    @Override
    public void fixTrack( String inTrackUri, LibraryItem inProps ) throws LibraryException {
        throwUnsupported( "fixTrack isn't supported" );
    }

    public InputStream getTrackInputStream( String inTrackUri, ProgressListener inListener ) {
        // Todo: Test the change below
        //       Remove FFMPegProgress class
        try {
            //return new FileMP3Encoder().getInputStream( cdUriToFileUri( inTrackUri ), inListener );

            File theFile = cdUriToFile( inTrackUri );
            String theCommand[] = { "/usr/bin/ffmpeg", "-i", theFile.getAbsolutePath(), "-f", "mp3", "-" };
            JMusicProcess theProcess = new JMusicProcess();
            theProcess.execute( theCommand );
            if ( inListener != null ) {
                new FFMPegProgress( theProcess, inListener ).start();
            }
            return theProcess.getInputStream();
        } catch( Exception theException ) {
            mLogger.throwing( "CDBackend", "getTrackInputStream", theException );
            return null;
        }
    }

    @Override
    public LibraryItem getTrack( String inTrackUri ) throws LibraryException {
        synchronized( mLock ) {
            if ( mRootUri == null ) {
                return null;
            }
            try {
                LibraryItem theTrack = getTrack( cdUriToFile( inTrackUri ) );
                theTrack.setAlbumName( LibraryItem.sUnknown );
                theTrack.setArtistName( LibraryItem.sUnknown );
                String theTitle = cdUriToFile( inTrackUri ).getName();
                if ( theTitle != null ) {
                    int theIndex = theTitle.lastIndexOf( "." );
                    if ( theIndex > 0 ) {
                        theTitle = theTitle.substring( 0, theIndex );
                    }
                } else {
                    theTitle = LibraryItem.sUnknown;
                }
                theTrack.setTitle( theTitle );
                theTrack.setDuration( 0 );
                return theTrack;
            } catch ( URISyntaxException theException ) {
                mLogger.throwing( "CDBackend", "getTrack", theException );
                throw new LibraryException( theException );
            }
        }
    }

    @Override
    public void importTrack( Backend inSourceBackend, String inSourceTrackUri, LibraryItem inTargetTrackProperties, ProgressListener inListener )
        throws LibraryException {
        throwUnsupported( "importTrack isn't supported" );
    }

    @Override
    public boolean isWriteable() {
        return false;
    }

    @Override
    public Map< String, LibraryItem > listTracks() throws LibraryException {
        synchronized( mLock ) {
            final Map< String, LibraryItem > theTracks = new HashMap<>();
            if ( mRootUri != null ) {
                try {
                    File theRoot = cdUriToFile( mRootUri );
                    for ( File theFile : theRoot.listFiles() ) {
                        if ( isTOC( theFile ) ) {
                            continue;
                        }
                        LibraryItem theTrack = getTrack( theFile );
                        theTracks.put( theTrack.getUri(), theTrack );
                    }
                } catch ( URISyntaxException theException ) {
                    mLogger.throwing( "CDBackend", "listTracks", theException );
                    throw new LibraryException( theException );
                }
            }
            return theTracks;
        }
    }

    @Override
    public void updateTrack( String inTrackUri, LibraryItem inProps ) throws LibraryException {
        throwUnsupported( "updateTrack isn't supported" );
    }

    private File cdUriToFile( String inUri ) throws URISyntaxException {
        return new File( new URI( cdUriToFileUri( inUri ).replace( " ", "%20" ) ).getPath() );
    }

    private String cdUriToFileUri( String inUri ) {
        return FileBackend.sUriScheme + inUri.substring( sUriScheme.length() );
    }

    private String fileToCDUri( File inFile ) {
        String theFileUri = inFile.toURI().toString();
        return sUriScheme + ":" + theFileUri.substring( FileBackend.sUriScheme.length() + 1 );
    }

    private LibraryItem getTrack( File inFile ) {
        LibraryItem theItem = new LibraryItem();
        theItem.setLastModified( inFile.lastModified() );
        theItem.setUri( fileToCDUri( inFile ) );
        return theItem;
    }

    private boolean isTOC( File inFile ) {
        return sTocFile.equals( inFile.getName() );
    }

    private void throwUnsupported( String inMessage ) throws LibraryException {
        LibraryException theException = new LibraryException( inMessage );
        theException.setErrorCode( LibraryException.ErrorCode.UnsupportedOperation );
        throw theException;
    }

    class FFMPegProgress extends Thread {
        private final JMusicProcess mProcess;
        private final ProgressListener mListener;

        FFMPegProgress( JMusicProcess inProcess, ProgressListener inListener ) {
            mProcess = inProcess;
            mListener = inListener;
            setName( "FFMPegProgress" );
            setDaemon( true );
        }

        @Override
        public void run() {
            BufferedReader theReader = new BufferedReader( new InputStreamReader( mProcess.getErrorStream() ) );
            String theLine;
            try {
                double theDuration = 0;
                int theIndex;
                while ( ( theLine = theReader.readLine() ) != null ) {
                    if ( theDuration == 0 ) {
                        theIndex = theLine.indexOf( "Duration: " );
                        if ( theIndex != -1 ) {
                            theDuration = stringToDuration( theLine.substring( theIndex + 10, theIndex + 21 ) );
                        }
                    } else {
                        theIndex = theLine.indexOf( "time=" );
                        if ( theIndex != -1 ) {
                            double theTime = stringToDuration( theLine.substring( theIndex + 5, theIndex + 16 ) );
                            mListener.onProgress( ( int )( theTime * 100 / theDuration ) );
                        }
                    }
                }
            } catch( Exception theException ) {
                mListener.onErrorMessage( theException.getMessage() );
            } finally {
                mListener.onComplete();
            }
        }

        private double stringToDuration( String inDuration ) {
            double theDuration = 0;
            StringTokenizer theTokeniser = new StringTokenizer( inDuration, ":" );
            theDuration += Integer.valueOf( theTokeniser.nextToken() ) * 60 * 60;
            theDuration += Integer.valueOf( theTokeniser.nextToken() ) * 60;
            theDuration += Double.valueOf( theTokeniser.nextToken() );
            return theDuration;
        }
    }
}