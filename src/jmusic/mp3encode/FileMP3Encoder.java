package jmusic.mp3encode;

import jmusic.util.JMusicProcess;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileMP3Encoder implements MP3Encoder {
    private static final String sSuffixMp3 = ".mp3";

    @Override
    public InputStream getInputStream( String inUri ) throws IOException {
        return isMP3File( new File( inUri ) ) ? getMP3InputStream( inUri ) : getNonMp3InputStream( inUri );
    }

    @Override
    public long getContentLength( String inUri ) {
        return new File( inUri ).length();
    }

    private InputStream getMP3InputStream( String inUri ) throws FileNotFoundException {
        return new BufferedInputStream( new FileInputStream( inUri ) );
    }

    private InputStream getNonMp3InputStream( String inUri ) throws IOException {
        String theCommand[] = { "/usr/bin/ffmpeg", "-i", inUri, "-f", "mp3", "-" };
        JMusicProcess theProcess = new JMusicProcess();
        theProcess.execute( theCommand );
        return theProcess.getInputStream();
    }

    private boolean isMP3File( File inFile ) {
        return inFile.getName().endsWith( sSuffixMp3 );
    }
}