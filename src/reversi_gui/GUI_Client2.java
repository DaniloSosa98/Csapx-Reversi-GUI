package reversi_gui;

import javafx.application.Application;

import java.util.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import reversi.ReversiException;
import reversi2.Board;
import reversi2.NetworkClient;
import reversi2.Observer;

/**
 * This application is the UI for Reversi.
 *
 * @author YOUR NAME HERE
 */
public class GUI_Client2 extends Application /* implements Observer<Board> */ {

    /**
     * Connection to network interface to server
     */
    private NetworkClient serverConn;

    Board model;

    /**
     * Create the board model, create the network connection based on
     * command line parameters, and use the first message received to
     * allocate the board size the server is also using.
     */
    public void init() throws ReversiException {

        try {
            List< String > args = getParameters().getRaw();

            // Get host info from command line
            String host = args.get( 0 );
            int port = Integer.parseInt( args.get( 1 ) );

            // Create uninitialized board.
            this.model = new Board();
            // Create the network connection.
            this.serverConn = new NetworkClient( host, port, this.model );

            this.model.initializeGame();
        }
        catch( ReversiException |
                ArrayIndexOutOfBoundsException |
                NumberFormatException e ) {
            System.out.println( e );
            throw new RuntimeException( e );
        }
    }

    public void start( Stage mainStage ) {
        Button b = new Button();
        b.setText("Button");
        Label l = new Label("Bottom Label");
        BorderPane bp = new BorderPane();
        bp.setBottom(l);
        bp.setCenter(b);
        bp.setPrefSize(400, 400);
        Scene scene = new Scene(bp);
        mainStage.setScene(scene);
        mainStage.setTitle("Reversi");
        mainStage.show();
        // start the network listener as the last thing
        //this.serverConn.startListener();
    }

    /**
     * Launch the JavaFX GUI.
     *
     * @param args not used, here, but named arguments are passed to the GUI.
     *             <code>--host=<i>hostname</i> --port=<i>portnum</i></code>
     */
    public static void main( String[] args ) {
        if (args.length != 2) {
            System.out.println("Usage: java GUI_Client2 host port");
            System.exit(0);
        } else {
            Application.launch(args);
        }
    }

}
