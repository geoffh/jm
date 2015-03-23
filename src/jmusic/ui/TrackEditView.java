package jmusic.ui;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class TrackEditView {
    private final TableView< TrackEditItem > mView;

    TrackEditView( TableView< TrackEditItem > inView, boolean inIsRegularEditView ) {
        mView = inView;
        mView.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        if ( inIsRegularEditView ) {
            createColumns();
        } else {
            createBokenColumn();
        }
    }

    TableView< TrackEditItem > getView() {
        return mView;
    }

    void setData( ObservableList< TrackEditItem > inData ) {
        mView.setItems( inData );
    }

    private void createBokenColumn() {
        ReadOnlyDoubleProperty theWidth = mView.widthProperty();
        mView.getColumns().add(
            createColumn( theWidth.multiply( 1 ),
                TrackEditItem.sColumnHeaderUri,
                TrackEditItem.sPropertyNameUri ) );
    }

    private void createColumns() {
        ReadOnlyDoubleProperty theWidth = mView.widthProperty();
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.1 ),
                TrackEditItem.sColumnHeaderNumber,
                TrackEditItem.sPropertyNameNumber ) );
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.5 ),
                TrackEditItem.sColumnHeaderTitle,
                TrackEditItem.sPropertyNameTitle ) );
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.25 ),
                TrackEditItem.sColumnHeaderAlbum,
                TrackEditItem.sPropertyNameAlbum ) );
        mView.getColumns().add(
            createColumn( theWidth.multiply( 0.15 ),
                TrackEditItem.sColumnHeaderArtist,
                TrackEditItem.sPropertyNameArtist ) );
    }

    private TableColumn< TrackEditItem, String > createColumn(
        DoubleBinding inWidth, String inColumnHeaderString, String inTrackPropertyName ) {
        TableColumn< TrackEditItem, String > theColumn = new TableColumn<>( inColumnHeaderString );
        theColumn.prefWidthProperty().bind( inWidth );
        theColumn.setCellValueFactory(
            new Callback< TableColumn.CellDataFeatures< TrackEditItem, String >, ObservableValue< String > >() {
                @Override
                public ObservableValue< String > call( TableColumn.CellDataFeatures< TrackEditItem, String > inParam ) {
                    TrackEditItem theItem = inParam.getValue();
                    ObservableValue< String > theValue = null;
                    if ( TrackEditItem.sPropertyNameAlbum.equals( inTrackPropertyName ) ) {
                        theValue = theItem.albumProperty();
                    } else if ( TrackEditItem.sPropertyNameArtist.equals( inTrackPropertyName ) ) {
                        theValue = theItem.artistProperty();
                    } else if ( TrackEditItem.sPropertyNameNumber.equals( inTrackPropertyName ) ) {
                        theValue = theItem.numberProperty();
                    } else if ( TrackEditItem.sPropertyNameTitle.equals( inTrackPropertyName ) ) {
                        theValue = theItem.titleProperty();
                    } else if ( TrackEditItem.sPropertyNameUri.equals( inTrackPropertyName ) ) {
                        theValue = theItem.uriProperty();
                    }
                    return theValue;
                }
            } );
        return theColumn;
    }
}