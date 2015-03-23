package jmusic.device.upnp;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;

public class UPNPUtils {
    public static final UDAServiceType sServiceAVTransport = new UDAServiceType( "AVTransport", 1 );
    public static final UDAServiceType sServiceRenderingControl = new UDAServiceType( "RenderingControl", 1 );
    public static final UDAServiceType sServiceContentDirectory = new UDAServiceType( "ContentDirectory", 1 );
    public static final UDADeviceType sMediaRendererDeviceType = new UDADeviceType( "MediaRenderer", 1 );
    public static final UDADeviceType sMediaServerDeviceType = new UDADeviceType( "MediaServer", 1 );

    public static boolean isMediaRendererDevice( Device inDevice ) {
        return sMediaRendererDeviceType.equals( inDevice.getType() );
    }

    public static boolean isMediaServerDevice( Device inDevice ) {
        return sMediaServerDeviceType.equals( inDevice.getType() );
    }
}