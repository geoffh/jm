package jmusic.ui;

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

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class TrackEditController implements Initializable, ChangeListener< TrackEditItem > {
    enum ControllerType {
        Edit,
        Broken,
        Unknown
    }
    private Stage mStage;
    private JMusicController mController;
    private ControllerType mControllerType;
    private TrackEditModel mModel;
    private TrackEditView mView;
    private boolean mNeedsSave = false;
    private final Logger mLogger = Logger.getLogger( getClass().getName() );

    @FXML private Button okButton;
    @FXML private Button upButton;
    @FXML private Button downButton;
    @FXML private TableView< TrackEditItem > trackTableView;
    @FXML private TextField trackNumber;
    @FXML private TextField trackName;
    @FXML private TextField trackArtist;
    @FXML private TextField trackAlbum;

    @Override
    public void changed( ObservableValue< ? extends TrackEditItem > inObservable,
                         TrackEditItem inOldValue,
                         TrackEditItem inNewValue ) {
        boolean bDisable = getSelectionCount() > 1;
        trackName.setDisable( bDisable );
        trackNumber.setDisable( bDisable );
        trackNumber.setText( getSelectedTrackNumber() );
        trackName.setText( getSelectedTrackName() );
        trackArtist.setText( getSelectedArtist() );
        trackAlbum.setText( getSelectedAlbum() );
    }

    @Override
    public void initialize( URL inLocation, ResourceBundle inResources ) {
    }

    void init( Stage inStage, JMusicController inController, ControllerType inType ) {
        mStage = inStage;
        mController = inController;
        mControllerType = inType;
        mModel = new TrackEditModel();
        mView = new TrackEditView( trackTableView, ControllerType.Edit.equals( inType ) );
        mView.setData( mModel.getData() );
        trackTableView.getSelectionModel().selectedItemProperty().addListener( this );
        loadData();
        initHandlers();
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
                mLogger.throwing( "TrackEditController", "fixBrokenTracks", theException );
            }
        }
        if ( theRootId != null ) {
            mController.getLibrary().refresh( theRootId );
        }
    }

    private String getSelectedArtist() {
        ObservableList< TrackEditItem > theItems = getSelectedItems();
        if ( theItems == null ) {
            return null;
        }
        String theSelectedArtist = null;
        for ( TrackEditItem theItem : theItems ) {
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
        ObservableList< TrackEditItem > theItems = getSelectedItems();
        if ( theItems == null ) {
            return null;
        }
        String theSelectedAlbum = null;
        for ( TrackEditItem theItem : theItems ) {
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

    private ObservableList< TrackEditItem > getSelectedItems() {
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
        ObservableList< TrackEditItem > theItems = getSelectedItems();
        return theItems != null ? theItems.size() : 0;
    }

    private void initHandlers() {
        trackNumber.textProperty().addListener( new ChangeListener< String >() {
            @Override
            public void changed( ObservableValue< ? extends String > inObservable, String inOldValue, String inNewValue ) {
                if ( ! trackNumber.isFocused() ) {
                    return;
                }
                for ( TrackEditItem theItem : getSelectedItems() ) {
                    try {
                        theItem.setTrackNumber( Integer.valueOf( inNewValue.trim() ) );
                        mModel.setModified( theItem );
                        setNeedsSave( true );
                    } catch( Exception theIgnore ) {}
                }
            }
        } );
        trackName.textProperty().addListener( new ChangeListener< String >() {
            @Override
            public void changed( ObservableValue< ? extends String > inObservable, String inOldValue, String inNewValue ) {
                if ( ! trackName.isFocused() ) {
                    return;
                }
                for ( TrackEditItem theItem : getSelectedItems() ) {
                    theItem.setTitle( inNewValue );
                    mModel.setModified( theItem );
                    setNeedsSave( true );
                }
            }
        } );
        trackArtist.textProperty().addListener( new ChangeListener< String >() {
            @Override
            public void changed( ObservableValue< ? extends String > inObservable, String inOldValue, String inNewValue ) {
                if ( ! trackArtist.isFocused() ) {
                    return;
                }
                for ( TrackEditItem theItem : getSelectedItems() ) {
                    theItem.setArtistName( inNewValue );
                    mModel.setModified( theItem );
                    setNeedsSave( true );
                }
            }
        } );
        trackAlbum.textProperty().addListener( new ChangeListener< String >() {
            @Override
            public void changed( ObservableValue< ? extends String > inObservable, String inOldValue, String inNewValue ) {
                if ( ! trackAlbum.isFocused() ) {
                    return;
                }
                for ( TrackEditItem theItem : getSelectedItems() ) {
                    theItem.setAlbumName( inNewValue );
                    mModel.setModified( theItem );
                    setNeedsSave( true );
                }
            }
        } );
    }

    private void loadBrokenTracks() {
        try {
            final Object theLock = new Object();
            mController.getLibrary().findBrokenTracks(
                mController.getSelectedContainer().getRootId(), new Library.BrokenTrackCallback() {
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
                        mModel.addTrack( new TrackEditItem( theItem ) );
                    }
                    @Override
                    public void onComplete() {}
                } );
        } catch( LibraryException theException ) {
            mLogger.throwing( "TrackEditController", "loadBrokenTracks", theException );
        }
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
        for ( TrackViewItem theItem : mController.getSelectedTracks() ) {
            mModel.addTrack( new TrackEditItem( theItem ) );
        }
    }

    private void loadUnknownTracks() {
        for ( LibraryItem theItem : mController.getLibrary().getUnknownTracks( mController.getSelectedContainer().getRootId() ) ) {
            mModel.addTrack( new TrackEditItem( theItem ) );
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