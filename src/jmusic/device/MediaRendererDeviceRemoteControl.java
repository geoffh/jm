package jmusic.device;

import jmusic.library.LibraryItem;
import jmusic.util.ProgressListener;

public interface MediaRendererDeviceRemoteControl {
    public void addProgressListener( ProgressListener inListener );
    public void pause();
    public void play( LibraryItem inItem );
    public void removeProgressListener( ProgressListener inListener );
    public void resume();
    public void setVolume( int inVolume );
    public void stop();
}