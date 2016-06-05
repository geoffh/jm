package jmusic.ui.settings;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import jmusic.library.Library;
import jmusic.library.LibraryItem;
import jmusic.util.Config;
import jmusic.util.ConfigConstants;
import jmusic.util.ConfigConstants.RefreshChoice;

import java.util.Map;
import java.util.Set;

class SettingsMusicSourcesHandler implements SettingsHandler {
    private final SettingsController mController;
    private final ObservableList< MusicSourceItem > mItems = FXCollections.observableArrayList();

    SettingsMusicSourcesHandler( SettingsController inController ) {
        mController = inController;
        initRefreshChoiceAndInterval();
        initMusicSources();
    }

    @Override
    public void getSettings( Map< String, String > inSettings, Set< String > inRemovals ) {
        inSettings.put(
            ConfigConstants.sPropNameRefreshType,
            mController.getMusicSourcesRefreshChoice().getValue().name() );
        inSettings.put(
            ConfigConstants.sPropNameRefreshInterval,
            mController.getMusicSourcesRefreshInterval().getText() );
        for ( MusicSourceItem theItem : mController.getMusicSourcesTableView().getItems() ) {
            Long theId = theItem.getId();
            inSettings.put(
                ConfigConstants.getPropNameMusicSourceRefreshEnabled( theId ),
                theItem.enabledProperty().getValue().toString() );
            inSettings.put(
                ConfigConstants.getPropNameMusicSourceRefreshInterval( theId ),
                String.valueOf( theItem.intervalProperty().getValue() ) );
        }
    }

    private void addTableData() {
        mController.getMusicSourcesTableView().setItems( mItems );
        Library theLibrary = mController.getLibrary();
        LibraryItem theRoot = theLibrary.getRootOfRoots();
        for ( LibraryItem theItem : theLibrary.browse( theRoot.getId() ).mResults ) {
            if ( theItem.isPlaylistsRoot() ) {
                continue;
            }
            Long theId = theItem.getId();
            mItems.add(
                new MusicSourceItem( theId,
                                     theItem.getTitle(),
                                     isMusicSourceRefreshEnabled( theId ),
                                     getMusicSourceRefreshInterval( theId ) ) );
        }
    }

    private void createTableColumns() {
        TableView< MusicSourceItem > theView = mController.getMusicSourcesTableView();
        DoubleBinding theWidth = theView.widthProperty().multiply( 0.33 );

        TableColumn< MusicSourceItem, String > theNameColumn = new TableColumn<>( "Name" );
        theNameColumn.setCellValueFactory( new PropertyValueFactory( "name" ) );
        theNameColumn.prefWidthProperty().bind( theWidth );

        TableColumn< MusicSourceItem, Boolean > theEnabledColumn = new TableColumn<>( "Enable Refresh" );
        theEnabledColumn.setCellValueFactory( new Callback< TableColumn.CellDataFeatures< MusicSourceItem, Boolean >, ObservableValue< Boolean > >() {
            @Override
            public ObservableValue< Boolean > call( TableColumn.CellDataFeatures< MusicSourceItem, Boolean > inParam ) {
                return inParam.getValue().enabledProperty();
            }
        } );
        theEnabledColumn.setCellFactory( new Callback< TableColumn< MusicSourceItem, Boolean >, TableCell< MusicSourceItem, Boolean > >() {
            @Override
            public TableCell< MusicSourceItem, Boolean > call( TableColumn< MusicSourceItem, Boolean > inParam ) {
                return new CheckBoxTableCell< MusicSourceItem, Boolean >();
            }
        } );
        theEnabledColumn.setEditable( true );
        theEnabledColumn.prefWidthProperty().bind( theWidth );

        TableColumn< MusicSourceItem, Number > theIntervalColumn = new TableColumn( "Interval" );
        theIntervalColumn.setCellValueFactory( new Callback< TableColumn.CellDataFeatures< MusicSourceItem, Number >, ObservableValue< Number > >() {
            @Override
            public ObservableValue< Number > call( TableColumn.CellDataFeatures< MusicSourceItem, Number > inParam ) {
                return inParam.getValue().intervalProperty();
            }
        } );
        theIntervalColumn.setCellFactory( new Callback< TableColumn< MusicSourceItem, Number >, TableCell< MusicSourceItem, Number > >() {
            @Override
            public TableCell< MusicSourceItem, Number > call( TableColumn< MusicSourceItem, Number > param ) {
                return new TextFieldTableCell< MusicSourceItem, Number >( new StringConverter< Number >() {
                    @Override
                    public String toString( Number inObject ) {
                        return inObject.toString();
                    }
                    @Override
                    public Number fromString( String inString ) {
                        return Integer.valueOf( inString );
                    }
                } );
            }
        } );
        theIntervalColumn.setEditable( true );
        theIntervalColumn.prefWidthProperty().bind( theWidth );

        theView.getColumns().addAll( theNameColumn, theEnabledColumn, theIntervalColumn );
    }

