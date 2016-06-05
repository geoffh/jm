package jmusic.ui;

import javafx.scene.Node;
import jmusic.library.LibraryItem;

import java.util.List;

public interface ContentController {
    void addContentsChangedListener( ContentsChangedListener inListener );

    void cancelEdit();

    void clearSelection();

    Node getView();

    List< LibraryItem > getItems();

    int getSelectedIndex();

    List< LibraryItem > getSelectedItems();

    void removeContentsChangedListener( ContentsChangedListener inListener );

    void selectItem( LibraryItem inItem );
}
