package jmusic.device;

public interface MediaDeviceDiscoverer {
    void addListener( MediaDeviceDiscoveryListener inListener );
    void removeListener( MediaDeviceDiscoveryListener inListener );
    void startDiscovery();
    void stopDiscovery();
}