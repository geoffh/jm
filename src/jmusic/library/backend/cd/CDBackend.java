package jmusic.library.backend.cd;

import jmusic.library.LibraryException;
import jmusic.library.LibraryItem;
import jmusic.library.backend.Backend;
import jmusic.library.backend.file.FileBackend;
import jmusic.util.JMusicExecutor;
import jmusic.util.ProgressListener;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Logger;

public class CDBackend implements Backend {
    public static final String sUriScheme = "cd";

    private final CDAccess mCDAccess = CDAccessFactory.getCDAccess();
    private final Object mLock = new Object();
    private String mRootUri;
    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    public CDBackend() {
        schedulePoll( 0 );
    }

    @Override
    public boolean canHandleUri( String inUri ) {
        return inUri.startsWith( sUriScheme );
    }

    @Override
    public void fixTrack( String inTrackUri, LibraryItem inProps ) throws LibraryException {
        throwUnsupported( "fixTrack" );
    }

    @Override
    public InputStream getTrackInputStream( String inTrackUri, ProgressListener inListener ) throws LibraryException {
        synchronized( mLock ) {
            if ( mRootUri == null ) {
                return null;
            }
            return mCDAccess.getTrackInputStream( cdUriToFile( inTrackUri ), inListener );
        }
    }

    @Override
    public LibraryItem getTrack( String inTrackUri ) throws LibraryException {
        synchronized( mLock ) {
            if ( mRootUri == null ) {
                return null;
            }
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
        }
    }

    @Override
    public void importTrack( Backend inSourceBackend, String inSourceTrackUri, LibraryItem inTargetTrackProperties, ProgressListener inListener ) throws LibraryException {
        throwUnsupported( "importTrack" );
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
                File theRoot = cdUriToFile( mRootUri );
                for ( File theFile : theRoot.listFiles() ) {
                    LibraryItem theTrack = getTrack( theFile );
                    theTracks.put( theTrack.getUri(), theTrack );
                }
            }
            return theTracks;
        }
    }

    @Override
    public void updateTrack( String inTrackUri, LibraryItem inProps ) throws LibraryException {
        throwUnsupported( "updateTrack" );
    }

    private File cdUriToFile( String inUri ) throws LibraryException {
        String theUri = FileBackend.sUriScheme + inUri.substring( sUriScheme.length() );
        try {
            return new File( new URI( theUri.replace( " ", "%20" ) ).getPath() );
        } catch( URISyntaxException theException ) {
            throw new LibraryException( theException );
        }
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

    private void schedulePoll( long inDelay ) {
        JMusicExecutor.scheduleTask( new CDPollTask(), inDelay );
    }

    private void throwUnsupported( String inOperation ) throws LibraryException {
        LibraryException theException = new LibraryException( inOperation + " isn't supported" );
        theException.setErrorCode( LibraryException.ErrorCode.UnsupportedOperation );
        throw theException;
    }

    class CDPollTask extends TimerTask {
        public void run() {
            String theRootPath = mCDAccess.getRootPath();
            synchronized( mLock ) {
                mRootUri = theRootPath != null ? sUriScheme + ":" + theRootPath : null;
            }
            schedulePoll( 5000 );
        }
    }
}
