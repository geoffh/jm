package jmusic.device;

import jmusic.library.LibraryItem;
import jmusic.util.ProgressListener;

public interface MediaRendererDeviceRemoteControl {
    void addProgressListener( ProgressListener inListener );
    void pause();
    void play( LibraryItem inItem );
    void removeProgressListener( ProgressListener inListener );
    void resume();
    void setVolume( int inVolume );
    void stop();
}