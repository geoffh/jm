package jmusic.ui.navigation;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import jmusic.library.LibraryItem;

class NavigationItem extends TreeItem< LibraryItem >  {
    private final NavigationModel mModel;
    private final ObservableList< TreeItem< LibraryItem > > mChildren;
    private boolean mFirstLoad = true;

    NavigationItem( NavigationModel inModel, LibraryItem inContainer ) {
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

    @Override
    public boolean isLeaf() {
        return mModel.isLeaf( getValue() );
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    void addItem( NavigationItem inItem ) {
        synchronized( mChildren ) {
            mChildren.add( findInsertionIndex( inItem ), inItem );
        }
    }

    void removeItem( NavigationItem inItem ) {
        synchronized( mChildren ) {
            mChildren.remove( inItem );
        }
    }

    private int findInsertionIndex( NavigationItem inItem ) {
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