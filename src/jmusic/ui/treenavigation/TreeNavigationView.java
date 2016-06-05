package jmusic.ui.treenavigation;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.StringConverter;
import jmusic.library.LibraryItem;
import jmusic.ui.Clipboard;

class TreeNavigationView {
    private final TreeView< LibraryItem > mView = new TreeView<>();
    private final TreeNavigationController mController;
    private Cell mEditingCell;
    private final ContextMenu mContextMenu =  new ContextMenu();
    private final Menu mNewMenu = new Menu( "New" );
    private final MenuItem mMenuItemNewRoot = new MenuItem( "Music Source ..." );
    private final MenuItem mMenuItemNewPlaylist = new MenuItem( "Playlist ..." );
    private final MenuItem mMenuItemRefresh = new MenuItem( "Refresh" );
    private final MenuItem mMenuItemBrokenTracks = new MenuItem( "Broken Tracks ..." );
    private final MenuItem mMenuItemUnknownTracks = new MenuItem( "Unknown Tracks ..." );
    private final MenuItem mMenuItemCopy = new MenuItem( "Copy" );
    private final MenuItem mMenuItemPaste = new MenuItem( "Paste" );
    private final MenuItem mMenuItemRemove = new MenuItem( "Remove" );

    TreeNavigationView( TreeNavigationController inController ) {
        mController = inController;
        mView.setShowRoot( false );
        mView.setCellFactory( new ContainerViewCellFactory( this ) );
        initContextMenu();
    }

    void cancelEdit() {
        if ( mEditingCell != null ) {
            mEditingCell.cancelEdit();
        }
    }

    ContextMenu getContextMenu() {
        return mContextMenu;
    }

    MenuItem getMenuItemBrokenTracks() {
        return mMenuItemBrokenTracks;
    }

    MenuItem getMenuItemCopy() {
        return mMenuItemCopy;
    }

    MenuItem getMenuItemNewPlaylist() {
        return mMenuItemNewPlaylist;
    }

    MenuItem getMenuItemNewRoot() {
        return mMenuItemNewRoot;
    }

    MenuItem getMenuItemPaste() {
        return mMenuItemPaste;
    }

    MenuItem getMenuItemRefresh() {
        return mMenuItemRefresh;
    }

    MenuItem getMenuItemRemove() {
        return mMenuItemRemove;
    }

    MenuItem getMenuItemUnknownTracks() {
        return mMenuItemUnknownTracks;
    }

    TreeItem< LibraryItem > getSelectedItem() {
        return mView.getSelectionModel().getSelectedItem();
    }

    ReadOnlyObjectProperty< TreeItem< LibraryItem > > getSelectedItemProperty() {
        return mView.getSelectionModel().selectedItemProperty();
    }

    TreeView< LibraryItem > getTreeView() {
        return mView;
    }

    void setSelection( TreeItem< LibraryItem > inItem ) {
        mView.getSelectionModel().select( inItem );
    }

    void setRoot( TreeItem< LibraryItem > inRoot ) {
        mView.setRoot( inRoot );
        mView.getSelectionModel().clearSelection();
    }

    private Clipboard getDragboard() {
        return mController.getDragboard();
    }

    private void initContextMenu() {
        mNewMenu.getItems().addAll( mMenuItemNewRoot, mMenuItemNewPlaylist );
        mContextMenu.getItems().addAll(
                mNewMenu, mMenuItemRefresh, mMenuItemBrokenTracks,
                mMenuItemUnknownTracks, mMenuItemCopy, mMenuItemPaste,
                mMenuItemRemove );
        mView.setContextMenu( mContextMenu );
    }

    private void startEdit( Cell inCell ) {
        mEditingCell = inCell;
    }

    class ContainerViewCellFactory implements Callback< TreeView< LibraryItem >,
        TreeCell< LibraryItem > > {
        private final TreeNavigationView mView;

        ContainerViewCellFactory( TreeNavigationView inView ) {
            mView = inView;
        }
        @Override
        public TreeCell< LibraryItem > call( TreeView< LibraryItem > inView ) {
            CancellableTextFieldTreeCell theTreeCell = new CancellableTextFieldTreeCell( mView );
            theTreeCell.setEditable( true );
            theTreeCell.setOnDragDetected( inEvent -> {
                LibraryItem theItem = theTreeCell.getItem();
                if ( theItem != null && ! theItem.isPlaylistsRoot() && ! theItem.isPlaylist() ) {
                    ClipboardContent theFakeContent = new ClipboardContent();
                    theFakeContent.putString( "" );
                    theTreeCell.startDragAndDrop( TransferMode.ANY ).setContent( theFakeContent );
                    getDragboard().setContent( theItem );
                }
                inEvent.consume();
            } );
            theTreeCell.setOnDragOver( inEvent -> {
                if ( mController.isValidPasteTarget( getDragboard().getContent(), theTreeCell.getItem() ) ) {
                    inEvent.acceptTransferModes( TransferMode.COPY );
                } else {
                    inEvent.acceptTransferModes( TransferMode.NONE );
                }
                inEvent.consume();
            } );
            theTreeCell.setOnDragDropped( inEvent -> {
                boolean bCompletedSuccessfully = false;
                if ( getDragboard().getContent() != null ) {
                    bCompletedSuccessfully = true;
                    mController.drop( getDragboard().getContent(), theTreeCell.getItem() );
                    getDragboard().setContent( ( LibraryItem )null );
                }
                inEvent.setDropCompleted( bCompletedSuccessfully );
                inEvent.consume();
                theTreeCell.setStyle( "-fx-border-style:none;" );
            } );
            theTreeCell.setOnDragEntered( inEvent -> {
                if ( getDragboard().getContent() != null &&
                    inEvent.getGestureSource() != theTreeCell &&
                    theTreeCell.getItem() != null ) {
                    theTreeCell.setStyle( "-fx-border-color:red;-fx-border-width:2;-fx-border-style:solid;" );
                }
                inEvent.consume();
            } );
            theTreeCell.setOnDragExited( inEvent -> {
                if ( getDragboard().getContent() != null &&
                    inEvent.getGestureSource() != theTreeCell ) {
                    theTreeCell.setStyle( "-fx-border-style:none;" );
                }
                inEvent.consume();
            } );
            return theTreeCell;
        }
    }

    class CancellableTextFieldTreeCell extends TextFieldTreeCell< LibraryItem > {
        private final TreeNavigationView mView;

        CancellableTextFieldTreeCell( TreeNavigationView inView ) {
            super();
            mView = inView;
            ContainerViewStringConverter theConverter = new ContainerViewStringConverter();
            setConverter( theConverter );
            theConverter.setCell( this );
        }

        public void startEdit( ) {
            super.startEdit();
            mView.startEdit( this );
        }
    }

    class ContainerViewStringConverter extends StringConverter< LibraryItem > {
        TreeCell< LibraryItem > mCell;

        @Override
        public String toString( LibraryItem inItem ) {
            // Occasional NPE without null check below
            return inItem != null ? inItem.getTitle() : "";
        }

        @Override
        public LibraryItem fromString( String inString ) {
            LibraryItem theItem = new LibraryItem( mCell.getTreeItem().getValue() );
            theItem.setTitle( inString );
            return theItem;
        }

        void setCell( TreeCell< LibraryItem > inCell ) {
            mCell = inCell;
        }
    }
}