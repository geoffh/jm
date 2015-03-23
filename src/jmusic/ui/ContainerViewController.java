package jmusic.ui;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import jmusic.library.LibraryBrowseResult;
import jmusic.library.LibraryItem;
import jmusic.library.LibraryListener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerViewController implements LibraryListener {
    private final JMusicController mMainController;
    private final ContainerViewModel mModel;
    private final ContainerView mView;

    ContainerViewController( JMusicController inMainController, ContainerViewModel inModel, ContainerView inView ) {
        mMainController = inMainController;
        mModel = inModel;
        mView = inView;
        mMainController.getLibrary().addListener( this );
        mModel.setController( this );
        mModel.setRootItem( mMainController.getLibrary().getRootOfRoots() );
        mView.setController( this );
        mView.setRoot( mModel.getData() );
    }

    Collection< LibraryItem > browse( Long inContainerId ) {
        LibraryBrowseResult theResults = mMainController.getLibrary().browse( inContainerId );
        return theResults.mMaxResults != -1 ? theResults.mResults : Collections.EMPTY_LIST;
    }

    @Override
    public void onObjectCreate( LibraryItem inObject ) {
        processObjectCreationOrDeletion( inObject, true );
    }

    @Override
    public void onObjectDestroy( LibraryItem inObject ) {
        processObjectCreationOrDeletion( inObject, false );
    }

    @Override
    public void onObjectUpdate( LibraryItem inObject ) {
        if ( ! mModel.containsItem( inObject ) ) {
            return;
        }
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                mModel.updateItem( inObject );
            }
        } );
    }

    Clipboard getClipboard() { return mMainController.getClipboard(); }

    Clipboard getDragboard() { return mMainController.getDragboard(); }

    boolean isValidPasteTarget( List< ? extends LibraryItem > inSources, LibraryItem inTarget ) {
        return mMainController.isValidPasteTarget( inSources, inTarget );
    }

    void drop( List< ? extends LibraryItem > inSource, LibraryItem inTarget ) {
        mMainController.drop( inSource, inTarget );
    }

    void updateContainer( LibraryItem inOldItem, LibraryItem inNewItem ) {
        String theOldTitle = inOldItem.getTitle().trim();
        String theNewTitle = inNewItem.getTitle().trim();
        if ( theOldTitle.equals( theNewTitle ) || theNewTitle.length() == 0 ) {
            inNewItem.setTitle( theOldTitle );
            return;
        }
        inNewItem.setTitle( theNewTitle );
        switch( inNewItem.getType() ) {
            case root:
                mMainController.updateRoot( inNewItem );
                break;
            case playlist:
                mMainController.updatePlaylist( inNewItem );
                break;
            case artist:
            case album:
                updateTrackContainer( inNewItem );
                break;
            default:
                break;
        }
    }

    private void processObjectCreationOrDeletion( LibraryItem inObject, boolean inIsCreation ) {
        if ( inObject.isTrack() ) {
            return;
        }
        Platform.runLater( new Runnable() {
            @Override
            public void run() {
                if ( inIsCreation ) {
                    mModel.addItem( inObject );
                } else {
                    TreeItem< LibraryItem > theItem = mView.getSelection();
                    mModel.removeItem( inObject );
                    // Not sure why, but deletion of an unselected item changes the selection
                    if ( theItem != null && theItem.getValue() != inObject ) {
                        mView.setSelection( theItem );
                    }
                }
            }
        } );
    }

    private void updateTrackContainer( LibraryItem inContainer ) {
        String theArtistName = null;
        String theAlbumName = null;
        if ( inContainer.isArtist() ) {
            theArtistName = inContainer.getTitle();
        } else {
            theAlbumName = inContainer.getTitle();
        }
        Map< Long, LibraryItem > theTracks = new HashMap<>();
        for ( LibraryItem theTrack : mMainController.getLibrary().getTracks( inContainer ) ) {
            if ( theArtistName != null ) {
                theTrack.setArtistName( theArtistName );
            }
            if ( theAlbumName != null ) {
                theTrack.setAlbumName( theAlbumName );
            }
            theTracks.put( theTrack.getId(), theTrack );
        }
        mMainController.updateTracks( theTracks );
    }
}