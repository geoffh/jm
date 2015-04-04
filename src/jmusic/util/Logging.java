package jmusic.util;

import java.util.logging.LogManager;

public class Logging implements ConfigListener {
    private static final Logging sInstance = new Logging();

    @Override
    public void onConfigChange( String inKey, String inOldValue, String inNewValue ) {
        if ( ConfigConstants.isCategoryLogLevel( inKey ) ) {
            reloadLogManager();
        }
    }

    public static void init() {
        Config.getInstance().addListener( sInstance );
        sInstance.reloadLogManager();
    }

    private void reloadLogManager() {
        System.out.println( "Reloading Log Manager" );
        try {
            LogManager theLogManager = LogManager.getLogManager();
            theLogManager.readConfiguration( Config.getInstance().getInputStream() );
        } catch( Exception theException ) {
            theException.printStackTrace();
        }
    }
}