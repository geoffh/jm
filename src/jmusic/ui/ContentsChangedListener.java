package jmusic.ui;

import jmusic.library.LibraryItem;

import java.util.List;

public interface ContentsChangedListener {
    void changed( List< LibraryItem > inContentsAdded, List< LibraryItem > inContentsRemoved );
}