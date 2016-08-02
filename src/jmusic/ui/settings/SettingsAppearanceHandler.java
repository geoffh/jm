package jmusic.ui.settings;

import jmusic.util.Config;
import jmusic.util.ConfigConstants;

import java.util.Map;
import java.util.Set;

public class SettingsAppearanceHandler implements SettingsHandler {
    private final SettingsController mController;

    SettingsAppearanceHandler( SettingsController inController ) {
        mController = inController;
        initNavigationSettings();
        initContentSettings();
    }

    @Override
    public void getSettings( Map< String, String > inSettings, Set< String > inRemovals ) {
        inSettings.put( ConfigConstants.sPropNameNavigationDisplayAlbums, String.valueOf( mController.getAppearanceDisplayAlbumsCheckBox().isSelected() ) );
    }

    private boolean getConfiguredDisplayAlbums() {
        return Boolean.valueOf(
            Config.getInstance().getProperty(
                ConfigConstants.sPropNameNavigationDisplayAlbums,
                ConfigConstants.sPropNavigationDisplayAlbumsDefault ) );
    }

    private void initContentSettings() {
    }

    private void initNavigationSettings() {
        mController.getAppearanceDisplayAlbumsCheckBox().setSelected( getConfiguredDisplayAlbums() );
    }
}
