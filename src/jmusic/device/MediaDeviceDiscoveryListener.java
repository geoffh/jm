package jmusic.device;

public interface MediaDeviceDiscoveryListener {
    public default void onMediaRendererDeviceAdded( MediaRendererDevice inMediaRendererDevice ) {}
    public default void onMediaRendererDeviceRemoved( MediaRendererDevice inMediaRendererDevice ) {}
    public default void onMediaServerDeviceAdded( MediaServerDevice inMediaServerDevice ) {}
    public default void onMediaServerDeviceRemoved( MediaServerDevice inMediaServerDevice ) {}
}