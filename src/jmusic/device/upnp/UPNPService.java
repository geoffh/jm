package jmusic.device.upnp;

import org.teleal.cling.DefaultUpnpServiceConfiguration;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.model.types.ServiceType;

public class UPNPService {
    private static final UPNPServiceConfiguration sServiceConfiguration = new UPNPServiceConfiguration();
    private static final UpnpService sService = new UpnpServiceImpl( sServiceConfiguration );

    static {
        Runtime.getRuntime().addShutdownHook( new Thread() {
                @Override
                public void run() {
                    sService.shutdown();
                }
            });
    }

    public static UpnpService getService() { return sService; }

    static class UPNPServiceConfiguration extends DefaultUpnpServiceConfiguration {
        public ServiceType[] getExclusiveServiceTypes() {
            return new ServiceType[] {
                UPNPUtils.sServiceAVTransport,
                UPNPUtils.sServiceRenderingControl,
                UPNPUtils.sServiceContentDirectory
            };
        }
    }
}