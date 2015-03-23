package jmusic.ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import jmusic.library.LibraryBrowseResult;
import jmusic.library.LibraryItem;
import jmusic.library.LibraryListener;

import java.util.logging.Logger;

public class TrackViewController implements LibraryListener, ChangeListener< TreeItem< LibraryItem > > {
    private final JMusicController mMainController;
    private final TrackViewModel mModel;
    private final TrackView mView;
    private final Logger mLogger = Logger.getLogger( TrackViewController.class.getName() );
    private LibraryItem mSelectedContainer;

    TrackViewController( JMusicController inMainController, TrackViewModel inModel, TrackView inView ) {
        mMainController = inMainController;
        mModel = inModel;
        mView = inView;
        mMainController.getLibrary().addListener( this );
        mMainController.getContainerView().getSelectionModel().selectedItemProperty().addListener( this );
        mView.setController( this );
        mView.setData( mModel.getData() );
    }

    @Override
    public void changed(
        ObservableValue< ? extends TreeItem< LibraryItem > > inObservable,
        TreeItem< LibraryItem > inOldValue,
        TreeItem< LibraryItem > inNewValue ) {
        containerSelectionChanged( inNewValue );
    }

    @Override
    public void onObjectCreate( LibraryItem inObject ) {
        if ( ! inObject.isTrack() || ! shouldTrackBeInModel( inObject ) ) {
            return;
        }
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                mModel.insertTrack( new TrackViewItem( inObject ) );
            }
        } );
    }

    @Override
    public void onObjectDestroy( LibraryItem inObject ) {
        if ( ! inObject.isTrack() ) {
            return;
        }
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                mModel.removeTrack( new TrackViewItem( inObject ) );
            }
        } );
    }

    @Override
    public void onObjectUpdate( LibraryItem inObject ) {
        if ( ! inObject.isTrack() ) {
            return;
        }
        if ( mSelectedContainer == null ) {
            return;
        }
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                TrackViewItem theTrack = new TrackViewItem( inObject );
                boolean shouldBeDisplayed = shouldTrackBeInModel( inObject );
                if ( mSelectedContainer.getType() == LibraryItem.Type.playlist ) {
                    boolean isDisplayed = mModel.isTrackInModel( theTrack );
                    if ( shouldBeDisplayed ) {
                        if ( isDisplayed ) {
                            mModel.updateTrack( theTrack );
                        } else {
                            mModel.addTrack( theTrack );
                        }
                    } else {
                        mModel.removeTrack( theTrack );
                    }
                } else {
                    mModel.removeTrack( theTrack );
                    if ( shouldBeDisplayed ) {
                        mModel.insertTrack( theTrack );
                    }
                }
            }
        } );
    }

    void containerSelectionChanged( TreeItem< LibraryItem > inContainer ) {
        if ( inContainer == null || inContainer.getValue() == null ) {
            return;
        }
        LibraryItem theContainer = inContainer.getValue();
        if ( theContainer.isTrack() ) {
            mLogger.warning( "Invalid type 'track' specified" );
            return;
        }
        ObservableList< TrackViewItem > theTracks = FXCollections.observableArrayList();
        switch( theContainer.getType() ) {
            case artist:
                loadArtist( theContainer, theTracks );
                break;
            case album:
                loadTrackContainer( theContainer, theTracks );
                break;
            case root:
            case cdroot:
                loadRoot( theContainer, theTracks );
                break;
            case playlist:
                loadTrackContainer( theContainer, theTracks );
                break;
            default:
                break;
        }
        mModel.setTracks( theTracks );
        mView.setEditable( mMainController.getLibrary().isWriteable( theContainer ) );
        mSelectedContainer = theContainer;
    }

    Clipboard getClipboard() { return mMainController.getClipboard(); }

    Clipboard getDragboard() { return mMainController.getDragboard(); }

    void updateTrack( TrackViewItem inTrack, int inField, String inOldValue, String inNewValue ) {
        boolean isValid = ! inOldValue.equals( inNewValue ) && inNewValue.length() > 0;
        switch( inField ) {
            case TrackView.sColumnNumber:
                inTrack.setTrackNumber( isValid ? Integer.valueOf( inNewValue) : Integer.valueOf( inOldValue ) );
                break;
            case TrackView.sColumnTitle:
                inTrack.setTitle( isValid ? inNewValue : inOldValue );
                break;
            case TrackView.sColumnAlbum:
                inTrack.setAlbumName( isValid ? inNewValue : inOldValue );
                break;
            case TrackView.sColumnArtist:
                inTrack.setArtistName( isValid ? inNewValue : inOldValue );
                break;
        }
        if ( isValid ) {
            mMainController.updateTrack( inTrack );
        }
    }

    private void loadArtist( LibraryItem inArtist, ObservableList< TrackViewItem > inTracks ) {
        LibraryBrowseResult theResult = mMainController.getLibrary().browse( inArtist.getId() );
        if ( theResult.mMaxResults == -1 ) {
            return;
        }
        for ( LibraryItem theAlbum : theResult.mResults ) {
            loadTrackContainer( theAlbum, inTracks );
        }
    }

    private void loadRoot( LibraryItem inRoot, ObservableList< TrackViewItem > inTracks ) {
        LibraryBrowseResult theResult = mMainController.getLibrary().browse( inRoot.getId() );
        if ( theResult.mMaxResults == -1 ) {
            return;
        }
        for ( LibraryItem theArtist : theResult.mResults ) {
            loadArtist( theArtist, inTracks );
        }
    }

    private void loadTrackContainer( LibraryItem inTrackContainer, ObservableList< TrackViewItem > inTracks ) {
        LibraryBrowseResult theResult = mMainController.getLibrary().browse( inTrackContainer.getId() );
        if ( theResult.mMaxResults == -1 ) {
            return;
        }
        for ( LibraryItem theTrack : theResult.mResults ) {
            inTracks.add( new TrackViewItem( theTrack ) );
        }
    }

    private boolean shouldTrackBeInModel( LibraryItem inTrack ) {
        if ( mSelectedContainer == null ) {
            return false;
        }
        boolean shouldBeInModel = false;
        switch( mSelectedContainer.getType() ) {
            case root:
                shouldBeInModel = inTrack.getRootId().equals( mSelectedContainer.getId() );
                break;
            case artist:
                shouldBeInModel = inTrack.getRootId().equals( mSelectedContainer.getRootId() ) &&
                    inTrack.getArtistName().equals( mSelectedContainer.getTitle() );
                break;
            case album:
                shouldBeInModel = inTrack.getParentId().equals( mSelectedContainer.getId() );
                break;
            case playlist:
                shouldBeInModel = inTrack.getPlaylistIds().contains( mSelectedContainer.getId() );
                break;
            default:
                break;
        }
        return shouldBeInModel;
    }
}