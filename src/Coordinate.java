import java.util.LinkedList;
import java.util.List;

/**
 * An immutable class storing x and y integers to allow for easier passing of pair values.
 *
 * @author Steven Lowes
 */
public class Coordinate{

    /**
     * The x value of the Coordinate.
     */
    private final int x;
    /**
     * The y value of the Coordinate.
     */
    private final int y;

    /**
     * Create a new immutable pair object.
     *
     * @param x The x value (Count from 0)
     * @param y The y value (Count from 0)
     */
    public Coordinate(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * @return The x value of the Coordinate.
     */
    public int getX(){
        return x;
    }

    /**
     * @return The y value of the Coordinate.
     */
    public int getY(){
        return y;
    }

    /**
     * Check that the coordinate values are greater than or equal to 0 and less than the bounds passed as parameters.
     *
     * @param sizeX The width of the game board.
     * @param sizeY The height of the game board.
     *
     * @return boolean indicating validity.
     */
    public boolean valid(int sizeX, int sizeY){
        boolean valid = true;
        if(x < 0){
            valid = false;
        }
        else if(y < 0){
            valid = false;
        }
        else if(x >= sizeX){
            valid = false;
        }
        else if(y >= sizeY){
            valid = false;
        }
        return valid;
    }

    /**
     * Returns a list of the valid coordinates around this coordinate
     *
     * @param xSize The width of the board
     * @param ySize The height of the board
     *
     * @return a list of the valid coordinates around this coordinate
     */
    public List<Coordinate> getAdjactents(int xSize, int ySize){
        List<Coordinate> adjacentsList = new LinkedList<Coordinate>();
        Coordinate[] adjacents = new Coordinate[]{new Coordinate(-1, 0), new Coordinate(-1, +1), new Coordinate(0, +1), new Coordinate(0, -1), new Coordinate(+1, 0), new
                Coordinate(+1, -1)};
        for(Coordinate adjacent : adjacents){
            Coordinate tempCoord = add(adjacent);
            if(tempCoord.valid(xSize, ySize)){
                adjacentsList.add(tempCoord);
            }
        }
        return adjacentsList;
    }

    /**
     * Returns a list of valid coordinates that are in appropriate positions to be bridges to/from this coordinate
     *
     * @param xSize The width of the board
     * @param ySize The height of the board
     *
     * @return a list of valid coordinates that are in appropriate positions to be bridges to/from this coordinate
     */
    public List<Coordinate> getBridges(int xSize, int ySize){
        List<Coordinate> bridgesList = new LinkedList<Coordinate>();
        Coordinate[] bridges = new Coordinate[]{new Coordinate(x - 2, y + 1), new Coordinate(x - 1, y - 1), new Coordinate(x + 1, y - 2), new Coordinate(x + 2, y - 1), new
                Coordinate(x + 1, y + 1), new Coordinate(x - 1, y + 2)};
        for(Coordinate bridge : bridges){
            if(bridge.valid(xSize, ySize)){
                bridgesList.add(bridge);
            }
        }
        return bridgesList;
    }

    /**
     * Compares equality to another object, returns true if both x and y are equal.
     *
     * @param o The object to compare equality to
     *
     * @return boolean indicating equality
     */
    @Override public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }

        Coordinate that = (Coordinate) o;

        return getX() == that.getX() && getY() == that.getY();

    }

    /**
     * @return integer hashcode of this object
     */
    @Override public int hashCode(){
        int result = getX();
        result = 31 * result + getY();
        return result;
    }

    /**
     * Return a new Coordinate which has x and y values equal to the combined x and y values of this Coordinate and another passed Coordinate. Such that newX = X + otherX and newY
     * = Y + otherY
     *
     * @param coords2 Coordinate object to add to this one.
     *
     * @return new Coordinate equal to the addition of this and another Coordinate.
     */
    public Coordinate add(Coordinate coords2){
        return new Coordinate(x + coords2.getX(), y + coords2.getY());
    }

    /**
     * @return bracketed, separated xy values such as (x,y)
     */
    public String toString(){
        return "(" + x + "," + y + ")";
    }
}