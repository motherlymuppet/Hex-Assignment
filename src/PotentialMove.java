import java.util.ArrayList;

/**
 * Stores a potential move for the Djikstra AI - storing first, and second moves, and the distances for each player after both moves have been made. The value is calculated as the
 * opponent distance - AI distance, therefore high values are best for the AI and low values better for the opponent.
 *
 * @author Steven Lowes
 */
public class PotentialMove{
    /**
     * The first move to be made
     */
    private Coordinate firstMove;

    /**
     * The second move to be made (made by the opponent). If there are more than one second move that all result in the same change in value, they are all added to the list
     */
    private ArrayList<Coordinate> secondMove;

    /**
     * The distance for the AI after both moves have been made. Null indicates it is impossible to get from the start to end nodes
     */
    private Double aiDistance;

    /**
     * The distance for the Opponent after both moves have been made. Null indicates it is impossible to get from the start to end nodes
     */
    private Double oppDistance;

    /**
     * Create a new PotentialMove and initalise all fields with the parameters passed.
     *
     * @param firstMove The first move to be made
     * @param secondMove The second move to be made (made by the Opponent)
     * @param aiDistance The distance for the AI once both moves are made
     * @param oppDistance The distance for the Opponent once both moves are made
     */
    public PotentialMove(Coordinate firstMove, Coordinate secondMove, Double aiDistance, Double oppDistance){
        this.firstMove = firstMove;
        this.secondMove = new ArrayList<Coordinate>();
        this.secondMove.add(secondMove);
        this.aiDistance = aiDistance;
        this.oppDistance = oppDistance;
    }

    /**
     * @return String in style: "firstMove secondMove value"
     */
    public String toString(){
        return firstMove + " " + secondMove + " " + getValue();
    }

    /**
     * @return Value - equal to opponent distance - ai distance
     */
    public Double getValue(){
        if(oppDistance == null){
            return Double.POSITIVE_INFINITY;
        }
        else if(aiDistance == null){
            return Double.NEGATIVE_INFINITY;
        }
        else{
            //high values are best for AI, low for opponent
            return oppDistance - aiDistance;
        }
    }

    /**
     * @param secondMove The second move to add to the list of second moves
     */
    public void addSecondMove(Coordinate secondMove){
        this.secondMove.add(secondMove);
    }

    /**
     * @return The first move
     */
    public Coordinate getFirstMove(){
        return firstMove;
    }

    /**
     * Set the first move to the value provided
     *
     * @param firstMove The new value of firstMove
     */
    public void setFirstMove(Coordinate firstMove){
        this.firstMove = firstMove;
    }

    /**
     * @return A list of second moves
     */
    public ArrayList<Coordinate> getSecondMove(){
        return secondMove;
    }

    /**
     * Set the list of second moves to be equal to the parameter passed.
     *
     * @param secondMove The new list of second moves.
     */
    public void setSecondMove(ArrayList<Coordinate> secondMove){
        this.secondMove = secondMove;
    }

    /**
     * Reset the list of second moves and add the parameter.
     *
     * @param secondMove The new secondMove to use.
     */
    public void setSecondMove(Coordinate secondMove){
        this.secondMove = new ArrayList<Coordinate>();
        this.secondMove.add(secondMove);
    }

    /**
     * Add all second moves from a list
     *
     * @param secondMove the list to add all from
     */
    public void addAllSecondMove(ArrayList<Coordinate> secondMove){
        this.secondMove.addAll(secondMove);
    }

    /**
     * @return The distance for the AI
     */
    public Double getAiDistance(){
        return aiDistance;
    }

    /**
     * Set the distance for the ai
     *
     * @param aiDistance The new value for ai distance
     */
    public void setAiDistance(Double aiDistance){
        this.aiDistance = aiDistance;
    }

    /**
     * @return The distance for the opponent
     */
    public Double getOppDistance(){
        return oppDistance;
    }

    /**
     * Set the distance for the opponent
     *
     * @param oppDistance The new value for opponent distance
     */
    public void setOppDistance(Double oppDistance){
        this.oppDistance = oppDistance;
    }

    /**
     * Return the number of second moves that are equally valid
     *
     * @return The size of the secondMoves list
     */
    public int getNumberEqual(){
        return secondMove.size();
    }
}
