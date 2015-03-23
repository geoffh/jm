package jmusic.device.upnp.discovery;

import jmusic.device.MediaDeviceDiscoverer;
import jmusic.device.MediaDeviceDiscoveryListener;
import jmusic.device.MediaRendererDevice;
import jmusic.device.MediaServerDevice;
import jmusic.device.upnp.UPNPUtils;
import jmusic.device.upnp.UPNPService;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UPNPMediaDeviceDiscoverer implements MediaDeviceDiscoverer, RegistryListener {
    private final List< MediaDeviceDiscoveryListener > mListeners = new ArrayList<>();
    private final Logger mLogger = Logger.getLogger( getClass().getName() );
    private DiscoveryThread mMediaDeviceDiscoveryThread;

    @Override
    public void addListener( MediaDeviceDiscoveryListener inListener ) {
        synchronized( mListeners ) {
            mListeners.add( inListener );
        }
    }

    @Override
    public void removeListener( MediaDeviceDiscoveryListener inListener ) {
        synchronized( mListeners ) {
            mListeners.remove( inListener );
        }
    }

    @Override
    synchronized public void startDiscovery() {
        stopDiscovery();
        mMediaDeviceDiscoveryThread = new DiscoveryThread( this );
        mMediaDeviceDiscoveryThread.start();
    }

    @Override
    synchronized public void stopDiscovery() {
        if ( mMediaDeviceDiscoveryThread != null ) {
            mMediaDeviceDiscoveryThread.interrupt();
            mMediaDeviceDiscoveryThread = null;
        }
    }

    @Override
    public void remoteDeviceDiscoveryStarted( Registry inRegistry, RemoteDevice inDevice ) {}

    @Override
    public void remoteDeviceDiscoveryFailed( Registry inRegistry, RemoteDevice inDevice, Exception inException ) {
        mLogger.warning( "Remote Device Discovery failed:" + inDevice );
        mLogger.throwing( "UPNPMediaDeviceDiscoverer", "run", inException );
    }

    @Override
    public void remoteDeviceAdded( Registry inRegistry, RemoteDevice inDevice ) {
        synchronized( mListeners ) {
            deviceAdded( inDevice );
        }
    }

    @Override
    public void remoteDeviceUpdated( Registry inRegistry, RemoteDevice inDevice ) {}

    @Override
    public void remoteDeviceRemoved( Registry inRegistry, RemoteDevice inDevice ) {
        synchronized( mListeners ) {
            deviceRemoved( inDevice );
        }
    }

    @Override
    public void localDeviceAdded( Registry inRegistry, LocalDevice inDevice ) {
        synchronized( mListeners ) {
            deviceAdded( inDevice );
        }
    }

    @Override
    public void localDeviceRemoved( Registry inRegistry, LocalDevice inDevice ) {
        synchronized( mListeners ) {
            deviceRemoved( inDevice );
        }
    }

    @Override
    public void beforeShutdown( Registry registry ) {}

    @Override
    public void afterShutdown() {}

    private void deviceAdded( Device inDevice ) {
        boolean isMediaRendererDevice = UPNPUtils.isMediaRendererDevice( inDevice );
        boolean isMediaServerDevice = UPNPUtils.isMediaServerDevice( inDevice );
        if ( ! isMediaRendererDevice && ! isMediaServerDevice ) {
            return;
        }
        UPNPMediaDevice theDevice = isMediaRendererDevice ?
            new UPNPMediaRendererDevice( inDevice ) : new UPNPMediaServerDevice( inDevice );
        synchronized( mListeners ) {
            for ( MediaDeviceDiscoveryListener theListener : mListeners ) {
                if ( isMediaRendererDevice ) {
                    theListener.onMediaRendererDeviceAdded( ( MediaRendererDevice )theDevice );
                } else {
                    theListener.onMediaServerDeviceAdded( ( MediaServerDevice )theDevice );
                }
            }
        }
    }

    private void deviceRemoved( Device inDevice ) {
        boolean isMediaRendererDevice = UPNPUtils.isMediaRendererDevice( inDevice );
        boolean isMediaServerDevice = UPNPUtils.isMediaServerDevice( inDevice );
        if ( ! isMediaRendererDevice && ! isMediaServerDevice ) {
            return;
        }
        UPNPMediaDevice theDevice = isMediaRendererDevice ?
            new UPNPMediaRendererDevice( inDevice ) : new UPNPMediaServerDevice( inDevice );
        synchronized( mListeners ) {
            for ( MediaDeviceDiscoveryListener theListener : mListeners ) {
                if ( isMediaRendererDevice ) {
                    theListener.onMediaRendererDeviceRemoved( ( MediaRendererDevice ) theDevice );
                } else {
                    theListener.onMediaServerDeviceRemoved( ( MediaServerDevice ) theDevice );
                }
            }
        }
    }

    class DiscoveryThread extends Thread {
        private final RegistryListener mRegistryListener;

        DiscoveryThread( RegistryListener inListener ) {
            mRegistryListener = inListener;
        }

        public void run() {
            mLogger.info( "UPNP Device Discovery Starting" );
            UPNPService.getService().getRegistry().addListener( mRegistryListener );
            UPNPService.getService().getControlPoint().search( new STAllHeader() );
        }
    }
}