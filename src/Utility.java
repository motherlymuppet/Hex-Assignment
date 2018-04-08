import java.util.ArrayList;
import java.util.List;

/**
 * Stores static utilities.
 *
 * @author Steven Lowes
 */
public class Utility{
    /**
     * Perform a deep clone of a 2D array.
     *
     * @param input the 2D array to be cloned.
     *
     * @return A deep clone of the input array.
     */
    public static Piece[][] clone(Piece[][] input){
        int x = input.length;
        int y = input[0].length;
        Piece[][] output = new Piece[x][y];
        for(int i = 0; i < x; i++){
            System.arraycopy(input[i], 0, output[i], 0, y);
        }
        return output;
    }

    /**
     * Check to see if a player has won based on a board array
     *
     * @param boardView The board state
     * @param player The colour of the player to check
     *
     * @return boolean indicating whether that player has won
     *
     * @throws InvalidColourException Player parameter isn't Piece.RED or Piece.BLUE
     */
    public static boolean playerWon(Piece[][] boardView, Piece player) throws InvalidColourException{
        int sizeX = boardView.length;
        int sizeY = boardView[0].length;
        //Check that the provided parameter is actually a player
        if(player == null || player == Piece.UNSET){
            //player isn't a player colour
            throw new InvalidColourException();
        }
        //Create a map of where the player has pieces.
        Boolean[][] playerPieces = new Boolean[sizeX][sizeY];
        for(int i = 0; i < sizeX; i++){
            for(int j = 0; j < sizeY; j++){
                playerPieces[i][j] = boardView[i][j] == player;
            }
        }

        //declare, initialise unchecked nodes
        List<Coordinate> uncheckedNodes = new ArrayList<Coordinate>();
        if(player == Piece.RED){
            for(int i = 0; i < sizeX; i++){
                if(playerPieces[i][0]){
                    Coordinate coords = new Coordinate(i, 0);
                    uncheckedNodes.add(coords);
                }
            }
        }
        else if(player == Piece.BLUE){
            for(int i = 0; i < sizeY; i++){
                if(playerPieces[0][i]){
                    Coordinate coords = new Coordinate(0, i);
                    uncheckedNodes.add(coords);
                }
            }
        }
        //iterate through unchecked nodes checking for connecting nodes
        while(uncheckedNodes.size() > 0){
            //get the coordinates we're checking
            Coordinate coords = uncheckedNodes.get(0);
            //and it's not unchecked anymore
            uncheckedNodes.remove(0);
            if(coords.valid(sizeX, sizeY)){
                //prevent search returning to this hex
                playerPieces[coords.getX()][coords.getY()] = false;

                //check if it's at the far edge yet
                if((coords.getX() == sizeX - 1 && player == Piece.BLUE) || coords.getY() == sizeY - 1 && player == Piece.RED){
                    return true;
                }

                //if not, look at the adjacent hexes and add them to the list
                List<Coordinate> adjacents = coords.getAdjactents(sizeX, sizeY);
                for(Coordinate newCoords : adjacents){
                    if(playerPieces[newCoords.getX()][newCoords.getY()]){
                        //Place piece into unchecked nodes list in the correct place to maintain sorting
                        playerPieces[newCoords.getX()][newCoords.getY()] = false;
                        boolean found = false;
                        int j = 0;
                        while(!found && j < uncheckedNodes.size()){
                            if(player == Piece.RED){
                                if(newCoords.getY() > uncheckedNodes.get(j).getY()){
                                    found = true;
                                }
                            }
                            else{
                                if(newCoords.getX() > uncheckedNodes.get(j).getX()){
                                    found = true;
                                }
                            }
                            j++;
                        }
                        uncheckedNodes.add(j, newCoords);
                    }
                }
            }
        }
        //Run out of unchecked nodes, not returned true
        return false;
    }
}

