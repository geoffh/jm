package jmusic.ui.treenavigation;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;
import jmusic.library.LibraryBrowseResult;
import jmusic.library.LibraryItem;
import jmusic.library.LibraryListener;
import jmusic.ui.*;
import jmusic.ui.edittrack.EditTrackController;

import java.util.*;

public class TreeNavigationController implements LibraryListener, NavigationController, ChangeListener< TreeItem< LibraryItem > > {
    private final JMusicController mMainController;
    private final TreeNavigationModel mModel = new TreeNavigationModel( this );
    private final TreeNavigationView mView = new TreeNavigationView( this );
    private final Set< SelectedItemListener > mSelectedItemListeners = new HashSet<>();

    public TreeNavigationController( JMusicController inMainController ) {
        mMainController = inMainController;
        mMainController.getLibrary().addListener( this );
        mModel.setRootItem( mMainController.getLibrary().getRootOfRoots() );
        mView.setRoot( mModel.getData() );
        mView.getSelectedItemProperty().addListener( this );
        initViewHandlers();
    }

    @Override
    public void addSelectedItemListener( SelectedItemListener inListener ) {
        synchronized( mSelectedItemListeners ) {
            mSelectedItemListeners.add( inListener );
        }
    }

    @Override
    public void cancelEdit() {
        mView.cancelEdit();
    }

    @Override
    public void changed( ObservableValue< ? extends TreeItem< LibraryItem > > inObservable, TreeItem< LibraryItem > inOldValue, TreeItem< LibraryItem > inNewValue ) {
        if ( mSelectedItemListeners.isEmpty() ) {
            return;
        }
        LibraryItem theOldValue = inOldValue != null ? inOldValue.getValue() : null;
        LibraryItem theNewValue = inNewValue != null ? inNewValue.getValue() : null;
        synchronized( mSelectedItemListeners ) {
            for ( SelectedItemListener theListener : mSelectedItemListeners ) {
                theListener.changed( theOldValue, theNewValue );
            }
        }
    }

    @Override
    public LibraryItem getSelectedItem() {
        TreeItem< LibraryItem > theItem = mView.getSelectedItem();
        return theItem != null ? theItem.getValue() : null;
    }

    @Override
    public Node getView() {
        return mView.getTreeView();
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
        Platform.runLater( () -> mModel.updateItem( inObject ) );
    }

    @Override
    public void removeSelectedItemListener( SelectedItemListener inListener ) {
        synchronized( mSelectedItemListeners ) {
            mSelectedItemListeners.remove( inListener );
        }
    }

    Collection< LibraryItem > browse( Long inContainerId ) {
        LibraryBrowseResult theResults = mMainController.getLibrary().browse( inContainerId );
        return theResults.mMaxResults != -1 ? theResults.mResults : Collections.EMPTY_LIST;
    }

    void drop( List< ? extends LibraryItem > inSource, LibraryItem inTarget ) {
        mMainController.drop( inSource, inTarget );
    }

    Clipboard getDragboard() { return mMainController.getDragboard(); }

    boolean isValidPasteTarget( List< ? extends LibraryItem > inSources, LibraryItem inTarget ) {
        return mMainController.isValidPasteTarget( inSources, inTarget );
    }

    private void handleContextMenuOnAction( ActionEvent inEvent ) {
        Object theSource = inEvent.getSource();
        if ( mView.getMenuItemNewRoot().equals( theSource ) ) {
            mMainController.addRoot();
        } else if ( mView.getMenuItemNewPlaylist().equals( theSource ) ) {
            mMainController.addPlaylist();
        } else if ( mView.getMenuItemRefresh().equals( theSource ) ) {
            mMainController.refresh();
        } else if ( mView.getMenuItemBrokenTracks().equals( theSource ) ) {
            mMainController.editTracks( jmusic.ui.edittrack.EditTrackController.ControllerType.Broken );
        } else if ( mView.getMenuItemUnknownTracks().equals( theSource ) ) {
            mMainController.editTracks( EditTrackController.ControllerType.Unknown );
        } else if ( mView.getMenuItemCopy().equals( theSource ) ) {
            mMainController.copy( true );
        } else if ( mView.getMenuItemPaste().equals( theSource ) ) {
            mMainController.paste();
        } else if ( mView.getMenuItemRemove().equals( theSource ) ) {
            mMainController.remove();
        }
        inEvent.consume();
    }