    private RefreshChoice getConfiguredRefreshChoice() {
        return RefreshChoice.valueOf(
            Config.getInstance().getProperty(
                ConfigConstants.sPropNameRefreshType, ConfigConstants.sPropRefreshTypeDefault.name() ) );
    }

    private String getConfiguredRefreshInterval() {
        return Config.getInstance().getProperty(
            ConfigConstants.sPropNameRefreshInterval, ConfigConstants.sPropRefreshIntervalDefault );
    }

    private int getMusicSourceRefreshInterval( Long inId ) {
        return Integer.valueOf(
            Config.getInstance().getProperty(
                ConfigConstants.getPropNameMusicSourceRefreshInterval( inId ),
                ConfigConstants.sPropRefreshIntervalDefault ) );
    }

    private void initMusicSources() {
        mController.getMusicSourcesTableView().setEditable( true );
        createTableColumns();
        addTableData();
    }

    private void initRefreshChoiceAndInterval() {
        ChoiceBox< RefreshChoice > theChoiceBox = mController.getMusicSourcesRefreshChoice();
        TextField theTextField = mController.getMusicSourcesRefreshInterval();
        theChoiceBox.getSelectionModel().selectedItemProperty().addListener( new ChangeListener< RefreshChoice >() {
            @Override
            public void changed( ObservableValue< ? extends RefreshChoice > inObservable, RefreshChoice inOldValue, RefreshChoice inNewValue ) {
                theTextField.setDisable( !RefreshChoice.configureAllEnabled.equals( inNewValue ) );
                mController.getMusicSourcesTableView().setDisable(
                    !RefreshChoice.configureIndividually.equals( inNewValue ) );
            }
        } );
        theChoiceBox.getItems().setAll(
            RefreshChoice.configureAllEnabled,
            RefreshChoice.configureAllDisabled,
            RefreshChoice.configureIndividually );
        theChoiceBox.setValue( getConfiguredRefreshChoice() );
        theTextField.setText( getConfiguredRefreshInterval() );
    }

    private boolean isMusicSourceRefreshEnabled( Long inId ) {
        return Boolean.valueOf(
            Config.getInstance().getProperty(
                ConfigConstants.getPropNameMusicSourceRefreshEnabled( inId ),
                ConfigConstants.sPropMusicSourceRefreshEnabledDefault ) );
    }

    public class MusicSourceItem {
        private final static String sPropertyNameName     = "name";
        private final static String sPropertyNameEnable   = "enabled";
        private final static String sPropertyNameInterval = "interval";

        private final Long mId;
        private final SimpleStringProperty mName = new SimpleStringProperty( this, sPropertyNameName );
        private final SimpleBooleanProperty mEnabled = new SimpleBooleanProperty( this, sPropertyNameEnable );
        private final SimpleIntegerProperty mInterval = new SimpleIntegerProperty( this, sPropertyNameInterval );

        MusicSourceItem( Long inId, String inName, boolean inIsEnabled, int inInterval ) {
            mId = inId;
            mName.setValue( inName );
            mEnabled.setValue( inIsEnabled );
            mInterval.setValue( inInterval );
        }

        public Long getId() { return mId; }
        public SimpleStringProperty nameProperty() { return mName; }
        public SimpleBooleanProperty enabledProperty() { return mEnabled; }
        public SimpleIntegerProperty intervalProperty() { return mInterval; }
    }
}