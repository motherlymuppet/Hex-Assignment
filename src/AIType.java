/**
 * The AI algorithm that should be used
 *
 * @author Steven Lowes
 */
public enum AIType{
    /**
     * Monte-Carlo tree search
     */
    MCTS,

    /**
     * Depth 2 search using djikstra's algorithm to detemine the value of a board position
     */
    DJIKSTRA,

    /**
     * Determine the best AI based on board and game conditions.
     */
    COMBO
}
