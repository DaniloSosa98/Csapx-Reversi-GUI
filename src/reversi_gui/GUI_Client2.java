package reversi_gui;

import javafx.application.Application;

import java.util.*;

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

    /**
     * Create the board model, create the network connection based on
     * command line parameters, and use the first message received to
     * allocate the board size the server is also using.
     */
    public void init() {
        // Get host info from command line
        List<String> args = getParameters().getRaw();

        // get host info and username from command line
        String host = args.get(0);
        int port = Integer.parseInt(args.get(1));
    }

    public void start( Stage mainStage ) {
        mainStage.show();

        // start the network listener as the last thing
        this.serverConn.startListener();
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
