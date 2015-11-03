package jmusic.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

public class Config extends Properties {
    private static final Config sInstance = new Config();

    private final String sResourceBundleBaseName = "jmusic.resources.jmusic";
    private final File sConfigDir = new File( System.getProperty( "user.home" ) + File.separator + ".jmusic" );
    private final File sConfigFile = new File( sConfigDir, "jmusic.properties" );
    private final ArrayList< ConfigListener > mListeners = new ArrayList<>();

    public void addListener( ConfigListener inListener ) {
        synchronized( mListeners ) {
            mListeners.add( inListener );
        }
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream( getPropertiesAsString().getBytes() );
    }

    public synchronized static Config getInstance() { return sInstance; }

    public String getDefaultProperty( String inKey ) {
        return defaults.getProperty( inKey );
    }

    public void removeListener( ConfigListener inListener ) {
        synchronized( mListeners ) {
            mListeners.remove( inListener );
        }
    }

    public void removeProperty( String inKey ) {
        removeProperty( inKey, true );
    }

    public void removeProperties( Set< String > inKeys ) {
        for ( String theKey : inKeys ) {
            removeProperty( theKey, false );
        }
        store();
    }

    @Override
    public Object setProperty( String inKey, String inValue ) {
        return setProperty( inKey, inValue, true );
    }

    public void setProperties( Map< String, String > inProperties ) {
        for ( String theKey : inProperties.keySet() ) {
            setProperty( theKey, inProperties.get( theKey ), false );
        }
        store();
    }

    private Config() {
        super();
        defaults = loadDefaults();
        load();
    }

    private String getPropertiesAsString() {
        StringBuilder theProperties = new StringBuilder();
        for ( String theKey : stringPropertyNames() ) {
            theProperties.append( theKey )
                         .append( "=" )
                         .append( getProperty( theKey ) )
                         .append( "\r\n" );
        }
        return theProperties.toString();
    }

    private void load() {
        if ( sConfigDir.isFile() ) {
            System.err.println( "Warning: Configuration directory '" +
                                sConfigDir.getAbsolutePath() +
                                "' exists but is a file." );
            return;
        }
        if ( ! sConfigDir.exists() ) {
            sConfigDir.mkdirs();
        }
        if ( sConfigFile.exists() ) {
            try {
                load( new FileInputStream( sConfigFile.getAbsolutePath() ) );
            } catch( IOException theException ) {
               theException.printStackTrace( System.err );
            }
        }
    }

    private Properties loadDefaults() {
        Properties theDefaults = new Properties();
        try {
            ResourceBundle theBundle =
                ResourceBundle.getBundle( sResourceBundleBaseName );
            for ( String theKey : theBundle.keySet() ) {
                theDefaults.setProperty( theKey, theBundle.getString( theKey ) );
            }
        } catch( Exception theException ) {
            theException.printStackTrace( System.err );
        }
        return theDefaults;
    }

    private void notifyChange( String inKey, String inOldValue, String inNewValue ) {
        if ( mListeners.isEmpty() ||
            ( inOldValue == null && inNewValue == null ) ||
            ( inOldValue != null && inNewValue != null && inOldValue.equals( inNewValue ) ) ) {
            return;
        }
        synchronized( mListeners ) {
            for ( ConfigListener theListener : mListeners ) {
                theListener.onConfigChange( inKey, inOldValue, inNewValue );
            }
        }

    }

    public void removeProperty( String inKey, boolean inStore ) {
        String theOldValue = ( String )remove( inKey );
        if ( inStore ) {
            store();
        }
        notifyChange( inKey, theOldValue, null );
    }

    private Object setProperty( String inKey, String inValue, boolean inStore ) {
        String theOldValue = getProperty( inKey );
        Object theObject = super.setProperty( inKey, inValue );
        if ( inStore ) {
            store();
        }
        notifyChange( inKey, theOldValue, inValue );
        return theObject;
    }

    private void store() {
        try {
            store( new FileOutputStream( sConfigFile.getAbsolutePath() ), "" );
        } catch( Exception theException ) {
            theException.printStackTrace( System.err );
        }
    }
}