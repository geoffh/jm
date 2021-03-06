package jmusic.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class JMusicProcess {
    public interface Callback {
        default void onExit( int inExitCode ) {}
        default boolean onStdErr( String inStdErr ) { return true; }
        default boolean onStdOut( String inStdOut ) { return true; }
    }

    private Process mProcess;
    private StreamReader mStdOutReader;
    private StreamReader mStdErrReader;
    private Callback mCallback;
    private final Logger mLogger = Logger.getLogger( JMusicProcess.class.getName() );

    public void execute( String[] inCommand, Callback inCallback ) throws IOException {
        executeCommand( inCommand );
        mCallback = inCallback;
        readStdErr();
        readStdOut();
        waitForExit();
        mCallback.onExit( mProcess.exitValue() );
    }

    public void execute( String[] inCommand ) throws IOException {
        executeCommand( inCommand );
    }

    public InputStream getErrorStream() {
        return new BufferedInputStream( mProcess.getErrorStream() );
    }

    public InputStream getInputStream() {
        return new BufferedInputStream( mProcess.getInputStream() );
    }

    private void executeCommand( String[] inCommand ) throws IOException {
        StringBuilder theBuilder = new StringBuilder( "Executing:" );
        for ( String theString : inCommand ) {
            theBuilder.append( theString ).append( " " );
        }
        mLogger.finest( theBuilder.toString() );
        mProcess = Runtime.getRuntime().exec( inCommand );
    }

    private void readStdErr() {
        mStdErrReader = new StreamReader(
            new BufferedReader( new InputStreamReader( mProcess.getErrorStream() ) ), false );
        mStdErrReader.start();
    }

    private void readStdOut() {
        mStdOutReader = new StreamReader(
            new BufferedReader( new InputStreamReader( mProcess.getInputStream() ) ), false );
        mStdOutReader.start();
    }

    private void waitForExit() {
        try {
            mProcess.waitFor();
            while ( mStdOutReader.isAlive() || mStdErrReader.isAlive() ) {
                try {
                    Thread.sleep( 500 );
                } catch( InterruptedException theIgnore ) {}
            }
        } catch( InterruptedException theException ) {}
    }

    class StreamReader extends Thread {
        private final BufferedReader mReader;
        private final boolean mIsStdOut;

        StreamReader( BufferedReader inReader, boolean inIsStdOut ) {
            mReader = inReader;
            mIsStdOut = inIsStdOut;
            setName( "StreamReader" );
            setDaemon( true );
        }

        @Override
        public void run() {
            String theLine;
            try {
                while ( ( theLine = mReader.readLine() ) != null ) {
                    if ( mIsStdOut ) {
                        if ( ! mCallback.onStdOut( theLine ) ) {
                            break;
                        }
                    } else {
                        if ( ! mCallback.onStdErr( theLine ) ) {
                            break;
                        }
                    }
                }
            } catch( IOException theException ) {
                mLogger.throwing( "JMusicProcess.StreamReader", "run", theException );
            }
        }
    }
}