package jmusic.library.backend;

import java.io.InputStream;
import java.util.Map;
import jmusic.library.LibraryException;
import jmusic.library.LibraryItem;
import jmusic.util.ProgressListener;

public interface Backend {
    boolean canHandleUri( String inUri );

    // writes mp3 tags to tracks
    void fixTrack( String inTrackUri, LibraryItem inProps )
        throws LibraryException;

    InputStream getThumbnailInputStream( String inUri )
        throws LibraryException;

    InputStream getTrackInputStream( String inTrackUri, ProgressListener inListener )
        throws LibraryException;

    LibraryItem getTrack( String inTrackUri )
        throws LibraryException;
    
    void importTrack( Backend inSourceBackend, String inSourceTrackUri,
                             LibraryItem inTargetTrackProperties, ProgressListener inListener )
        throws LibraryException;

    boolean isWriteable();
    
    // returns uri & lastModified for each track file
    Map< String, LibraryItem > listTracks()
        throws LibraryException;

    void updateTrack( String inTrackUri, LibraryItem inProps )
        throws LibraryException;
}