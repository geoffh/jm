package jmusic.device.upnp.discovery;

import jmusic.device.MediaRendererDevice;
import org.teleal.cling.model.meta.Device;

public class UPNPMediaRendererDevice extends UPNPMediaDevice implements MediaRendererDevice {
    public UPNPMediaRendererDevice( Device inDevice ) {
        super( inDevice );
    }
}