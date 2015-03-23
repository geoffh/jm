package jmusic.oldneedsrewrite.upnp.mediarenderercontrolpoint;


import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.GetMediaInfo;
import org.teleal.cling.support.model.MediaInfo;

import java.util.logging.Logger;

class GetMedInfo extends GetMediaInfo {
    private final Logger mLogger = Logger.getLogger( getClass().getName() );
    private MediaInfo mMediaInfo;

    public GetMedInfo( Service inService ) {
        super( inService );
    }

    public MediaInfo getMediaInfo() { return mMediaInfo; }

    @Override
    public void failure( ActionInvocation inInvocation,
                         UpnpResponse inResponse,
                         String inDefaultMsg ) {
        mLogger.finest( "GetMediaInfo failure: " + inInvocation + " : " +
                        inResponse + " : " + inDefaultMsg );
    }

    @Override
    public void success( ActionInvocation inActionInvocation ) {
        super.success( inActionInvocation );
    }

    @Override
    public void received( ActionInvocation inInvocation,
                          MediaInfo inMediaInfo ) {
        mMediaInfo = inMediaInfo;
    }
}
