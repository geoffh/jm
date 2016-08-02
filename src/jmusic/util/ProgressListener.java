package jmusic.util;

public interface ProgressListener {
    default void data( Object inData ) {}
    default void onErrorMessage( String inMessage ) {}
    default void onComplete() {}
    default void onProgress( int inPercent ) {}
    default void onStatusMessage( String inMessage ) {}
}