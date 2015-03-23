package jmusic.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import jmusic.library.LibraryItem;

import java.util.HashMap;
import java.util.logging.Logger;

class ContainerViewModel {
    private ContainerViewController mController;
    private ContainerViewItem mRoot;
    private final HashMap< Long, ContainerViewItem > mItems = new HashMap<>();
    private final Logger mLogger = Logger.getLogger( ContainerViewModel.class.getName() );

    void addItem( LibraryItem inItem ) {
        ContainerViewItem theParent = findParent( inItem );
        if ( theParent != null ) {
            theParent.addItem( createContainerViewItem( inItem ) );
        }
    }

    ObservableList< TreeItem< LibraryItem > > browse( Long inContainerId ) {
        ObservableList< TreeItem< LibraryItem > > theItems = FXCollections.observableArrayList();
        for ( LibraryItem theItem : mController.browse( inContainerId ) ) {
            theItems.add( createContainerViewItem( theItem ) );
        }
        return theItems;
    }

    boolean containsItem( LibraryItem inItem ) {
        return mItems.containsKey( inItem.getId() );
    }

    ContainerViewItem getData() {
        return mRoot;
    }

    ContainerViewItem getItem( Long inId ) {
        return mItems.get( inId );
    }

    void removeItem( LibraryItem inItem ) {
        synchronized( mItems ) {
            Long theChildId = inItem.getId();
            ContainerViewItem theChild = mItems.get( theChildId );
            if ( theChild == null ) {
                return;
            }
            ContainerViewItem theParent = mItems.get( inItem.getParentId() );
            if ( theParent != null ) {
                theParent.removeItem( theChild );
            }
            mItems.remove( theChildId );
        }
    }

    void setController( ContainerViewController inController ) {
        mController = inController;
    }

    void setRootItem( LibraryItem inRootItem ) {
        mRoot = createContainerViewItem( inRootItem );
    }

    void updateItem( LibraryItem inItem ) {
        synchronized( mItems ) {
            ContainerViewItem theItem = mItems.get( inItem.getId() );
            if ( theItem == null ) {
                return;
            }
            theItem.setValue( inItem );
        }
    }

    private ContainerViewItem createContainerViewItem( LibraryItem inItem ) {
        ContainerViewItem theItem = new ContainerViewItem( this, inItem );
        synchronized( mItems ) {
            mItems.put( inItem.getId(), theItem );
        }
        return theItem;
    }

    private ContainerViewItem findParent( LibraryItem inChildItem ) {
        return mItems.get( inChildItem.getParentId() );
    }
}