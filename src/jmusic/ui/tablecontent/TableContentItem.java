package jmusic.ui.tablecontent;

import javafx.beans.property.SimpleStringProperty;
import jmusic.library.LibraryItem;

public class TableContentItem extends LibraryItem {
    public static final String sPropertyNameNumber = "number";
    public static final String sPropertyNameAlbum  = "album";
    public static final String sPropertyNameArtist = "artist";
    public static final String sPropertyNameTitle  = "title";
    public static final String sColumnHeaderNumber = "Number";
    public static final String sColumnHeaderAlbum  = "Album";
    public static final String sColumnHeaderArtist = "Artist";
    public static final String sColumnHeaderTitle  = "Title";

    private final SimpleStringProperty mNumber;
    private final SimpleStringProperty mAlbum;
    private final SimpleStringProperty mArtist;
    private final SimpleStringProperty mTitle;

    TableContentItem( LibraryItem inTrack ) {
        putAll( inTrack );
        mNumber = new SimpleStringProperty( this, sPropertyNameNumber );
        int theTrackNumber = getTrackNumber();
        mNumber.setValue(
            theTrackNumber != LibraryItem.sTrackNumberUnknown ?
            String.valueOf( theTrackNumber ) : "" );
        mAlbum = new SimpleStringProperty( this, sPropertyNameAlbum );
        mAlbum.setValue( getAlbumName() );
        mArtist = new SimpleStringProperty( this, sPropertyNameArtist );
        mArtist.setValue( getArtistName() );
        mTitle = new SimpleStringProperty( this, sPropertyNameTitle );
        mTitle.setValue( getTitle() );
    }

    public SimpleStringProperty albumProperty() { return mAlbum; }
    public SimpleStringProperty artistProperty() { return mArtist; }
    public SimpleStringProperty numberProperty() { return mNumber; }
    public SimpleStringProperty titleProperty() { return mTitle; }
}