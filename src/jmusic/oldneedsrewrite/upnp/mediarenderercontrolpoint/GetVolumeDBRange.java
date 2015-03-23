package jmusic.oldneedsrewrite.upnp.mediarenderercontrolpoint;


import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionException;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ErrorCode;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.model.Channel;
import org.teleal.cling.support.model.VolumeDBRange;

import java.util.logging.Logger;

public class GetVolumeDBRange extends ActionCallback {
    private VolumeDBRange mRange;
    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    public GetVolumeDBRange( Service inService ) {
        this( new UnsignedIntegerFourBytes( 0 ), inService );
    }

    public GetVolumeDBRange( UnsignedIntegerFourBytes inInstanceId, Service inService ) {
        super( new ActionInvocation( inService.getAction( "GetVolumeDBRange" ) ) );
        getActionInvocation().setInput( "InstanceID", inInstanceId );
        getActionInvocation().setInput( "Channel", Channel.Master.toString() );
    }

    public VolumeDBRange getRange() { return mRange; }

    public void success( ActionInvocation inInvocation ) {
        try {
            int theMinValue = Integer.valueOf(inInvocation.getOutput( "MinValue" ).getValue().toString() );
            int theMaxValue = Integer.valueOf(inInvocation.getOutput( "MaxValue" ).getValue().toString() );
            mRange = new VolumeDBRange( theMinValue, theMaxValue );
        } catch (Exception ex) {
            mRange = new VolumeDBRange( 0, 0 );
            inInvocation.setFailure(
                new ActionException( ErrorCode.ACTION_FAILED,
                              "Can't parse ProtocolInfo response: " + ex, ex)
            );
            mLogger.finest( "GetVolumeDB failure:" );
        }
    }

    @Override
    public void failure(ActionInvocation inInvocation, UpnpResponse inResponse, String inDefaultMsg) {
        mRange = new VolumeDBRange( 0, 0 );
        mLogger.finest( "GetVolumeDBRange failure: " + inInvocation + " : " +
                        inResponse + " : " + inDefaultMsg );
    }
}
