package jmusic.device.upnp.discovery;

import jmusic.device.MediaDevice;
import org.teleal.cling.model.meta.Device;

public abstract class UPNPMediaDevice implements MediaDevice {
    protected Device mDevice;

    public UPNPMediaDevice( Device inDevice ) {
        mDevice = inDevice;
    }

    public Device getDevice() { return mDevice; }

    @Override
    public String getId() {
        return mDevice != null ? mDevice.getIdentity().getUdn().getIdentifierString() : null;
    }

    @Override
    public String getName() {
        return mDevice != null ? mDevice.getDetails().getFriendlyName() : null;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.UPNP;
    }

    @Override
    public String toString() { return getName(); }
}