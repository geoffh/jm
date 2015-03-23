package jmusic.ui;

import java.util.Map;
import java.util.Set;

interface SettingsHandler {
    void getSettings( Map< String, String > inSettings, Set< String > inRemovals );
}