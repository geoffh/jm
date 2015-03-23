package jmusic.device.upnp.localdevice;

import jmusic.device.upnp.UPNPUtils;
import jmusic.library.Library;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.ProtocolInfos;

import java.util.logging.Logger;

public class UPNPMediaServerDevice implements UPNPLocalDeviceUtils.Fireable {
    private static final String sMediaServerDeviceName = "Geoff's Media Server";
    private static final ProtocolInfos sMediaServerProtocols =
        new ProtocolInfos(
            new ProtocolInfo(
                org.teleal.cling.support.model.Protocol.HTTP_GET,
                ProtocolInfo.WILDCARD,
                "audio/mpeg",
                "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01" ) );
    private LocalService< UPNPContentDirectoryService > mContentDirectoryService;
    private final LocalDevice mDevice;
    private final Logger mLogger = Logger.getLogger( UPNPMediaServerDevice.class.getName() );

    public UPNPMediaServerDevice( Library inLibrary ) {
        mDevice = createDevice( inLibrary );
    }

    public void fireLastChange() {
        if ( mContentDirectoryService != null ) {
            mContentDirectoryService.getManager().getImplementation().fireLastChange();
        }
    }

    private LocalDevice createDevice( Library inLibrary ) {
        mContentDirectoryService =
            UPNPLocalDeviceUtils.createLocalService( new UPNPContentDirectoryService( inLibrary ), this );
        try {
            return UPNPLocalDeviceUtils.createDevice(
                sMediaServerDeviceName,
                UPNPUtils.sMediaServerDeviceType,
                new LocalService[]{
                    mContentDirectoryService,
                    UPNPLocalDeviceUtils.createConnectionManagerService( sMediaServerProtocols, null )
                } );
        } catch( ValidationException theException ) {
            mLogger.throwing( "UPNPMediaServerDevice", "createDevice", theException );
            return null;
        }
    }
}