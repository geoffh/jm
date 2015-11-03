package jmusic.mp3encode;

import jmusic.util.JMusicProcess;
import jmusic.util.ProgressListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class FileMP3Encoder implements MP3Encoder {
    private static final String sSuffixMp3 = ".mp3";

    @Override
    public InputStream getInputStream( String inUri, ProgressListener inListener ) throws IOException {
        return isMP3File( new File( inUri ) ) ? getMP3InputStream( inUri, inListener ) : getNonMp3InputStream( inUri, inListener );
    }

    @Override
    public long getContentLength( String inUri ) {
        return new File( inUri ).length();
    }

    private InputStream getMP3InputStream( String inUri, ProgressListener inListener ) throws FileNotFoundException {
        return new BufferedInputStream( new FileInputStream( inUri ) );
    }

    private InputStream getNonMp3InputStream( String inUri, ProgressListener inListener ) throws IOException {
        String theCommand[] = { "/usr/bin/ffmpeg", "-i", inUri, "-f", "mp3", "-" };
        JMusicProcess theProcess = new JMusicProcess();
        theProcess.execute( theCommand );
        if ( inListener != null ) {
            new FFMPegProgress( theProcess, inListener ).start();
        }
        return theProcess.getInputStream();
    }

    private boolean isMP3File( File inFile ) {
        return inFile.getName().endsWith( sSuffixMp3 );
    }

    class FFMPegProgress extends Thread {
        private final JMusicProcess mProcess;
        private final ProgressListener mListener;

        FFMPegProgress( JMusicProcess inProcess, ProgressListener inListener ) {
            mProcess = inProcess;
            mListener = inListener;
            setName( "FFMPegProgress" );
            setDaemon( true );
        }

        @Override
        public void run() {
            BufferedReader theReader = new BufferedReader( new InputStreamReader( mProcess.getErrorStream() ) );
            String theLine;
            try {
                double theDuration = 0;
                int theIndex;
                while ( ( theLine = theReader.readLine() ) != null ) {
                    if ( theDuration == 0 ) {
                        theIndex = theLine.indexOf( "Duration: " );
                        if ( theIndex != -1 ) {
                            theDuration = stringToDuration( theLine.substring( theIndex + 10, theIndex + 21 ) );
                        }
                    } else {
                        theIndex = theLine.indexOf( "time=" );
                        if ( theIndex != -1 ) {
                            double theTime = stringToDuration( theLine.substring( theIndex + 5, theIndex + 16 ) );
                            mListener.onProgress( ( int )( theTime * 100 / theDuration ) );
                        }
                    }
                }
            } catch( Exception theException ) {
                mListener.onErrorMessage( theException.getMessage() );
            } finally {
                mListener.onComplete();
            }
        }

        private double stringToDuration( String inDuration ) {
            double theDuration = 0;
            StringTokenizer theTokeniser = new StringTokenizer( inDuration, ":" );
            theDuration += Integer.valueOf( theTokeniser.nextToken() ) * 60 * 60;
            theDuration += Integer.valueOf( theTokeniser.nextToken() ) * 60;
            theDuration += Double.valueOf( theTokeniser.nextToken() );
            return theDuration;
        }
    }
}