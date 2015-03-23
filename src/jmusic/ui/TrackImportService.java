package jmusic.ui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import jmusic.library.Library;
import jmusic.library.LibraryItem;
import jmusic.util.ProgressListener;

import java.util.Map;

class TrackImportService extends Service< Void > {
    private final Library mLibrary;
    private final Map< Long, LibraryItem > mTracks;
    private final Long mTargetRootId;

    TrackImportService( Library inLibrary, Map< Long, LibraryItem > inTracks, Long inTargetRootId ) {
        mLibrary = inLibrary;
        mTracks = inTracks;
        mTargetRootId = inTargetRootId;
    }

    @Override
    protected Task< Void > createTask() {
        return new Task< Void >() {
            boolean isComplete = false;
            @Override
            protected Void call() throws Exception {
                mLibrary.importTracks( mTracks, mTargetRootId, new ProgressListener() {
                    @Override
                    public void onErrorMessage( String inMessage ) { updateMessage( inMessage ); }
                    @Override
                    public void onComplete() {
                        updateMessage( "Track Import Complete" );
                        for ( LibraryItem theTrack : mTracks.values() ) {
                            mLibrary.refresh( theTrack.getRootId() );
                            break;
                        }
                        mLibrary.refresh( mTargetRootId );
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