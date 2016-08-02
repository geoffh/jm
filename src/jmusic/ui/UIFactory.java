package jmusic.ui;

import jmusic.ui.content.simpletablecontent.SimpleTableContentController;
import jmusic.ui.navigation.NavigationController;

public class UIFactory {
    public static ContentController getContentController( JMusicController inMainController ) {
        return new SimpleTableContentController( inMainController );
    }

    public static NavigationController getNavigationController( JMusicController inMainController ) {
        return new NavigationController( inMainController );
    }
}