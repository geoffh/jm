package jmusic.ui;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Cell;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

class TrackView implements EventHandler< TableColumn.CellEditEvent< TrackViewItem, String > > {
    static final int sColumnNumber = 0;
    static final int sColumnTitle  = 1;
    static final int sColumnAlbum  = 2;
    static final int sColumnArtist = 3;

    private final TableView< TrackViewItem > mView;
    private TrackViewController mController;
    private Cell mEditingCell;

    TrackView( TableView< TrackViewItem > inTrackView ) {
        mView = inTrackView;
        mView.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        mView.setRowFactory( new TrackViewRowFactory() );
        createColumns();
        mView.setTableMenuButtonVisible( true );
    }

    @Override
    public void handle( TableColumn.CellEditEvent< TrackViewItem, String > inEvent ) {
        mController.updateTrack( inEvent.getRowValue(),
                inEvent.getTablePosition().getColumn(),
                inEvent.getOldValue().trim(),
                inEvent.getNewValue().trim() );
        inEvent.consume();
    }

    void cancelEdit() {
        if ( mEditingCell != null ) {
            mEditingCell.cancelEdit();
        }
    }

    ObservableList< TrackViewItem > getSelectedItems() {
        return mView.getSelectionModel().getSelectedItems();
    }

    void setController( TrackViewController inController ) {
        mController = inController;
    }

    void setData( ObservableList< TrackViewItem > inData ) {
        mView.setItems( inData );
    }

    void setEditable( boolean inEditable ) {
        mView.setEditable( inEditable );
    }

    void startEdit( Cell inCell ) {
        mEditingCell = inCell;
    }

    private void createColumns() {
        ReadOnlyDoubleProperty theWidth = mView.widthProperty();
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.1 ),
                TrackViewItem.sColumnHeaderNumber,
                TrackViewItem.sPropertyNameNumber ) );
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.5 ),
                TrackViewItem.sColumnHeaderTitle,
                TrackViewItem.sPropertyNameTitle ) );
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.25 ),
                TrackViewItem.sColumnHeaderAlbum,
                TrackViewItem.sPropertyNameAlbum ) );
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.15 ),
                TrackViewItem.sColumnHeaderArtist,
                TrackViewItem.sPropertyNameArtist ) );
    }

    private TableColumn< TrackViewItem, String > createColumn(
        DoubleBinding inWidth, String inColumnHeaderString, String inTrackPropertyName ) {
        TableColumn< TrackViewItem, String > theColumn = new TableColumn<>( inColumnHeaderString );
        theColumn.prefWidthProperty().bind( inWidth );
        theColumn.setCellValueFactory( new PropertyValueFactory( inTrackPropertyName ) );
        theColumn.setCellFactory( CancellableTextFieldTableCell.< TrackViewItem >forTableColumn( this ) );
        theColumn.setOnEditCommit( this );
        return theColumn;
    }

    Clipboard getDragboard() {
        return mController.getDragboard();
    }

    class TrackViewRowFactory implements Callback< TableView< TrackViewItem >, TableRow< TrackViewItem > > {
        @Override
        public TableRow< TrackViewItem > call( TableView< TrackViewItem > inTableView ) {
            TableRow< TrackViewItem > theTableRow = new TableRow< TrackViewItem >() {
                protected void updateItem( TrackViewItem inItem, boolean inEmpty ) {
                    super.updateItem( inItem, inEmpty );
                }
            };
            theTableRow.setOnDragDetected( new EventHandler< MouseEvent >() {
                @Override
                public void handle( MouseEvent inEvent ) {
                    ClipboardContent theFakeContent = new ClipboardContent();
                    theFakeContent.putString( "" );
                    theTableRow.startDragAndDrop( TransferMode.ANY ).setContent( theFakeContent );
                    getDragboard().setContent( getSelectedItems() );
                    inEvent.consume();
                }
            } );
            return theTableRow;
        }
    }

    static class CancellableTextFieldTableCell extends TextFieldTableCell< TrackViewItem, String > {
        private final TrackView mView;

        public CancellableTextFieldTableCell( TrackView inView ) {
            super( new DefaultStringConverter() );
            mView = inView;
        }

        public void startEdit() {
            super.startEdit();
            mView.startEdit( this );
        }

        public static Callback< TableColumn< TrackViewItem, String >, TableCell< TrackViewItem, String > > forTableColumn( TrackView inView ) {
            return new Callback< TableColumn< TrackViewItem, String >, TableCell< TrackViewItem, String > >() {
                @Override
                public TableCell< TrackViewItem, String > call( TableColumn< TrackViewItem, String > inParam ) {
                    return new CancellableTextFieldTableCell( inView );
                }
            };
        }
    }
}