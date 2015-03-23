package jmusic.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TrackEditModel {
    private final ObservableList< TrackEditItem > mTracks = FXCollections.observableArrayList();
    private final HashMap< Long, TrackEditItem > mModifiedTracks = new HashMap<>();

    ObservableList< TrackEditItem > getData() {
        return mTracks;
    }

    List< TrackEditItem > getModifiedData() {
        return new ArrayList<>( mModifiedTracks.values() );
    }

    void addTrack( TrackEditItem inTrack ) {
        synchronized( mTracks ) {
            mTracks.add( inTrack );
        }
    }

    void setModified( TrackEditItem inItem ) {
        mModifiedTracks.put( inItem.getId(), inItem );
    }
}