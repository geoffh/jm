package jmusic.ui.addroot;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jmusic.library.Library;
import jmusic.library.LibraryException;

import java.io.IOException;
import java.util.logging.Logger;

public class AddRoot {
    private static final String sFXMLDocument = "AddRoot.fxml";
    private static final Logger sLogger = Logger.getLogger( AddRoot.class.getName() );

    public static void addRoot( Library inLibrary ) {
        try {
            FXMLLoader theLoader = new FXMLLoader( AddRoot.class.getResource( sFXMLDocument ) );
            Parent theParent = theLoader.load();
            Stage theStage = new Stage();
            AddRootController theController = theLoader.< AddRootController >getController();
            theController.setStage( theStage );
            theStage.setScene( new Scene( theParent ) );
            theStage.initModality( Modality.APPLICATION_MODAL);
            theStage.showAndWait();
            if ( theController.isCanceled() ) {
                return;
            }
            inLibrary.addRoot( theController.getLocation(), theController.getName() );
        } catch( LibraryException | IOException theException ) {
            sLogger.throwing( "AddRoot", "addRoot", theException );
        }
    }
}