package jmusic.oldneedsrewrite.upnp.mediarendererservice;

import org.teleal.cling.support.avtransport.impl.state.AbstractState;
import org.teleal.cling.support.avtransport.impl.state.NoMediaPresent;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;

import java.net.URI;
import java.util.logging.Logger;

public class UPNPMediaRendererNoMediaPresent extends NoMediaPresent {
    private final Logger mLogger = Logger.getLogger( UPNPMediaRendererStopped.class.getName() );

    public UPNPMediaRendererNoMediaPresent( AVTransport inTransport ) {
        super( inTransport );
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
}