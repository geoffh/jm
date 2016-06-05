package jmusic.ui;

import jmusic.library.LibraryItem;

public interface SelectedItemListener {
    void changed( LibraryItem inOldItem, LibraryItem inNewItem );
}