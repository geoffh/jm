package jmusic.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import jmusic.library.LibraryItem;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class TrackEditItem extends LibraryItem {
    public static final String sPropertyNameNumber = "number";
    public static final String sPropertyNameAlbum  = "album";
    public static final String sPropertyNameArtist = "artist";
    public static final String sPropertyNameTitle  = "title";
    public static final String sColumnHeaderNumber = "Number";
    public static final String sColumnHeaderAlbum  = "Album";
    public static final String sColumnHeaderArtist = "Artist";
    public static final String sColumnHeaderTitle  = "Title";
    public static final String sPropertyNameUri    = "uri";
    public static final String sColumnHeaderUri    = "Uri";

    private final ViewItemStringProperty mNumber;
    private final ViewItemStringProperty mAlbum;
    private final ViewItemStringProperty mArtist;
    private final ViewItemStringProperty mTitle;
    private final ViewItemStringProperty mUri;

    TrackEditItem( LibraryItem inItem ) {
        super( inItem );
        int theTrackNumber = getTrackNumber();
        mNumber = new ViewItemStringProperty(
            theTrackNumber != LibraryItem.sTrackNumberUnknown ?
            String.valueOf( theTrackNumber ) : "" );
        mAlbum = new ViewItemStringProperty( getAlbumName() );
        mArtist = new ViewItemStringProperty( getArtistName() );
        mTitle = new ViewItemStringProperty( getTitle() );
        String theUri;
        try {
            theUri = URLDecoder.decode( getUri(), "UTF-8" );
        } catch ( UnsupportedEncodingException theException ) {
            theUri = "";
            theException.printStackTrace();
        }
        mUri = new ViewItemStringProperty( theUri );
    }

    public ViewItemStringProperty albumProperty() { return mAlbum; }
    public ViewItemStringProperty artistProperty() { return mArtist; }
    public ViewItemStringProperty numberProperty() { return  mNumber; }
    public ViewItemStringProperty titleProperty() { return mTitle; }
    public ViewItemStringProperty uriProperty() { return mUri; }

    public void setTrackNumber( Integer inValue ) {
        super.setTrackNumber( inValue );
        mNumber.setValue( String.valueOf( inValue) );
    }

    public void setAlbumName( String inValue ) {
        super.setAlbumName( inValue );
        mAlbum.setValue( inValue );
    }

    public void setArtistName( String inValue ) {
        super.setArtistName( inValue );
        mArtist.setValue( inValue );
    }

    public void setTitle( String inValue ) {
        super.setTitle( inValue );
        mTitle.setValue( inValue );
    }

    public void setUri( String inValue ) {
        super.setUri( inValue );
        mUri.setValue( inValue );
    }

    class ViewItemStringProperty implements ObservableValue< String > {
        private String mValue;
        private ArrayList< ChangeListener< ? super String > > mCListeners = new ArrayList<>();
        private ArrayList< InvalidationListener > mIListeners = new ArrayList<>();

        ViewItemStringProperty( String inValue ) {
            mValue = inValue;
        }

        @Override
        public void addListener( ChangeListener< ? super String > inListener ) {
            synchronized( mCListeners ) {
                mCListeners.add( inListener );
            }
        }

        @Override
        public void addListener( InvalidationListener inListener ) {
            synchronized( mIListeners ) {
                mIListeners.add( inListener );
            }
        }

        @Override
        public void removeListener( ChangeListener< ? super String > inListener ) {
            synchronized( mCListeners ) {
                mCListeners.remove( inListener );
            }
        }

        @Override
        public void removeListener( InvalidationListener inListener ) {
            synchronized( mIListeners ) {
                mIListeners.add( inListener );
            }
        }

        @Override
        public String getValue() {
            return mValue;
        }

        public void setValue( String inValue ) {
            String theOldValue = mValue;
            mValue = inValue;
            synchronized( mIListeners ) {
                for ( InvalidationListener theListener : mIListeners ) {
                    theListener.invalidated( this );
                }
                mValue = inValue;
            }
            synchronized( mCListeners ) {
                for ( ChangeListener< ? super String > theListener : mCListeners ) {
                    theListener.changed( this, theOldValue, mValue );
                }
            }
        }
    }
}