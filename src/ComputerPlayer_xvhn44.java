import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An AI player implementation of the PlayerInterface interface which implements four AI types: Mirror If player colour is blue, and the board size satisfies y = x + 1, Mirror the
 * opponent as seen here: http://mathworld.wolfram.com/GameofHex.html Middle If it is the first move, go for the middle hex Djikstra Look at every possible move up to two layers
 * deep, evaluating the value of the board using djikstra's algorithm. Assumes that the opponent plays intelligently. The Djikstra AI can also block the opponent, and use advanced
 * techniques such as bridges. Quickly becomes slow on larger boards. MCTS Use monte-carlo tree search, playing out a number of games randomly - choose the move with the highest
 * average win rate. Assumes randomly playing opponent, but is the most comprehensive AI and works in any situation.
 *
 * @author Steven Lowes
 */
public class ComputerPlayer_xvhn44 implements PlayerInterface{
    /**
     * The AI algorithm that should be used.
     * <p>
     * Default: AIType.COMBO
     */
    private AIType aiType;

    /**
     * For the mirror AI, store the previous board so that the changed hex can be calculated. Needs to be in a field so that it is accessible across method calls.
     */
    private Piece[][] pastBoard;

    /**
     * The time the AI should take to decide on a move in seconds. Used for MCTS only.
     * <p>
     * Default: 60
     */
    private double timeGoal;

    /**
     * The colour the player is playing as.
     */
    private Piece colour;

    /**
     * Create a new object setting fields with defaults to their default value.
     */
    public ComputerPlayer_xvhn44(){
        timeGoal = 90;
        this.aiType = AIType.COMBO;
    }

    /**
     * Create a new computer player, assigning the ai type to be equal to the parameter.
     *
     * @param aiType The algorithm to use for the AI.
     */
    public ComputerPlayer_xvhn44(AIType aiType){
        this.aiType = aiType;
        timeGoal = 90;
    }

    /**
     * Updates the value of timeGoal, with a 1% factor of safety.
     *
     * @param timeGoal The desired new timeGoal.
     *
     * @return boolean indicates successful operation
     */
    public boolean setTimeGoal(double timeGoal){
        this.timeGoal = timeGoal * 0.99;
        return true;
    }

    /**
     * Runs the correct method based on AI Type - defaulting to AIType.COMBO.
     *
     * @param boardView the current state of the board
     *
     * @return MoveInterface object representing the chosen move or concession.
     *
     * @throws NoValidMovesException Every space on the board is filled.
     */
    public MoveInterface makeMove(Piece[][] boardView) throws NoValidMovesException{
        MoveInterface move;
        if(aiType == null){
            move = makeMove(boardView, AIType.COMBO);
        }
        else{
            move = makeMove(boardView, aiType);
        }
        return move;
    }

    /**
     * Creates a list of free spaces on the board, fills the free spaces map, and passes those to the chosen AI method
     *
     * @param boardView The current state of the board.
     *
     * @return A Move object representing the desired place to place a piece or a Move object representing a concession if the AI determines that the game is unwinnable.
     *
     * @throws NoValidMovesException Indicates that no valid moves are possible - e.g. all cells on the board are already occupied by a piece.
     */
    private MoveInterface makeMove(Piece[][] boardView, AIType aiType) throws NoValidMovesException{

        //Generate list of free spaces
        ArrayList<Coordinate> freeSpaces = new ArrayList<Coordinate>();
        for(int i = 0; i < boardView.length; ++i){
            for(int j = 0; j < boardView[0].length; ++j){
                if(boardView[i][j] == Piece.UNSET){
                    freeSpaces.add(new Coordinate(i, j));
                }
            }
        }

        if(freeSpaces.size() == 0){ //All spaces filled
            throw new NoValidMovesException();
        }

        //Map first move to new lists of free spaces (i.e. 1,1 2,2 and 3,3 are free, 1,1 will be mapped to {2,2 3,3})
        HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap = new HashMap<Coordinate, ArrayList<Coordinate>>();
        for(Coordinate coords : freeSpaces){
            ArrayList<Coordinate> freeSpacesCopy = (ArrayList<Coordinate>) freeSpaces.clone();
            freeSpacesCopy.remove(coords);
            freeSpacesMap.put(coords, freeSpacesCopy);
        }

        MoveInterface move = new Move();

        //Run the appropriate AI
        if(aiType == AIType.DJIKSTRA){
            move = djikstraAI(boardView, freeSpaces, freeSpacesMap);
        }
        else if(aiType == AIType.MCTS){
            move = mctsAI(boardView, freeSpaces, freeSpacesMap);
        }
        else{
            move = comboAI(boardView, freeSpaces, freeSpacesMap);
        }
        return move;
    }

