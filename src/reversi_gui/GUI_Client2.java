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
import reversi_ptui.ConsoleApplication;

/**
 * This application is the UI for Reversi.
 *
 * @author YOUR NAME HERE
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
        for (int y = 0; y < dimension; y++) {
            for(int x=0; x < dimension; x++){
                Button b = new Button();
                if( (x == (dimension/2)-1 || y == (dimension/2)-1) && (x == dimension/2 || y == dimension/2) ){
                    b.setGraphic(new ImageView(black));
                }else if( (x == dimension/2 || y == (dimension/2)-1) && (x == (dimension/2)-1 || y == dimension/2) ){
                    b.setGraphic(new ImageView(white));
                }else{
                    b.setGraphic(new ImageView(empty));
                    int finalX = x;
                    int finalY = y;
                    b.setOnAction((ActionEvent)->{
                        if(this.model.isMyTurn()){
                            if (this.model.isValidMove(finalY, finalX)) {
                                //this.refresh(finalY, finalX);
                                this.serverConn.sendMove(finalY, finalX);
                                b.setGraphic(new ImageView(black));
                                l.setText("Move: " + finalY + " " + finalX);
                            }
                        }else{
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

    /*@Override
    public void go(Scanner userIn, PrintWriter userOut) {
        this.userIn = userIn;
        this.userOut = userOut;

        // Connect UI to model. Can't do it sooner because streams not set up.
        this.model.addObserver( this );

        // Start the network listener thread
        this.serverConn.startListener();

        // Manually force a display of all board state, since it's too late
        // to trigger update().
        this.refresh();
        while ( this.model.getStatus() == Board.Status.NOT_OVER ) {
            try {
                this.wait();
            }
            catch( InterruptedException ie ) {}
        }
    }*/

    @Override
    public void stop() {
        this.userIn.close();
        this.userOut.close();
        this.serverConn.close();
    }

    private synchronized void endGame() {
        this.notify();
    }

    private void refresh(int row, int col) {
        if ( !this.model.isMyTurn() ) {
            this.userOut.println( this.model );
            this.userOut.println( this.model.getMovesLeft() + " moves left." );

            Board.Status status = this.model.getStatus();
            switch ( status ) {
                case ERROR:
                    this.userOut.println( status );
                    this.endGame();
                    break;
                case I_WON:
                    this.userOut.println( "You won. Yay!" );
                    this.endGame();
                    break;
                case I_LOST:
                    this.userOut.println( "You lost. Boo!" );
                    this.endGame();
                    break;
                case TIE:
                    this.userOut.println( "Tie game. Meh." );
                    this.endGame();
                    break;
                default:
                    this.userOut.println();
            }
        }
        else {
            boolean done = false;
            do {
                this.userOut.print("type move as row◻︎column: ");
                this.userOut.flush();
                if (this.model.isValidMove(row, col)) {
                    this.userOut.println(row + " " + col);
                    this.serverConn.sendMove(row, col);
                    done = true;
                }
            } while (!done);
        }
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
