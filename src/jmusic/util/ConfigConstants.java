package jmusic.util;

public class ConfigConstants {
    public static final String sJMUsic = "jmusic";

    public static final String sCategoryMusicSource = sJMUsic + ".musicsources.";

    public static final String sPropNameRefreshInterval = sCategoryMusicSource + "refreshInterval";
    public static final String sPropNameRefreshType = sCategoryMusicSource + "refreshType";
    public static final String sPropRefreshIntervalDefault = "20";
    public static final RefreshChoice sPropRefreshTypeDefault = RefreshChoice.configureAllEnabled;

    public static final String sPropNameMusicSourceRefreshEnabled = sCategoryMusicSource + "%s.refreshEnabled";
    public static final String sPropNameMusicSourceRefreshInterval = sCategoryMusicSource + "%s.refreshInterval";
    public static final String sPropMusicSourceRefreshEnabledDefault = "true";

    public static final String sCategoryAppearance = sJMUsic + ".appearance";
    public static final String sCategoryAppearanceNavigation = sCategoryAppearance + ".navigation.";
    public static final String sPropNameNavigationDisplayAlbums = sCategoryAppearanceNavigation + "displayAlbums";
    public static final String sPropNavigationDisplayAlbumsDefault = "true";

    public static final String sPropNameLogLevel = ".level";

    public static final String sPropNameVolumePercent = sJMUsic + ".volumePercent";

    public static boolean isCategoryAppearance( String inPropName ) {
        return inPropName.startsWith( sCategoryAppearance );
    }

    public static boolean isCategoryAppearanceNavigation( String inPropName ) {
        return inPropName.startsWith( sCategoryAppearanceNavigation );
    }

    public static boolean isCategoryLogLevel( String inPropName ) {
        return inPropName.endsWith( sPropNameLogLevel );
    }

    public static boolean isCategoryMusicSource( String inPropName ) {
        return inPropName.startsWith( sCategoryMusicSource );
    }

    public static String getPropNameMusicSourceRefreshEnabled( Long inId ) {
        return String.format( sPropNameMusicSourceRefreshEnabled, String.valueOf( inId ) );
    }

    public static String getPropNameMusicSourceRefreshInterval( Long inId ) {
        return String.format( sPropNameMusicSourceRefreshInterval, String.valueOf( inId ) );
    }

    public static String getPropNameClassLoglevel( String inClassName ) {
        return inClassName != null ? inClassName + sPropNameLogLevel : sPropNameLogLevel;
    }

    public enum RefreshChoice {
        configureAllEnabled( "Use the same refresh interval for all sources" ),
        configureAllDisabled( "Disable refresh" ),
        configureIndividually( "Use an individual interval for each source" );
        private final String mValue;
        RefreshChoice( String inValue ) { mValue = inValue; }
        public String toString() { return mValue; }
    }
}
