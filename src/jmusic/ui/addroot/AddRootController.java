package jmusic.ui.addroot;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class AddRootController implements Initializable {
    private Stage mStage;
    @FXML private TextField nameTextField;
    @FXML private TextField locationTextField;
    @FXML private Button okButton;
    private boolean mCanceled = false;

    @Override
    public void initialize( URL inLocation, ResourceBundle inResources ) {
    }

    public String getName() { return nameTextField.getText().trim(); }

    public String getLocation() {
        return new File( locationTextField.getText().trim() ).toURI().toString();
    }

    public boolean isCanceled() { return mCanceled; }

    public void setStage( Stage inStage ) { mStage = inStage; }

    @FXML
    private void handleBrowseButtonAction( ActionEvent inEvent ) {
        File theFile = new DirectoryChooser().showDialog( mStage.getOwner() );
        if ( theFile != null ) {
            locationTextField.setText( theFile.toString() );
        }
        toggleOKButton();
    }

    @FXML
    private void handleCancelButtonAction( ActionEvent inEvent ) {
        mCanceled = true;
        mStage.close();
    }

    @FXML
    private void handleOKButtonAction( ActionEvent inEvent ) {
        mStage.close();
    }

    @FXML
    private void handleTextInput( KeyEvent inEvent ) {
        toggleOKButton();
    }

    private void toggleOKButton() {
        okButton.setDisable( nameTextField.getText().trim().length() == 0 ||
                             locationTextField.getText().trim().length() == 0);
    }
}