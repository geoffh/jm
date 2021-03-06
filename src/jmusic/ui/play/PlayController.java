package jmusic.ui.play;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;
import jmusic.device.MediaDeviceDiscoveryListener;
import jmusic.device.MediaDeviceManager;
import jmusic.device.MediaRendererDevice;
import jmusic.device.MediaRendererDeviceRemoteControl;
import jmusic.library.LibraryItem;
import jmusic.ui.ContentsChangedListener;
import jmusic.ui.JMusicController;
import jmusic.ui.SelectedItemListener;
import jmusic.ui.util.FontAwesome;
import jmusic.util.*;

import java.util.List;

public class PlayController implements MediaDeviceDiscoveryListener, ProgressListener, SelectedItemListener {
    private final JMusicController mMainController;
    private final PlayControls mControls;
    private final ObservableList mMediaRenderers;
    private final SingleSelectionModel< MediaRendererDevice > mMediaRenderersSelection;
    private final SimpleDoubleProperty mProgress = new SimpleDoubleProperty();
    private MediaRendererDeviceRemoteControl mRemoteControl;
    private PlaySelection mPlaySelection;
    private LibraryItem mSelectedContainer;
    private final Object mLock = new Object();

    public PlayController( JMusicController inMainController, PlayControls inControls ) {
        mMainController = inMainController;
        mMainController.addNavigationSelectedItemListener( this );
        mControls = inControls;
        initControls();
        mMediaRenderers = inControls.getRendererComboBox().getItems();
        mMediaRenderersSelection = inControls.getRendererComboBox().getSelectionModel();
        initDeviceManager();
        onStop();
    }

    @Override
    public void changed( LibraryItem inOldValue, LibraryItem inNewValue ) {
        selectTrackInView();
    }

    public void onMediaRendererDeviceAdded( MediaRendererDevice inMediaRendererDevice ) {
        MediaDeviceManager.instance().createMediaServerDeviceForRendererDevice( inMediaRendererDevice );
        mMediaRenderers.add( inMediaRendererDevice );
        if ( mMediaRenderersSelection.getSelectedItem() == null ) {
            mMediaRenderersSelection.select( inMediaRendererDevice );
        }
    }

    public void onMediaRendererDeviceRemoved( MediaRendererDevice inMediaRendererDevice ) {
        mMediaRenderers.remove( inMediaRendererDevice );
    }

    @Override
    public void onProgress( int inPercent ) {
        setProgress( inPercent );
        if ( inPercent == 100 ) {
            playNextTrack();
        }
    }

    private PlaySelection createPlaySelection() {
        List< LibraryItem > theItems = mMainController.getContentItems();
        List< LibraryItem > theSelectedItems = mMainController.getSelectedContentItems();
        int theFirstIndex = theSelectedItems.isEmpty() ? 0 :  theItems.indexOf( theSelectedItems.get( 0 ) );
        PlaySelection thePlaySelection = new PlaySelection( false );
        for ( int theIndex = theFirstIndex ; theIndex < theItems.size(); theIndex ++ ) {
            thePlaySelection.addItem( theItems.get( theIndex ) );
        }
        return thePlaySelection;
    }

    private void initControls() {
        mControls.getProgressBar().progressProperty().bind( mProgress );
        FontAwesome.setIcon( mControls.getRewindButton(), FontAwesome.sFA_STEP_BACKWARD );
        FontAwesome.setIcon( mControls.getForwardButton(), FontAwesome.sFA_STEP_FORWARD );
        initHandlers();
    }

    private void initDeviceManager() {
        MediaDeviceManager theManager = MediaDeviceManager.instance();
        theManager.init( mMainController.getLibrary() );
        theManager.addListener( this );
        theManager.startDeviceDiscovery();
        theManager.createDefaultMediaRendererDevices();
    }

    private void initHandlers() {
        mControls.getForwardButton().setOnAction( event -> onForward() );
        mControls.getRewindButton().setOnAction( event -> onRewind() );
        mControls.getRendererVolume().valueProperty().addListener( ( inObservable, inOldValue, inNewValue ) -> setVolume( inNewValue.intValue() ) );
        mControls.getRendererVolume().valueProperty().set(
            Integer.parseInt( Config.getInstance().getProperty( ConfigConstants.sPropNameVolumePercent ) ) );
        mMainController.addContentsChangedListener( ( inContentsAdded, inContentsRemoved ) -> {
            if ( mRemoteControl == null ) {
                mControls.disablePlayButton( isTrackListEmpty() );
            }
        } );
    }

    private boolean isTrackListEmpty() {
        return mMainController.getContentItems().isEmpty();
    }

    private String getArtistAlbum( LibraryItem inItem ) {
        return inItem.getArtistName() + " - " + inItem.getAlbumName();
    }

    private void onForward() {
        playNextTrack();
    }

    private void onPlay() {
        new OnPlay().runNow();
    }

    private void onRewind() {
        playPreviousTrack();
    }

    private void onStop() {
        new OnStop().runNow();
    }

