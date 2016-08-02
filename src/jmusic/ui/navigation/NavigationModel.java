package jmusic.ui.navigation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import jmusic.library.LibraryItem;

import java.util.HashMap;
import java.util.stream.Collectors;

class NavigationModel {
    private final NavigationController mController;
    private NavigationItem mRoot;
    private boolean mDisplayAlbums;
    private final HashMap< Long, NavigationItem > mItems = new HashMap<>();

    NavigationModel( NavigationController inController ) {
        mController = inController;
    }

    void addItem( LibraryItem inItem ) {
        NavigationItem theParent = findParent( inItem );
        if ( theParent != null ) {
            theParent.addItem( createContainerViewItem( inItem ) );
        }
    }

    ObservableList< TreeItem< LibraryItem > > browse( Long inContainerId ) {
        ObservableList< TreeItem< LibraryItem > > theItems = FXCollections.observableArrayList();
        theItems.addAll( mController.browse( inContainerId ).stream().map( this::createContainerViewItem ).collect( Collectors.toList() ) );
        return theItems;
    }

    boolean containsItem( LibraryItem inItem ) {
        return mItems.containsKey( inItem.getId() );
    }

    NavigationItem getData() {
        return mRoot;
    }

    NavigationItem getItem( Long inId ) {
        return mItems.get( inId );
    }

    boolean isLeaf( LibraryItem inItem ) {
        return inItem.isTrack() || inItem.isAlbum() || inItem.isPlaylist() || ( ! mDisplayAlbums && inItem.isArtist() );
    }

    void removeItem( LibraryItem inItem ) {
        synchronized( mItems ) {
            Long theChildId = inItem.getId();
            NavigationItem theChild = mItems.get( theChildId );
            if ( theChild == null ) {
                return;
            }
            NavigationItem theParent = mItems.get( inItem.getParentId() );
            if ( theParent != null ) {
                theParent.removeItem( theChild );
            }
            mItems.remove( theChildId );
        }
    }

    void setDisplayAlbums( boolean inDisplayAlbums ) {
        mDisplayAlbums = inDisplayAlbums;
    }

    void setRootItem( LibraryItem inRootItem ) {
        mRoot = createContainerViewItem( inRootItem );
    }

    void updateItem( LibraryItem inItem ) {
        synchronized( mItems ) {
            NavigationItem theItem = mItems.get( inItem.getId() );
            if ( theItem == null ) {
                return;
            }
            theItem.setValue( inItem );
        }
    }

    private NavigationItem createContainerViewItem( LibraryItem inItem ) {
        NavigationItem theItem = new NavigationItem( this, inItem );
        synchronized( mItems ) {
            mItems.put( inItem.getId(), theItem );
        }
        return theItem;
    }

    private NavigationItem findParent( LibraryItem inChildItem ) {
        return mItems.get( inChildItem.getParentId() );
    }
}