package jmusic.cdpoll.macos;

import jmusic.cdpoll.AbstractCDPoll;
import jmusic.library.backend.cd.CDBackend;
import jmusic.util.JMusicExecutor;
import jmusic.util.JMusicProcess;

import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Logger;

public class CDPoll extends AbstractCDPoll {
    private static final String sDISKUtil[] = { "/usr/sbin/diskutil", "info", "device" };
    private static final String sDRUtil[] = { "/usr/bin/drutil", "status" };
    private boolean mHaveCD = false;
    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    @Override
    public void poll() {
        schedulePoll( 0 );
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

    private String getUri( String inDevice ) {
        DISKUtilCallback theCallback = new DISKUtilCallback();
        try {
            sDISKUtil[ 2 ] = inDevice;
            new JMusicProcess().execute( sDISKUtil, theCallback );
        } catch( IOException theException ) {
            mLogger.throwing( "CDPoll", "getUri", theException );
        }
        return CDBackend.sUriScheme + ":" + theCallback.getMountPoint();
    }

    private void schedulePoll( long inDelay) {
        JMusicExecutor.scheduleTask( new PollTask(), inDelay );
    }

    class PollTask extends TimerTask {
        public void run() {
            String theDevice = getDevice();
            if ( theDevice != null ) {
                if ( ! mHaveCD ) {
                    String theURI = getUri( theDevice );
                    if ( theURI != null ) {
                        mHaveCD = true;
                        notifyCDInserted( theURI );
                    }
                }
            } else {
                if ( mHaveCD ) {
                    mHaveCD = false;
                    notifyCDEjected();
                }
            }
            schedulePoll( 5000 );
        }
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
            if ( inInput.indexOf( "Type:" ) != -1 ) {
                if ( inInput.indexOf( "No Media" ) == -1 ) {
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