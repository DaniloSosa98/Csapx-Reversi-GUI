package reversi_gui;

import javafx.application.Application;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import reversi.ReversiException;
import reversi2.Board;
import reversi2.NetworkClient;
import reversi2.Observer;

/**
 * This application is the UI for Reversi.
 *
 * @author Danilo Sosa
 */
public class GUI_Client2 extends Application implements Observer<Board>  {
    Stage stage;
    /**
     * Connection to network interface to server
     */
    private NetworkClient serverConn;

    Board model;

    /**
     * What to read to see what user types
     */
    private Scanner userIn;

    /**
     * Where to send text that the user can see
     */
    private PrintWriter userOut;

    Image empty = new Image(getClass().getResourceAsStream("empty.jpg"));
    Image white = new Image(getClass().getResourceAsStream("othelloP1.jpg"));
    Image black = new Image(getClass().getResourceAsStream("othelloP2.jpg"));

    /**
     * Create the board model, create the network connection based on
     * command line parameters, and use the first message received to
     * allocate the board size the server is also using.
     */
    public void init() {

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

    @Override
    public synchronized void start(Stage mainStage) throws Exception {
        //HBox and Labels for player notifications
        HBox hb = new HBox();
        Label l = new Label("Bottom Label");
        Label l2 = new Label("Bottom Label2");
        Label l3 = new Label("Bottom Label3");
        hb.setSpacing(10);
        hb.getChildren().addAll(l, l2, l3);
        BorderPane bp = new BorderPane();
        bp.setBottom(hb);

        GridPane gp = new GridPane();

        int dimension = this.model.getDIM();
        //Add all the bottuns to the gridpane
        for (int y = 0; y < dimension; y++) {
            for(int x=0; x < dimension; x++){
                Button b = new Button();
                //Set black initial pieces
                if( (x == (dimension/2)-1 || y == (dimension/2)-1) && (x == dimension/2 || y == dimension/2) ){
                    b.setGraphic(new ImageView(black));
                //set white initial pieces
                }else if( (x == dimension/2 || y == (dimension/2)-1) && (x == (dimension/2)-1 || y == dimension/2) ){
                    b.setGraphic(new ImageView(white));
                //after initial pieces are done, add empty buttons
                }else{
                    b.setGraphic(new ImageView(empty));
                    int finalX = x;
                    int finalY = y;
                    //set action with similar work as "refresh"
                    b.setOnAction((ActionEvent)->{
                        //check if is gui turn
                        if(this.model.isMyTurn()){
                            //validate move
                            if (this.model.isValidMove(finalY, finalX)) {
                                this.serverConn.sendMove(finalY, finalX);
                                b.setGraphic(new ImageView(black));
                                //show move made in label 1
                                l.setText("Move: " + finalY + " " + finalX);
                            }
                        }else{
                            //if is not gui turn show message
                            l.setText("Not your turn");
                            this.userOut.println( this.model );
                            this.userOut.println( this.model.getMovesLeft() + " moves left." );
                            l2.setText(this.model.getMovesLeft() + " moves left.");
                            Board.Status status = this.model.getStatus();
                            switch ( status ) {
                                case ERROR:
                                    this.userOut.println( status );
                                    l3.setText( status.toString() );
                                    this.endGame();
                                    break;
                                case I_WON:
                                    this.userOut.println( "You won. Yay!" );
                                    l3.setText("You won. Yay!");
                                    this.endGame();
                                    break;
                                case I_LOST:
                                    this.userOut.println( "You lost. Boo!" );
                                    l3.setText("You lost. Boo!");
                                    this.endGame();
                                    break;
                                case TIE:
                                    this.userOut.println( "Tie game. Meh." );
                                    l3.setText("Tie game. Meh.");
                                    this.endGame();
                                    break;
                                default:
                                    this.userOut.println();
                            }
                        }

                    });
                }

                gp.add(b, x, y);
            }
        }

        bp.setCenter(gp);

        Scene scene = new Scene(bp);
        mainStage.setScene(scene);
        mainStage.setTitle("Reversi");
        this.stage = mainStage;
        stage.show();
        // start the network listener as the last thing
        this.serverConn.startListener();

    }
    @Override
    public void stop() {
        this.userIn.close();
        this.userOut.close();
        this.serverConn.close();
    }

    private synchronized void endGame() {
        this.notify();
    }

    @Override
    public void update(Board board) {
        //this.refresh();
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
