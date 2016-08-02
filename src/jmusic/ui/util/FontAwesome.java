package jmusic.ui.util;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.text.Font;

public class FontAwesome {
    public static final String sFA_ARROW_DOWN    = '\uf063' + "";
    public static final String sFA_ARROW_UP      = '\uf062' + "";
    public static final String sFA_STEP_BACKWARD = '\uf048' + "";
    public static final String sFA_STEP_FORWARD  = '\uf051' + "";
    public static final String sFA_PLAY          = '\uf04b' + "";
    public static final String sFA_STOP          = '\uf04d' + "";

    public static final double sButtonSize = 35;

    public static final Font sFont = Font.font( "FontAwesome" );

    public static Button createFontAwesomeButton( String inIcon ) {
        Button theButton = new Button();
        setIcon( theButton, inIcon );
        return theButton;
    }

    public static Button createFontAwesomeButton( String inText, double inSize ) {
        Button theButton = createFontAwesomeButton( inText );
        theButton.setMinSize( inSize, inSize );
        theButton.setPrefSize( inSize, inSize );
        return theButton;
    }

    public static Font getFont() { return sFont; }

    public static void setIcon( Labeled inLabeled, String inIcon ) {
        inLabeled.setText( inIcon );
        inLabeled.setFont( FontAwesome.getFont() );
    }
}
