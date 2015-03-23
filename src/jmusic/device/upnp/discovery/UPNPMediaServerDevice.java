package jmusic.device.upnp.discovery;

import jmusic.device.MediaServerDevice;
import org.teleal.cling.model.meta.Device;

public class UPNPMediaServerDevice extends UPNPMediaDevice implements MediaServerDevice {
    public UPNPMediaServerDevice( Device inDevice ) {
        super( inDevice );
    }
}