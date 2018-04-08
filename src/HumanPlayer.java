import java.awt.Dimension;

/**
 * A human player that takes user input and makes the move.
 *
 * @author Steven Lowes
 */
public class HumanPlayer implements PlayerInterface{

    /**
     * The pixel size of the display when it is created
     * <p>
     * Default: 800x800
     */
    public static Dimension screenSize;

    /**
     * The graphical display
     */
    private static GUIFrame display;

    /**
     * Colour of player, either Piece.RED or Piece.BLUE.
     */
    private Piece colour;


    /**
     * Create a new HumanPlayer
     */
    public HumanPlayer(){
        screenSize = new Dimension(800, 800);
    }

    /**
     * Create a new display with custom screen size
     *
     * @param width The width in pixels of the display
     * @param height The height in pixels of the display
     */
    public HumanPlayer(int width, int height){
        screenSize = new Dimension(width, height);
    }

    /**
     * Asks the player to make a move, and returns it once made
     *
     * @param boardView the current state of the board
     *
     * @return The move that the human player wishes to make. Contains either coordinates or a concession.
     *
     * @throws NoValidMovesException There's nowhere on the board left to go - no locations are equal to Piece.UNSET
     */
    public MoveInterface makeMove(Piece[][] boardView) throws NoValidMovesException{
        //Check that there are valid moves
        int i = 0;
        int j;
        boolean found = false;
        while(i < boardView.length && !found){
            j = 0;
            while(j < boardView[0].length && !found){
                if(boardView[i][j] == Piece.UNSET){
                    found = true;
                }
                j++;
            }
            i++;
        }
        if(!found){
            throw new NoValidMovesException();
        }

        if(display == null){
            //initialise the display if it hasn't yet been initialised
            display = new GUIFrame(boardView, screenSize);
        }
        MoveInterface move = null;
        try{
            //trigger the display to ask for a move
            move = display.getMove(boardView, colour);
        }
        catch(InvalidColourException e){
            e.printStackTrace();
        }

        if(!move.hasConceded()){
            //Update the display with the new move
            boardView[move.getXPosition()][move.getYPosition()] = colour;
            updateBoard(boardView);
        }
        return move;
    }

    /**
     * Ask the display to redraw the board so as to represent the new board view
     *
     * @param boardView The new board to be represented
     *
     * @return boolean indicating successful
     */
    private boolean updateBoard(Piece[][] boardView){
        if(display == null){
            //Initialise the board if it is not already
            display = new GUIFrame(boardView, screenSize);
        }
        else{
            //Update the board
            display.updateBoard(boardView);
        }
        return true;
    }

    /**
     * Inform the user of the final game state.
     *
     * @param state either WON or LOST.
     *
     * @return boolean indicating successful completion.
     */
    public boolean finalGameState(GameState state){
        //Ask the display to inform the user of the winner
        if(state == GameState.WON){
            try{
                display.updateWinner(colour);
            }
            catch(InvalidColourException e){
                e.printStackTrace();
            }
        }
        else if(state == GameState.LOST){
            Piece otherColour;
            if(colour == Piece.RED){
                otherColour = Piece.BLUE;
            }
            else if(colour == Piece.BLUE){
                otherColour = Piece.RED;
            }
            else{
                return false;
            }
            try{
                display.updateWinner(otherColour);
            }
            catch(InvalidColourException e){
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Update the colour of the player.
     *
     * @param colour A Piece (RED/BLUE) that this player will be.
     *
     * @return boolean indicating successful operation.
     *
     * @throws InvalidColourException New colour is neither Piece.RED or Piece.BLUE.
     * @throws ColourAlreadySetException Player already has a colour defined.
     */
    public boolean setColour(Piece colour) throws InvalidColourException, ColourAlreadySetException{
        if(colour == null || colour == Piece.UNSET){
            throw new InvalidColourException();
        }

        if(this.colour != null){
            throw new ColourAlreadySetException();
        }

        this.colour = colour;
        return true;
    }

    /**
     * @return Colour of player as Piece value.
     */
    public Piece getColour(){
        return colour;
    }
}
