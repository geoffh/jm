package jmusic.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddPlaylistController implements Initializable {
    private Stage mStage;

    @FXML
    private TextField nameTextField;
    @FXML private Button okButton;
    private boolean mCanceled = false;

    @Override
    public void initialize( URL inLocation, ResourceBundle inResources ) {
    }

    String getName() { return nameTextField.getText().trim(); }

    boolean isCanceled() { return mCanceled; }

    void setStage( Stage inStage ) { mStage = inStage; }

    @FXML private void handleCancelButtonAction( ActionEvent inEvent ) {
        mCanceled = true;
        mStage.close();
    }

    @FXML private void handleOKButtonAction( ActionEvent inEvent ) {
        mStage.close();
    }

    @FXML private void handleTextInput( KeyEvent inEvent ) {
        toggleOKButton();
    }

    private void toggleOKButton() {
        okButton.setDisable( nameTextField.getText().trim().length() == 0 );
    }
}