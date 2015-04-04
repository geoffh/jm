package jmusic.oldneedsrewrite.upnp.mediarenderercontrolpoint;


import jmusic.device.MediaRendererDevice;
import jmusic.device.MediaRendererDeviceRemoteControl;
import jmusic.device.upnp.UPNPService;
import jmusic.device.upnp.discovery.UPNPMediaRendererDevice;
import jmusic.library.LibraryItem;
import jmusic.oldneedsrewrite.http.HttpServer;
import jmusic.util.ProgressListener;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.support.avtransport.callback.Pause;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.avtransport.callback.Stop;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.TransportState;
import org.teleal.cling.support.model.VolumeDBRange;

import java.util.ArrayList;
import java.util.logging.Logger;

public class UPNPMediaRendererDeviceRemoteControl implements MediaRendererDeviceRemoteControl {
    private static final ServiceId sAVTransportServiceId =
        new UDAServiceId( "AVTransport" );
    private static final ServiceId sRenderingControlServiceId =
        new UDAServiceId( "RenderingControl" );
    private final Device mDevice;
    private final Logger mLogger = Logger.getLogger( getClass().getName() );
    private final ArrayList< ProgressListener > mProgressListeners =
        new ArrayList< ProgressListener >();
    private ProgressListenerThread mProgressListenerThread;

    public UPNPMediaRendererDeviceRemoteControl( MediaRendererDevice inDevice ) {
        mDevice = ( ( UPNPMediaRendererDevice )inDevice ).getDevice();
    }

    public void addProgressListener( ProgressListener inListener ) {
        synchronized( mProgressListeners ) {
            mProgressListeners.add( inListener );
            if ( mProgressListeners.size() == 1 ) {
                listenToProgress( true );
            }
        }
    }

    public String getName() {
        return mDevice.getDisplayString();
    }

    public MediaInfo getMediaInfo() {
        GetMedInfo theGetMedInfo = new GetMedInfo( getAVTransportService() );
        MediaAction theAction = new MediaAction( theGetMedInfo );
        UPNPService.getService().getControlPoint().execute( theAction );
        theAction.waitForComplete();
        return theGetMedInfo.getMediaInfo();
    }

    public PositionInfo getPositionInfo() {
        GetPosInfo theGetPosInfo = new GetPosInfo( getAVTransportService() );
        MediaAction theAction = new MediaAction( theGetPosInfo );
        UPNPService.getService().getControlPoint().execute( theAction );
        theAction.waitForComplete();
        return theGetPosInfo.getPositionInfo();
    }

    public TransportInfo getTransportInfo() {
        GetTranspInfo theGetTranspInfo = new GetTranspInfo( getAVTransportService() );
        MediaAction theAction = new MediaAction( theGetTranspInfo );
        UPNPService.getService().getControlPoint().execute( theAction );
        theAction.waitForComplete();
        return theGetTranspInfo.getTransportInfo();
    }

    public void pause() {
        MediaAction theAction = new MediaAction(
            new Pause( getAVTransportService() ) {
                @Override
                public void failure( ActionInvocation inInvocation,
                                     UpnpResponse inResponse,
                                     String inDefaultMsg ) {
                    mLogger.info( inDefaultMsg );
                } } );
        UPNPService.getService().getControlPoint().execute( theAction );
        theAction.waitForComplete();
    }

    public void play( LibraryItem inTrack ) {
        setURI( inTrack );
        resume();
    }

    public void removeProgressListener( ProgressListener inListener ) {
        synchronized( mProgressListeners ) {
            mProgressListeners.remove( inListener );
            if ( mProgressListeners.size() == 0 ) {
                listenToProgress( false );
            }
        }
    }

    public void resume() {
        MediaAction theAction = new MediaAction(
            new Play( getAVTransportService() ) {
                @Override
                public void failure( ActionInvocation inInvocation,
                                     UpnpResponse inResponse,
                                     String inDefaultMsg ) {
                    mLogger.info( inDefaultMsg );
                } } );
        UPNPService.getService().getControlPoint().execute( theAction );
        theAction.waitForComplete();
    }

