package jmusic.ui.tablecontent;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import jmusic.ui.Clipboard;

import java.util.ArrayList;
import java.util.List;

class TableContentView {
    static final int sColumnNumber = 0;
    static final int sColumnTitle  = 1;
    static final int sColumnAlbum  = 2;
    static final int sColumnArtist = 3;

    private final TableView< TableContentItem > mView = new TableView<>();
    private final List< TableColumn< TableContentItem, String > > mColumns = new ArrayList<>();
    private final ContextMenu mContextMenu =  new ContextMenu();
    private final MenuItem mMenuItemEdit = new MenuItem( "Edit ..." );
    private final MenuItem mMenuItemCopy = new MenuItem( "Copy" );
    private final MenuItem mMenuItemRemove = new MenuItem( "Remove ..." );
    private final TableContentController mController;
    private Cell mEditingCell;

    TableContentView( TableContentController inController ) {
        mController = inController;
        mView.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        mView.setRowFactory( new TrackViewRowFactory() );
        mView.setTableMenuButtonVisible( true );
        createColumns();
        initContextMenu();
    }

    void cancelEdit() {
        if ( mEditingCell != null ) {
            mEditingCell.cancelEdit();
        }
    }

    void clearSelection() {
        mView.getSelectionModel().clearSelection();
    }

    List< TableColumn< TableContentItem, String > > getColumns() {
        return mColumns;
    }

    ContextMenu getContextMenu() {
        return mContextMenu;
    }

    ObservableList< TableContentItem > getItems() { return mView.getItems(); }

    MenuItem getMenuItemCopy() {
        return mMenuItemCopy;
    }

    MenuItem getMenuItemEdit() {
        return mMenuItemEdit;
    }

    MenuItem getMenuItemRemove() {
        return mMenuItemRemove;
    }

    int getSelectedIndex() {
        return mView.getSelectionModel().getSelectedIndex();
    }

    ObservableList< TableContentItem > getSelectedItems() {
        return mView.getSelectionModel().getSelectedItems();
    }

    TableView< TableContentItem > getTableView() {
        return mView;
    }

    void selectItem( TableContentItem inItem ) {
        mView.getSelectionModel().select( inItem );
    }

    void setData( ObservableList< TableContentItem > inData ) {
        mView.setItems( inData );
    }

    void setEditable( boolean inEditable ) {
        mView.setEditable( inEditable );
    }

    private void startEdit( Cell inCell ) {
        mEditingCell = inCell;
    }

    private void createColumns() {
        ReadOnlyDoubleProperty theWidth = mView.widthProperty();
        mColumns.add(
                createColumn( theWidth.multiply( 0.1 ),
                        TableContentItem.sColumnHeaderNumber,
                        TableContentItem.sPropertyNameNumber ) );
        mColumns.add(
                createColumn( theWidth.multiply( 0.5 ),
                    TableContentItem.sColumnHeaderTitle,
                    TableContentItem.sPropertyNameTitle ) );
        mColumns.add(
            createColumn( theWidth.multiply( 0.25 ),
                    TableContentItem.sColumnHeaderAlbum,
                    TableContentItem.sPropertyNameAlbum ) );
        mColumns.add(
            createColumn( theWidth.multiply( 0.15 ),
                    TableContentItem.sColumnHeaderArtist,
                    TableContentItem.sPropertyNameArtist ) );
        for ( TableColumn< TableContentItem, String > theColumn : mColumns ) {
            mView.getColumns().add( theColumn );
        }
    }

    private TableColumn< TableContentItem, String > createColumn(
        DoubleBinding inWidth, String inColumnHeaderString, String inTrackPropertyName ) {
        TableColumn< TableContentItem, String > theColumn = new TableColumn<>( inColumnHeaderString );
        theColumn.prefWidthProperty().bind( inWidth );
        theColumn.setCellValueFactory( new PropertyValueFactory( inTrackPropertyName ) );
        theColumn.setCellFactory( CancellableTextFieldTableCell.< TableContentItem >forTableColumn( this ) );
        return theColumn;
    }

    private Clipboard getDragboard() {
        return mController.getDragboard();
    }

    private void initContextMenu() {
        mContextMenu.getItems().addAll( mMenuItemEdit, mMenuItemCopy, mMenuItemRemove );
        mView.setContextMenu( mContextMenu );
    }

    private class TrackViewRowFactory implements Callback< TableView< TableContentItem >, TableRow< TableContentItem > > {
        @Override
        public TableRow< TableContentItem > call( TableView< TableContentItem > inTableView ) {
            TableRow< TableContentItem > theTableRow = new TableRow< TableContentItem >() {
                protected void updateItem( TableContentItem inItem, boolean inEmpty ) {
                    super.updateItem( inItem, inEmpty );
                }
            };
            theTableRow.setOnDragDetected( inEvent -> {
                ClipboardContent theFakeContent = new ClipboardContent();
                theFakeContent.putString( "" );
                theTableRow.startDragAndDrop( TransferMode.ANY ).setContent( theFakeContent );
                getDragboard().setContent( getSelectedItems() );
                inEvent.consume();
            } );
            return theTableRow;
        }
    }

    static class CancellableTextFieldTableCell extends TextFieldTableCell< TableContentItem, String > {
        private final TableContentView mView;

        public CancellableTextFieldTableCell( TableContentView inView ) {
            super( new DefaultStringConverter() );
            mView = inView;
        }

        public void startEdit() {
            super.startEdit();
            mView.startEdit( this );
        }

        public static Callback< TableColumn< TableContentItem, String >, TableCell< TableContentItem, String > > forTableColumn( TableContentView inView ) {
            return inParam -> new CancellableTextFieldTableCell( inView );
        }
    }
}