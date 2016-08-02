package jmusic.ui.settings;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jmusic.library.Library;
import jmusic.ui.JMusicController;
import jmusic.util.Config;
import jmusic.util.ConfigConstants;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class SettingsController implements Initializable, ChangeListener< Tab > {
    private static final String sFXMLDocument = "settings/Settings.fxml";

    private Stage mStage;
    private JMusicController mController;
    private SettingsHandler mMusicSourcesController;
    private SettingsHandler mAppearanceController;
    private SettingsHandler mAdvancedController;
    @FXML private TabPane settingsTabPane;
    @FXML private Tab settingsMusicSourcesTab;
    @FXML private Tab settingsAppearanceTab;
    @FXML private Tab settingsAdvancedTab;
    @FXML private AnchorPane settingsAdvancedTabAnchorPane;
    @FXML private ChoiceBox< ConfigConstants.RefreshChoice > settingsMusicSourcesRefreshChoice;
    @FXML private TextField settingsMusicSourcesRefreshInterval;
    @FXML private CheckBox settingsAppearanceDisplayAlbumsCheckbox;
    @FXML private TableView< SettingsMusicSourcesHandler.MusicSourceItem > settingsMusicSourcesTableView;

    @Override
    public void changed( ObservableValue< ? extends Tab > inObservable, Tab inOldValue, Tab inNewValue ) {
        if ( settingsAdvancedTab == inNewValue && mAdvancedController == null ) {
            mAdvancedController = new SettingsAdvancedHandler( this );
        } else if ( settingsAppearanceTab == inNewValue ) {
            mAppearanceController = new SettingsAppearanceHandler( this );
        } else if ( settingsMusicSourcesTab == inNewValue ) {
            mMusicSourcesController = new SettingsMusicSourcesHandler( this );
        }
    }

    public static String getFXMLDocument() {
        return sFXMLDocument;
    }

    public void init( Stage inStage, JMusicController inController ) {
        mStage = inStage;
        mController = inController;
        settingsTabPane.getSelectionModel().selectedItemProperty().addListener( this );
        mMusicSourcesController = new SettingsMusicSourcesHandler( this );
    }

    @Override
    public void initialize( URL inLocation, ResourceBundle inResources ) {}

    CheckBox getAppearanceDisplayAlbumsCheckBox() {
        return settingsAppearanceDisplayAlbumsCheckbox;
    }

    Library getLibrary() { return mController.getLibrary(); }

    ChoiceBox< ConfigConstants.RefreshChoice > getMusicSourcesRefreshChoice() {
        return settingsMusicSourcesRefreshChoice;
    }

    TextField getMusicSourcesRefreshInterval() { return settingsMusicSourcesRefreshInterval; }

    TableView< SettingsMusicSourcesHandler.MusicSourceItem > getMusicSourcesTableView() { return settingsMusicSourcesTableView; }

    AnchorPane getSettingsAdvancedTabAnchorPane() { return settingsAdvancedTabAnchorPane; }

    @FXML
    private void handleCancelButtonAction( ActionEvent inEvent ) {
        mStage.close();
    }

    @FXML
    private void handleOKButtonAction( ActionEvent inEvent ) {
        Map< String, String > theSettings = new HashMap<>();
        Set< String > theRemovals = new HashSet<>();
        if ( mMusicSourcesController != null ) {
            mMusicSourcesController.getSettings( theSettings, theRemovals );
        }
        if ( mAppearanceController != null ) {
            mAppearanceController.getSettings( theSettings, theRemovals );
        }
        if ( mAdvancedController != null ) {
            mAdvancedController.getSettings( theSettings, theRemovals );
        }
        Config.getInstance().setProperties( theSettings );
        Config.getInstance().removeProperties( theRemovals );
        mStage.close();
    }
}
