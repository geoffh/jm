package jmusic.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jmusic.oldneedsrewrite.http.HttpServer;
import jmusic.library.Library;
import jmusic.util.Logging;

public class JMusic extends Application {
    private static final String sMainDocument = "JMusic.fxml";
    private final Library mLibrary = new Library( "Yeho" );
    public static void main( String[] inArgs ) {
        launch( inArgs );
    }
    
    @Override
    public void init() {
        Logging.init();
        // Can I moved this to UPNP/ContentDirectoyService
        HttpServer.getInstance().addDocRoot( "/" );
    }
    
    @Override
    public void start( Stage inPrimaryStage ) throws Exception {
        FXMLLoader theLoader =
            new FXMLLoader( getClass().getResource( sMainDocument ) );
        Parent theRoot = theLoader.load();
        theLoader.< JMusicController >getController().init( mLibrary );
        inPrimaryStage.setScene( new Scene( theRoot ) );
        inPrimaryStage.show();
    }
}