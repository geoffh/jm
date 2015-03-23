package jmusic.oldneedsrewrite.upnp.mediarenderercontrolpoint;


import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.GetTransportInfo;
import org.teleal.cling.support.model.TransportInfo;

import java.util.logging.Logger;

class GetTranspInfo extends GetTransportInfo {
    private final Logger mLogger = Logger.getLogger( getClass().getName() );
    private TransportInfo mTransportInfo;

    public GetTranspInfo( Service inService ) {
        super( inService );
    }

    public TransportInfo getTransportInfo() { return mTransportInfo; }

    @Override
    public void failure( ActionInvocation inInvocation,
                         UpnpResponse inResponse,
                         String inDefaultMsg ) {
        mLogger.finest( "GetTranspInfo failure: " + inInvocation + " : " +
                        inResponse + " : " + inDefaultMsg );
    }

    @Override
    public void success( ActionInvocation inActionInvocation ) {
        super.success( inActionInvocation );
    }

    @Override
    public void received( ActionInvocation inInvocation,
                          TransportInfo inPositionInfo ) {
        mTransportInfo = inPositionInfo;
    }
}
