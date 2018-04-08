import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The MCTSRunnable is an object which runs the MCTS code in a thread to allow for multithreading of the AI.
 *
 * @author Steven Lowes
 */
public class MCTSRunnable implements Runnable{
    /**
     * The number of games simulated for each first move position
     */
    private final int[][] plays;

    /**
     * The number of wins for each first move position
     */
    private final int[][] wins;

    /**
     * The colour of the AI.
     */
    private final Piece colour;

    /**
     * The board state before making a move
     */
    private final Piece[][] boardView;

    /**
     * Maps the first move to the moves available for the second move
     */
    private final HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap;

    /**
     * Lists which moves are available to be made
     */
    private final ArrayList<Coordinate> allFreeSpaces;

    /**
     * Create a new MCTSRunnable, setting the fields to the values provided by the parameters.
     *
     * @param boardView The current state of the board
     * @param colour The colour of the AI
     * @param freeSpacesMap Maps the first move to the moves available for the second move
     * @param allFreeSpaces Lists which moves are available to be made
     */
    public MCTSRunnable(Piece[][] boardView, Piece colour, HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap, List<Coordinate> allFreeSpaces){
        this.colour = colour;
        plays = new int[boardView.length][boardView[0].length];
        wins = new int[boardView.length][boardView[0].length];
        this.boardView = boardView;
        this.allFreeSpaces = new ArrayList<Coordinate>(allFreeSpaces);
        this.freeSpacesMap = freeSpacesMap;
    }

    /**
     * Begin calculation, checking isInterrupted() and stopping when it returns true
     */
    public void run(){
        simulateGames(boardView);
    }

    /**
     * @return The number of games simulated for each first move position
     */
    public int[][] getPlays(){
        return plays;
    }

    /**
     * @return The number of wins for each first move position
     */
    public int[][] getWins(){
        return wins;
    }

    /**
     * Get the plays of a specific location
     *
     * @param x The x value of the location specified
     * @param y The y value of the location specified
     *
     * @return The number of games played with the location specified as the first move
     */
    public int getPlays(int x, int y){
        return plays[x][y];
    }

    /**
     * Get the wins of a specific location
     *
     * @param x The x value of the location specified
     * @param y The y value of the location specified
     *
     * @return The number of games won with the location specified as the first move
     */
    public int getWins(int x, int y){
        return wins[x][y];
    }

    /**
     * Simulate games until the thread is interrupted
     *
     * @param boardView The current state of the board.
     */
    private void simulateGames(Piece[][] boardView){
        List<Coordinate> freeSpaces = allFreeSpaces;

        while(!Thread.currentThread().isInterrupted()){ //The threads are interrupted when the timeGoal is up
            for(Coordinate coords : freeSpaces){
                int x = coords.getX();
                int y = coords.getY();

                int xSize = boardView.length;
                int ySize = boardView[0].length;
                Piece[][] newBoard = new Piece[xSize][ySize];
                //Deep copy the boardView
                for(int i = 0; i < xSize; i++){
                    System.arraycopy(boardView[i], 0, newBoard[i], 0, ySize);
                }

                newBoard[x][y] = colour;
                //Simulate a game
                newBoard = playGame(newBoard, new Coordinate(x, y));
                try{
                    if(Utility.playerWon(newBoard, colour)){
                        wins[x][y] += 1;
                    }
                }
                catch(InvalidColourException e){
                    System.out.println("Computer player appears to be set as invalid colour:" + colour);
                    e.printStackTrace();
                }
                plays[x][y] += 1;
            }
        }
    }

    /**
     * Play out a game randomly until every position is filled.
     *
     * @param boardView The current state of the board.
     *
     * @return The final state of the board.
     */
    private Piece[][] playGame(Piece[][] boardView, Coordinate firstMove){
        //Get free spaces
        List<Coordinate> freeSpaces;
        freeSpaces = new ArrayList<Coordinate>(freeSpacesMap.get(firstMove));

        //fill empty spaces
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        Piece turn = colour;
        if(turn == Piece.BLUE){
            turn = Piece.RED;
        }
        else{
            turn = Piece.BLUE;
        }
        while(freeSpaces.size() > 0){
            int index = rand.nextInt(freeSpaces.size());

            Coordinate coords = freeSpaces.get(index);
            boardView[coords.getX()][coords.getY()] = turn;

            //This is faster than removing the value at index
            int last = freeSpaces.size() - 1;
            freeSpaces.set(index, freeSpaces.get(last));
            freeSpaces.remove(last);

            if(turn == Piece.BLUE){
                turn = Piece.RED;
            }
            else{
                turn = Piece.BLUE;
            }
        }
        return boardView;
    }
}
