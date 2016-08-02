package jmusic.ui.edittrack;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import jmusic.library.Library;
import jmusic.library.LibraryException;
import jmusic.library.LibraryItem;
import jmusic.ui.JMusicController;
import jmusic.ui.util.FontAwesome;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class EditTrackController implements Initializable, ChangeListener< EditTrackItem > {
    public enum ControllerType {
        Edit,
        Broken,
        Unknown
    }

    private Stage mStage;
    private JMusicController mController;
    private ControllerType mControllerType;
    private EditTrackModel mModel;
    private EditTrackView mView;
    private boolean mNeedsSave = false;
    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    @FXML private Button okButton;
    @FXML private Button upButton;
    @FXML private Button downButton;
    @FXML private TableView< EditTrackItem > trackTableView;
    @FXML private TextField trackNumber;
    @FXML private TextField trackName;
    @FXML private TextField trackArtist;
    @FXML private TextField trackAlbum;

    @Override
    public void changed( ObservableValue< ? extends EditTrackItem > inObservable,
                         EditTrackItem inOldValue,
                         EditTrackItem inNewValue ) {
        boolean bDisable = getSelectionCount() > 1;
        trackName.setDisable( bDisable );
        trackNumber.setDisable( bDisable );
        trackNumber.setText( getSelectedTrackNumber() );
        trackName.setText( getSelectedTrackName() );
        trackArtist.setText( getSelectedArtist() );
        trackAlbum.setText( getSelectedAlbum() );
    }

    public void init( Stage inStage, JMusicController inController, ControllerType inType ) {
        mStage = inStage;
        mController = inController;
        mControllerType = inType;
        mModel = new EditTrackModel();
        mView = new EditTrackView( trackTableView, ControllerType.Edit.equals( inType ) );
        mView.setData( mModel.getData() );
        trackTableView.getSelectionModel().selectedItemProperty().addListener( this );
        loadData();
        initControls();
        initHandlers();
    }

    @Override
    public void initialize( URL inLocation, ResourceBundle inResources ) {
    }

    @FXML
    private void handleCancelButtonAction( ActionEvent inEvent ) {
        mStage.close();
    }

    @FXML
    private void handleDownButtonAction( ActionEvent inEvent ) {
        selectAndScrollToIndex( mView.getView().getSelectionModel().getSelectedIndex() + 1 );
    }

    @FXML private void handleOKButtonAction( ActionEvent inEvent ) {
        mStage.close();
        if ( mNeedsSave ) {
            if ( ControllerType.Broken.equals( mControllerType ) ) {
                fixBrokenTracks();
            } else {
                updateTracks();
            }
        }
    }

    @FXML
    private void handleUpButtonAction( ActionEvent inEvent ) {
        selectAndScrollToIndex( mView.getView().getSelectionModel().getSelectedIndex() - 1 );
    }

    private void fixBrokenTracks() {
        Long theRootId = null;
        for ( LibraryItem theItem : mModel.getModifiedData() ) {
            try {
                mController.getLibrary().fixBrokenTrack( theItem );
                theRootId = theItem.getRootId();
            } catch( LibraryException theException ) {
                mLogger.throwing( "EditTrackController", "fixBrokenTracks", theException );
            }
        }
        if ( theRootId != null ) {
            mController.getLibrary().refresh( theRootId );
        }
    }

    private String getSelectedArtist() {
        ObservableList< EditTrackItem > theItems = getSelectedItems();
        if ( theItems == null ) {
            return null;
        }
        String theSelectedArtist = null;
        for ( EditTrackItem theItem : theItems ) {
            String theArtist = theItem.getArtistName();
            if ( theArtist == null ) {
                theSelectedArtist = null;
                break;
            }
            if ( theSelectedArtist == null ) {
                theSelectedArtist = theArtist;
                continue;
            }
            if ( ! theArtist.equals( theSelectedArtist ) ) {
                theSelectedArtist = null;
                break;
            }
        }
        return theSelectedArtist;
    }

    private String getSelectedAlbum() {
        ObservableList< EditTrackItem > theItems = getSelectedItems();
        if ( theItems == null ) {
            return null;
        }
        String theSelectedAlbum = null;
        for ( EditTrackItem theItem : theItems ) {
            String theAlbum = theItem.getAlbumName();
            if ( theAlbum == null ) {
                theSelectedAlbum = null;
                break;
            }
            if ( theSelectedAlbum == null ) {
                theSelectedAlbum = theAlbum;
                continue;
            }
            if ( ! theAlbum.equals( theSelectedAlbum ) ) {
                theSelectedAlbum = null;
                break;
            }
        }
        return theSelectedAlbum;
    }

    private String getSelectedTrackName() {
        if ( getSelectionCount() > 1 ) {
            return null;
        }
        return trackTableView.getSelectionModel().getSelectedItems().get( 0 ).getTitle();
    }

    private ObservableList< EditTrackItem > getSelectedItems() {
        return trackTableView.getSelectionModel().getSelectedItems();
    }

    private String getSelectedTrackNumber() {
        if ( getSelectionCount() > 1 ) {
            return null;
        }
        Integer theTrackNumber =
            trackTableView.getSelectionModel().getSelectedItems().get( 0 ).getTrackNumber();
        return theTrackNumber == null || theTrackNumber == -1 ?
            "" : String.valueOf( theTrackNumber );
    }

    private int getSelectionCount() {
        ObservableList< EditTrackItem > theItems = getSelectedItems();
        return theItems != null ? theItems.size() : 0;
    }

    private void initControls() {
        FontAwesome.setIcon( upButton, FontAwesome.sFA_ARROW_UP );
        FontAwesome.setIcon( downButton, FontAwesome.sFA_ARROW_DOWN );
    }

    private void initHandlers() {
        trackNumber.textProperty().addListener( ( inObservable, inOldValue, inNewValue ) -> {
            if ( ! trackNumber.isFocused() ) {
                return;
            }
            for ( EditTrackItem theItem : getSelectedItems() ) {
                try {
                    theItem.setTrackNumber( Integer.valueOf( inNewValue.trim() ) );
                    mModel.setModified( theItem );
                    setNeedsSave( true );
                } catch( Exception theIgnore ) {}
            }
        } );
        trackName.textProperty().addListener( ( inObservable, inOldValue, inNewValue ) -> {
            if ( ! trackName.isFocused() ) {
                return;
            }
            for ( EditTrackItem theItem : getSelectedItems() ) {
                theItem.setTitle( inNewValue );
                mModel.setModified( theItem );
                setNeedsSave( true );
            }
        } );
        trackArtist.textProperty().addListener( ( inObservable, inOldValue, inNewValue ) -> {
            if ( ! trackArtist.isFocused() ) {
                return;
            }
            for ( EditTrackItem theItem : getSelectedItems() ) {
                theItem.setArtistName( inNewValue );
                mModel.setModified( theItem );
                setNeedsSave( true );
            }
        } );
        trackAlbum.textProperty().addListener( ( inObservable, inOldValue, inNewValue ) -> {
            if ( ! trackAlbum.isFocused() ) {
                return;
            }
            for ( EditTrackItem theItem : getSelectedItems() ) {
                theItem.setAlbumName( inNewValue );
                mModel.setModified( theItem );
                setNeedsSave( true );
            }
        } );
    }

    private void loadBrokenTracks() {
        final Object theLock = new Object();
        mController.getLibrary().findBrokenTracks(
            mController.getSelectedNavigationItem().getRootId(), new Library.BrokenTrackCallback() {
                @Override
                public void onBrokenTrackFound( String inUri ) {
                    LibraryItem theItem = new LibraryItem();
                    theItem.setUri( inUri );
                    synchronized( theLock ) {
                        // Have to fake an id to store correctly in the models
                        // modified data map
                        long theId = mModel.getData().size();
                        theItem.setId( theId );
                    }
                    mModel.addTrack( new EditTrackItem( theItem ) );
                }
                @Override
                public void onComplete() {}
            } );
    }

    private void loadData() {
        if ( ControllerType.Broken.equals( mControllerType ) ) {
            loadBrokenTracks();
        } else if ( ControllerType.Unknown.equals( mControllerType ) ) {
            loadUnknownTracks();
        } else {
            loadTracks();
        }
    }

    private void loadTracks() {
        for ( LibraryItem theItem : mController.getSelectedContentItems() ) {
            mModel.addTrack( new EditTrackItem( theItem ) );
        }
    }

    private void loadUnknownTracks() {
        for ( LibraryItem theItem : mController.getLibrary().getUnknownTracks( mController.getSelectedNavigationItem().getRootId() ) ) {
            mModel.addTrack( new EditTrackItem( theItem ) );
        }
    }

    private void selectAndScrollToIndex( int inIndex ) {
        mView.getView().getSelectionModel().clearAndSelect( inIndex );
        mView.getView().scrollTo( inIndex );
        trackNumber.requestFocus();
    }

    private void setNeedsSave( boolean inNeedsSave ) {
        mNeedsSave = inNeedsSave;
        okButton.setDisable( ! inNeedsSave );
    }

    private void updateTracks() {
        HashMap< Long, LibraryItem > theTracks = new HashMap<>();
        for ( LibraryItem theItem : mModel.getModifiedData() ) {
            theTracks.put( theItem.getId(), theItem );
        }
        mController.updateTracks( theTracks );
    }
}