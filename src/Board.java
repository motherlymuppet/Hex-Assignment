/**
 * An implementation of a hex board, storing the positions as a 2-dimensional array of Piece values Included win-checking algorithm is implemented as depth-first search designed to
 * be fast in the most common circumstances at the expense of being slower in unusual circumstances. The board is intended to be thrown away and a new one created once a game is
 * completed.
 *
 * @author Steven Lowes
 * @version 1.0 29/03/2016
 */
public class Board implements BoardInterface{
    /**
     * The width of the board in hexes.
     */
    private int sizeX;

    /**
     * The height of the board in hexes.
     */
    private int sizeY;

    /**
     * A 2-dimensional array of Piece values indicating the state of the board. boardArray is initialised to be full of Piece.UNSET as opposed to null in setBoardSize as opposed to
     * the constructor.
     * <p>
     * Default: null
     */
    private Piece[][] boardArray;

    /**
     * The state of the game, Piece.RED for red won, Piece.BLUE for blue won, Piece.UNSET for neither play yet won.
     * <p>
     * Default: Piece.UNSET
     */
    private Piece gameWon;

    /**
     * The Piece (Representing player colour) whose turn it currently is. Should be either Piece.RED or Piece.UNSET. Is initialised as Piece.RED.
     * <p>
     * Default: Piece.RED
     */
    private Piece turn;

    /**
     * Create a new Board object, setting turn to Piece.RED and not initialising boardArray.
     */
    public Board(){
        turn = Piece.RED;
        gameWon = Piece.UNSET;
    }

    /**
     * Sets the board height and width in hexes, also initialising boardArray.
     *
     * @param sizeX how wide the board will be.
     * @param sizeY how tall the board will be.
     *
     * @return boolean indicating successful operation.
     *
     * @throws InvalidBoardSizeException One or both of provided sizes were invalid (less than or equal to 0).
     * @throws BoardAlreadySizedException The size of the board has already been set.
     */
    public boolean setBoardSize(int sizeX, int sizeY) throws InvalidBoardSizeException, BoardAlreadySizedException{
        if(boardArray != null){ //boardArray is default null, non-null indicates it is initialised
            throw new BoardAlreadySizedException();
        }

        if(sizeX > 0 && sizeY > 0){
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            boardArray = new Piece[sizeX][sizeY];
            for(int i = 0; i < sizeX; i++){
                for(int j = 0; j < sizeY; j++){
                    boardArray[i][j] = Piece.UNSET;
                }
            }
        }
        else{
            throw new InvalidBoardSizeException();
        }
        return true;
    }

    /**
     * Returns a deep copy of boardArray.
     * <p>
     * A copy is required as it means that the variables stored in the board object cannot be edited by another class directly. The deep copy is required to ensure that every value
     * stored in the array is copied.
     *
     * @return a deep copy 2D array of Piece indicating current board state.
     *
     * @throws NoBoardDefinedException boardArray has not been initialised - call setBoardSize(int sizeX, int sizeY).
     */
    public Piece[][] getBoardView() throws NoBoardDefinedException{
        if(boardArray == null){
            throw new NoBoardDefinedException();
        }
        return Utility.clone(boardArray);
    }

    /**
     * Attempts to place a piece on the board. Also handles concession and increments turn.
     *
     * @param colour the colour of the player that is making the move.
     * @param move the position the player wishes to place a piece. Also stores concession data.
     *
     * @return boolean indicating successful operation.
     *
     * @throws PositionAlreadyTakenException Player attempts to make a move to a position that is not equal to Piece.Unset.
     * @throws InvalidPositionException Player attempts to make a move to an invalid board position where x or y are less than zero.
     * @throws InvalidColourException The colour parameter is not equal to Piece.RED or Piece.BLUE.
     * @throws NoBoardDefinedException Board has not been defined - can't place piece on undefined board - call setBoardSize(xSize,ySize)
     */
    public boolean placePiece(Piece colour, MoveInterface move) throws PositionAlreadyTakenException, InvalidPositionException, InvalidColourException, NoBoardDefinedException{
        if(turn != colour){
            //a player is attempting to make a move when it is not their turn
            throw new InvalidColourException();
        }

        if(boardArray == null){
            throw new NoBoardDefinedException();
        }

        //check for concession
        if(move.hasConceded()){
            if(colour == Piece.RED){
                gameWon = Piece.BLUE;
                return true;
            }
            else if(colour == Piece.BLUE){
                gameWon = Piece.RED;
                return true;
            }
        }

        //Place the piece on the board
        int xPosition = move.getXPosition();
        int yPosition = move.getYPosition();

        if(xPosition >= sizeX || yPosition >= sizeY || xPosition < 0 || yPosition < 0){
            throw new InvalidPositionException();
        }

        Piece currentPiece = boardArray[xPosition][yPosition];
        if(currentPiece != Piece.UNSET){
            if(currentPiece == null){
                System.out.println("Strange error - it seems like the board hasn't been initialised properly because there is a null value mapped onto the board.");
            }
            throw new PositionAlreadyTakenException();
        }

        if(turn == null || turn == Piece.UNSET){
            System.out.println("Turn value unset in Board.placePiece");
            return false;
        }

        try{
            boardArray[xPosition][yPosition] = colour;
        }
        catch(IndexOutOfBoundsException e){
            //a player is trying to make a move to a position that does not exist
            throw new InvalidPositionException();
        }
        if(!nextTurn()){
            return false;
        }
        return true;
    }

    /**
     * Checks whether red, then blue, has won, using PlayerWon. This uses a depth-first search which should be faster than a breadth-first search in most cases, but in some cases
     * may be slower due to the need to maintain a sorted list.
     *
     * @return Piece indicating winner of game, Piece.RED or Piece.BLUE, or Piece.UNSET if game is not won.
     *
     * @throws NoBoardDefinedException boardArray has not been initialised - call setBoardSize(int sizeX, int sizeY).
     */
    public Piece gameWon() throws NoBoardDefinedException{

        if(boardArray == null){
            throw new NoBoardDefinedException();
        }

        //check for the cached value first
        if(gameWon == Piece.RED || gameWon == Piece.BLUE){
            return gameWon;
        }

        Piece gameWon = null;

        try{
            if(Utility.playerWon(boardArray, Piece.RED)){
                gameWon = Piece.RED;
            }
            else if(Utility.playerWon(boardArray, Piece.BLUE)){
                gameWon = Piece.BLUE;
            }
            else{
                gameWon = Piece.UNSET;
            }
        }
        catch(InvalidColourException e){
            //playerWon throws invalid colour exception
            System.out.println("Strange error - these are hardcoded.");
            e.printStackTrace();
        }
        return gameWon;
    }

    /**
     * Increment turn - Piece.RED becomes Piece.BLUE and vice versa.
     *
     * @return boolean No error has been thrown and execution has been successful
     *
     * @throws InvalidColourException Turn is not set - cannot be incremented
     */
    private boolean nextTurn() throws InvalidColourException{
        boolean success = false;
        if(turn == Piece.RED){
            turn = Piece.BLUE;
            success = true;
        }
        else if(turn == Piece.BLUE){
            turn = Piece.RED;
            success = true;
        }
        else{
            throw new InvalidColourException();
        }
        return success;
    }
}
