package jmusic.ui;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import jmusic.device.MediaRendererDevice;

public class PlayControls {
    private Button mRewindButton;
    private Button mPlayButton;
    private Button mForwardButton;
    private Label mArtistAlbumLabel;
    private Label mTrackLabel;
    private Label mPlayTimeElapsed;
    private Label mPlayTimeRemaining;
    private ProgressBar mProgressBar;
    private ComboBox< MediaRendererDevice > mRendererComboBox;
    private Slider mRendererVolume;

    public void disableForwardButton() {
        mForwardButton.setDisable( true );
    }

    public void disablePlayButton() {
        disablePlayButton( true );
    }

    public void disablePlayButton( boolean inDisable ) {
        mPlayButton.setDisable( inDisable );
    }

    public void disableRewindButton() {
        mRewindButton.setDisable( true );
    }

    public void disableRendererComboBox() {
        mRendererComboBox.setDisable( true );
    }

    public void enableForwardButton() {
        mForwardButton.setDisable( false );
    }

    public void enablePlayButton() {
        enablePlayButton( true );
    }

    public void enablePlayButton( boolean inEnable ) {
        mPlayButton.setDisable( ! inEnable );
    }

    public void enableRewindButton() {
        mRewindButton.setDisable( false );
    }

    public void enableRendererComboBox() {
        mRendererComboBox.setDisable( false );
    }

    public Label getArtistAlbumLabel() {
        return mArtistAlbumLabel;
    }

    public Button getForwardButton() {
        return mForwardButton;
    }

    public Button getPlayButton() {
        return mPlayButton;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public ComboBox getRendererComboBox() {
        return mRendererComboBox;
    }

    public Slider getRendererVolume() { return mRendererVolume; }

    public Button getRewindButton() {
        return mRewindButton;
    }

    public Label getTrackLabel() {
        return mTrackLabel;
    }

    public Label getPlayTimeElapsedLabel() { return mPlayTimeElapsed; }

    public Label getPlayTimeRemainingLabel() { return mPlayTimeRemaining; }

    public void setArtistAlbum( String inArtist ) {
        mArtistAlbumLabel.setText( inArtist );
    }

    public void setTrack( String inTrack ) {
        mTrackLabel.setText( inTrack );
    }

    public void setPlayTimeElapsedLabel( Label inPlayTimeElapsedLabel ) {
        mPlayTimeElapsed = inPlayTimeElapsedLabel;
    }

    public void setPlayTimeRemainingLabel( Label inPlayTimeRemainingLabel ) {
        mPlayTimeRemaining = inPlayTimeRemainingLabel;
    }

    public void setPlayTimeElapsed( String inPlayTimeElapsed ) {
        mPlayTimeElapsed.setText( inPlayTimeElapsed );
    }

    public void setPlayTimeRemaining( String inPlayTimeRemaining ) {
        mPlayTimeRemaining.setText( inPlayTimeRemaining );
    }

    public void setArtistAlbumLabel( Label mArtistLabel ) {
        this.mArtistAlbumLabel = mArtistLabel;
    }

    public void setForwardButton( Button forwardButton ) {
        this.mForwardButton = forwardButton;
    }

    public void setProgressBar( ProgressBar inProgressBar ) {
        mProgressBar = inProgressBar;
    }

    public void setRewindButton( Button rewindButton ) {
        this.mRewindButton = rewindButton;
    }

    public void setPlayButton( Button playButton ) {
        this.mPlayButton = playButton;
    }

    public void setRendererComboBox( ComboBox rendererComboBox ) {
        this.mRendererComboBox = rendererComboBox;
    }

    public void setRendererVolume( Slider inRendererVolume ) { this.mRendererVolume = inRendererVolume; }

    public void setTrackLabel( Label mTrackLabel ) {
        this.mTrackLabel = mTrackLabel;
    }
}