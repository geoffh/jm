package jmusic.ui.treenavigation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import jmusic.library.LibraryItem;

import java.util.HashMap;
import java.util.stream.Collectors;

class TreeNavigationModel {
    private final TreeNavigationController mController;
    private TreeNavigationItem mRoot;
    private final HashMap< Long, TreeNavigationItem > mItems = new HashMap<>();

    TreeNavigationModel( TreeNavigationController inController ) {
        mController = inController;
    }

    void addItem( LibraryItem inItem ) {
        TreeNavigationItem theParent = findParent( inItem );
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

    TreeNavigationItem getData() {
        return mRoot;
    }

    TreeNavigationItem getItem( Long inId ) {
        return mItems.get( inId );
    }

    void removeItem( LibraryItem inItem ) {
        synchronized( mItems ) {
            Long theChildId = inItem.getId();
            TreeNavigationItem theChild = mItems.get( theChildId );
            if ( theChild == null ) {
                return;
            }
            TreeNavigationItem theParent = mItems.get( inItem.getParentId() );
            if ( theParent != null ) {
                theParent.removeItem( theChild );
            }
            mItems.remove( theChildId );
        }
    }

    void setRootItem( LibraryItem inRootItem ) {
        mRoot = createContainerViewItem( inRootItem );
    }

    void updateItem( LibraryItem inItem ) {
        synchronized( mItems ) {
            TreeNavigationItem theItem = mItems.get( inItem.getId() );
            if ( theItem == null ) {
                return;
            }
            theItem.setValue( inItem );
        }
    }

    private TreeNavigationItem createContainerViewItem( LibraryItem inItem ) {
        TreeNavigationItem theItem = new TreeNavigationItem( this, inItem );
        synchronized( mItems ) {
            mItems.put( inItem.getId(), theItem );
        }
        return theItem;
    }

    private TreeNavigationItem findParent( LibraryItem inChildItem ) {
        return mItems.get( inChildItem.getParentId() );
    }
}