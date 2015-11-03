package jmusic.library.backend.cd.linux;

import jmusic.library.LibraryException;
import jmusic.util.FFMpeg;
import jmusic.util.JMusicProcess;
import jmusic.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class CDAccess implements jmusic.library.backend.cd.CDAccess {
    private static final String[] sMount = { "/bin/mount", "-t", "fuse.gvfsd-fuse" };

    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    @Override
    public String getRootPath() {
        MountCallback theCallback = new MountCallback();
        try {
            new JMusicProcess().execute( sMount, theCallback );
        } catch( IOException theException ) {
            mLogger.throwing( "CDPoll", "getDevice", theException );
        }
        return theCallback.getPath();
    }

    @Override
    public InputStream getTrackInputStream( File inFile, ProgressListener inListener ) throws LibraryException {
        try {
            return FFMpeg.fileToMP3Stream( inFile, inListener );
        } catch( IOException theException ) {
            mLogger.throwing( "CDAccess", "getTrackInputStream", theException );
            throw new LibraryException( theException );
        }
    }

    class MountCallback implements JMusicProcess.Callback {
        private String mPath;

        String getPath() {
            return mPath;
        }

        @Override
        public boolean onStdErr( String inStdErr ) {
            StringTokenizer theTokeniser = new StringTokenizer( inStdErr );
            if ( theTokeniser.countTokens() < 3 ) {
                return true;
            }
            theTokeniser.nextToken();
            if ( ! "on".equals( theTokeniser.nextToken() ) ) {
                return true;
            }
            File theParent = new File( theTokeniser.nextToken() );
            for ( String theChild : theParent.list() ) {
                if ( theChild.startsWith( "cdda" ) ) {
                    mPath = theParent.getAbsolutePath() + File.separator + theChild;
                    break;
                }
            }
            return false;
        }
    }
}