package jmusic.ui.settings;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jmusic.ui.JMusicController;

import java.io.IOException;
import java.util.logging.Logger;

public class Settings {
    private static final String sFXMLDocument = "Settings.fxml";
    private static final Logger sLogger = Logger.getLogger( Settings.class.getName() );

    public static void settings( JMusicController inController ) {
        try {
            FXMLLoader theLoader = new FXMLLoader( Settings.class.getResource( sFXMLDocument ) );
            Parent theParent = theLoader.load();
            Stage theStage = new Stage();
            SettingsController theController = theLoader.getController();
            theController.init( theStage, inController );
            theStage.setScene( new Scene( theParent ) );
            theStage.initModality( Modality.APPLICATION_MODAL);
            theStage.showAndWait();
        } catch( IOException theException ) {
            sLogger.throwing( "Settings", "settings", theException );
        }
    }
}