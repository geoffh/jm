package jmusic.device;

public interface MediaDeviceDiscoverer {
    public void addListener( MediaDeviceDiscoveryListener inListener );
    public void removeListener( MediaDeviceDiscoveryListener inListener );
    public void startDiscovery();
    public void stopDiscovery();
}