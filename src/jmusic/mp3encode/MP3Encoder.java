package jmusic.mp3encode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface MP3Encoder {
    public InputStream getInputStream( String inUri ) throws IOException;
    public long getContentLength( String inUri );
}