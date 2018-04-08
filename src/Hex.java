import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;

/**
 * Created by Steven on 23/04/2016.
 */
public class Hex{
    /**
     * The color to use for the red player
     */
    public static final Color RED_COLOR = Color.RED;

    /**
     * The color to use for the blue player
     */
    public static final Color BLUE_COLOR = Color.BLUE;

    /**
     * The color to use for unset hexes
     */
    public static final Color UNSET_COLOR = Color.LIGHT_GRAY;

    /**
     * The color to use for hexes that are selected
     */
    public static final Color SELECTED_COLOR = Color.GREEN;

    /**
     * Indicates whether the hex is selected
     */
    private boolean selected = false;

    /**
     * Indicates the colour of the hex in terms of Piece enumerations
     */
    private Piece pieceColour;

    /**
     * Stores the polygon to be drawn on the panel
     */
    private Polygon hexShape;

    /**
     * Stores the coordinates on the board
     */
    private Coordinate coords;

    /**
     * Creates a hex object, intialising the hexPoly field
     *
     * @param northWest The point which represents the top left corner of the bounding rectangle of the hexagon
     * @param radius The radius of the hexagon (outer circle)
     * @param pieceColour The colour of the hex in terms of Piece enumerations
     * @param coords The coordinates of this hexagon on the board
     */
    public Hex(Point northWest, int radius, Piece pieceColour, Coordinate coords){
        this.coords = coords;
        hexShape = calculatePoints(northWest, radius);
        this.pieceColour = pieceColour;
    }

    /**
     * Selects this hex if it is Piece.UNSET
     *
     * @return boolean indicating success
     */
    public boolean select(){
        if(pieceColour == Piece.UNSET){
            selected = true;
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Get the bottom right corner of the bounding rectangle
     *
     * @return Point representing the bottom right corner
     */
    public Point getBottomRightPoint(){
        int x = hexShape.xpoints[5];
        int y = hexShape.ypoints[3];
        Point point = new Point(x, y);
        return point;
    }

    /**
     * @return The colour of the hex as a Piece enumeration
     */
    public Piece getPieceColour(){
        return pieceColour;
    }

    /**
     * Updates the colour of the hex
     *
     * @param pieceColour The new value of the colour of the hex
     */
    public void setPieceColour(Piece pieceColour){
        this.pieceColour = pieceColour;
    }

    /**
     * @return The coordinates of the hex on a board
     */
    public Coordinate getCoords(){
        return new Coordinate(coords.getX() - 1, coords.getY() - 1);
    }

    /**
     * Set the hex to be unselected
     */
    public void deselect(){
        selected = false;
    }

    /**
     * @return The hex shape to be drawn
     */
    public Polygon getHexPoly(){
        return hexShape;
    }

    /**
     * @return String representation fo the object in style "coords colour"
     */
    public String toString(){
        return coords.toString() + " " + pieceColour;
    }

    /**
     * @return color for the panel to draw the hex in
     *
     * @throws InvalidColourException The Piece colour is not one that can be translated into a display colour.
     */
    public Color getColor() throws InvalidColourException{
        if(selected){
            return SELECTED_COLOR;
        }
        else if(pieceColour == Piece.UNSET){
            return UNSET_COLOR;
        }
        else if(pieceColour == Piece.RED){
            return RED_COLOR;
        }
        else if(pieceColour == Piece.BLUE){
            return BLUE_COLOR;
        }
        else if(pieceColour == null){
            return null;
        }
        else{
            throw new InvalidColourException();
        }
    }

    /**
     * @return The top left point of the bounding box of a hex that is drawn below this one
     */
    public Point getBelowHexPoint(){
        return new Point(hexShape.xpoints[2], hexShape.ypoints[3]);
    }

    /**
     * @return The top left point of the bounding box of a hex that is drawn to the right of this one
     */
    public Point getRightHexPoint(){
        return new Point(hexShape.xpoints[0], hexShape.ypoints[2]);
    }

    /**
     * Determines whether a click at a certain point constitutes clicking this hex
     *
     * @param point The position that was clicked
     *
     * @return boolean indicated whether this hex was clicked
     */
    public boolean clicked(Point point){
        return hexShape.contains(point);
    }

    /**
     * Create the hex polygon
     *
     * @param northWest The top left point of the bounding box of the hex to be drawn
     * @param radius The radius of the outer circle of the hexagon - i.e. the distance from the origin to any point
     *
     * @return the hex polygon that was created
     */
    public Polygon calculatePoints(Point northWest, int radius){
        Point center = new Point(northWest.x + radius, (int) ((double) (northWest.y) + (double) (radius) * Math.sqrt(3) / 2));
        int[] xPoints = {(int) (center.getX() + 0.5 * (double) radius), (int) (center.getX() - 0.5 * (double) radius), (int) (center.getX() - (double) radius), (int) (center
                .getX() - 0.5 * (double) radius), (int) (center.getX() + 0.5 * (double) radius), (int) (center.getX() + (double) radius)};
        int[] yPoints = {(int) (center.getY() - (double) radius * (Math.sqrt(3) / 2)), (int) (center.getY() - (double) radius * (Math.sqrt(3) / 2)), (int) (center.getY()), (int)
                (center.getY() + (double) radius * (Math.sqrt(3) / 2)), (int) (center.getY() + (double) radius * (Math.sqrt(3) / 2)), (int) (center.getY())};
        Polygon hex = new Polygon(xPoints, yPoints, 6);
        return hex;
    }
}