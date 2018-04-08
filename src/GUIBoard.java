import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import sun.awt.image.ImageWatched;

/**
 * GameManager defines how the game is set up and calls the methods needed for the game to be played.
 *
 * @author Steven Lowes
 */
public class GUIBoard{

    /**
     * The board that is being represented
     */
    private Piece[][] boardView;

    /**
     * The hexes to be drawn on the screen
     */
    private HashMap<Coordinate, Hex> positions;

    /**
     * The pixel size of the display
     */
    private Dimension size;

    /**
     * Create a blank object
     */
    public GUIBoard(){
    }

    /**
     * Updates the board, keeping the colours of the hexes the same
     *
     * @param size The new size of the board
     */
    public synchronized void updateBoard(Dimension size){
        if(boardView != null){
            updateBoard(boardView, size);
        }
    }

    /**
     * Iterates through the board array and updates the colours of the hexes.
     *
     * @param boardView The updated board.
     * @param size The dimensions of the panel
     */
    public synchronized void updateBoard(Piece[][] boardView, Dimension size){
        this.boardView = boardView;
        positions = new HashMap<Coordinate, Hex>();

        //Determine whether the board will be tight against the top and bottom or left and right of the window
        //Work out the aspect ratio of the window
        double frameAspectRatio = (double) size.getWidth() / (double) size.getHeight();

        int xSize = 0;
        int ySize = 0;
        try{
            //Add two to allow for the coloured hexes around the border
            xSize = boardView.length + 2;
            ySize = boardView[0].length + 2;
        }
        catch(NullPointerException e){
            e.printStackTrace();
        }

        //Calculate the width and height of the board if the hexSize was set to 1
        double sizeWidth = 0.5 + 1.5 * xSize;
        double sizeHeight = (ySize + ((double) xSize - 1) / 2) * Math.sqrt(3);
        //Use those to figure out the aspect ratio of the drawn board
        double sizeAspectRatio = sizeWidth / sizeHeight;

        int hexSize;
        if(sizeAspectRatio > frameAspectRatio){ //tight horizontally
            //Set the hex size based on width
            hexSize = (int) (size.getWidth() / (0.5 + 1.5 * xSize));
        }
        else{ // tight vertically
            //Set the hex size based on height
            hexSize = (int) (size.getHeight() / ((ySize + ((double) xSize - 1) / 2) * Math.sqrt(3)));
        }
        //Determine how much to move the drawing to ensure that it is centered
        int width = (int) ((0.5 + 1.5 * xSize) * hexSize - xSize);
        int height = (int) ((ySize + ((double) xSize - 1) / 2) * Math.sqrt(3) * hexSize - (ySize + ((double) xSize - 1) / 2));
        int adjustmentX = (int) ((size.getWidth() - width) / 2);
        int adjustmentY = (int) ((size.getHeight() - height) / 2);
        Point startPos = new Point(adjustmentX, adjustmentY);

        for(int y = 0; y < ySize; y++){
            for(int x = 0; x < xSize; x++){
                //Set the colour of the border hexes
                Piece colour = null;
                if(y == 0 || y == ySize - 1){
                    if((y == 0 && x == 0) || (y == ySize - 1 && x == xSize - 1)){
                        colour = null;
                    }
                    else{
                        colour = Piece.RED;
                    }
                }
                else if(x == 0 || x == xSize - 1){
                    colour = Piece.BLUE;
                }
                else{
                    colour = boardView[x - 1][y - 1];
                }

                Point nextHexPoint = null;
                if(x == 0 && y == 0){
                    //make the first hex at the starting position determined earlier
                    nextHexPoint = startPos;
                }
                else{
                    //Create hexes, choosing the location based on the previously made hexes
                    Hex prevHex;
                    if(x == 0){
                        prevHex = positions.get(new Coordinate(0, y - 1));
                        if(prevHex == null){
                            System.out.println("Error");
                        }
                        else{
                            nextHexPoint = prevHex.getBelowHexPoint();
                        }
                    }
                    else{
                        prevHex = positions.get(new Coordinate(x - 1, y));
                        if(prevHex == null){
                            System.out.println("Error");
                        }
                        else{
                            nextHexPoint = prevHex.getRightHexPoint();
                        }
                    }
                }
                positions.put(new Coordinate(x, y), new Hex(nextHexPoint, hexSize, colour, new Coordinate(x, y)));
            }
        }

        Hex hex = positions.get(new Coordinate(boardView.length - 1, boardView[0].length - 1));
        Point point = hex.getBottomRightPoint();
        this.size = new Dimension(point.x, point.y);
    }

    /**
     * Get the size of the board drawing
     *
     * @return Size Dimension
     */
    public Dimension getSize(){
        return size;
    }

    /**
     * @return The list of hexes objects drawn on the panel
     */
    public synchronized List<Hex> getHexes(){
        LinkedList<Hex> list = new LinkedList<>();
        if(positions != null){
            list.addAll(positions.values());
        }
        return list;
    }

    /**
     * Get the hex that was clicked
     *
     * @param point The position on the panel that was clicked
     *
     * @return The hex which contains the point passed
     */
    public synchronized Hex getHex(Point point){

        for(Hex hex : positions.values()){
            if(hex.clicked(point)){
                return hex;
            }
        }
        return null;
    }

    /**
     * Set selected to false on all hexes
     */
    public synchronized void deselect(){
        for(Hex hex : positions.values()){
            hex.deselect();
        }
    }
}