    /**
     * Chooses an AI method based on board conditions: Mirror ySize = xSize + 1 and player is Blue. Middle First move only Djikstra Free spaces is less than or equal to 256 (16x16
     * equivalent or smaller) MTCS All others
     *
     * @param boardView The current state of the board
     * @param freeSpaces The spaces on the board that are Piece.UNSET
     * @param freeSpacesMap A map - for each free space, the spaces that would be free after placing a piece at that location
     *
     * @return A Move object representing the desired place to place a piece or a Move object representing a concession if the AI determines that the game is unwinnable.
     */
    private MoveInterface comboAI(Piece[][] boardView, ArrayList<Coordinate> freeSpaces, HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap){
        MoveInterface move = new Move();

        //For the first move, the Combo AI chooses the middle hexagon
        if(freeSpaces.size() == boardView.length * boardView[0].length){
            move = chooseMiddleAI(boardView);
        }

        //If the AI is playing blue and the board is sized (x,x+1), mirror the opponent
        else if(boardView.length + 1 == boardView[0].length && colour == Piece.BLUE){
            move = mirrorAI(boardView);
        }
        else{
            Piece otherColour;
            if(colour == Piece.RED){
                otherColour = Piece.BLUE;
            }
            else{
                otherColour = Piece.RED;
            }
            Graph playerGraph = new Graph();
            playerGraph.populateGraph(boardView, colour);
            Double distance1 = playerGraph.getDistance();
            Graph oppGraph = new Graph();
            oppGraph.populateGraph(boardView, otherColour);
            Double distance2 = oppGraph.getDistance();

            //If the djikstra AI can't see a way to win, use MCTS (Shouldn't happen under normal operation but is included to reduce the number of assumptions made)
            if(distance1 == null || distance2 == null || distance1 < Graph.UNSET_WEIGHT){
                move = mctsAI(boardView, freeSpaces, freeSpacesMap);
            }

            //The other player has bridges the whole way across the board - the game is unwinnable
            else if(distance2 < Graph.UNSET_WEIGHT){
                move.setConceded();
            }

            //The board is equivalent to one larger than 14x14 (i.e. 15x15 with 35 spaces filled will not use this AI, it will use the DjikstraAI)
            else if(boardView.length * boardView[0].length > 196){
                move = mctsAI(boardView, freeSpaces, freeSpacesMap);
            }

            //There's nothing stopping us using the (better) djikstra AI, so let's use it.
            else{
                move = djikstraAI(boardView, freeSpaces, freeSpacesMap);
            }
        }
        return move;
    }

    /**
     * Choose the middle value (rounded down)
     *
     * @param boardView The current state of the board
     *
     * @return A Move object representing the desired place to place a piece or a Move object representing a concession if the AI determines that the game is unwinnable.
     */
    private MoveInterface chooseMiddleAI(Piece[][] boardView){
        MoveInterface move = new Move();
        try{
            move.setPosition(boardView.length / 2, boardView[0].length / 2);
        }
        catch(InvalidPositionException e){
            e.printStackTrace();
        }
        return move;
    }

