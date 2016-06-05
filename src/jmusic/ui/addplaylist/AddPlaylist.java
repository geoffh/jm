package jmusic.ui.addplaylist;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jmusic.library.Library;

import java.io.IOException;
import java.util.logging.Logger;

public class AddPlaylist {
    private static final String sFXMLDocument = "AddPlaylist.fxml";
    private static final Logger sLogger = Logger.getLogger( AddPlaylist.class.getName() );

    public static void addPlaylist( Library inLibrary ) {
        try {
            FXMLLoader theLoader = new FXMLLoader( AddPlaylist.class.getResource( sFXMLDocument ) );
            Parent theParent = theLoader.load();
            Stage theStage = new Stage();
            AddPlaylistController theController = theLoader.< AddPlaylistController >getController();
            theController.setStage( theStage );
            theStage.setScene( new Scene( theParent ) );
            theStage.initModality( Modality.APPLICATION_MODAL );
            theStage.showAndWait();
            if ( theController.isCanceled() ) {
                return;
            }
            inLibrary.addPlaylist( theController.getName() );
        } catch( IOException theException ) {
            sLogger.throwing( "AddPlaylist", "addPlaylist", theException );
        }
    }
}