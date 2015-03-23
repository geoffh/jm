package jmusic.oldneedsrewrite.upnp.mediarenderercontrolpoint;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.model.Channel;

import java.util.logging.Logger;

class SetVolumeDB extends ActionCallback {
    private final Logger mLogger = Logger.getLogger( getClass().getName() );
    public SetVolumeDB( Service inService, int inVolume ) {
        this( new UnsignedIntegerFourBytes( 0 ), inService, inVolume );
    }

    public SetVolumeDB( UnsignedIntegerFourBytes inInstanceId, Service inService, int inVolume ) {
        super( new ActionInvocation( inService.getAction( "SetVolumeDB" ) ) );
        getActionInvocation().setInput( "InstanceID", inInstanceId);
        getActionInvocation().setInput( "Channel", Channel.Master.toString());
        getActionInvocation().setInput( "DesiredVolume", inVolume );
    }

    @Override
    public void failure( ActionInvocation inInvocation,
                         UpnpResponse inResponse,
                         String inDefaultMsg ) {
        mLogger.finest( "SetVolumeDBRange failure: " + inInvocation + " : " +
                        inResponse + " : " + inDefaultMsg );
    }

    @Override
    public void success( ActionInvocation inActionInvocation ) {}
}
