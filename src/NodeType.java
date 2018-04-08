/**
 * PLAYER nodes belong to the player (i.e. player colour is Piece.RED and node colour is Piece.RED) OPPONENT nodes belong to the opponent (i.e. player colour is Piece.RED and node
 * colour is Piece.BLUE) UNSET nodes are Piece.UNSET STARTEND nodes are the start and end nodes
 *
 * @author Steven Lowes
 */
public enum NodeType{
    /**
     * PLAYER nodes belong to the player (i.e. player colour is Piece.RED and node colour is Piece.RED)
     */
    PLAYER,

    /**
     * OPPONENT nodes belong to the opponent (i.e. player colour is Piece.RED and node colour is Piece.BLUE)
     */
    OPPONENT,

    /**
     * UNSET nodes are Piece.UNSET
     */
    UNSET,

    /**
     * STARTEND nodes are the start and end nodes
     */
    STARTEND
}