    /**
     * Mirror the move that was just made, as seen here http://mathworld.wolfram.com/GameofHex.html
     *
     * @param newBoard The current state of the board
     *
     * @return A Move object representing the desired place to place a piece or a Move object representing a concession if the AI determines that the game is unwinnable.
     */
    private MoveInterface mirrorAI(Piece[][] newBoard){
        MoveInterface move = new Move();

        //It can be determined which side of the mirror line a location is on based on how (x+y) >= barrier evaluates.
        int barrier = Math.max(newBoard.length, newBoard[0].length) - 1;

        //pastBoard is used to determine which move the opponent made, create a blank one if it's the second (first for the AI) move.
        if(pastBoard == null){
            pastBoard = new Piece[newBoard.length][newBoard[0].length];
            for(int i = 0; i < newBoard.length; i++){
                for(int j = 0; j < newBoard[0].length; j++){
                    pastBoard[i][j] = Piece.UNSET;
                }
            }
        }

        //Get where the AI moved
        Coordinate changedHex = null;
        try{
            changedHex = getBoardDifference(pastBoard, newBoard, colour);
        }
        catch(InvalidColourException e){
            e.printStackTrace();
        }

        //adjustmentValue is the distance from the barrier for the opponent's move
        int adjustmentValue = changedHex.getX() + changedHex.getY() - barrier;
        //Use it to determine where the new move should go
        int adjustmentX;
        int adjustmentY;
        if(changedHex.getX() + changedHex.getY() < barrier){
            adjustmentX = -(adjustmentValue) - 1;
            adjustmentY = -adjustmentValue;
        }
        else{
            adjustmentX = -adjustmentValue;
            adjustmentY = -(adjustmentValue) - 1;
        }
        Coordinate newMove = changedHex.add(new Coordinate(adjustmentX, adjustmentY));
        try{
            move.setPosition(newMove.getX(), newMove.getY());
        }
        catch(InvalidPositionException e){
            e.printStackTrace();
        }
        newBoard[newMove.getX()][newMove.getY()] = colour;
        pastBoard = newBoard;
        return move;
    }

    /**
     * Return the difference between the two boards.
     *
     * @param board1 The first board to compare
     * @param board2 The second board to compare
     * @param colour The colour of the player - only pieces of the other colour will be returned
     *
     * @return The coordinate of the piece that changed. If multiple pieces have changed, the method silently returns the first one, reading down then right.
     *
     * @throws InvalidColourException there is no difference between board1 and board2 that is the colour other than that passed in the parameter.
     */
    private Coordinate getBoardDifference(Piece[][] board1, Piece[][] board2, Piece colour) throws InvalidColourException{
        if(board1.length != board2.length){
            return null;
        }
        if(board1[0].length != board2[0].length){
            return null;
        }

        Piece otherColour;
        if(colour == Piece.RED){
            otherColour = Piece.BLUE;
        }
        else if(colour == Piece.BLUE){
            otherColour = Piece.RED;
        }
        else{
            throw new InvalidColourException();
        }

        for(int i = 0; i < board1.length; i++){
            for(int j = 0; j < board1[0].length; j++){
                if(board1[i][j] != board2[i][j] && (board1[i][j] == otherColour || board2[i][j] == otherColour)){
                    return new Coordinate(i, j);
                }
            }
        }
        return null;
    }

