package jmusic.ui;

import javafx.scene.Node;
import jmusic.library.LibraryItem;

public interface NavigationController {
    void cancelEdit();

    LibraryItem getSelectedItem();

    Node getView();

    void addSelectedItemListener( SelectedItemListener inListener );

    void removeSelectedItemListener( SelectedItemListener inListener );
}