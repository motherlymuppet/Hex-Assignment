import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GUIFrame extends JFrame{
    /**
     * The colour in which to draw the background
     */
    private static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY;

    /**
     * The font to use for labels
     */
    private static final Font FONT = new Font("Helvetica", Font.BOLD, 20);

    /**
     * An object to be used for thread synchronisation, so that the getMove method can wait until a move is made on the mouseLister thread
     */
    private final Object lock = new Object();

    /**
     * The panel which contains the three other panels
     */
    private JPanel mainPanel;

    /**
     * The layout manager for the main panel
     */
    private BorderLayout mainPanelLayout;

    /**
     * The panel which contains the hexagon board
     */
    private GUIPanel centerPanel;

    /**
     * The panel which informs the user of whose turn it is, in addition to the winner of the game
     */
    private JPanel northPanel;

    /**
     * The panel which contains the make move and concede buttons
     */
    private JPanel southPanel;

    /**
     * The label informing the player of whose turn it is, and whether anyone has won
     */
    private JLabel turn;

    /**
     * The label for the buttons at the bottom of the screen
     */
    private JLabel moveOptions;

    /**
     * The button used to concede
     */
    private JButton concede;

    /**
     * The button used to make the move
     */
    private JButton makeMove;

    /**
     * The mouse listener, running in its own thread
     */
    private MouseListenerThread mouseListenerThread;

    /**
     * Indicates whether or not the board is ready for a move to be made
     */
    private boolean mouseReady;

    /**
     * Holds the most recently selected hex on the board, between the mouseListenerThread and this object.
     */
    private Hex lastClickedHex;

    /**
     * Holds whether the user has done an action that would result in the move being made when getMove is called.
     */
    private boolean makeMoveNow;

    /**
     * Indicates whether the user wishes to concede.
     */
    private boolean hasConceded;

    /**
     * Indicates which player has won the game.
     */
    private boolean gameWon;

    public GUIFrame(Piece[][] boardView, Dimension size){
        //Create the main panel
        setSize(size);
        setTitle("Hex");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel = new JPanel();
        mainPanelLayout = new BorderLayout();
        mainPanel.setLayout(mainPanelLayout);
        mainPanel.setBackground(BACKGROUND_COLOR);

        //Create the north panel
        northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        turn = new JLabel("");
        turn.setFont(FONT);
        turn.setBackground(Color.WHITE);
        northPanel = new JPanel();
        northPanel.add(turn);
        northPanel.setPreferredSize(new Dimension((int) (size.getWidth()), 30));
        northPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.add(northPanel, BorderLayout.NORTH);

        //Create the south panel
        southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
        makeMove = new JButton("Make Move");
        makeMove.setFont(FONT);
        concede = new JButton("Concede");
        concede.setFont(FONT);
        southPanel.add(makeMove);
        moveOptions = new JLabel("Move Options:");
        moveOptions.setFont(FONT);
        southPanel.add(moveOptions);
        southPanel.add(concede);
        southPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        southPanel.setVisible(true);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        //Create the center panel
        centerPanel = new GUIPanel();
        centerPanel.setVisible(true);
        centerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        //Create the mouse listener and start it running
        mouseListenerThread = new MouseListenerThread();
        mouseListenerThread.start();

        //Add the mouse listeners to the objects that need them: the two buttons and the hex board
        centerPanel.addMouseListener(mouseListenerThread);
        concede.addMouseListener(mouseListenerThread);
        makeMove.addMouseListener(mouseListenerThread);
        //Add a window listener so that the board can be updated and redrawn when it is resized
        addComponentListener(new WindowListenerClass());

        southPanel.validate();
        centerPanel.validate();
        northPanel.validate();
        mainPanel.validate();
        //Add the container panel to the window, then draw the window and the hex board.
        add(mainPanel);
        validate();
        setContentPane(mainPanel);
        setVisible(true);
        updateBoard(boardView);
        repaint();
        //pack();
    }

    public void updateBoard(Piece[][] boardView){
        //Update the hex board
        centerPanel.updateBoard(boardView);
        repaint();
    }

    public MoveInterface getMove(Piece[][] boardView, Piece currentTurn) throws InvalidColourException{
        //Show the turn label, and the concede button
        turn.setVisible(true);
        concede.setVisible(true);
        makeMoveNow = false;
        hasConceded = false;
        mouseReady = true;
        repaint();
        //Show an updated view of the board
        updateBoard(boardView);
        repaint();
        makeMove.setVisible(false);
        if(currentTurn == Piece.RED){
            turn.setText("It's Red's Turn!");
            turn.setForeground(Color.BLACK);
            moveOptions.setForeground(Color.BLACK);
            northPanel.setBackground(Hex.RED_COLOR);
            southPanel.setBackground(Hex.RED_COLOR);
        }
        else if(currentTurn == Piece.BLUE){
            turn.setText("It's Blue's Turn!");
            turn.setForeground(Color.WHITE);
            moveOptions.setForeground(Color.WHITE);
            northPanel.setBackground(Hex.BLUE_COLOR);
            southPanel.setBackground(Hex.BLUE_COLOR);
        }
        else{
            throw new InvalidColourException();
        }

        boolean success = false;
        while(!success){
            //wait until a move is made
            synchronized(lock){
                while(!makeMoveNow){
                    try{
                        lock.wait();
                    }
                    catch(InterruptedException e){
                    }
                }
                repaint();
            }
            if(hasConceded || (lastClickedHex != null && lastClickedHex.getPieceColour() == Piece.UNSET)){
                success = true;
            }
        }
        //make the chosen move
        MoveInterface move = new Move();
        if(hasConceded){
            move.setConceded();
        }
        else{
            Coordinate moveCoords = lastClickedHex.getCoords();
            try{
                move.setPosition(moveCoords.getX(), moveCoords.getY());
            }
            catch(InvalidPositionException e){
                e.printStackTrace();
            }
        }

        //Hide the turn notification, and the buttons
        makeMove.setVisible(false);
        turn.setVisible(false);
        concede.setVisible(false);
        makeMoveNow = false;
        hasConceded = false;
        mouseReady = false;

        turn.setForeground(Color.BLACK);
        northPanel.setBackground(Color.WHITE);
        southPanel.setBackground(Color.WHITE);

        return move;
    }

    /**
     * Notify the user of the winner on the display
     *
     * @param winner the winner of the game
     *
     * @throws InvalidColourException The winner parameter is neither Piece.RED or Piece.BLUE
     */
    public void updateWinner(Piece winner) throws InvalidColourException{
        if(!gameWon){
            gameWon = true;
            int millisToRunFor = 15000;
            int interval = 150;
            if(winner == Piece.RED){
                turn.setVisible(true);
                turn.setText("RED IS THE WINNER!!");
                long startTime = System.currentTimeMillis();
                long finishTime = startTime + millisToRunFor;
                int i = 0;
                while(System.currentTimeMillis() < finishTime){
                    //Flash red and white
                    if(i % 2 == 0){
                        centerPanel.setBackground(Hex.RED_COLOR);
                        northPanel.setBackground(Color.WHITE);
                        southPanel.setBackground(Color.WHITE);
                    }
                    else{
                        centerPanel.setBackground(Color.WHITE);
                        northPanel.setBackground(Hex.RED_COLOR);
                        southPanel.setBackground(Hex.RED_COLOR);

                    }
                    ThreadLocalRandom rand = ThreadLocalRandom.current();
                    //Add text to the center panel
                    centerPanel.informWinner("RED IS THE WINNER!!");
                    i++;
                    try{
                        Thread.currentThread().sleep(interval);
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
                centerPanel.setBackground(Color.WHITE);
                northPanel.setBackground(Hex.RED_COLOR);
                southPanel.setBackground(Hex.RED_COLOR);
            }
            else if(winner == Piece.BLUE){
                turn.setVisible(true);
                turn.setText("BLUE IS THE WINNER!!");
                long startTime = System.currentTimeMillis();
                long finishTime = startTime + millisToRunFor;
                int i = 0;
                while(System.currentTimeMillis() < finishTime){
                    //Flash blue and white
                    if(i % 2 == 0){
                        centerPanel.setBackground(Hex.BLUE_COLOR);
                        northPanel.setBackground(Color.WHITE);
                        southPanel.setBackground(Color.WHITE);
                        turn.setForeground(Color.BLACK);
                    }
                    else{
                        centerPanel.setBackground(Color.WHITE);
                        northPanel.setBackground(Hex.BLUE_COLOR);
                        southPanel.setBackground(Hex.BLUE_COLOR);
                        turn.setForeground(Color.WHITE);
                    }
                    //Add text to the center panel
                    centerPanel.informWinner("BLUE IS THE WINNER!!");
                    i++;
                    try{
                        Thread.currentThread().sleep(interval);
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
                centerPanel.setBackground(Color.WHITE);
                northPanel.setBackground(Hex.BLUE_COLOR);
                southPanel.setBackground(Hex.BLUE_COLOR);
                turn.setForeground(Color.WHITE);
            }
            else{
                throw new InvalidColourException();
            }
            try{
                //Wait 3 secs
                Thread.currentThread().sleep(3000);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
            //Then clear the text on the center panel
            centerPanel.clearWinnerText();
            repaint();
        }
    }

    /**
     * Runs the mouse listener in a different thread
     */
    private class MouseListenerThread extends Thread implements MouseListener{

        public MouseListenerThread(){
        }

        @Override public void mouseClicked(MouseEvent e){

        }

        @Override public void mousePressed(MouseEvent e){
            if(mouseReady){
                if(e.getSource() == centerPanel){
                    //ask the GUIBoard which hex was clicked
                    Point clickPoint = new Point(e.getPoint());
                    lastClickedHex = centerPanel.getBoard().getHex(clickPoint);
                    //Deselect all hexes
                    centerPanel.getBoard().deselect();
                    if(lastClickedHex != null && lastClickedHex.getPieceColour() == Piece.UNSET){
                        //If a valid hex was clicked, select it
                        lastClickedHex.select();
                        makeMove.setVisible(true);
                    }
                    else{
                        makeMove.setVisible(false);
                    }
                }
                else{
                    if(e.getSource() == makeMove){
                        wake();
                    }
                    else if(e.getSource() == concede){
                        hasConceded = true;
                        wake();
                    }
                }
                repaint();
            }
        }

        public void wake(){
            synchronized(lock){
                makeMoveNow = true;
                //Tell the getMove method that it can make a move now
                lock.notifyAll();
            }
        }

        @Override public void mouseReleased(MouseEvent e){

        }

        @Override public void mouseEntered(MouseEvent e){

        }

        @Override public void mouseExited(MouseEvent e){

        }
    }

    private class WindowListenerClass implements ComponentListener{

        @Override public void componentResized(ComponentEvent e){
            centerPanel.updateBoard();
            repaint();
        }

        @Override public void componentMoved(ComponentEvent e){

        }

        @Override public void componentShown(ComponentEvent e){

        }

        @Override public void componentHidden(ComponentEvent e){

        }
    }
}
