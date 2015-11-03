package jmusic.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jmusic.device.MediaRendererDevice;
import jmusic.library.Library;
import jmusic.library.LibraryException;
import jmusic.library.LibraryItem;
import org.controlsfx.dialog.Dialogs;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class JMusicController implements Initializable {
    private static final String sAddPlaylistDocument = "AddPlaylist.fxml";
    private static final String sAddRootDocument     = "AddRoot.fxml";
    private static final String sSettingsDocument    = "Settings.fxml";
    private static final String sEditTracksDocument  = "TrackEdit.fxml";

    private final Logger mLogger = Logger.getLogger( JMusicController.class.getName() );
    private Library mLibrary;
    private ContainerViewController mContainerViewController;
    private ContainerView mContainerView;
    private TrackViewController mTrackViewController;
    private TrackView mTrackView;
    private PlayController mPlayController;
    private boolean mContainerViewIsSelected = true;
    private Clipboard mClipboard;
    private Clipboard mDragboard;

    @FXML private AnchorPane root;
    @FXML private TreeView< LibraryItem > containerTreeView;
    @FXML private TableView< TrackViewItem > trackTableView;
    @FXML private MenuBar menuBar;
    @FXML private MenuItem menuItemFileNewPlaylist;
    @FXML private MenuItem menuItemFileNewRoot;
    @FXML private MenuItem menuItemFileRefresh;
    @FXML private MenuItem menuItemFileBrokenTracks;
    @FXML private MenuItem menuItemFileUnknownTracks;
    @FXML private MenuItem menuItemFileSettings;
    @FXML private MenuItem menuItemFileClose;
    @FXML private MenuItem menuItemEditEdit;
    @FXML private MenuItem menuItemEditCopy;
    @FXML private MenuItem menuItemEditPaste;
    @FXML private MenuItem menuItemEditRemove;
    @FXML private MenuItem menuItemContainerContextNewRoot;
    @FXML private MenuItem menuItemContainerContextNewPlaylist;
    @FXML private MenuItem menuItemContainerContextRefresh;
    @FXML private MenuItem menuItemContainerContextBrokenTracks;
    @FXML private MenuItem menuItemContainerContextUnknownTracks;
    @FXML private MenuItem menuItemContainerContextCopy;
    @FXML private MenuItem menuItemContainerContextPaste;
    @FXML private MenuItem menuItemContainerContextRemove;
    @FXML private MenuItem menuItemTrackContextCopy;
    @FXML private MenuItem menuItemTrackContextEdit;
    @FXML private MenuItem menuItemTrackContextRemove;
    @FXML private ComboBox< MediaRendererDevice > playRendererComboBox;
    @FXML private Button playToolBarRewindButton;
    @FXML private Button playToolBarPlayButton;
    @FXML private Button playToolBarForwardButton;
    @FXML private Label playArtistAlbumLabel;
    @FXML private Label playTrackLabel;
    @FXML private Label playTimeElapsed;
    @FXML private Label playTimeRemaining;
    @FXML private ProgressBar playProgressBar;
    @FXML private Slider playRendererVolumeSlider;

    @Override
    public void initialize( URL inUrl, ResourceBundle inResourceBundle ) {}

    public void onContainerViewContextMenuShowing( Event inEvent ) {
        LibraryItem theItem = getSelectedContainer();
        boolean bDisableBrokenTracks = true;
        boolean bDisableUnknownTracks = true;
        boolean bDisableCopy = true;
        boolean bDisablePaste = ! isValidPasteTarget( mClipboard.getContent(), theItem );
        boolean bDisableRefresh = true;
        boolean bDisableRemove = true;
        if ( theItem != null ) {
            switch ( theItem.getType() ) {
                case root:
                    bDisableBrokenTracks = false;
                    bDisableUnknownTracks = false;
                    bDisableCopy = false;
                    bDisableRefresh = false;
                    bDisableRemove = false;
                    break;
                case cdroot:
                    bDisableCopy = false;
                    bDisableRefresh = false;
                    break;
                case playlist:
                    bDisableRemove = false;
                    break;
                case artist:
                case album:
                    bDisableCopy = false;
                    break;
                default:
                    break;
            }
        }
        menuItemContainerContextCopy.setDisable( bDisableCopy );
        menuItemContainerContextPaste.setDisable( bDisablePaste );
        menuItemContainerContextRefresh.setDisable( bDisableRefresh );
        menuItemContainerContextBrokenTracks.setDisable( bDisableBrokenTracks );
        menuItemContainerContextUnknownTracks.setDisable( bDisableUnknownTracks );
        menuItemContainerContextRemove.setDisable( bDisableRemove );
        inEvent.consume();
    }

    public void onContainerViewMouseClicked( MouseEvent inEvent ) {
        mContainerViewIsSelected = true;
        mTrackView.cancelEdit();
    }

    public void onEditMenuShowing( Event inEvent ) {
        LibraryItem theItem = getSelectedItemFromFocusedView();
        boolean bDisableCopy = true;
        boolean bDisableEdit = true;
        boolean bDisablePaste = true;
        boolean bDisableRemove = true;
        if ( theItem != null ) {
            bDisablePaste = ! isValidPasteTarget( mClipboard.getContent(), theItem );
            switch ( theItem.getType() ) {
                case root:
                    bDisableRemove = false;
                    bDisableCopy = false;
                    break;
                case cdroot:
                    bDisableCopy = false;
                    break;
                case playlist:
                    bDisableRemove = false;
                    break;
                case artist:
                case album:
                    bDisableCopy = false;
                    break;
                case track:
                    bDisableCopy = false;
                    bDisableEdit = ! mLibrary.isWriteable( theItem );
                    bDisableRemove = ! getSelectedContainer().isPlaylist();
                    break;
                default:
                    break;
            }
        }
        menuItemEditCopy.setDisable( bDisableCopy );
        menuItemEditEdit.setDisable( bDisableEdit );
        menuItemEditPaste.setDisable( bDisablePaste );
        menuItemEditRemove.setDisable( bDisableRemove );
        inEvent.consume();
    }

    public void onFileMenuShowing( Event inEvent ) {
        boolean bDisableRefresh = true;
        boolean bDisableBrokenTracks = true;
        boolean bDisableUnknownTracks = true;
        LibraryItem theItem = getSelectedItemFromFocusedView();
        if ( theItem != null ) {
            if ( theItem.isRoot() ) {
                bDisableRefresh = false;
                bDisableBrokenTracks = false;
                bDisableUnknownTracks = false;
            } else if ( theItem.isCDRoot() ) {
                bDisableRefresh = false;
                bDisableBrokenTracks = true;
                bDisableUnknownTracks = true;
            }
        }
        menuItemFileRefresh.setDisable( bDisableRefresh );
        menuItemFileBrokenTracks.setDisable( bDisableBrokenTracks );
        menuItemFileUnknownTracks.setDisable( bDisableUnknownTracks );
        inEvent.consume();
    }

    public void onKeyPressed( KeyEvent inEvent ) {
        if ( inEvent.getCode().equals( KeyCode.CONTROL ) ) {
            // Omitting this results in context menu being raised and cells
            // starting to edit at the same time.
            containerTreeView.setEditable( false );
            trackTableView.setEditable( false );
        }
    }

    public void onKeyReleased( KeyEvent inEvent ) {
        if ( inEvent.getCode().equals( KeyCode.CONTROL ) ) {
            containerTreeView.setEditable( true );
            trackTableView.setEditable( true );
        }
    }

    public void onTrackViewContextMenuShowing( Event inEvent ) {
        List< TrackViewItem > theSelectedTracks = getSelectedTracks();
        boolean isDisabled = theSelectedTracks == null || theSelectedTracks.isEmpty();
        menuItemTrackContextCopy.setDisable( isDisabled );
        menuItemTrackContextEdit.setDisable( isDisabled );
        LibraryItem theContainer = getSelectedContainer();
        menuItemTrackContextRemove.setDisable( isDisabled || theContainer == null || ! theContainer.isPlaylist() );
        inEvent.consume();
    }

    public void onTrackViewMouseClicked( MouseEvent inEvent ) {
        mContainerViewIsSelected = false;
        mContainerView.cancelEdit();
    }

    void drop( List< ? extends LibraryItem > inSource, LibraryItem inTarget ) {
        if ( ! isValidPasteTarget( inSource, inTarget ) ) {
            return;
        }
        if ( inTarget.isPlaylist() ) {
            dropToPlaylist( inSource, inTarget );
        } else {
            dropToContainer( inSource, inTarget );
        }
    }

    Clipboard getClipboard() { return mClipboard; }

    TreeView< LibraryItem > getContainerView() { return containerTreeView; }

    TableView< TrackViewItem > getTrackView() { return trackTableView; }

    Clipboard getDragboard() { return mDragboard; }

    Library getLibrary() { return mLibrary; }

    LibraryItem getSelectedContainer() {
        TreeItem< LibraryItem > theContainer = containerTreeView.getSelectionModel().getSelectedItem();
        return theContainer != null ? theContainer.getValue() : null;
    }

    List< LibraryItem > getSelectedContainerAsCollection() {
        LinkedList< LibraryItem > theSelectedItems = new LinkedList<>();
        LibraryItem theSelectedItem = getSelectedContainer();
        theSelectedItems.add( theSelectedItem );
        return theSelectedItems;
    }

    List< TrackViewItem > getSelectedTracks() {
        return trackTableView.getSelectionModel().getSelectedItems();
    }

    LibraryItem getSelectedItemFromFocusedView() {
        List< ? extends LibraryItem > theSelectedItems = getSelectedItemsFromFocusedView();
        return ! theSelectedItems.isEmpty() ? theSelectedItems.get( 0 ) : null;
    }

    List< ? extends LibraryItem > getSelectedItemsFromFocusedView() {
        return mContainerViewIsSelected ? getSelectedContainerAsCollection() : getSelectedTracks();
    }

    void importTracks( Map< Long, LibraryItem > inTracks, Long inTargetRootId ) {
        if ( inTracks.isEmpty() ) {
            return;
        }
        TrackImportService theService = new TrackImportService( mLibrary, inTracks, inTargetRootId );
        Dialogs.create()
            .owner( root )
            .title( "Importing Tracks" )
            .masthead( "Importing Tracks" )
            .showWorkerProgress( theService );
        theService.start();
    }

    void init( Library inLibrary ) {
        mLibrary = inLibrary;

        mContainerView = new ContainerView( containerTreeView );
        mContainerViewController =
            new ContainerViewController( this, new ContainerViewModel(), mContainerView );
        mTrackView = new TrackView( trackTableView );
        mTrackViewController = new TrackViewController( this, new TrackViewModel(), mTrackView );
        mPlayController = new PlayController( this, initPlayControls() );
        mClipboard = new Clipboard( mLibrary );
        mDragboard = new Clipboard( mLibrary );
        menuBar.toFront();
    }

    PlayControls initPlayControls() {
        PlayControls thePlayControls = new PlayControls();
        thePlayControls.setRewindButton( playToolBarRewindButton );
        thePlayControls.setPlayButton( playToolBarPlayButton );
        thePlayControls.setForwardButton( playToolBarForwardButton );
        thePlayControls.setArtistAlbumLabel( playArtistAlbumLabel );
        thePlayControls.setTrackLabel( playTrackLabel );
        thePlayControls.setPlayTimeElapsedLabel( playTimeElapsed );
        thePlayControls.setPlayTimeRemainingLabel( playTimeRemaining );
        thePlayControls.setProgressBar( playProgressBar );
        thePlayControls.setRendererComboBox( playRendererComboBox  );
        thePlayControls.setRendererVolume( playRendererVolumeSlider );
        return thePlayControls;
    }

    boolean isPlayDisabled() {
        return trackTableView.getItems().isEmpty();
    }

    boolean isValidPasteTarget( LibraryItem inSource, LibraryItem inTarget ) {
        if ( inSource == null || inTarget == null ||
             inTarget.isPlaylistsRoot() || inTarget.isCDRoot()  ||
             ( ! inTarget.isPlaylist() && ! mLibrary.isWriteable( inTarget ) ) ) {
            return false;
        }
        boolean isValidTarget = false;
        Long theTargetId = inTarget.getId();
        switch( inSource.getType() ) {
            // Todo: Can't copy/drag a cd track to a playlist
            case track:
                isValidTarget =
                    ( inTarget.isRoot()   && theTargetId.equals( inSource.getRootId() ) )   ||
                    ( inTarget.isArtist() && theTargetId.equals( inSource.getArtistId() ) ) ||
                    ( inTarget.isAlbum()  && theTargetId.equals( inSource.getParentId() ) ) ||
                      inTarget.isTrack() ?
                        false : true;
                break;
            case artist:
                isValidTarget = inTarget.isRoot() && ! theTargetId.equals( inSource.getRootId() );
                break;
            case album:
                isValidTarget =
                    ( inTarget.isRoot() && ! theTargetId.equals( inSource.getRootId() ) ) ||
                    ( inTarget.isArtist() && ! theTargetId.equals( inSource.getParentId() ) );
                break;
            case root:
                isValidTarget = inTarget.isRoot() && ! theTargetId.equals( inSource.getId() );
                break;
            default:
                break;
        }
        return isValidTarget;
    }

    boolean isValidPasteTarget( List< ? extends LibraryItem > inSources, LibraryItem inTarget ) {
        if ( inSources == null || inSources.isEmpty() ) {
            return false;
        }
        for ( LibraryItem theSource : inSources ) {
            if ( ! isValidPasteTarget( theSource, inTarget ) ) {
                return false;
            }
        }
        return true;
    }

    void updatePlaylist( LibraryItem inPlaylist ) {
        mLibrary.updatePlaylist( inPlaylist );
        mLibrary.refresh( inPlaylist.getRootId() );
    }

    void updateRoot( LibraryItem inRoot ) {
        mLibrary.updateRoot( inRoot );
        mLibrary.refresh( inRoot.getRootId() );
    }

    void updateTrack( LibraryItem inTrack ) {
        Map< Long, LibraryItem > theTracks = new HashMap<>();
        theTracks.put( inTrack.getId(), inTrack );
        updateTracks( theTracks );
    }

    void updateTracks( Map< Long, LibraryItem > inTracks ) {
        if ( inTracks.isEmpty() ) {
            return;
        }
        TrackUpdateService theService = new TrackUpdateService( mLibrary, inTracks );
        Dialogs.create()
            .owner( root )
            .title( "Updating Tracks" )
            .masthead( "Updating Tracks" )
            .showWorkerProgress( theService );
        theService.start();
    }

    private void addPlaylist() {
        try {
            FXMLLoader theLoader =
                new FXMLLoader( getClass().getResource( sAddPlaylistDocument ) );
            Parent theParent = theLoader.load();
            Stage theStage = new Stage();
            AddPlaylistController theController = theLoader.< AddPlaylistController >getController();
            theController.setStage( theStage );
            theStage.setScene( new Scene( theParent ) );
            theStage.initModality( Modality.APPLICATION_MODAL);
            theStage.showAndWait();
            if ( theController.isCanceled() ) {
                return;
            }
            mLibrary.addPlaylist( theController.getName() );
        } catch( IOException theException ) {
            mLogger.throwing( "JMusicController", "addPlaylist", theException );
        }
    }
    
    private void addRoot() {
        try {
            FXMLLoader theLoader =
                new FXMLLoader( getClass().getResource( sAddRootDocument ) );
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
            mLibrary.addRoot( theController.getLocation(), theController.getName() );
        } catch( LibraryException | IOException theException ) {
            mLogger.throwing( "JMusicController", "addRoot", theException );
        }
    }

    private void copy( boolean inCopyFromContainerView ) {
        mClipboard.setContent( inCopyFromContainerView ?
            getSelectedContainerAsCollection() : getSelectedTracks() );
    }

    private void dropToContainer( List< ? extends LibraryItem > inSource, LibraryItem inContainer ) {
        LibraryItem theSourceItem = inSource.get( 0 );
        String theArtistName = null;
        String theAlbumName = null;
        List< ? extends LibraryItem > theTracks;
        switch( theSourceItem.getType() ) {
            case track:
                theTracks = inSource;
                if ( inContainer.isArtist() ) {
                    theArtistName = inContainer.getTitle();
                } else if ( inContainer.isAlbum() ) {
                    theArtistName = mLibrary.getItem( inContainer.getParentId() ).getTitle();
                    theAlbumName = inContainer.getTitle();
                }
                break;
            case album:
                theArtistName = inContainer.isArtist() ? inContainer.getTitle() : null;
            default:
                theTracks = mLibrary.getTracks( theSourceItem );
                break;
        }
        HashMap< Long, LibraryItem > theItems = new HashMap<>();
        for ( LibraryItem theItem : theTracks ) {
            if ( theArtistName != null ) {
                theItem.setArtistName( theArtistName );
            }
            if ( theAlbumName != null ) {
                theItem.setAlbumName( theAlbumName );
            }
            theItems.put( theItem.getId(), theItem );
        }
        if ( theSourceItem.getRootId().equals( inContainer.getRootId() ) ) {
            updateTracks( theItems );
        } else {
            importTracks( theItems, inContainer.getRootId() );
        }
    }

    private void dropToPlaylist( List< ? extends LibraryItem > inTracks, LibraryItem inPlaylist ) {
        Long thePlaylistId = inPlaylist.getId();
        for ( LibraryItem theTrack : inTracks ) {
            mLibrary.addTrackToPlaylist( theTrack.getId(), thePlaylistId );
        }
    }

    private void editTracks( TrackEditController.ControllerType inType ) {
        try {
            FXMLLoader theLoader =
                new FXMLLoader( getClass().getResource( sEditTracksDocument ) );
            Parent theParent = theLoader.load();
            Stage theStage = new Stage();
            TrackEditController theController = theLoader.< TrackEditController >getController();
            theController.init( theStage, this, inType );
            theStage.setScene( new Scene( theParent ) );
            theStage.initModality( Modality.APPLICATION_MODAL);
            theStage.showAndWait();
        } catch( IOException theException ) {
            mLogger.throwing( "JMusicController", "addRoot", theException );
        }
    }

    @FXML private void onMenuItemPressed( ActionEvent inEvent ) {
        Object theSource = ( MenuItem )inEvent.getSource();
        if ( menuItemFileNewRoot.equals( theSource ) ||
             menuItemContainerContextNewRoot.equals( theSource ) ) {
            addRoot();
        } else if ( menuItemFileNewPlaylist.equals( theSource ) ||
                    menuItemContainerContextNewPlaylist.equals( theSource ) ) {
            addPlaylist();
        } else if ( menuItemFileRefresh.equals( theSource ) ||
                    menuItemContainerContextRefresh.equals( theSource ) ) {
            refresh();
        } else if ( menuItemFileBrokenTracks.equals( theSource ) ||
            menuItemContainerContextBrokenTracks.equals( theSource ) ) {
            editTracks( TrackEditController.ControllerType.Broken );
        } else if ( menuItemFileUnknownTracks.equals( theSource ) ||
            menuItemContainerContextUnknownTracks.equals( theSource ) ) {
            editTracks( TrackEditController.ControllerType.Unknown );
        } else if ( menuItemFileSettings.equals( theSource ) ) {
            settings();
        } else if ( menuItemFileClose.equals( theSource ) ) {
            System.exit( 0 );
        } else if ( menuItemEditEdit.equals( theSource ) ||
                    menuItemTrackContextEdit.equals( theSource ) ) {
            editTracks( TrackEditController.ControllerType.Edit );
        } else if ( menuItemEditCopy.equals( theSource ) ) {
            copy( mContainerViewIsSelected );
        } else if ( menuItemContainerContextCopy.equals( theSource ) ) {
            copy( true );
        } else if ( menuItemTrackContextCopy.equals( theSource ) ) {
            copy( false );
        } else if ( menuItemEditPaste.equals( theSource ) ||
                    menuItemContainerContextPaste.equals( theSource ) ) {
            paste();
        } else if ( menuItemEditRemove.equals( theSource ) ||
                    menuItemContainerContextRemove.equals( theSource ) ||
                    menuItemTrackContextRemove.equals( theSource ) ) {
            remove();
        }
    }

    private void paste() {
        drop( mClipboard.getContent(), getSelectedContainer() );
    }

    private static final String sDeleteRoot = "Remove Music Source";
    private static final String sAboutToDeleteRoot = "About to remove Music Source '%s'";
    private static final String sDeletePlaylist = "Remove Playlist";
    private static final String sAboutToDeletePlaylist = "About to remove Playlist '%s'";
    private static final String sDeleteTrack = "Remove Track(s)";
    private static final String sAboutToDeleteTrack = "About to remove Track(s) from Playlist '%s'";
    private void remove() {
        LibraryItem theItem = getSelectedItemFromFocusedView();
        String theName = theItem.getTitle();
        String theTitle;
        String theHeader;
        LibraryItem.Type theType = theItem.getType();
        switch( theType ) {
            case root:
                theTitle = sDeleteRoot;
                theHeader = String.format( sAboutToDeleteRoot, theName );
                break;
            case playlist:
                theTitle = sDeletePlaylist;
                theHeader = String.format( sAboutToDeletePlaylist, theName );
                break;
            case track:
                theTitle = sDeleteTrack;
                theHeader = String.format( sAboutToDeleteTrack, getSelectedContainer().getTitle() );
                break;
            default:
                mLogger.warning( "Unexpected type '" + theItem.getType() + "'" );
                return;
        }
        Alert theAlert = new Alert( Alert.AlertType.CONFIRMATION );
        theAlert.setTitle( theTitle );
        theAlert.setHeaderText( theHeader );
        theAlert.setContentText( "Press OK to continue" );
        Optional< ButtonType > theResult = theAlert.showAndWait();
        if ( theResult.get().getButtonData().isDefaultButton() ) {
            switch ( theType ) {
                case root:
                    mLibrary.removeRoot( theItem.getId() );
                    break;
                case playlist:
                    mLibrary.removePlaylist( theItem.getId() );
                    break;
                case track:
                    Long thePlaylistId = getSelectedContainer().getId();
                    for ( TrackViewItem theTrack : getSelectedTracks() ) {
                        mLibrary.removeTrackFromPlaylist( theTrack.getId(), thePlaylistId );
                    }
                    break;
            }
        }
    }

    private void refresh() {
        LibraryItem theItem = getSelectedContainer();
        if ( theItem == null ) {
            return;
        }
        mLibrary.refresh( theItem.getRootId() );
    }

    private void settings() {
        try {
            FXMLLoader theLoader =
                new FXMLLoader( getClass().getResource( sSettingsDocument ) );
            Parent theParent = theLoader.load();
            Stage theStage = new Stage();
            SettingsController theController = theLoader.< SettingsController >getController();
            theController.init( theStage, this );
            theStage.setScene( new Scene( theParent ) );
            theStage.initModality( Modality.APPLICATION_MODAL);
            theStage.showAndWait();
        } catch( IOException theException ) {
            mLogger.throwing( "JMusicController", "settings", theException );
        }
    }
}