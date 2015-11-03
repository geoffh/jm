package jmusic.util;

import java.io.*;
import java.util.StringTokenizer;

public class FFMpeg {
    public static InputStream fileToMP3Stream( File inFile, ProgressListener inListener ) throws IOException {
        String theCommand[] = { "/usr/bin/ffmpeg", "-i", inFile.getAbsolutePath(), "-f", "mp3", "-" };
        JMusicProcess theProcess = new JMusicProcess();
        theProcess.execute( theCommand );
        new FFMPegProgress( theProcess, inListener ).start();
        return theProcess.getInputStream();
    }

    static class FFMPegProgress extends Thread {
        private final JMusicProcess mProcess;
        private final ProgressListener mListener;

        FFMPegProgress( JMusicProcess inProcess, ProgressListener inListener ) {
            mProcess = inProcess;
            mListener = inListener;
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