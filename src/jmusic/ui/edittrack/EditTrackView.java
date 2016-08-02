package jmusic.ui.edittrack;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class EditTrackView {
    private final TableView< EditTrackItem > mView;

    EditTrackView( TableView< EditTrackItem > inView, boolean inIsRegularEditView ) {
        mView = inView;
        mView.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        if ( inIsRegularEditView ) {
            createColumns();
        } else {
            createBokenColumn();
        }
    }

    TableView< EditTrackItem > getView() {
        return mView;
    }

    void setData( ObservableList< EditTrackItem > inData ) {
        mView.setItems( inData );
    }

    private void createBokenColumn() {
        ReadOnlyDoubleProperty theWidth = mView.widthProperty();
        mView.getColumns().add(
            createColumn( theWidth.multiply( 1 ),
                EditTrackItem.sColumnHeaderUri,
                EditTrackItem.sPropertyNameUri ) );
    }

    private void createColumns() {
        ReadOnlyDoubleProperty theWidth = mView.widthProperty();
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.1 ),
                EditTrackItem.sColumnHeaderNumber,
                EditTrackItem.sPropertyNameNumber ) );
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.5 ),
                EditTrackItem.sColumnHeaderTitle,
                EditTrackItem.sPropertyNameTitle ) );
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.25 ),
                EditTrackItem.sColumnHeaderAlbum,
                EditTrackItem.sPropertyNameAlbum ) );
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.15 ),
                EditTrackItem.sColumnHeaderArtist,
                EditTrackItem.sPropertyNameArtist ) );
    }

    private TableColumn< EditTrackItem, String > createColumn(
        DoubleBinding inWidth, String inColumnHeaderString, String inTrackPropertyName ) {
        TableColumn< EditTrackItem, String > theColumn = new TableColumn<>( inColumnHeaderString );
        theColumn.prefWidthProperty().bind( inWidth );
        theColumn.setCellValueFactory(
                inParam -> {
                    EditTrackItem theItem = inParam.getValue();
                    ObservableValue< String > theValue = null;
                    if ( EditTrackItem.sPropertyNameAlbum.equals( inTrackPropertyName ) ) {
                        theValue = theItem.albumProperty();
                    } else if ( EditTrackItem.sPropertyNameArtist.equals( inTrackPropertyName ) ) {
                        theValue = theItem.artistProperty();
                    } else if ( EditTrackItem.sPropertyNameNumber.equals( inTrackPropertyName ) ) {
                        theValue = theItem.numberProperty();
                    } else if ( EditTrackItem.sPropertyNameTitle.equals( inTrackPropertyName ) ) {
                        theValue = theItem.titleProperty();
                    } else if ( EditTrackItem.sPropertyNameUri.equals( inTrackPropertyName ) ) {
                        theValue = theItem.uriProperty();
                    }
                    return theValue;
                } );
        return theColumn;
    }
}