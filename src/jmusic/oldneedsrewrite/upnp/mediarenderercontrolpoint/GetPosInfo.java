package jmusic.oldneedsrewrite.upnp.mediarenderercontrolpoint;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.model.PositionInfo;

import java.util.logging.Logger;

class GetPosInfo extends GetPositionInfo {
    private final Logger mLogger = Logger.getLogger( getClass().getName() );
    private PositionInfo mPositionInfo;

    public GetPosInfo( Service inService ) {
        super( inService );
    }

    public PositionInfo getPositionInfo() { return mPositionInfo; }

    @Override
    public void failure( ActionInvocation inInvocation,
                         UpnpResponse inResponse,
                         String inDefaultMsg ) {
        mLogger.finest( "GetPosInfo failure: " + inInvocation + " : " +
                        inResponse + " : " + inDefaultMsg );
    }

    @Override
    public void success( ActionInvocation inActionInvocation ) {
        try {
            super.success( inActionInvocation );
        } catch( Exception theIgnore ){}
    }

    @Override
    public void received( ActionInvocation inInvocation,
                          PositionInfo inPositionInfo ) {
        mPositionInfo = inPositionInfo;
    }
}
