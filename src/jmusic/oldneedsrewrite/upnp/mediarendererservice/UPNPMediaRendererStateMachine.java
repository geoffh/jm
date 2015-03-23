package jmusic.oldneedsrewrite.upnp.mediarendererservice;

import org.teleal.cling.support.avtransport.impl.AVTransportStateMachine;
import org.teleal.common.statemachine.States;

@States( {
    UPNPMediaRendererNoMediaPresent.class,
    UPNPMediaRendererStopped.class,
    UPNPMediaRendererPlaying.class } )

public interface UPNPMediaRendererStateMachine extends AVTransportStateMachine {}