    private void playNextTrack() {
        new PlayNextTrack().runNow();
    }

    private void playPreviousTrack() {
        new PlayPreviousTrack().runNow();
    }

    private void selectTrackInView() {
        if ( mPlaySelection != null && mMainController.getSelectedNavigationItem().equals( mSelectedContainer ) ) {
            mMainController.clearContentViewSelection();
            mMainController.selectContentItem( mPlaySelection.current() );
        }
    }

    private void setProgress( int inPercent ) {
        new SetProgress( inPercent ).runNow();
    }

    private void setVolume( int inVolume ) {
        Config.getInstance().setProperty( ConfigConstants.sPropNameVolumePercent, String.valueOf( inVolume ) );
        if ( mRemoteControl != null ) {
            mRemoteControl.setVolume( inVolume );
        }
    }

    class OnPlay extends Runner {
        @Override
        public void doWork() {
            mPlaySelection = createPlaySelection();
            if ( mPlaySelection == null ) {
                return;
            }
            mSelectedContainer = mMainController.getSelectedNavigationItem();
            mRemoteControl = MediaDeviceManager.instance().createRemoteControl( mMediaRenderersSelection.getSelectedItem() );
            playNextTrack();
            mControls.enableRewindButton();
            FontAwesome.setIcon( mControls.getPlayButton(), FontAwesome.sFA_STOP );
            mControls.getPlayButton().setOnAction( event -> onStop() );
            mControls.enablePlayButton();
            mControls.enableForwardButton();
            mControls.disableRendererComboBox();
        }
    }

    class OnStop extends Runner {
        @Override
        public void doWork() {
            if ( mRemoteControl != null ) {
                mRemoteControl.removeProgressListener( PlayController.this );
                mRemoteControl.stop();
            }
            mControls.disableRewindButton();
            FontAwesome.setIcon( mControls.getPlayButton(), FontAwesome.sFA_PLAY );
            mControls.getPlayButton().setOnAction( event -> onPlay() );
            mControls.enablePlayButton( !isTrackListEmpty() );
            mControls.disableForwardButton();
            mControls.setTrack( "" );
            mControls.setArtistAlbum( "" );
            setProgress( 0 );
            mControls.enableRendererComboBox();
            mRemoteControl = null;
            mPlaySelection = null;
            mSelectedContainer = null;
        }
    }

    class PlayNextTrack extends PlayTrack {
        @Override
        public void doWork() {
            LibraryItem theItem = mPlaySelection.next();
            if ( theItem != null ) {
                playTrack( theItem );
            } else {
                onStop();
            }
        }
    }

    class PlayPreviousTrack extends PlayTrack {
        @Override
        public void doWork() {
            LibraryItem theItem = mPlaySelection.previous();
            if ( theItem != null ) {
                playTrack( theItem );
            }
        }
    }

    class SetProgress extends Runner {
        private final int mPercent;

        SetProgress( int inPercent ) {
            mPercent = inPercent;
        }

        @Override
        public void doWork() {
            mProgress.set( ( double ) mPercent / 100 );
            if ( mPlaySelection != null ) {
                LibraryItem theItem = mPlaySelection.current();
                int theDuration = theItem.getDuration();
                int theElapsed = theDuration * mPercent / 100;
                int theRemainder = theDuration - theElapsed;
                mControls.setPlayTimeElapsed( secondsToString( theElapsed ) );
                mControls.setPlayTimeRemaining( secondsToString( theRemainder ) );
            } else {
                mControls.setPlayTimeElapsed( "" );
                mControls.setPlayTimeRemaining( "" );
            }
        }

        String secondsToString( int inSeconds ) {
            int theMinutes = inSeconds / 60;
            int theSeconds = inSeconds % 60;
            return theMinutes + ":" + ( theSeconds < 10 ? "0" : "" ) +  theSeconds;
        }
    }

    abstract class PlayTrack extends Runner {
        protected void playTrack( LibraryItem inItem ) {
            mRemoteControl.removeProgressListener( PlayController.this );
            mRemoteControl.play( inItem );
            mRemoteControl.addProgressListener( PlayController.this );
            mRemoteControl.setVolume( ( int ) mControls.getRendererVolume().getValue() );
            mControls.setTrack( inItem.getTitle() );
            mControls.setArtistAlbum( getArtistAlbum( inItem ) );
            selectTrackInView();
        }
    }

    abstract class Runner implements Runnable {
        private final Object mRunnerLock = new Object();

        public void run() {
            synchronized( mLock ) {
                synchronized( mRunnerLock ) {
                    mRunnerLock.notify();
                }
                doWork();
            }
        }

        abstract void doWork();

        void runNow() {
            synchronized( mRunnerLock ) {
                if ( Platform.isFxApplicationThread() ) {
                    run();
                } else {
                    Platform.runLater( this );
                    try {
                        mRunnerLock.wait();
                    } catch ( InterruptedException theException ) {
                        theException.printStackTrace();
                    }
                }
            }
        }
    }
}