package jmusic.mp3encode;

import jmusic.util.ProgressListener;

import java.io.IOException;
import java.io.InputStream;

public interface MP3Encoder {
    InputStream getInputStream( String inUri, ProgressListener inListener ) throws IOException;
    long getContentLength( String inUri );
}