package jmusic.device.upnp.localdevice;

import jmusic.device.upnp.UPNPService;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.support.avtransport.impl.AVTransportService;
import org.teleal.cling.support.avtransport.impl.AVTransportStateMachine;
import org.teleal.cling.support.avtransport.impl.state.AbstractState;
import org.teleal.cling.support.connectionmanager.ConnectionManagerService;
import org.teleal.cling.support.model.ProtocolInfos;

import java.util.logging.Logger;

public class UPNPLocalDeviceUtils {
    public interface Fireable {
        public void fireLastChange();
    }

    private static final String sDefaultDeviceNumber = "1";
    private static final ManufacturerDetails sManufacturerDetails =
        new ManufacturerDetails( "Manufactured by Geoff Higgins" );
    private static final Logger sLogger = Logger.getLogger( UPNPLocalDeviceUtils.class.getName() );

    public static LocalService< AVTransportService > createAVTransportService( Class< ? extends AVTransportStateMachine > inStateMachineDefinition,
                                                                               Class< ? extends AbstractState > inInitialState,
                                                                               Fireable inFireable ) {
        return createLocalService( new AVTransportService( inStateMachineDefinition, inInitialState ), inFireable );
    }

    public static LocalService< ConnectionManagerService > createConnectionManagerService( ProtocolInfos inSourceProtocls, ProtocolInfos inSinkProtocols ) {
        return createLocalService( new ConnectionManagerService( inSourceProtocls, inSinkProtocols ) );
    }

    public static LocalDevice createDevice( String inDeviceName, DeviceType inDeviceType, LocalService[] inServices ) throws ValidationException {
        LocalDevice theDevice = new LocalDevice(
            createDeviceIdentity( inDeviceName ),
            inDeviceType,
            createDeviceDetails( inDeviceName ),
            ( Icon )null,
            inServices );
        UPNPService.getService().getRegistry().addDevice( theDevice );
        return theDevice;
    }

    public static DeviceDetails createDeviceDetails( String inDeviceName ) {
        return new DeviceDetails(
            inDeviceName,
            sManufacturerDetails,
            new ModelDetails(
                inDeviceName, inDeviceName, sDefaultDeviceNumber ) );
    }

    public static DeviceIdentity createDeviceIdentity( String inDeviceName ) {
        return new DeviceIdentity( UDN.uniqueSystemIdentifier( inDeviceName ) );
    }

    public static < T > LocalService createLocalService( T inServiceObject ) {
        return createLocalService( inServiceObject, null );
    }

    public static < T > LocalService createLocalService( T inServiceObject, Fireable inFireable ) {
        LocalService theService = new AnnotationLocalServiceBinder().read( inServiceObject.getClass() );
        theService.setManager(
            new DefaultServiceManager( theService, inServiceObject.getClass() ) {
                @Override
                protected T createServiceInstance() throws Exception {
                    return inServiceObject;
                }
            }
        );
        if ( inFireable != null ) {
            new EventThread( inFireable ).start();
        }
        return theService;
    }

    static class EventThread extends Thread {
        private final Fireable mFireable;

        EventThread( Fireable inFireable ) {
            mFireable = inFireable;
        }

        @Override
        public void run() {
            while ( ! interrupted() ) {
                try {
                    mFireable.fireLastChange();
                } catch( Exception theIgnore ) {
                    sLogger.throwing( "UPNPLocalDevice.EventThread", "run", theIgnore );
                }
                finally {
                    try {
                        sleep( 1000 );
                    } catch( Exception theIgnore ) {
                        sLogger.throwing( "UPNPLocalDevice.EventThread", "run", theIgnore );
                    }
                }
            }
        }
    }
}