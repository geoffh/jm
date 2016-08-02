package jmusic.library.backend.cd.macos;

import jmusic.library.LibraryException;
import jmusic.util.JMusicProcess;
import jmusic.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class CDAccess implements jmusic.library.backend.cd.CDAccess {
    private static final String sDISKUtil[] = { "/usr/sbin/diskutil", "info", "device" };
    private static final String sDRUtil[] = { "/usr/bin/drutil", "status" };

    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    @Override
    public String getRootPath() {
        return getMountPoint( getDevice() );
    }

    @Override
    public InputStream getTrackInputStream( File inFile, ProgressListener inListener ) throws LibraryException {
        return null;
    }

    private String getDevice() {
        DRUtilCallback theCallback = new DRUtilCallback();
        try {
            new JMusicProcess().execute( sDRUtil, theCallback );
        } catch( IOException theException ) {
            mLogger.throwing( "CDPoll", "getDevice", theException );
        }
        return theCallback.getDevice();
    }

    private String getMountPoint( String inDevice ) {
        if ( inDevice == null ) {
            return null;
        }
        DISKUtilCallback theCallback = new DISKUtilCallback();
        try {
            sDISKUtil[ 2 ] = inDevice;
            new JMusicProcess().execute( sDISKUtil, theCallback );
        } catch( IOException theException ) {
            mLogger.throwing( "CDPoll", "getUri", theException );
        }
        return theCallback.getMountPoint();
    }

    class DRUtilCallback implements JMusicProcess.Callback {
        private String mDevice = null;

        @Override
        public boolean onStdErr( String inStdErr ) {
            return handleDRUtilInput( inStdErr );
        }

        @Override
        public boolean onStdOut( String inStdOut ) {
            return handleDRUtilInput( inStdOut );
        }

        String getDevice() { return mDevice; }

        private boolean handleDRUtilInput( String inInput ) {
            if ( inInput.contains( "Type:" ) ) {
                if ( ! inInput.contains( "No Media" ) ) {
                    int theIndex = inInput.indexOf( "Name:" );
                    if ( theIndex != -1 ) {
                        mDevice = inInput.substring( theIndex + 6 );
                    }
                }
                return false;
            }
            return true;
        }
    }

    class DISKUtilCallback implements JMusicProcess.Callback {
        private String mMountPoint = null;

        @Override
        public boolean onStdErr( String inStdErr ) {
            return handleDISKUtilInput( inStdErr );
        }

        @Override
        public boolean onStdOut( String inStdOut ) {
            return handleDISKUtilInput( inStdOut );
        }

        String getMountPoint() { return mMountPoint; }

        private boolean handleDISKUtilInput( String inInput ) {
            int theIndex = inInput.indexOf( "Mount Point:" );
            if ( theIndex != -1 ) {
                mMountPoint = inInput.substring( theIndex + 13 ).trim();
                return false;
            }
            return true;
        }
    }
}