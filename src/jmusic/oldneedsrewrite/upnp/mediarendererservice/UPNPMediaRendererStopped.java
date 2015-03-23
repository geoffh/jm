package jmusic.oldneedsrewrite.upnp.mediarendererservice;

import jmusic.oldneedsrewrite.player.Player;
import org.teleal.cling.support.avtransport.impl.state.AbstractState;
import org.teleal.cling.support.avtransport.impl.state.Stopped;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.SeekMode;

import java.net.URI;
import java.util.logging.Logger;

public class UPNPMediaRendererStopped extends Stopped {
    private final Logger mLogger = Logger.getLogger( UPNPMediaRendererStopped.class.getName() );

    public UPNPMediaRendererStopped( AVTransport inTransport ) {
        super( inTransport );
    }

    @Override
    public void onEntry() {
        super.onEntry();
        Player.getInstance().stop();
    }

    public void onExit() {
    }

    @Override
    public Class< ? extends AbstractState > setTransportURI( URI inUri, String inMetaData ) {
        String theURI = inUri.toString();
        AVTransport theTransport = getTransport();
        theTransport.setMediaInfo( new MediaInfo( theURI, inMetaData ) );
        theTransport.setPositionInfo(
            new PositionInfo( 1, inMetaData, theURI, null, null ) );
        theTransport.getLastChange().setEventedValue(
            theTransport.getInstanceId(),
            new AVTransportVariable.AVTransportURI( inUri ),
            new AVTransportVariable.CurrentTrackURI( inUri ) );
        return UPNPMediaRendererStopped.class;
    }

    @Override
    public Class stop() {
        return UPNPMediaRendererStopped.class;
    }

    @Override
    public Class play( String string ) {
        return UPNPMediaRendererPlaying.class;
    }

    @Override
    public Class next() {
        return UPNPMediaRendererStopped.class;
    }

    @Override
    public Class previous() {
        return UPNPMediaRendererStopped.class;
    }

    @Override
    public Class seek( SeekMode sm, String string ) {
        return UPNPMediaRendererStopped.class;
    }
}
