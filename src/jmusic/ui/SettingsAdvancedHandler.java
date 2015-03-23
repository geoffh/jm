package jmusic.ui;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import jmusic.util.Config;
import jmusic.util.ConfigConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

class SettingsAdvancedHandler implements SettingsHandler {
    private static final String sClassesFile = "jmusic/resources/classes";
    private static final TreeItem< LoggingItem > sRootItem;
    private static final ObservableList< String > sLevels =
        FXCollections.observableArrayList(
            "Unset",
            Level.OFF.getName(),
            Level.SEVERE.getName(),
            Level.WARNING.getName(),
            Level.INFO.getName(),
            Level.CONFIG.getName(),
            Level.FINE.getName(),
            Level.FINER.getName(),
            Level.FINEST.getName(),
            Level.ALL.getName() );

    static {
        Level theRootLevel = null;
        String theRootSetting = Config.getInstance().getProperty( ConfigConstants.sPropNameLogLevel );
        if ( theRootSetting != null ) {
            theRootLevel = Level.parse( theRootSetting );
        }
        sRootItem = new TreeItem<>( new LoggingItem( null, null, theRootLevel ) );
        getNames();
    }
    private final SettingsController mController;

    SettingsAdvancedHandler( SettingsController inController ) {
        mController = inController;
        createTreeTable();
    }

    @Override
    public void getSettings( Map< String, String > inSettings, Set< String > inRemovals ) {
        traverseItems( sRootItem, inSettings, inRemovals );
    }

    private static TreeItem< LoggingItem > createItem( TreeItem< LoggingItem > inParent, String inName ) {
        for ( TreeItem< LoggingItem > theChild : inParent.getChildren() ) {
            if ( theChild.getValue().getName().equals( inName ) ) {
                return theChild;
            }
        }
        LoggingItem theParentItem = inParent.getValue();
        String theParentName = theParentItem.getFullName();
        String thePropName = theParentName != null ?
            ConfigConstants.getPropNameClassLoglevel( theParentName + "." + inName ) :
            ConfigConstants.getPropNameClassLoglevel( inName );
        String thePropSetting = Config.getInstance().getProperty( thePropName );
        if ( "".equals( thePropSetting ) ) {
            thePropSetting = null;
        }
        Level theLevel = thePropSetting != null ? Level.parse( thePropSetting ) : null;
        TreeItem< LoggingItem > theChild = new TreeItem<>( new LoggingItem( theParentItem, inName, theLevel ) );
        inParent.getChildren().add( theChild );
        return theChild;
    }

    private static void getNames() {

        try {
            BufferedReader theReader =
                new BufferedReader(
                    new InputStreamReader(
                        SettingsAdvancedHandler.class.getClassLoader().getResourceAsStream( sClassesFile ) ) );
            String theLine;
            while ( ( theLine = theReader.readLine() ) != null ) {
                TreeItem< LoggingItem > theParent = sRootItem;
                StringTokenizer theTokenizer = new StringTokenizer( theLine, "/" );
                while ( theTokenizer.hasMoreTokens() ) {
                    String theToken = theTokenizer.nextToken();
                    theParent = createItem( theParent, theToken );
                }
            }
        } catch( Exception theException ) {
            theException.printStackTrace();
        }
    }

    private void createTreeTable() {
        AnchorPane thePane = mController.getSettingsAdvancedTabAnchorPane();
        ReadOnlyDoubleProperty theWidth = thePane.widthProperty();

        TreeTableColumn< LoggingItem, String > theNameColumn = new TreeTableColumn<>( "Name" );
        theNameColumn.prefWidthProperty().bind( theWidth.multiply( 0.75 ) );
        theNameColumn.setCellValueFactory(
            new Callback< TreeTableColumn.CellDataFeatures< LoggingItem, String >, ObservableValue< String > >() {
            @Override
            public ObservableValue< String > call( TreeTableColumn.CellDataFeatures< LoggingItem, String > inParam ) {
                return new ReadOnlyStringWrapper( inParam.getValue().getValue().getName() );
            }
        } );
        TreeTableView< LoggingItem > theTreeTableView = new TreeTableView<>( sRootItem );

        TreeTableColumn< LoggingItem, ComboBox< String > > theLevelColumn = new TreeTableColumn<>( "Level" );
        theLevelColumn.prefWidthProperty().bind( theWidth.multiply( 0.25 ) );
        theLevelColumn.setCellValueFactory(
            new Callback< TreeTableColumn.CellDataFeatures< LoggingItem, ComboBox< String > >, ObservableValue< ComboBox< String > > >() {
            @Override
            public ObservableValue< ComboBox< String > > call( TreeTableColumn.CellDataFeatures< LoggingItem, ComboBox< String > > inParam ) {
                ComboBox< String > theComboBox = new ComboBox< String >( sLevels );
                LoggingItem theItem = inParam.getValue().getValue();
                theComboBox.setUserData( theItem );
                Level theLevel = theItem.getLevel();
                theComboBox.getSelectionModel().select(
                    theLevel != null ? theLevel.getLocalizedName() : "Unset" );
                theComboBox.getSelectionModel().selectedItemProperty().addListener( new ChangeListener< String >() {
                    @Override
                    public void changed( ObservableValue< ? extends String > inObservable, String inOldValue, String inNewValue ) {
                        Level theLevel = null;
                        if ( ! "Unset".equals( inNewValue ) ) {
                            theLevel = Level.parse( inNewValue );
                        }
                        ( ( LoggingItem )theComboBox.getUserData() ).setLevel( theLevel );
                    }
                } );
                return new ReadOnlyObjectWrapper< ComboBox< String > >( theComboBox );
            }
        } );
        theTreeTableView.getColumns().setAll( theNameColumn, theLevelColumn );

        thePane.setTopAnchor( theTreeTableView, 0.0 );
        thePane.setBottomAnchor( theTreeTableView, 0.0 );
        thePane.setLeftAnchor( theTreeTableView, 0.0 );
        thePane.setRightAnchor( theTreeTableView, 0.0 );
        thePane.getChildren().add( theTreeTableView );
    }

    private void traverseItems( TreeItem< LoggingItem > inItem, Map< String, String > inSettings, Set< String > inRemovals ) {
        LoggingItem theItem = inItem.getValue();
        String thePropName = ConfigConstants.getPropNameClassLoglevel( theItem.getFullName() );
        Level theLevel = theItem.getLevel();
        if ( theLevel != null   ) {
            inSettings.put( thePropName, theLevel.getName() );
        } else if ( theItem.hasLevelChanged() ) {
            inRemovals.add( thePropName );
        }
        for ( TreeItem< LoggingItem > theChild : inItem.getChildren() ) {
            traverseItems( theChild, inSettings, inRemovals );
        }
    }

    static class LoggingItem {
        private final LoggingItem mParent;
        private final SimpleStringProperty mName;
        private Level mOriginalLevel;
        private Level mLevel;

        LoggingItem( LoggingItem inParent, String inName, Level inLevel ) {
            mParent = inParent;
            mName = new SimpleStringProperty( this, "name" );
            mName.set( inName );
            mOriginalLevel = inLevel;
            mLevel = inLevel;
        }

        String getFullName() {
            String theParentName = mParent != null ? mParent.getFullName() : null;
            return theParentName != null ? theParentName + "." + getName() : getName();
        }

        String getName() {
            return mName.get();
        }

        Level getLevel() {
            return mLevel;
        }

        boolean hasLevelChanged() {
            return mOriginalLevel != mLevel;
        }

        void setLevel( Level inLevel ) {
            mLevel = inLevel;
        }
    }
}