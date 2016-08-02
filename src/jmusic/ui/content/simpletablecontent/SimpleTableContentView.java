package jmusic.ui.content.simpletablecontent;

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

class SimpleTableContentView {
    static final int sColumnNumber = 0;
    static final int sColumnTitle  = 1;
    static final int sColumnAlbum  = 2;
    static final int sColumnArtist = 3;

    private final TableView< SimpleTableContentItem > mView = new TableView<>();
    private final List< TableColumn< SimpleTableContentItem, String > > mColumns = new ArrayList<>();
    private final ContextMenu mContextMenu =  new ContextMenu();
    private final MenuItem mMenuItemEdit = new MenuItem( "Edit ..." );
    private final MenuItem mMenuItemCopy = new MenuItem( "Copy" );
    private final MenuItem mMenuItemRemove = new MenuItem( "Remove ..." );
    private final SimpleTableContentController mController;
    private Cell mEditingCell;

    SimpleTableContentView( SimpleTableContentController inController ) {
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

    List< TableColumn< SimpleTableContentItem, String > > getColumns() {
        return mColumns;
    }

    ContextMenu getContextMenu() {
        return mContextMenu;
    }

    ObservableList< SimpleTableContentItem > getItems() { return mView.getItems(); }

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

    ObservableList< SimpleTableContentItem > getSelectedItems() {
        return mView.getSelectionModel().getSelectedItems();
    }

    TableView< SimpleTableContentItem > getTableView() {
        return mView;
    }

    void selectItem( SimpleTableContentItem inItem ) {
        mView.getSelectionModel().select( inItem );
    }

    void setData( ObservableList< SimpleTableContentItem > inData ) {
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
                        SimpleTableContentItem.sColumnHeaderNumber,
                        SimpleTableContentItem.sPropertyNameNumber ) );
        mColumns.add(
                createColumn( theWidth.multiply( 0.5 ),
                    SimpleTableContentItem.sColumnHeaderTitle,
                    SimpleTableContentItem.sPropertyNameTitle ) );
        mColumns.add(
            createColumn( theWidth.multiply( 0.25 ),
                    SimpleTableContentItem.sColumnHeaderAlbum,
                    SimpleTableContentItem.sPropertyNameAlbum ) );
        mColumns.add(
            createColumn( theWidth.multiply( 0.15 ),
                    SimpleTableContentItem.sColumnHeaderArtist,
                    SimpleTableContentItem.sPropertyNameArtist ) );
        for ( TableColumn< SimpleTableContentItem, String > theColumn : mColumns ) {
            mView.getColumns().add( theColumn );
        }
    }

    private TableColumn< SimpleTableContentItem, String > createColumn(
        DoubleBinding inWidth, String inColumnHeaderString, String inTrackPropertyName ) {
        TableColumn< SimpleTableContentItem, String > theColumn = new TableColumn<>( inColumnHeaderString );
        theColumn.prefWidthProperty().bind( inWidth );
        theColumn.setCellValueFactory( new PropertyValueFactory( inTrackPropertyName ) );
        theColumn.setCellFactory( CancellableTextFieldTableCell.< SimpleTableContentItem >forTableColumn( this ) );
        return theColumn;
    }

    private Clipboard getDragboard() {
        return mController.getDragboard();
    }

    private void initContextMenu() {
        mContextMenu.getItems().addAll( mMenuItemEdit, mMenuItemCopy, mMenuItemRemove );
        mView.setContextMenu( mContextMenu );
    }

    private class TrackViewRowFactory implements Callback< TableView< SimpleTableContentItem >, TableRow< SimpleTableContentItem > > {
        @Override
        public TableRow< SimpleTableContentItem > call( TableView< SimpleTableContentItem > inTableView ) {
            TableRow< SimpleTableContentItem > theTableRow = new TableRow< SimpleTableContentItem >() {
                protected void updateItem( SimpleTableContentItem inItem, boolean inEmpty ) {
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

    static class CancellableTextFieldTableCell extends TextFieldTableCell< SimpleTableContentItem, String > {
        private final SimpleTableContentView mView;

        public CancellableTextFieldTableCell( SimpleTableContentView inView ) {
            super( new DefaultStringConverter() );
            mView = inView;
        }

        public void startEdit() {
            super.startEdit();
            mView.startEdit( this );
        }

        public static Callback< TableColumn< SimpleTableContentItem, String >, TableCell< SimpleTableContentItem, String > > forTableColumn( SimpleTableContentView inView ) {
            return inParam -> new CancellableTextFieldTableCell( inView );
        }
    }
}