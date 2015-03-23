package jmusic.oldneedsrewrite.util;

import jmusic.util.ProgressListener;

import java.io.*;

public class ProgressInputStream extends InputStream {
    private final InputStream mStream;
    private final long mSize;
    private final ProgressListener mListener;
    private long mPosition = 0;

    public ProgressInputStream( InputStream inStream, long inSize, ProgressListener inListener ) {
        mStream = new BufferedInputStream( inStream );
        mSize = inSize;
        mListener = inListener;
    }

    public int read() throws IOException {
        int theByte = mStream.read();
        ++ mPosition;
        if ( theByte == -1 ) {
        } else {
            update( ( int ) ( ( mPosition * 100 ) / mSize ), "In progress" );
        }
        return theByte;
    }

    private void update( int inProgress, String inMsg ) {
        mListener.onProgress( inProgress );
        mListener.onStatusMessage( inMsg );
    }
}
