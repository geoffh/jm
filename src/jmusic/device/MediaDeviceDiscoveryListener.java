package jmusic.device;

public interface MediaDeviceDiscoveryListener {
    default void onMediaRendererDeviceAdded( MediaRendererDevice inMediaRendererDevice ) {}
    default void onMediaRendererDeviceRemoved( MediaRendererDevice inMediaRendererDevice ) {}
    default void onMediaServerDeviceAdded( MediaServerDevice inMediaServerDevice ) {}
    default void onMediaServerDeviceRemoved( MediaServerDevice inMediaServerDevice ) {}
}