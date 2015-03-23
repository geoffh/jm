package jmusic.device;

import jmusic.device.upnp.discovery.UPNPMediaDeviceDiscoverer;
import jmusic.device.upnp.localdevice.UPNPMediaRendererDevice;
import jmusic.device.upnp.localdevice.UPNPMediaServerDevice;
import jmusic.library.Library;
import jmusic.oldneedsrewrite.upnp.mediarenderercontrolpoint.UPNPMediaRendererDeviceRemoteControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class MediaDeviceManager implements MediaDeviceDiscoveryListener {
    private static final MediaDeviceManager sInstance = new MediaDeviceManager();

    private Library mLibrary;
    private final List< MediaDeviceDiscoveryListener > mListeners = new ArrayList<>();
    private final List< MediaDeviceDiscoverer > mDiscoverers = new ArrayList<>();
    private final List< MediaServerDevice > mMediaServerDevices = new ArrayList<>();
    private final List< MediaRendererDevice > mMediaRendererDevices = new ArrayList<>();
    private final HashMap< MediaDevice.Protocol, Object > mLocalMediaServerDevices = new HashMap<>();
    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    private MediaDeviceManager() {}

    public void addListener( MediaDeviceDiscoveryListener inListener ) {
        synchronized( mListeners ) {
            mListeners.add( inListener );
        }
    }

    public void createDefaultMediaRendererDevices() {
        new UPNPMediaRendererDevice();
    }

    public void createMediaServerDeviceForRendererDevice( MediaRendererDevice inMediaRendererDevice ) {
        MediaDevice.Protocol theProtocol = inMediaRendererDevice.getProtocol();
        synchronized( mLocalMediaServerDevices ) {
            if ( ! mLocalMediaServerDevices.containsKey( theProtocol ) ) {
                // Only UPNP at the moment
                mLocalMediaServerDevices.put( theProtocol, new UPNPMediaServerDevice( mLibrary ) );
            }
        }
    }

    public MediaRendererDeviceRemoteControl createRemoteControl( MediaRendererDevice inMediaRendererDevice ) {
        // Only UPNP at the moment
        return new UPNPMediaRendererDeviceRemoteControl( inMediaRendererDevice );
    }

    synchronized public void init( Library inLibrary ) {
        if ( mLibrary == null ) {
            mLibrary = inLibrary;
        }
    }

    public static MediaDeviceManager instance() {
        return sInstance;
    }

    @Override
    public void onMediaRendererDeviceAdded( MediaRendererDevice inMediaRendererDevice ) {
        mLogger.fine( "MediaRenderer Added:" + inMediaRendererDevice.getName() );
        synchronized( mMediaRendererDevices ) {
            mMediaRendererDevices.add( inMediaRendererDevice );
        }
        synchronized( mListeners ) {
            for ( MediaDeviceDiscoveryListener theListener : mListeners ) {
                theListener.onMediaRendererDeviceAdded( inMediaRendererDevice );
            }
        }
    }

    @Override
    public void onMediaRendererDeviceRemoved( MediaRendererDevice inMediaRendererDevice ) {
        mLogger.fine( "MediaRenderer Removed:" + inMediaRendererDevice.getName() );
        synchronized( mMediaRendererDevices ) {
            mMediaRendererDevices.remove( inMediaRendererDevice );
        }
        synchronized( mListeners ) {
            for ( MediaDeviceDiscoveryListener theListener : mListeners ) {
                theListener.onMediaRendererDeviceRemoved( inMediaRendererDevice );
            }
        }
    }

    @Override
    public void onMediaServerDeviceAdded( MediaServerDevice inMediaServerDevice ) {
        mLogger.fine( "MediaServer Added:" + inMediaServerDevice.getName() );
        synchronized( mMediaServerDevices ) {
            mMediaServerDevices.add( inMediaServerDevice );
        }
        synchronized( mListeners ) {
            for ( MediaDeviceDiscoveryListener theListener : mListeners ) {
                theListener.onMediaServerDeviceAdded( inMediaServerDevice );
            }
        }
    }

    @Override
    public void onMediaServerDeviceRemoved( MediaServerDevice inMediaServerDevice ) {
        mLogger.fine( "MediaServer Removed:" + inMediaServerDevice.getName() );
        synchronized( mMediaServerDevices ) {
            mMediaServerDevices.remove( inMediaServerDevice );
        }
        synchronized( mListeners ) {
            for ( MediaDeviceDiscoveryListener theListener : mListeners ) {
                theListener.onMediaServerDeviceRemoved( inMediaServerDevice );
            }
        }
    }

    public void removeListener( MediaDeviceDiscoveryListener inListener ) {
        synchronized( mListeners ) {
            mListeners.remove( inListener );
        }
    }

    public void startDeviceDiscovery() {
        UPNPMediaDeviceDiscoverer theDiscoverer = new UPNPMediaDeviceDiscoverer();
        theDiscoverer.addListener( this );
        theDiscoverer.startDiscovery();
        synchronized( mDiscoverers ) {
            mDiscoverers.add( theDiscoverer );
        }
    }

    public void stopDeviceDiscovery() {
        synchronized( mDiscoverers ) {
            for ( MediaDeviceDiscoverer theDiscoverer : mDiscoverers ) {
                theDiscoverer.stopDiscovery();
            }
        }
    }
}