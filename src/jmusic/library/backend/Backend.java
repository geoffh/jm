package jmusic.library.backend;

import java.io.InputStream;
import java.util.Map;
import jmusic.library.LibraryException;
import jmusic.library.LibraryItem;
import jmusic.util.ProgressListener;

public interface Backend {
    public boolean canHandleUri( String inUri );

    // writes mp3 tags to tracks
    public void fixTrack( String inTrackUri, LibraryItem inProps )
        throws LibraryException;
    
    public InputStream getTrackInputStream( String inTrackUri, ProgressListener inListener )
        throws LibraryException;
    
    public LibraryItem getTrack( String inTrackUri )
        throws LibraryException;
    
    public void importTrack( Backend inSourceBackend, String inSourceTrackUri,
                             LibraryItem inTargetTrackProperties, ProgressListener inListener )
        throws LibraryException;

    public boolean isWriteable();
    
    // returns uri & lastModified for each track file
    public Map< String, LibraryItem > listTracks()
        throws LibraryException;

    public void updateTrack( String inTrackUri, LibraryItem inProps )
        throws LibraryException;
}