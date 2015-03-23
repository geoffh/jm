package jmusic.ui;

import javafx.event.EventHandler;
import javafx.scene.control.Cell;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.StringConverter;
import jmusic.library.LibraryItem;

class ContainerView implements EventHandler< TreeView.EditEvent< LibraryItem > > {
    private final TreeView< LibraryItem > mView;
    private ContainerViewController mController;
    private Cell mEditingCell;

    ContainerView( TreeView< LibraryItem > inView ) {
        mView = inView;
        mView.setCellFactory( new ContainerViewCellFactory( this ) );
        mView.setOnEditCommit( this );
    }

    @Override
    public void handle( TreeView.EditEvent< LibraryItem > inEvent ) {
        mController.updateContainer( inEvent.getOldValue(), inEvent.getNewValue() );
        inEvent.consume();
    }

    void cancelEdit() {
        if ( mEditingCell != null ) {
            mEditingCell.cancelEdit();
        }
    }

    Clipboard getDragboard() {
        return mController.getDragboard();
    }

    TreeItem< LibraryItem > getSelection() {
        return mView.getSelectionModel().getSelectedItem();
    }

    void setController( ContainerViewController inController ) {
        mController = inController;
    }

    void setSelection( TreeItem< LibraryItem > inItem ) {
        mView.getSelectionModel().select( inItem );
    }

    void setRoot( TreeItem< LibraryItem > inRoot ) {
        mView.setRoot( inRoot );
        mView.getSelectionModel().clearSelection();
    }

    void startEdit( Cell inCell ) {
        mEditingCell = inCell;
    }

    class ContainerViewCellFactory implements Callback< TreeView< LibraryItem >,
        TreeCell< LibraryItem > > {
        private final ContainerView mView;

        ContainerViewCellFactory( ContainerView inView ) {
            mView = inView;
        }
        @Override
        public TreeCell< LibraryItem > call( TreeView< LibraryItem > inView ) {
            CancellableTextFieldTreeCell theTreeCell = new CancellableTextFieldTreeCell( mView );
            theTreeCell.setEditable( true );
            theTreeCell.setOnDragDetected( new EventHandler< MouseEvent >() {
                @Override
                public void handle( MouseEvent inEvent ) {
                    LibraryItem theItem = theTreeCell.getItem();
                    if ( theItem != null && ! theItem.isPlaylistsRoot() && ! theItem.isPlaylist() ) {
                        ClipboardContent theFakeContent = new ClipboardContent();
                        theFakeContent.putString( "" );
                        theTreeCell.startDragAndDrop( TransferMode.ANY ).setContent( theFakeContent );
                        getDragboard().setContent( theItem );
                    }
                    inEvent.consume();
                }
            } );
            theTreeCell.setOnDragOver( new EventHandler< DragEvent >() {
                @Override
                public void handle( DragEvent inEvent ) {
                    if ( mController.isValidPasteTarget( getDragboard().getContent(), theTreeCell.getItem() ) ) {
                        inEvent.acceptTransferModes( TransferMode.COPY );
                    } else {
                        inEvent.acceptTransferModes( TransferMode.NONE );
                    }
                    inEvent.consume();
                }
            } );
            theTreeCell.setOnDragDropped( new EventHandler< DragEvent >() {
                @Override
                public void handle( DragEvent inEvent ) {
                    boolean bCompletedSuccessfully = false;
                    if ( getDragboard().getContent() != null ) {
                        bCompletedSuccessfully = true;
                        mController.drop( getDragboard().getContent(), theTreeCell.getItem() );
                        getDragboard().setContent( ( LibraryItem )null );
                    }
                    inEvent.setDropCompleted( bCompletedSuccessfully );
                    inEvent.consume();
                    theTreeCell.setStyle( "-fx-border-style:none;" );
                }
            } );
            theTreeCell.setOnDragEntered( new EventHandler< DragEvent >() {
                @Override
                public void handle( DragEvent inEvent ) {
                    if ( getDragboard().getContent() != null &&
                        inEvent.getGestureSource() != theTreeCell &&
                        theTreeCell.getItem() != null ) {
                        theTreeCell.setStyle( "-fx-border-color:red;-fx-border-width:2;-fx-border-style:solid;" );
                    }
                    inEvent.consume();
                }
            } );
            theTreeCell.setOnDragExited( new EventHandler< DragEvent >() {
                @Override
                public void handle( DragEvent inEvent ) {
                    if ( getDragboard().getContent() != null &&
                        inEvent.getGestureSource() != theTreeCell ) {
                        theTreeCell.setStyle( "-fx-border-style:none;" );
                    }
                    inEvent.consume();
                }
            } );
            return theTreeCell;
        }
    }

    class CancellableTextFieldTreeCell extends TextFieldTreeCell< LibraryItem > {
        private final ContainerView mView;

        CancellableTextFieldTreeCell( ContainerView inView ) {
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