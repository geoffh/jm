package jmusic.ui;

import jmusic.ui.tablecontent.TableContentController;
import jmusic.ui.treenavigation.TreeNavigationController;

public class UIFactory {
    public static ContentController getContentController( JMusicController inMainController ) {
        return new TableContentController( inMainController );
    }

    public static NavigationController getNavigationController( JMusicController inMainController ) {
        return new TreeNavigationController( inMainController );
    }
}