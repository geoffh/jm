package jmusic.ui.tablecontent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jmusic.library.LibraryItem;

import java.util.Collection;

class TableContentModel< T extends LibraryItem >  {
    private final ObservableList< T > mTracks = FXCollections.observableArrayList();

    void addTrack( T inTrack ) {
        synchronized( mTracks ) {
            mTracks.add( inTrack );
        }
    }

    ObservableList< T > getData() {
        return mTracks;
    }

    void insertTrack( T inTrack ) {
        synchronized( mTracks ) {
            mTracks.add( findInsertionIndex( inTrack ), inTrack );
        }
    }

    boolean isTrackInModel( T inTrack ) {
        Long theId = inTrack.getId();
        synchronized( mTracks ) {
            for ( T theTrack : mTracks ) {
                if ( theTrack.getId().equals( theId ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    void removeTrack( T inTrack ) {
        Long theId = inTrack.getId();
        synchronized( mTracks ) {
            for ( T theTrack : mTracks ) {
                if ( theId.equals( theTrack.getId() ) ) {
                    mTracks.remove( theTrack );
                    break;
                }
            }
        }
    }

    void setTracks( Collection< T > inTracks ) {
        synchronized( mTracks ) {
            mTracks.setAll( inTracks );
        }
    }

    void updateTrack( T inTrack ) {
        Long theId = inTrack.getId();
        synchronized( mTracks ) {
            for ( T theTrack : mTracks ) {
                if ( theTrack.getId().equals( theId ) ) {
                    mTracks.set( mTracks.indexOf( theTrack ), inTrack );
                    break;
                }
            }
        }
    }

    private int findInsertionIndex( T inTrack ) {
        int theIndex = 0;
        synchronized( mTracks ) {
            for ( T theTrack : mTracks ) {
                if ( theTrack.compare( inTrack ) > 0 ) {
                    break;
                }
                ++ theIndex;
            }
        }
        return theIndex;
    }
}