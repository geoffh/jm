package jmusic.ui.edittrack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditTrackModel {
    private final ObservableList< EditTrackItem > mTracks = FXCollections.observableArrayList();
    private final HashMap< Long, EditTrackItem > mModifiedTracks = new HashMap<>();

    ObservableList< EditTrackItem > getData() {
        return mTracks;
    }

    List< EditTrackItem > getModifiedData() {
        return new ArrayList<>( mModifiedTracks.values() );
    }

    void addTrack( EditTrackItem inTrack ) {
        synchronized( mTracks ) {
            mTracks.add( inTrack );
        }
    }

    void setModified( EditTrackItem inItem ) {
        mModifiedTracks.put( inItem.getId(), inItem );
    }
}