    /**
     * Look at each possible move, two levels deep, rating each possible move using djikstra's algorithm. The advantages of this algorithm over the MCTS algorithm is that it
     * assumes a competent opponent, in addition to utilising blocking and advanced strategies such as bridges.
     *
     * @param boardView The current state of the board
     * @param freeSpaces The spaces on the board that are Piece.UNSET
     * @param freeSpacesMap A map - for each free space, the spaces that would be free after placing a piece at that location
     *
     * @return A Move object representing the desired place to place a piece or a Move object representing a concession if the AI determines that the game is unwinnable.
     */
    private MoveInterface djikstraAI(Piece[][] boardView, ArrayList<Coordinate> freeSpaces, HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap){
        ArrayList<PotentialMove> moveOptions = djikstraCompute(boardView, freeSpaces, freeSpacesMap);
        ArrayList<PotentialMove> bestMoves = new ArrayList<PotentialMove>();
        PotentialMove bestMove = null;
        Double bestValue = null;
        Integer numberEqual = null;
        //Look at all the moves and find the one with the best value
        for(PotentialMove move : moveOptions){
            if(bestValue == null || move.getValue() >= bestValue){
                if(bestValue != null && move.getValue().equals(bestValue)){
                    if(numberEqual == null || move.getNumberEqual() >= numberEqual){
                        if(numberEqual != null && move.getNumberEqual() == numberEqual){
                            bestMoves.add(move);
                            //add to bestMoves list
                        }
                        else{
                            bestMoves = new ArrayList<PotentialMove>();
                            bestMoves.add(move);
                            bestMove = move;
                            numberEqual = move.getNumberEqual();
                            //replace bestMove, recreate bestMoves list, add to bestMoves list
                        }
                    }
                }
                else{
                    bestMoves = new ArrayList<PotentialMove>();
                    bestMoves.add(move);
                    bestMove = move;
                    bestValue = move.getValue();
                    //better move
                }
            }
        }

        ThreadLocalRandom rand = ThreadLocalRandom.current();
        //If there are multiple equally good, pick one at random
        PotentialMove chosenMove = bestMoves.get(rand.nextInt(bestMoves.size()));

        MoveInterface move = new Move();

        if(bestMove.getValue() == Double.NEGATIVE_INFINITY){ //There's no move that doesn't result in a loss for us
            move.setConceded();
            return move;
        }

        try{
            move.setPosition(chosenMove.getFirstMove().getX(), chosenMove.getFirstMove().getY());
        }
        catch(InvalidPositionException e){
            e.printStackTrace();
        }
        return move;
    }

