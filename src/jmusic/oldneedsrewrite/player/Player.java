package jmusic.oldneedsrewrite.player;

import javazoom.jl.player.advanced.AdvancedPlayer;
import jmusic.oldneedsrewrite.util.ProgressInputStream;
import jmusic.oldneedsrewrite.util.VolumeRange;
import jmusic.util.ProgressListener;
import org.teleal.cling.support.model.VolumeDBRange;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import java.io.InputStream;
import java.util.logging.Logger;

public class Player {
    private static final Player sInstance = new Player();
    private static final VolumeDBRange sVolumeDBRange;
    private final Logger mLogger = Logger.getLogger( getClass().getName() );
    private PlayThread mPlayThread;

    static {
        VolumeRange theRange = new VolumeRange( 0, 0 );
        Line theLine = getFakeLine();
        if ( theLine != null ) {
            FloatControl theControl =
                ( FloatControl )theLine.getControl( FloatControl.Type.MASTER_GAIN );
            theRange.minValue = ( int )theControl.getMinimum();
            theRange.maxValue = ( int )theControl.getMaximum();
            theLine.close();
        }
        sVolumeDBRange = new VolumeDBRange( theRange.minValue, theRange.maxValue );
    }

    private Player(){}

    public static Player getInstance() { return sInstance; }

    public synchronized VolumeDBRange getVolumeDBRange() {
        return mPlayThread != null ? mPlayThread.getVolumeDBRange() : sVolumeDBRange;
    }

    public synchronized void play( InputStream inStream,
                                   long inLength,
                                   ProgressListener inListener ) throws Exception {
        stop();
        mPlayThread = new PlayThread( inStream, inLength, inListener );
        mPlayThread.start();
    }

    public synchronized void setVolumeDB( int inVolume ) {
        if ( mPlayThread != null ) {
            mPlayThread.setVolumeDB( inVolume );
        }
    }
    
    public synchronized void stop() {
        if ( mPlayThread != null ) {
            mPlayThread.terminate();
            mPlayThread = null;
        }
    }

    private static Line getFakeLine() {
        Line theFakeLine = null;
        Mixer theMixer = AudioSystem.getMixer( null );
        for ( Line.Info linfo : theMixer.getSourceLineInfo() ) {
            try {
                Line theLine = AudioSystem.getLine( linfo );
                if ( theLine instanceof Clip ) {
                    continue;
                }
                theLine.open();
                if ( theLine.isControlSupported( FloatControl.Type.MASTER_GAIN ) ) {
                    theFakeLine = theLine;
                    break;
                }
            } catch( Exception theIgnore ) {}
        }
        return theFakeLine;
    }

    class PlayThread extends Thread {
        private AdvancedPlayer mPlayer;

        PlayThread( InputStream inStream,
                    long inLength,
                    ProgressListener inListener ) throws Exception {
            mPlayer =
                new AdvancedPlayer(
                    new ProgressInputStream( inStream, inLength, inListener ) );
            setName( "PlayThread" );
            setDaemon( true );
        }

        @Override
        public void run() {
            try {
                mPlayer.play();
                terminate();
            } catch( Exception theException ) {
                mLogger.throwing( "Player.PlayThread", "run", theException  );
            }
        }

        void terminate() {
            if ( mPlayer != null ) {
                mPlayer.close();
                mPlayer = null;
            }
        }

        VolumeDBRange getVolumeDBRange() {
            javazoom.jl.player.VolumeRange theRange = mPlayer.getVolumeDBRange();
            return new VolumeDBRange( ( int )theRange.mMinVolume, ( int )theRange.mMaxVolume );
        }

        void setVolumeDB( int inVolume ) {
            mPlayer.setVolumeDB( inVolume );
        }
    }
}