package jmusic.oldneedsrewrite.upnp.mediarendererservice;

import jmusic.oldneedsrewrite.player.Player;
import jmusic.util.ProgressListener;
import org.teleal.cling.support.avtransport.impl.state.AbstractState;
import org.teleal.cling.support.avtransport.impl.state.Playing;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.SeekMode;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

public class UPNPMediaRendererPlaying extends Playing implements ProgressListener {
    private final Logger mLogger = Logger.getLogger( UPNPMediaRendererStopped.class.getName() );
    private int mProgress = -2;

    public UPNPMediaRendererPlaying( AVTransport inTransport ) {
        super( inTransport );
    }

    @Override
    public void onEntry() {
        super.onEntry();
        MediaInfo theMediaInfo = getTransport().getMediaInfo();
        try {
            String theURL = theMediaInfo.getCurrentURI();
            mLogger.finest( "Starting player for '" + theURL + "'" );
            URLConnection theConnection =
                new URL( theURL ).openConnection();
            Player.getInstance().play(
                theConnection.getInputStream(),
                theConnection.getContentLength(),
                this );
        } catch( Exception theException ) {
            mLogger.throwing( "MediaRendererPlaying", "onEntry", theException );
        }
    }

    public void onProgress( int inProgress ) {
        if ( mProgress != inProgress ) {
            PositionInfo thePositionInfo = getTransport().getPositionInfo();
            long theDuration = thePositionInfo.getTrackDurationSeconds();
            long theElapsed = theDuration * inProgress / 100;
            getTransport().setPositionInfo(
                new PositionInfo( thePositionInfo, theElapsed, 0 ) );
            mProgress = inProgress;
        }
    }

    @Override
    public Class< ? extends AbstractState > setTransportURI( URI inURI, String inMetaData ) {
        String theURI = inURI.toString();
        AVTransport theTransport = getTransport();
        theTransport.setMediaInfo( new MediaInfo( theURI, inMetaData ) );
        theTransport.setPositionInfo(
            new PositionInfo( 1, inMetaData, theURI, null, null ) );
        theTransport.getLastChange().setEventedValue(
            theTransport.getInstanceId(),
            new AVTransportVariable.AVTransportURI( inURI ),
            new AVTransportVariable.CurrentTrackURI( inURI ) );
        return UPNPMediaRendererStopped.class;
    }

    @Override
    public Class stop() {
        onProgress( 0 );
        return UPNPMediaRendererStopped.class;
    }

    @Override
    public Class play(String string) {
        return UPNPMediaRendererStopped.class;
    }

    @Override
    public Class pause() {
        return UPNPMediaRendererStopped.class;
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
    public Class seek(SeekMode sm, String string) {
        return UPNPMediaRendererStopped.class;
    }
}