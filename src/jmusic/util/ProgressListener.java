package jmusic.util;

public interface ProgressListener {
    public default void data( Object inData ) {}
    public default void onErrorMessage( String inMessage ) {}
    public default void onComplete() {}
    public default void onProgress( int inPercent ) {}
    public default void onStatusMessage( String inMessage ) {}
}