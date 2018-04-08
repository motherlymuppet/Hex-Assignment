/**
 * The type of an edge
 *
 * @author Steven Lowes
 */
public enum EdgeType{
    /**
     * STANDARD edges are the expected edges that connect adjacent nodes.
     */
    STANDARD,

    /**
     * BRIDGE edges are edges that connect two nodes which share two adjacent nodes, both of which are NodeType.UNSET.
     *
     * See here: https://en.wikipedia.org/wiki/Hex_(board_game)#/media/File:Hex_situation_bridge.svg
     */
    BRIDGE,

    /**
     * STARTEND edges are edges that connect nodes to the startNode and endNode.
     */
    STARTEND
}