    private void handleContextMenuOnShown( WindowEvent inEvent ) {
        LibraryItem theItem = mView.getSelectedItem() != null ? mView.getSelectedItem().getValue() : null;
        boolean bDisableBrokenTracks = true;
        boolean bDisableUnknownTracks = true;
        boolean bDisableCopy = true;
        boolean bDisablePaste = ! isValidPasteTarget( mMainController.getClipboard().getContent(), theItem );
        boolean bDisableRefresh = true;
        boolean bDisableRemove = true;
        if ( theItem != null ) {
            switch ( theItem.getType() ) {
                case root:
                    bDisableBrokenTracks = false;
                    bDisableUnknownTracks = false;
                    bDisableCopy = false;
                    bDisableRefresh = false;
                    bDisableRemove = false;
                    break;
                case cdroot:
                    bDisableCopy = false;
                    bDisableRefresh = false;
                    break;
                case playlist:
                    bDisableRemove = false;
                    break;
                case artist:
                case album:
                    bDisableCopy = false;
                    break;
                default:
                    break;
            }
        }
        mView.getMenuItemCopy().setDisable( bDisableCopy );
        mView.getMenuItemPaste().setDisable( bDisablePaste );
        mView.getMenuItemRefresh().setDisable( bDisableRefresh );
        mView.getMenuItemBrokenTracks().setDisable( bDisableBrokenTracks );
        mView.getMenuItemUnknownTracks().setDisable( bDisableUnknownTracks );
        mView.getMenuItemRemove().setDisable( bDisableRemove );
        inEvent.consume();
    }

    private void handleOnEditCommit( TreeView.EditEvent< LibraryItem > inEvent ) {
        updateContainer( inEvent.getOldValue(), inEvent.getNewValue() );
        inEvent.consume();
    }

    private void handleOnKeyPressed( KeyEvent inEvent ) {
        if ( inEvent.getCode().equals( KeyCode.CONTROL ) ) {
            mView.getTreeView().setEditable( false );
            inEvent.consume();
        }
    }

    private void handleOnKeyReleased( KeyEvent inEvent ) {
        if ( inEvent.getCode().equals( KeyCode.CONTROL ) ) {
            mView.getTreeView().setEditable( true );
            inEvent.consume();
        }
    }

    private void handleOnMouseClicked( MouseEvent inEvent ) {
        mMainController.onNavigationViewMouseClicked( inEvent );
    }

    private void initViewHandlers() {
        mView.getTreeView().setOnEditCommit( this::handleOnEditCommit );
        mView.getTreeView().setOnKeyPressed( this::handleOnKeyPressed );
        mView.getTreeView().setOnKeyReleased( this::handleOnKeyReleased );
        mView.getTreeView().setOnMouseClicked( this::handleOnMouseClicked );
        mView.getContextMenu().setOnShown( this::handleContextMenuOnShown );
        //mView.getContextMenu().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemBrokenTracks().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemCopy().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemNewPlaylist().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemNewRoot().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemPaste().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemRefresh().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemRemove().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemUnknownTracks().setOnAction( this::handleContextMenuOnAction );
    }

    private void processObjectCreationOrDeletion( LibraryItem inObject, boolean inIsCreation ) {
        if ( inObject.isTrack() ) {
            return;
        }
        Platform.runLater( () -> {
            if ( inIsCreation ) {
                mModel.addItem( inObject );
            } else {
                TreeItem< LibraryItem > theItem = mView.getSelectedItem();
                mModel.removeItem( inObject );
                // Not sure why, but deletion of an unselected item changes the selection
                if ( theItem != null && theItem.getValue() != inObject ) {
                    mView.setSelection( theItem );
                }
            }
        } );
    }

    private void updateContainer( LibraryItem inOldItem, LibraryItem inNewItem ) {
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