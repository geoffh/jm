package jmusic.ui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import jmusic.library.Library;
import jmusic.library.LibraryItem;
import jmusic.util.ProgressListener;

import java.util.HashSet;
import java.util.Map;

class TrackUpdateService extends Service< Void > {
    private final Library mLibrary;
    private final Map< Long, LibraryItem > mTracks;

    TrackUpdateService( Library inLibrary, Map< Long, LibraryItem > inTracks ) {
        mLibrary = inLibrary;
        mTracks = inTracks;
    }

    @Override
    protected Task< Void > createTask() {
        return new Task< Void >() {
            boolean isComplete = false;
            @Override
            protected Void call() throws Exception {
                mLibrary.updateTracks( mTracks, new ProgressListener() {
                    @Override
                    public void onErrorMessage( String inMessage ) { updateMessage( inMessage ); }
                    @Override
                    public void onComplete() {
                        updateMessage( "Track Update Complete" );
                        for ( LibraryItem theTrack : mTracks.values() ) {
                            mLibrary.refresh( theTrack.getRootId() );
                            break;
                        }
                        isComplete = true;
                    }
                    @Override
                    public void onProgress( int inPercent ) { updateProgress( inPercent, 100 ); }
                    @Override
                    public void onStatusMessage( String inMessage ) { updateMessage( inMessage ); }
                } );
                while ( ! isComplete ) { Thread.sleep( 1000 ); }
                return null;
            }
        };
    }
}
