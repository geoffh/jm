package jmusic.ui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jmusic.oldneedsrewrite.http.HttpServer;
import jmusic.library.Library;
import jmusic.util.Logging;

public class JMusic extends Application {
    private static final String sMainDocument = "JMusic.fxml";
    private final Library mLibrary = new Library( "Yeho" );
    public static void main( String[] inArgs ) {
        launch( inArgs );
    }

    public JMusic() {
        super();
    }
    
    @Override
    public void init() {
        Logging.init();
        // Todo: Can I move this to UPNP/ContentDirectoryService
        HttpServer.getInstance().addDocRoot( "/" );
    }
    
    @Override
    public void start( Stage inPrimaryStage ) throws Exception {
        FXMLLoader theLoader =
            new FXMLLoader( getClass().getResource( sMainDocument ) );
        Parent theRoot = theLoader.load();
        theLoader.< JMusicController >getController().init( mLibrary );
        Scene theScene = new Scene( theRoot );
        theScene.getStylesheets().addAll( getClass().getResource( "/jmusic/resources/css/root.css" ).toExternalForm() );
        inPrimaryStage.setScene( theScene );
        inPrimaryStage.setOnCloseRequest( inEvent -> System.exit( 0 ) );
        inPrimaryStage.show();
    }
}