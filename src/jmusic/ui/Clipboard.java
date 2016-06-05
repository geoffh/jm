package jmusic.ui;

import jmusic.library.Library;
import jmusic.library.LibraryItem;
import jmusic.library.LibraryListener;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Clipboard implements LibraryListener {
    private final LinkedList< LibraryItem > mItems = new LinkedList<>();

    Clipboard( Library inLibrary ) {
        inLibrary.addListener( this );
    }

    public List< LibraryItem > getContent() { return new LinkedList<>( mItems ); }

    LibraryItem getContentItem() { return ! mItems.isEmpty() ? mItems.get( 0 ) : null; }

    @Override
    public void onObjectCreate( LibraryItem inObject ) {}

    @Override
    public void onObjectDestroy( LibraryItem inObject ) {
        Long theId = inObject.getId();
        synchronized( mItems ) {
            for ( LibraryItem theItem : getContent() ) {
                if ( theItem.getId().equals( theId ) ) {
                    mItems.remove( theItem );
                    break;
                }
            }
        }
    }

    @Override
    public void onObjectUpdate( LibraryItem inObject ) {}

    public void setContent( LibraryItem inItem ) {
        synchronized( mItems ) {
            mItems.clear();
            if ( inItem != null ) {
                mItems.add( inItem );
            }
        }
    }

    public void setContent( Collection< ? extends LibraryItem > inItems ) {
        synchronized( mItems ) {
            mItems.clear();
            mItems.addAll( inItems );
        }
    }
}