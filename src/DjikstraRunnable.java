import java.util.ArrayList;
import java.util.HashMap;

/**
 * A runnable which calculates the best moves that can be made and stores them, ready to provide back to the ComputerPlayer that created the object. Implemented to allow for
 * multi-threading in the Djikstra AI.
 */
public class DjikstraRunnable implements Runnable{
    /**
     * A piece view of the current board state
     */
    private final Piece[][] boardView;

    /**
     * The colour of this player. It is assumed that it is the passed colour's turn.
     */
    private final Piece colour;

    /**
     * The moves that can be made
     */
    private final ArrayList<Coordinate> freeSpaces;

    /**
     * If I make this move (Coordinate), which moves could the opponent make (ArrayList of Coordinate)
     */
    private final HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap;

    /**
     * Is the runnable finished calculating
     */
    private boolean done = false;

    /**
     * Stores the moves available and the value of each
     */
    private ArrayList<PotentialMove> moveOptions;

    /**
     * Initialise the variables passed as parameters.
     *
     * @param boardView The current state of the board
     * @param colour The colour of the AI player
     * @param freeSpaces The moves that are available to be made - does not represent all free spaces on the board, only those that the runnable is allocated to calculate value
     * for.
     * @param freeSpacesMap The moves that are available for the second move for each first move
     *
     * @throws NullPointerException boardView, freeSpaces, or freeSpacesMap are null
     * @throws InvalidColourException colour parameter is not Piece.RED or Piece.BLUE
     */
    public DjikstraRunnable(Piece[][] boardView, Piece colour, ArrayList<Coordinate> freeSpaces, HashMap<Coordinate, ArrayList<Coordinate>> freeSpacesMap) throws
                                                                                                                                                           NullPointerException,
                                                                                                                                                           InvalidColourException{
        if(boardView == null || freeSpaces == null || freeSpacesMap == null){
            throw new NullPointerException();
        }
        if(colour != Piece.RED && colour != Piece.BLUE){
            throw new InvalidColourException();
        }
        this.boardView = boardView;
        this.colour = colour;
        this.freeSpaces = freeSpaces;
        this.freeSpacesMap = freeSpacesMap;
    }

    /**
     * Indicates that the runnable has finished calculating the value of its allocated moves
     *
     * @return boolean indicating completion
     */
    public boolean isDone(){
        return done;
    }

    /**
     * Return the values for each potential move, as PotentialMove objects. If not done calculating, return null.
     *
     * @return Null if not done calculating, else a list of PotentialMove options
     */
    public ArrayList<PotentialMove> getMoveOptions(){
        if(done){
            return moveOptions;
        }
        else{
            return null;
        }
    }

    /**
     * Calculate the values of each move, 2 layers deep. Assume the both players make the best move for themselves.
     */
    public void run(){
        //Generate the initial graphs for the board stored.
        Graph playerGraph = new Graph();
        playerGraph.populateGraph(boardView, colour);
        Piece otherColour;

        if(colour == Piece.RED){
            otherColour = Piece.BLUE;
        }
        else{
            otherColour = Piece.RED;
        }
        Graph opponentGraph = new Graph();
        opponentGraph.populateGraph(boardView, otherColour);

        //high values are better for player, low values better for opponent
        moveOptions = new ArrayList<PotentialMove>();

        for(Coordinate coords : freeSpaces){
            //For each first move
            ArrayList<Coordinate> newFreeSpaces = freeSpacesMap.get(coords);
            ArrayList<PotentialMove> options = new ArrayList<PotentialMove>();

            for(Coordinate coords2 : newFreeSpaces){
                //For each second move for that first move
                HashMap<Coordinate, Node> nodesMap = playerGraph.getNodesMap();
                //Store the data about the current state of the nodes
                Node prevNode1 = nodesMap.get(coords);
                Node prevNode2 = nodesMap.get(coords2);
                Double node1PrevWeight = null;
                Double node2PrevWeight = null;
                try{
                    node1PrevWeight = prevNode1.getWeight();
                    node2PrevWeight = prevNode2.getWeight();
                }
                catch(NodeWeightNotSetException e){
                    System.out.println("Doing djikstras on a graph that hasn't been initialised properly - node " + "weight == null");
                    e.printStackTrace();
                }

                NodeType node1PrevType = prevNode1.getType();
                NodeType node2PrevType = prevNode2.getType();

                try{
                    //Make the moves
                    playerGraph.changeNode(coords, NodeType.PLAYER, Graph.PLAYER_WEIGHT);
                    opponentGraph.changeNode(coords, NodeType.OPPONENT, null);
                    playerGraph.changeNode(coords2, NodeType.OPPONENT, null);
                    opponentGraph.changeNode(coords2, NodeType.PLAYER, Graph.PLAYER_WEIGHT);

                    //Have a look at the new distances to traverse the graph for each player
                    Double playerDistance = playerGraph.getDistance();
                    Double opponentDistance = opponentGraph.getDistance();

                    //Store the move
                    options.add(new PotentialMove(coords, coords2, playerDistance, opponentDistance));

                    //Change the nodes back to how they were
                    playerGraph.changeNode(coords, node1PrevType, node1PrevWeight);
                    opponentGraph.changeNode(coords, node1PrevType, node1PrevWeight);
                    playerGraph.changeNode(coords2, node2PrevType, node2PrevWeight);
                    opponentGraph.changeNode(coords2, node2PrevType, node2PrevWeight);
                    playerGraph.reset();
                    opponentGraph.reset();
                }
                catch(InvalidPositionException e){
                    e.printStackTrace();
                }
            }

            //assume the opponent plays optimally
            PotentialMove bestMove = null;
            Double bestValue = null;
            for(PotentialMove move : options){
                //low values best for AI
                if(bestValue == null || move.getValue() <= bestValue){
                    if(bestValue != null && move.getValue().equals(bestValue)){
                        bestMove.addAllSecondMove(move.getSecondMove());
                    }
                    else{
                        bestMove = move;
                        bestValue = move.getValue();
                        //better move
                    }
                }
            }
            moveOptions.add(bestMove);
        }
        done = true;
    }

}
