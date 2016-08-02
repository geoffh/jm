package jmusic.ui.edittrack;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jmusic.ui.JMusicController;

import java.io.IOException;
import java.util.logging.Logger;

public class EditTrack {
    private static final String sFXMLDocument = "EditTrack.fxml";
    private static final Logger sLogger = Logger.getLogger( EditTrack.class.getName() );

    public static void editTrack( JMusicController inMainController, EditTrackController.ControllerType inType ) {
        try {
            FXMLLoader theLoader = new FXMLLoader( EditTrack.class.getResource( sFXMLDocument ) );
            Parent theParent = theLoader.load();
            Stage theStage = new Stage();
            EditTrackController theController = theLoader.getController();
            theController.init( theStage, inMainController, inType );
            theStage.setScene( new Scene( theParent ) );
            theStage.initModality( Modality.APPLICATION_MODAL);
            theStage.showAndWait();
        } catch( IOException theException ) {
            sLogger.throwing( "EditTrack", "editTrack", theException );
        }
    }
}