    /**
     * Uses DjikstraRunnables to perform the computations for the DjikstraAI and return a list of potential moves and their values.
     *
     * @param boardView The current state of the board
     * @param freeSpaces The spaces on the board that are Piece.UNSET
     * @param freeSpacesMap A map - for each free space, the spaces that would be free after placing a piece at that location
     *
     * @return A list of PotentialMove objects each storing a pair of moves and the value of that move to be computed by the mctsAI method.
     */
    private ArrayList<PotentialMove> djikstraCompute(Piece[][] boardView, ArrayList<Coordinate> freeSpaces, HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap){
        int procs = Runtime.getRuntime().availableProcessors();
        LinkedList<DjikstraRunnable> runnables = new LinkedList<>();
        ArrayList<ArrayList<Coordinate>> sublists = new ArrayList<ArrayList<Coordinate>>();

        for(int i = 0; i < procs; i++){
            sublists.add(new ArrayList<Coordinate>());
        }

        int runnableNo = 0;
        //Run through the spaces available for a first move, splitting them evenly between threads
        for(Coordinate coords : freeSpaces){
            sublists.get(runnableNo).add(coords);
            runnableNo = (runnableNo + 1) % procs;
        }

        try{
            for(int i = 0; i < procs; i++){
                //Start the calculations
                DjikstraRunnable runnable = new DjikstraRunnable(boardView, colour, sublists.get(i), freeSpacesMap);
                runnables.add(runnable);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        }
        catch(InvalidColourException e){
            e.printStackTrace();
        }

        boolean done = false;
        while(!done){
            //check 10 times per second whether the threads are done, stop when all are.
            try{
                Thread.sleep(100);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
            done = true;
            int i = 0;
            while(done && i < runnables.size()){
                done = runnables.get(i).isDone();
                i++;
            }
        }
        //Combine all the results
        ArrayList<PotentialMove> moves = new ArrayList<PotentialMove>();
        for(DjikstraRunnable runnable : runnables){
            moves.addAll(runnable.getMoveOptions());
        }
        return moves;
    }

    /**
     * Uses Monte-Carlo Tree Search to determine the best move. Plays a number of games and keeps track of the win rate - the first move with the highest average win rate is the
     * winner and is chosen. To improve speed, the board is entirely filled before checking which player has won. Since the game cannot end in a draw, and once a player has won,
     * further play cannot change that, it is safe to continue to fill up the board until you are certain there is a winner (i.e. the board is full). This is done as making the
     * moves is relatively quick but checking the win state is slow. The algorithm assumes a randomly playing opponent and is relatively dumb, but can take as long or short as
     * required, whereas the Djikstra AI has time complexity around O(n^4) where the board is nxn.
     *
     * @param boardView The current state of the board
     * @param freeSpaces The spaces on the board that are Piece.UNSET
     * @param freeSpacesMap A map - for each free space, the spaces that would be free after placing a piece at that location
     *
     * @return A Move object representing the desired place to place a piece or a Move object representing a concession if the AI determines that the game is unwinnable.
     */
    private MoveInterface mctsAI(Piece[][] boardView, ArrayList<Coordinate> freeSpaces, HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap){
        //get the winRate tables
        Double[][] winRate = mctsCompute(boardView, freeSpaces, freeSpacesMap);
        //get the best move
        double bestWinRate = 0;
        Coordinate bestMove = null;
        for(int k = 0; k < boardView.length; k++){
            for(int l = 0; l < boardView[0].length; l++){
                if(winRate[k][l] > bestWinRate){
                    bestWinRate = winRate[k][l];
                    bestMove = new Coordinate(k, l);
                }
            }
        }

        //return the best move
        MoveInterface move = new Move();
        if(bestMove != null){
            try{
                move.setPosition(bestMove.getX(), bestMove.getY());
            }
            catch(InvalidPositionException e){
                System.out.println("Computer tried to move to an invalid position?");
                e.printStackTrace();
            }
        }
        else{
            move.setConceded();
        }
        return move;
    }

    /**
     * Use MCTSRunnables to compute the random games, interrupt them when done and add their win rate arrays together.
     *
     * @param boardView The current state of the board
     * @param freeSpaces The spaces on the board that are Piece.UNSET
     * @param freeSpacesMap A map - for each free space, the spaces that would be free after placing a piece at that location
     *
     * @return A 2D array of Doubles representing the
     */
    private Double[][] mctsCompute(Piece[][] boardView, ArrayList<Coordinate> freeSpaces, HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap){
        int procs = Runtime.getRuntime().availableProcessors();
        LinkedList<MCTSRunnable> runnables = new LinkedList<>();
        LinkedList<Thread> threads = new LinkedList<>();
        for(int k = 0; k < procs; k++){
            //Create a thread for each processor thread and start calculating the data
            MCTSRunnable runnable = new MCTSRunnable(boardView, colour, freeSpacesMap, freeSpaces);
            Thread thread = new Thread(runnable);
            thread.start();
            threads.add(thread);
            runnables.add(runnable);
        }
        try{
            //Sleep until the timeGoal has passed
            Thread.sleep((long) (timeGoal * 1000));
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }

        for(Thread thread : threads){
            thread.interrupt();
        }

        //Combine the data
        int[][] plays = new int[boardView.length][boardView[0].length];
        int[][] wins = new int[boardView.length][boardView[0].length];

        for(MCTSRunnable runnable : runnables){
            for(int k = 0; k < boardView.length; k++){
                for(int l = 0; l < boardView[0].length; l++){
                    plays[k][l] += runnable.getPlays(k, l);
                    wins[k][l] += runnable.getWins(k, l);
                }
            }
        }

        //Convert plays and wins to winrate
        Double[][] winRate = new Double[boardView.length][boardView[0].length];
        for(int i = 0; i < plays.length; i++){
            for(int j = 0; j < plays[0].length; j++){
                winRate[i][j] = (double) (wins[i][j]) / (double) (plays[i][j]);
            }
        }
        return winRate;
    }

    /**
     * React to being informed of final game state. - Doesn't do anything, as AI doesn't care whether it's won or lost.
     *
     * @param state either WON or LOST
     *
     * @return boolean true if the state passed is win or lose, else false
     */
    public boolean finalGameState(GameState state){
        //AIs don't need informing
        return true;
    }

    /**
     * Set the colour of the player to a new colour.
     *
     * @param colour A Piece (RED/BLUE) that this player will be.
     *
     * @return boolean indicating successful operation.
     *
     * @throws InvalidColourException New colour is neither Piece.RED or Piece.BLUE.
     * @throws ColourAlreadySetException Player already has colour assigned.
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
     * @return the colour of the player (Piece.RED/Piece.BLUE).
     */
    public Piece getColour(){
        return colour;
    }
}
