package jmusic.ui.tablecontent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;
import jmusic.library.LibraryBrowseResult;
import jmusic.library.LibraryItem;
import jmusic.library.LibraryListener;
import jmusic.ui.*;
import jmusic.ui.edittrack.EditTrackController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TableContentController implements LibraryListener, SelectedItemListener, ContentController {
    private final JMusicController mMainController;
    private final TableContentModel<TableContentItem> mModel = new TableContentModel<>();
    private final TableContentView mView = new TableContentView( this );
    private final Set< ContentsChangedListener > mListeners = new HashSet<>();
    private final Logger mLogger = Logger.getLogger( TableContentController.class.getName() );
    private LibraryItem mSelectedContainer;

    public TableContentController( JMusicController inMainController) {
        mMainController = inMainController;
        mMainController.getLibrary().addListener( this );
        mMainController.addNavigationSelectedItemListener( this );
        mView.setData( mModel.getData() );
        initViewHandlers();
    }

    @Override
    public void addContentsChangedListener( ContentsChangedListener inListener ) {
        synchronized( mListeners ) {
            mListeners.add( inListener );
        }
    }

    @Override
    public void cancelEdit() {
        mView.cancelEdit();
    }

    @Override
    public void changed( LibraryItem inOldItem, LibraryItem inNewItem ) {
        containerSelectionChanged( inNewItem );
    }

    @Override
    public void clearSelection() {
        mView.clearSelection();
    }

    @Override
    public List< LibraryItem > getItems() {
        return new ArrayList<>( mView.getItems() );
    }

    @Override
    public int getSelectedIndex() {
        return mView.getSelectedIndex();
    }

    @Override
    public List< LibraryItem > getSelectedItems() {
        return new ArrayList<>( mView.getSelectedItems() );
    }

    @Override
    public Node getView() {
        return mView.getTableView();
    }

    @Override
    public void onObjectCreate( LibraryItem inObject ) {
        if ( ! inObject.isTrack() || ! shouldTrackBeInModel( inObject ) ) {
            return;
        }
        Platform.runLater( () -> mModel.insertTrack( new TableContentItem( inObject ) ) );
    }

    @Override
    public void onObjectDestroy( LibraryItem inObject ) {
        if ( ! inObject.isTrack() ) {
            return;
        }
        Platform.runLater( () -> mModel.removeTrack( new TableContentItem( inObject ) ) );
    }

    @Override
    public void onObjectUpdate( LibraryItem inObject ) {
        if ( ! inObject.isTrack() ) {
            return;
        }
        if ( mSelectedContainer == null ) {
            return;
        }
        Platform.runLater( () -> {
            TableContentItem theTrack = new TableContentItem( inObject );
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
        } );
    }

    @Override
    public void removeContentsChangedListener( ContentsChangedListener inListener ) {
        synchronized( mListeners ) {
            mListeners.remove( inListener );
        }
    }

    @Override
    public void selectItem( LibraryItem inItem ) {
        mView.selectItem( new TableContentItem( inItem ) );
    }

    Clipboard getClipboard() { return mMainController.getClipboard(); }

    Clipboard getDragboard() { return mMainController.getDragboard(); }

    void updateTrack( TableContentItem inTrack, int inField, String inOldValue, String inNewValue ) {
        boolean isValid = ! inOldValue.equals( inNewValue ) && inNewValue.length() > 0;
        switch( inField ) {
            case TableContentView.sColumnNumber:
                inTrack.setTrackNumber( isValid ? Integer.valueOf( inNewValue) : Integer.valueOf( inOldValue ) );
                break;
            case TableContentView.sColumnTitle:
                inTrack.setTitle( isValid ? inNewValue : inOldValue );
                break;
            case TableContentView.sColumnAlbum:
                inTrack.setAlbumName( isValid ? inNewValue : inOldValue );
                break;
            case TableContentView.sColumnArtist:
                inTrack.setArtistName( isValid ? inNewValue : inOldValue );
                break;
        }
        if ( isValid ) {
            mMainController.updateTrack( inTrack );
        }
    }

    private void containerSelectionChanged( LibraryItem inContainer ) {
        if ( inContainer == null ) {
            return;
        }
        if ( inContainer.isTrack() ) {
            mLogger.warning( "Invalid type 'track' specified" );
            return;
        }
        ObservableList< TableContentItem > theTracks = FXCollections.observableArrayList();
        switch( inContainer.getType() ) {
            case artist:
                loadArtist( inContainer, theTracks );
                break;
            case album:
                loadTrackContainer( inContainer, theTracks );
                break;
            case root:
            case cdroot:
                loadRoot( inContainer, theTracks );
                break;
            case playlist:
                loadTrackContainer( inContainer, theTracks );
                break;
            default:
                break;
        }
        mModel.setTracks( theTracks );
        mView.setEditable( mMainController.getLibrary().isWriteable( inContainer ) );
        mSelectedContainer = inContainer;
    }

    private void handleContextMenuOnAction( ActionEvent inEvent ) {
        Object theSource = inEvent.getSource();
        if ( mView.getMenuItemEdit().equals( theSource ) ) {
            mMainController.editTracks( EditTrackController.ControllerType.Edit );
        } else if ( mView.getMenuItemCopy().equals( theSource ) ) {
            mMainController.copy( false );
        } else if ( mView.getMenuItemRemove().equals( theSource ) ) {
            mMainController.remove();
        }
        inEvent.consume();
    }

    private void handleContextMenuOnShown( WindowEvent inEvent ) {
        boolean isDisabled = getSelectedItems().isEmpty();
        mView.getMenuItemCopy().setDisable( isDisabled );
        mView.getMenuItemEdit().setDisable( isDisabled );
        LibraryItem theItem = mMainController.getSelectedNavigationItem();
        mView.getMenuItemRemove().setDisable( isDisabled || theItem == null || ! theItem.isPlaylist() );
        inEvent.consume();
    }

    private void handleOnContentsChanged( ListChangeListener.Change< ? extends TableContentItem > inChanges ) {
        if ( mListeners.isEmpty() ) {
            return;
        }

        List< LibraryItem > theAddedItems = new ArrayList<>();
        List< LibraryItem > theRemovedItems = new ArrayList<>();
        while ( inChanges.next() ) {
            if ( inChanges.wasAdded() ) {
                theAddedItems.addAll( inChanges.getAddedSubList() );
            } else if ( inChanges.wasRemoved() ) {
                theRemovedItems.addAll( inChanges.getRemoved() );
            }
        }
        synchronized( mListeners ) {
            for ( ContentsChangedListener theListener : mListeners ) {
                theListener.changed( theAddedItems, theRemovedItems );
            }
        }
    }

    private void handleOnEditCommit( TableColumn.CellEditEvent< TableContentItem, String > inEvent ) {
        updateTrack( inEvent.getRowValue(),
                inEvent.getTablePosition().getColumn(),
                inEvent.getOldValue().trim(),
                inEvent.getNewValue().trim() );
        inEvent.consume();
    }

    private void handleOnKeyPressed( KeyEvent inEvent ) {
        if ( inEvent.getCode().equals( KeyCode.CONTROL ) ) {
            mView.getTableView().setEditable( false );
            inEvent.consume();
        }
    }

    private void handleOnKeyReleased( KeyEvent inEvent ) {
        if ( inEvent.getCode().equals( KeyCode.CONTROL ) ) {
            mView.getTableView().setEditable( true );
            inEvent.consume();
        }
    }

    private void handleOnMouseClicked( MouseEvent inEvent ) {
        mMainController.onContentViewMouseClicked( inEvent );
    }

    private void initViewHandlers() {
        mView.getTableView().setOnKeyPressed( this::handleOnKeyPressed );
        mView.getTableView().setOnKeyReleased( this::handleOnKeyReleased );
        mView.getTableView().setOnMouseClicked( this::handleOnMouseClicked );
        mView.getTableView().itemsProperty().getValue().addListener( this::handleOnContentsChanged );
        for ( TableColumn<TableContentItem, String > theColumn : mView.getColumns() ) {
            theColumn.setOnEditCommit( this::handleOnEditCommit );
        }
        mView.getContextMenu().setOnShown( this::handleContextMenuOnShown );
        mView.getMenuItemCopy().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemEdit().setOnAction( this::handleContextMenuOnAction );
        mView.getMenuItemRemove().setOnAction( this::handleContextMenuOnAction );
    }

    private void loadArtist( LibraryItem inArtist, ObservableList< TableContentItem > inTracks ) {
        LibraryBrowseResult theResult = mMainController.getLibrary().browse( inArtist.getId() );
        if ( theResult.mMaxResults == -1 ) {
            return;
        }
        for ( LibraryItem theAlbum : theResult.mResults ) {
            loadTrackContainer( theAlbum, inTracks );
        }
    }

    private void loadRoot( LibraryItem inRoot, ObservableList< TableContentItem > inTracks ) {
        LibraryBrowseResult theResult = mMainController.getLibrary().browse( inRoot.getId() );
        if ( theResult.mMaxResults == -1 ) {
            return;
        }
        for ( LibraryItem theArtist : theResult.mResults ) {
            loadArtist( theArtist, inTracks );
        }
    }

    private void loadTrackContainer( LibraryItem inTrackContainer, ObservableList< TableContentItem > inTracks ) {
        LibraryBrowseResult theResult = mMainController.getLibrary().browse( inTrackContainer.getId() );
        if ( theResult.mMaxResults == -1 ) {
            return;
        }
        inTracks.addAll( theResult.mResults.stream().map( TableContentItem::new ).collect( Collectors.toList() ) );
    }

    private boolean shouldTrackBeInModel( LibraryItem inTrack ) {
        if ( mSelectedContainer == null ) {
            return false;
        }
        boolean shouldBeInModel = false;
        switch( mSelectedContainer.getType() ) {
            case root:
            case cdroot:
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