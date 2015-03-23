package jmusic.device.upnp.localdevice;

import jmusic.device.upnp.UPNPUtils;
import jmusic.oldneedsrewrite.upnp.mediarendererservice.UPNPMediaRenderService;
import jmusic.oldneedsrewrite.upnp.mediarendererservice.UPNPMediaRendererNoMediaPresent;
import jmusic.oldneedsrewrite.upnp.mediarendererservice.UPNPMediaRendererStateMachine;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.support.avtransport.impl.AVTransportService;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.ProtocolInfos;

import java.util.logging.Logger;

public class UPNPMediaRendererDevice implements UPNPLocalDeviceUtils.Fireable {
    private static final String sMediaRendererDeviceName = "Geoff's Media Renderer";
    private static final ProtocolInfos sMediaRendererProtocols =
        new ProtocolInfos(
            new ProtocolInfo(
                org.teleal.cling.support.model.Protocol.HTTP_GET,
                ProtocolInfo.WILDCARD,
                "audio/mpeg",
                ProtocolInfo.WILDCARD ),
            new ProtocolInfo(
                org.teleal.cling.support.model.Protocol.HTTP_GET,
                ProtocolInfo.WILDCARD,
                "audio/mpeg3",
                ProtocolInfo.WILDCARD ),
            new ProtocolInfo(
                org.teleal.cling.support.model.Protocol.HTTP_GET,
                ProtocolInfo.WILDCARD,
                "audio/mp3",
                ProtocolInfo.WILDCARD ) );
    private LocalDevice mDevice;
    private LocalService< AVTransportService > mAVTransportService;
    private final Logger mLogger = Logger.getLogger( UPNPMediaRendererDevice.class.getName() );

    public UPNPMediaRendererDevice() {
        mDevice = createDevice();
    }

    public void fireLastChange() {
        if ( mAVTransportService != null ) {
            mAVTransportService.getManager().getImplementation().fireLastChange();
        }
    }

    private LocalDevice createDevice() {
        mAVTransportService =
            UPNPLocalDeviceUtils.createAVTransportService(
                UPNPMediaRendererStateMachine.class,
                UPNPMediaRendererNoMediaPresent.class,
                this );
        try {
            return UPNPLocalDeviceUtils.createDevice(
                sMediaRendererDeviceName,
                UPNPUtils.sMediaRendererDeviceType,
                new LocalService[] {
                    mAVTransportService,
                    UPNPLocalDeviceUtils.createLocalService( new UPNPMediaRenderService() ),
                    UPNPLocalDeviceUtils.createConnectionManagerService( null, sMediaRendererProtocols )
                } );
        } catch( ValidationException theException ) {
            mLogger.throwing( "UPNPMediaServer", "createDevice", theException );
            return null;
        }
    }
}