    public VolumeDBRange getVolumeDBRange() {
        GetVolumeDBRange theGetVolumeDBRange =
            new GetVolumeDBRange( getRenderingControlService() );
        MediaAction theAction = new MediaAction( theGetVolumeDBRange, 1 );
        UPNPService.getService().getControlPoint().execute( theAction );
        theAction.waitForComplete();
        return theGetVolumeDBRange.getRange();
    }

    public void setURI( LibraryItem inTrack ) {
        String theURL = HttpServer.getInstance().getURL( inTrack.getUri() );
        String theDuration = ModelUtil.toTimeString( inTrack.getDuration() );
        MediaAction theAction = new MediaAction(
            new SetAVTransportURI( getAVTransportService(), theURL, theDuration ) {
                @Override
                public void failure( ActionInvocation inInvocation,
                                     UpnpResponse inResponse,
                                     String inDefaultMsg ) {
                    mLogger.info( inDefaultMsg );
                } } );
        UPNPService.getService().getControlPoint().execute( theAction );
        theAction.waitForComplete();
    }

    public void setVolume( int inVolume ) {
        short theVolumeDB = percentToVolumeDB( inVolume );
        SetVolumeDB theSetVol = new SetVolumeDB( getRenderingControlService(), theVolumeDB );
        MediaAction theAction = new MediaAction( theSetVol );
        UPNPService.getService().getControlPoint().execute( theAction );
        theAction.waitForComplete();
    }

    public void stop() {
        MediaAction theAction = new MediaAction(
            new Stop( getAVTransportService() ) {
                @Override
                public void failure( ActionInvocation inInvocation,
                                     UpnpResponse inResponse,
                                     String inDefaultMsg ) {} } );
        UPNPService.getService().getControlPoint().execute( theAction );
        theAction.waitForComplete();
    }

    short percentToVolumeDB( int inPercent ) {
        VolumeDBRange theRange = getVolumeDBRange();
        int theMinValue = theRange.getMinValue();
        int theMaxValue = theRange.getMaxValue();
        return ( short )( theMinValue +
               ( ( ( theMaxValue - theMinValue ) * inPercent ) / 100 ) );
    }

    @Override
    public String toString() { return mDevice.getDetails().getFriendlyName(); }

    private Service getAVTransportService() {
        return mDevice.findService( sAVTransportServiceId );
    }

    private Service getRenderingControlService() {
        return mDevice.findService( sRenderingControlServiceId );
    }

    private void listenToProgress( boolean inListen ) {
        if ( mProgressListenerThread != null ) {
            mProgressListenerThread.terminate();
            mProgressListenerThread = null;
        }
        if ( inListen ) {
            mProgressListenerThread = new ProgressListenerThread();
            mProgressListenerThread.start();
        }
    }

    class ProgressListenerThread extends Thread {
        private boolean shouldRun = true;
        private final Object mLock = new Object();

        ProgressListenerThread() {
            setName( "ProgressListenerThread" );
            setDaemon( true );
        }

        @Override
        public void run() {
            while ( shouldRun() ) {
                try {
                    TransportInfo theTransportInfo = getTransportInfo();
                    if ( theTransportInfo.getCurrentTransportState() !=
                            TransportState.PLAYING ) {
                        notifyListeners( 100 );
                    } else {
                        PositionInfo thePositionInfo = getPositionInfo();
                        MediaInfo theMediaInfo = getMediaInfo();
                        if ( thePositionInfo != null && theMediaInfo != null ) {
                            long theDuration = ModelUtil.fromTimeString( theMediaInfo.getCurrentURIMetaData() );
                            long theElapsed  = ModelUtil.fromTimeString( thePositionInfo.getRelTime() );
                            long thePercent = ( theElapsed * 100 ) / theDuration;
                            notifyListeners( ( int )thePercent );
                        }
                    }
                } catch( Exception theIgnore ) {}
                try {
                    sleep( 1000 );
                } catch( Exception theIgnore ) {}
            }
        }

        void terminate() {
            synchronized( mLock ) {
                shouldRun = false;
            }
        }

        private void notifyListeners( int inPercentComplete ) {
            synchronized( mProgressListeners ) {
                for ( ProgressListener theListener : mProgressListeners ) {
                    theListener.onProgress( inPercentComplete );
                }
            }
        }

        private boolean shouldRun() {
            synchronized( mLock ) {
                return shouldRun;
            }
        }
    }
}
