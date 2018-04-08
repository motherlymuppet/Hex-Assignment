/**
 * GameManager defines how the game is set up and calls the methods needed for the game to be played.
 *
 * @author Steven Lowes
 */
public class GameManager implements GameManagerInterface{
    /**
     * Board object storing game data.
     */
    private final BoardInterface board;

    /**
     * Player who is playing red.
     */
    private PlayerInterface redPlayer;
    /**
     * Player who is playing blue.
     */
    private PlayerInterface bluePlayer;
    /**
     * The colour whose turn it is currently. Should be Piece.RED or Piece.BLUE never Piece.UNSET or null.
     */
    private Piece turn;

    /**
     * Create a new GameManager with a GUI display of max height and width 500 pixels.
     */
    public GameManager(){
        board = new Board();
    }

    /**
     * Define who will be playing each colour. This method will be called twice for each game once for RED and once for BLUE.
     *
     * @param player the player who will be playing red
     * @param colour the enum for a Piece (RED or BLUE)
     *
     * @return boolean true if the player was successfully set to the specified colour
     *
     * @throws ColourAlreadySetException If the colour is already allocated to a player
     */
    public boolean specifyPlayer(PlayerInterface player, Piece colour) throws ColourAlreadySetException, InvalidColourException{
        if(colour != Piece.RED && colour != Piece.BLUE){
            throw new InvalidColourException();
        }
        if(colour == Piece.RED && redPlayer != null){
            throw new ColourAlreadySetException();
        }
        else if(colour == Piece.BLUE && bluePlayer != null){
            throw new ColourAlreadySetException();
        }

        try{
            if(colour == Piece.RED){
                redPlayer = player;
            }
            else if(colour == Piece.BLUE){
                bluePlayer = player;
            }
            else{
                throw new InvalidColourException();
            }
            player.setColour(colour);
        }
        catch(InvalidColourException e){
            System.out.println("Invalid Colour Exception");
            return false;
        }

        return true;
    }

    /**
     * Specifiy the size of the board that we are playing on. Both numbers must be greater than zero
     *
     * @param sizeX how wide the board will be
     * @param sizeY how tall the board will be
     *
     * @return boolean indicating successful operation
     *
     * @throws InvalidBoardSizeException If either size value is less than one.
     * @throws BoardAlreadySizedException If the board has already been created.
     */
    public boolean boardSize(int sizeX, int sizeY) throws InvalidBoardSizeException, BoardAlreadySizedException{
        board.setBoardSize(sizeX, sizeY);
        return true;
    }

    /**
     * The core of the game manager. This requests each player to make a move and plays these out on the game board. It is also checked whether the game is won and if so reported
     * to the players that that is the case.
     *
     * @return boolean indicating successful operation
     */
    public boolean playGame(){
        //Red goes first
        turn = Piece.RED;
        boolean gameEnd = false;

        while(!gameEnd){
            //Main game loop
            if(turn == Piece.RED || turn == Piece.BLUE){
                MoveInterface move = null;
                //Get the move from the player whose turn it is
                try{
                    if(turn == Piece.RED){
                        move = redPlayer.makeMove(board.getBoardView());
                    }
                    else{
                        move = bluePlayer.makeMove(board.getBoardView());
                    }
                }
                catch(NoValidMovesException e){
                    System.out.println("No valid moves remaining");
                    return false;
                }
                catch(NoBoardDefinedException e){
                    System.out.println("Board not yet defined - define board before making move");
                    return false;
                }
                try{
                    if(board.placePiece(turn, move)){
                        //Increment turn if move made with no issues
                        if(!nextTurn()){
                            return false;
                        }
                    }
                }
                catch(PositionAlreadyTakenException e){
                    System.out.println("That position is already taken, try again");
                }
                catch(InvalidPositionException e){
                    System.out.println("Invalid position choice, try again");
                }
                catch(InvalidColourException e){
                    System.out.println("playGame of GameManager called before turn set");
                    return false;
                }
                catch(NoBoardDefinedException e){
                    System.out.println("playGame of GameManager called before board defined");
                    return false;
                }
            }
            else{
                System.out.println("playGame of GameManager class called before turn was initialised");
            }

            //Check to see if anyone won
            Piece winner = Piece.UNSET;
            try{
                winner = board.gameWon();
            }
            catch(NoBoardDefinedException e){
                System.out.println("playGame of GameManager called before board defined");
                return false;
            }

            //If so, let the players know
            if(winner != null && winner != Piece.UNSET){
                gameEnd = true;
                if(winner == Piece.RED){
                    bluePlayer.finalGameState(GameState.LOST);
                    redPlayer.finalGameState(GameState.WON);
                }
                else{
                    bluePlayer.finalGameState(GameState.WON);
                    redPlayer.finalGameState(GameState.LOST);
                }
            }
        }
        return true;
    }

    /**
     * Calls board.gameWon()
     *
     * @return Piece value indicating game won, Piece.RED for red wins, Piece.BLUE for blue wins, Piece.UNSET for no winner yet.
     *
     * @throws NoBoardDefinedException the board has not been defined - call boardSize.
     */
    public Piece getWon() throws NoBoardDefinedException{
        return board.gameWon();
    }

    /**
     * Increment turn
     *
     * @return boolean indicating successful incrementation - false indicates turn is not Piece.BLUE or Piece.RED
     *
     * @throws InvalidColourException turn is not Piece.RED or Piece.BLUE
     */
    private boolean nextTurn() throws InvalidColourException{
        if(turn == Piece.RED){
            turn = Piece.BLUE;
        }
        else if(turn == Piece.BLUE){
            turn = Piece.RED;
        }
        else{
            throw new InvalidColourException();
        }
        return true;
    }
}
