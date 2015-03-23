package jmusic.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import jmusic.library.LibraryItem;

class ContainerViewItem extends TreeItem< LibraryItem >  {
    private final ContainerViewModel mModel;
    private ObservableList< TreeItem< LibraryItem > > mChildren;
    private boolean mFirstLoad = true;

    ContainerViewItem( ContainerViewModel inModel, LibraryItem inContainer ) {
        mModel = inModel;
        setValue( inContainer );
        mChildren = super.getChildren();
    }

    @Override
    synchronized public ObservableList< TreeItem< LibraryItem > > getChildren() {
        if ( mFirstLoad ) {
            mFirstLoad = false;
            if ( ! getValue().isAlbum() ) {
                mChildren.setAll( mModel.browse( getValue().getId() ) );
            }
        }
        return mChildren;
    }

    @Override public boolean isLeaf() {
        LibraryItem theItem = getValue();
        return theItem.isAlbum() || theItem.isPlaylist();
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    void addItem( ContainerViewItem inItem ) {
        synchronized( mChildren ) {
            mChildren.add( findInsertionIndex( inItem ), inItem );
        }
    }

    void removeItem( ContainerViewItem inItem ) {
        synchronized( mChildren ) {
            mChildren.remove( inItem );
        }
    }

    private int findInsertionIndex( ContainerViewItem inItem ) {
        int theIndex = 0;
        synchronized( mChildren ) {
            for ( TreeItem< LibraryItem > theItem : mChildren ) {
                if ( theItem.getValue().compare( inItem.getValue() ) > 0 ) {
                    break;
                }
                ++ theIndex;
            }
        }
        return theIndex;
    }
}