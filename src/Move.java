/**
 * Represents a move a player wishes to make
 *
 * @author Steven Lowes
 */
public class Move implements MoveInterface{
    private int xPosition;
    private int yPosition;
    private boolean conceded;

    /**
     * Creates a blank move object
     */
    public Move(){
    }

    /**
     * Set the position that the Player wishes to use - both x and y coordinate.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     *
     * @return true indicating value set correctly
     *
     * @throws InvalidPositionException The position is invalid. E.g. both x and y are negative.
     */
    public boolean setPosition(int x, int y) throws InvalidPositionException{
        if(x >= 0 && y >= 0){
            xPosition = x;
            yPosition = y;
        }
        else{
            //one or both of x and y are negative
            throw new InvalidPositionException();
        }
        return true;
    }

    /**
     * Has the player conceded in this move? i.e. have they yielded to the fact that their opponent has won before all required moves are made.
     *
     * @return true if the player has conceded.
     */
    public boolean hasConceded(){
        return conceded;
    }

    /**
     * Get the x coordinate of the move.
     *
     * @return the x coordinate.
     */
    public int getXPosition(){
        return xPosition;
    }

    /**
     * Get the y coordnate of the move.
     *
     * @return the y coordinate.
     */
    public int getYPosition(){
        return yPosition;
    }

    /**
     * Indicate that the player has conceded in this move.
     *
     * @return true indicating conceded is set.
     */
    public boolean setConceded(){
        conceded = true;
        return true;
    }

    /**
     * @return String in style "(x,y)" or "Concede"
     */
    public String toString(){
        if(!conceded){
            return "(" + xPosition + "," + yPosition + ")";
        }
        else{
            return "Concede";
        }
    }
}
