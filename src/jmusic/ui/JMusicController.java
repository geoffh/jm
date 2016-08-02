package jmusic.ui;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import jmusic.device.MediaRendererDevice;
import jmusic.library.Library;
import jmusic.library.LibraryItem;
import jmusic.ui.addplaylist.AddPlaylist;
import jmusic.ui.addroot.AddRoot;
import jmusic.ui.edittrack.EditTrack;
import jmusic.ui.edittrack.EditTrackController;
import jmusic.ui.navigation.NavigationController;
import jmusic.ui.play.PlayController;
import jmusic.ui.play.PlayControls;
import jmusic.ui.services.TrackImportService;
import jmusic.ui.services.TrackUpdateService;
import jmusic.ui.settings.Settings;
import org.controlsfx.dialog.Dialogs;

import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class JMusicController implements Initializable {
    private static final String sDeleteRoot = "Remove Music Source";
    private static final String sAboutToDeleteRoot = "About to remove Music Source '%s'";
    private static final String sDeletePlaylist = "Remove Playlist";
    private static final String sAboutToDeletePlaylist = "About to remove Playlist '%s'";
    private static final String sDeleteTrack = "Remove Track(s)";
    private static final String sAboutToDeleteTrack = "About to remove Track(s) from Playlist '%s'";

    private final Logger mLogger = Logger.getLogger( JMusicController.class.getName() );
    private Library mLibrary;
    private NavigationController mNavigationController;
    private ContentController mContentController;
    private PlayController mPlayController;
    private boolean mNavigationViewIsSelected = true;
    private Clipboard mClipboard;
    private Clipboard mDragboard;

    @FXML private AnchorPane root;
    @FXML private SplitPane splitPane;
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

    public void addContentsChangedListener( ContentsChangedListener inListener ) {
        mContentController.addContentsChangedListener( inListener );
    }

    public void addNavigationSelectedItemListener( SelectedItemListener inListener ) {
        mNavigationController.addSelectedItemListener( inListener );
    }

    public void addPlaylist() {
        AddPlaylist.addPlaylist( mLibrary );
    }

    public void addRoot() {
        AddRoot.addRoot( mLibrary );
    }

    public void clearContentViewSelection() {
        mContentController.clearSelection();
    }

    public void copy( boolean inCopyFromNavigationView ) {
        mClipboard.setContent( inCopyFromNavigationView ?
                getSelectedNavigationItemAsCollection() : getSelectedContentItems() );
    }

    public void drop( List< ? extends LibraryItem > inSource, LibraryItem inTarget ) {
        if ( ! isValidPasteTarget( inSource, inTarget ) ) {
            return;
        }
        if ( inTarget.isPlaylist() ) {
            dropToPlaylist( inSource, inTarget );
        } else {
            dropToNavigationItem( inSource, inTarget );
        }
    }

    public void editTracks( EditTrackController.ControllerType inType ) {
        EditTrack.editTrack( this, inType );
    }

    public Clipboard getClipboard() { return mClipboard; }

    public List< LibraryItem > getContentItems() {
        return mContentController.getItems();
    }

    public Clipboard getDragboard() { return mDragboard; }

    public Library getLibrary() { return mLibrary; }

    public int getSelectedContentIndex() {
        return mContentController.getSelectedIndex();
    }

    public List< LibraryItem > getSelectedContentItems() {
        return mContentController.getSelectedItems();
    }

    public LibraryItem getSelectedNavigationItem() {
        return mNavigationController.getSelectedItem();
    }

    @Override
    public void initialize( URL inUrl, ResourceBundle inResourceBundle ) {}

    public boolean isValidPasteTarget( List< ? extends LibraryItem > inSources, LibraryItem inTarget ) {
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

    public void onContentViewMouseClicked( MouseEvent inEvent ) {
        mNavigationViewIsSelected = false;
        mNavigationController.cancelEdit();
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
                    bDisableRemove = ! getSelectedNavigationItem().isPlaylist();
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

    public void onNavigationViewMouseClicked( MouseEvent inEvent ) {
        mNavigationViewIsSelected = true;
        mContentController.cancelEdit();
    }

    public void paste() {
        drop( mClipboard.getContent(), getSelectedNavigationItem() );
    }

    public void refresh() {
        LibraryItem theItem = getSelectedNavigationItem();
        if ( theItem == null ) {
            return;
        }
        mLibrary.refresh( theItem.getRootId() );
    }

    public void remove() {
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
                theHeader = String.format( sAboutToDeleteTrack, getSelectedNavigationItem().getTitle() );
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
                    Long thePlaylistId = getSelectedNavigationItem().getId();
                    for ( LibraryItem theTrack : getSelectedContentItems() ) {
                        mLibrary.removeTrackFromPlaylist( theTrack.getId(), thePlaylistId );
                    }
                    break;
            }
        }
    }

    public void selectContentItem( LibraryItem inItem ) {
        mContentController.selectItem( inItem );
    }

    public void updatePlaylist( LibraryItem inPlaylist ) {
        mLibrary.updatePlaylist( inPlaylist );
        mLibrary.refresh( inPlaylist.getRootId() );
    }

    public void updateRoot( LibraryItem inRoot ) {
        mLibrary.updateRoot( inRoot );
        mLibrary.refresh( inRoot.getRootId() );
    }

    public void updateTrack( LibraryItem inTrack ) {
        Map< Long, LibraryItem > theTracks = new HashMap<>();
        theTracks.put( inTrack.getId(), inTrack );
        updateTracks( theTracks );
    }

    public void updateTracks( Map< Long, LibraryItem > inTracks ) {
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

    LibraryItem getSelectedItemFromFocusedView() {
        List< LibraryItem > theSelectedItems = getSelectedItemsFromFocusedView();
        return ! theSelectedItems.isEmpty() ? theSelectedItems.get( 0 ) : null;
    }

    List< LibraryItem > getSelectedItemsFromFocusedView() {
        return mNavigationViewIsSelected ? getSelectedNavigationItemAsCollection() : getSelectedContentItems();
    }

    List< LibraryItem > getSelectedNavigationItemAsCollection() {
        LinkedList< LibraryItem > theSelectedItems = new LinkedList<>();
        LibraryItem theSelectedItem = getSelectedNavigationItem();
        theSelectedItems.add( theSelectedItem );
        return theSelectedItems;
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
        mNavigationController = UIFactory.getNavigationController( this );
        mContentController = UIFactory.getContentController( this );
        splitPane.getItems().setAll( mNavigationController.getView(), mContentController.getView() );
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

    void removeContentsChangedListener( ContentsChangedListener inListener ) {
        mContentController.removeContentsChangedListener( inListener );
    }

    void removeNavigationSelectedItemListener( SelectedItemListener inListener ) {
        mNavigationController.removeSelectedItemListener( inListener );
    }

    private void dropToNavigationItem( List< ? extends LibraryItem > inSource, LibraryItem inNavigationItem ) {
        LibraryItem theSourceItem = inSource.get( 0 );
        String theArtistName = null;
        String theAlbumName = null;
        List< ? extends LibraryItem > theTracks;
        switch( theSourceItem.getType() ) {
            case track:
                theTracks = inSource;
                if ( inNavigationItem.isArtist() ) {
                    theArtistName = inNavigationItem.getTitle();
                } else if ( inNavigationItem.isAlbum() ) {
                    theArtistName = mLibrary.getItem( inNavigationItem.getParentId() ).getTitle();
                    theAlbumName = inNavigationItem.getTitle();
                }
                break;
            case album:
                theArtistName = inNavigationItem.isArtist() ? inNavigationItem.getTitle() : null;
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
        if ( theSourceItem.getRootId().equals( inNavigationItem.getRootId() ) ) {
            updateTracks( theItems );
        } else {
            importTracks( theItems, inNavigationItem.getRootId() );
        }
    }

    private void dropToPlaylist( List< ? extends LibraryItem > inTracks, LibraryItem inPlaylist ) {
        Long thePlaylistId = inPlaylist.getId();
        for ( LibraryItem theTrack : inTracks ) {
            mLibrary.addTrackToPlaylist( theTrack.getId(), thePlaylistId );
        }
    }

    @FXML private void onMenuItemPressed( ActionEvent inEvent ) {
        Object theSource = inEvent.getSource();
        if ( menuItemFileNewRoot.equals( theSource ) ) {
            addRoot();
        } else if ( menuItemFileNewPlaylist.equals( theSource ) ) {
            addPlaylist();
        } else if ( menuItemFileRefresh.equals( theSource ) ) {
            refresh();
        } else if ( menuItemFileBrokenTracks.equals( theSource ) ) {
            editTracks( jmusic.ui.edittrack.EditTrackController.ControllerType.Broken );
        } else if ( menuItemFileUnknownTracks.equals( theSource ) ) {
            editTracks( EditTrackController.ControllerType.Unknown );
        } else if ( menuItemFileSettings.equals( theSource ) ) {
            settings();
        } else if ( menuItemFileClose.equals( theSource ) ) {
            System.exit( 0 );
        } else if ( menuItemEditEdit.equals( theSource ) ) {
            editTracks( jmusic.ui.edittrack.EditTrackController.ControllerType.Edit );
        } else if ( menuItemEditCopy.equals( theSource ) ) {
            copy( mNavigationViewIsSelected );
        } else if ( menuItemEditPaste.equals( theSource ) ) {
            paste();
        } else if ( menuItemEditRemove.equals( theSource ) ) {
            remove();
        }
    }

    private void settings() {
        Settings.settings( this );
    }
}