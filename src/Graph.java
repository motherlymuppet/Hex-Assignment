import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A graph stores nodes such that the distance from a start to end node can be calculated.
 *
 * @author Steven Lowes
 */
public class Graph{
    /**
     * The default weight of a node that is NodeType.UNSET
     */
    public static final double UNSET_WEIGHT = 2.0;

    /**
     * The default weight of a node that is NodeType.PLAYER
     */
    public static final double PLAYER_WEIGHT = 0;

    /**
     * The default edge weight modifier of an edge that is EdgeType.STANDARD
     */
    public static final double STANDARD_EDGE_WEIGHT_MODIFIER = 0;

    /**
     * The default edge weight modifier of an edge that is EdgeType.BRIDGE and connects two nodes that are NodeType.PLAYER
     */
    public static final double OBTAINED_BRIDGE_WEIGHT_MODIFIER = +0.01;

    /**
     * The default edge weight modifier of an edge that is EdgeType.BRIDGE and connects one node that is NodeType.PLAYER and another that is NodeType.UNSET
     */
    public static final double SEMI_OBTAINED_BRIDGE_WEIGHT_MODIFIER = +1.01;

    /**
     * The default edge weight modifier of an edge that is EdgeType.BRIDGE and connects two nodes that are NodeType.UNSET
     */
    public static final double UNOBTAINED_BRIDGE_WEIGHT_MODIFIER = +1.01;

    /**
     * The amount of weight to add to an edge that connects two blocked nodes.
     */
    public static final double BLOCKED_WEIGHT_MODIFIER = +1000;

    /**
     * For debugging and tuning - should blocking be used?
     */
    private final boolean useBlocking = true;

    /**
     * For debugging and tuning - should bridge edges be used?
     */
    private final boolean useBridges = true;

    /**
     * Map coordinates to nodes, such that nodes can be referenced by the coordinate they are at.
     */
    private HashMap<Coordinate, Node> nodesMap;

    /**
     * The node that distance starts being calculated from
     */
    private Node startNode;

    /**
     * The node that distance finishes being calculated at
     */
    private Node endNode;

    /**
     * The width of the board
     */
    private int xSize;

    /**
     * The height of the board
     */
    private int ySize;

    /**
     * The colour of the player who this graph represents.
     */
    private Piece colour;

    /**
     * Create the start and end nodes, initialise the node map.
     */
    public Graph(){
        nodesMap = new HashMap<Coordinate, Node>();
        startNode = new Node(0.0, NodeType.STARTEND, new Coordinate(-1, -1));
        endNode = new Node(0.0, NodeType.STARTEND, new Coordinate(-1, -1));
    }

    /**
     * Create nodes and edges representing the boardView passed.
     *
     * @param boardView the board to represent as a graph.
     * @param colour The colour of the player the graph should represent.
     */
    public void populateGraph(Piece[][] boardView, Piece colour){

        //Create nodes
        xSize = boardView.length;
        ySize = boardView[0].length;
        this.colour = colour;
        for(int i = 0; i < xSize; i++){
            for(int j = 0; j < ySize; j++){
                //Make a new node for each position on the board, add them to the map
                Node node;
                Coordinate coords = new Coordinate(i, j);
                if(boardView[i][j] == Piece.UNSET){
                    node = new Node(Graph.UNSET_WEIGHT, NodeType.UNSET, coords);
                }
                else if(boardView[i][j] == colour){
                    node = new Node(Graph.PLAYER_WEIGHT, NodeType.PLAYER, coords);
                }
                else{
                    node = new Node(null, NodeType.OPPONENT, coords);
                }
                nodesMap.put(coords, node);
            }
        }

        for(Node node : nodesMap.values()){
            //Calculate standard edges and bridges for all non-opponent nodes
            if(node.getType() != NodeType.OPPONENT){
                calculateEdges(node);
                calculateBridges(node);
            }
        }

        try{
            //Join the start and end nodes to the relevant edge nodes
            joinStartEnd(startNode);
            joinStartEnd(endNode);
        }
        catch(InvalidPositionException e){
            System.out.println("Start/End node is not NodeType.STARTEND??");
            e.printStackTrace();
        }

        //Update which nodes are blocked
        calculateBlocking();
    }

    /**
     * @return The distance to the endNode from the startNode, using djikstra's algorithm
     */
    public Double getDistance(){
        calculateDistance();
        return endNode.getDistance();
    }

    /**
     * Reset the distances to each node to allow them to be recalculated.
     */
    public void reset(){
        //Reset the distance from startNode for each node
        for(Node node : nodesMap.values()){
            node.setDistance(null);
        }
        startNode.setDistance(null);
        endNode.setDistance(null);
    }

    /**
     * Change a node at a position, and update all the edges affected by this - meaning that a new graph doesn't have to be created each time you want to test a different board.
     *
     * @param coords The coordinates of the node to change
     * @param type The new type of the node
     * @param weight The new weight of the node
     *
     * @return boolean indicating successful operation
     *
     * @throws InvalidPositionException The coordinates passed don't represent a node.
     */
    public boolean changeNode(Coordinate coords, NodeType type, Double weight) throws InvalidPositionException{
        Node node = nodesMap.get(coords);

        if(node == null){
            throw new InvalidPositionException();
        }

        //Update type and weight
        node.setType(type);
        node.setWeight(weight);

        //Remove all edges
        for(Edge edge : node.getConnectedEdges()){
            Node otherNode = edge.getOtherNode(node);
            otherNode.removeEdge(edge);
        }
        node.removeEdges();

        //Recalculate standard edges for this node
        if(node.getType() != NodeType.OPPONENT){
            calculateEdges(node);
        }

        //Remove bridges for adjacents
        List<Coordinate> adjacents = coords.getAdjactents(xSize, ySize);
        for(Coordinate newCoords : adjacents){
            Node updateNode = nodesMap.get(newCoords);
            if(updateNode != null){
                ArrayList<Edge> edges = new ArrayList<Edge>(updateNode.getConnectedEdges());
                for(Edge edge : edges){
                    if(edge.getType() == EdgeType.BRIDGE){
                        edge.getNode1().removeEdge(edge);
                        edge.getNode2().removeEdge(edge);
                    }
                }
            }
        }

        //Recalculate bridges for adjacents
        for(Coordinate newCoords : adjacents){
            Node updateNode = nodesMap.get(newCoords);
            if(updateNode != null && updateNode.getType() != NodeType.OPPONENT){
                calculateBridges(updateNode);
            }
        }

        //Calculate bridges for this node
        if(node.getType() != NodeType.OPPONENT){
            calculateBridges(node);
        }

        //Join start and end
        if(node.getType() != NodeType.OPPONENT){
            if(colour == Piece.RED && (node.getCoords().getY() == 0 || node.getCoords().getY() == ySize - 1)){
                joinStartEnd(startNode);
                joinStartEnd(endNode);
            }
            else if(colour == Piece.BLUE && (node.getCoords().getX() == 0 || node.getCoords().getX() == xSize - 1)){
                joinStartEnd(startNode);
                joinStartEnd(endNode);
            }
        }

        //Recalculate start and end
        ArrayList<Edge> edges = new ArrayList<Edge>(startNode.getConnectedEdges());
        for(Edge edge : edges){
            startNode.removeEdge(edge);
            edge.getOtherNode(startNode).removeEdge(edge);
        }
        edges = new ArrayList<Edge>(endNode.getConnectedEdges());
        for(Edge edge : edges){
            endNode.removeEdge(edge);
            edge.getOtherNode(endNode).removeEdge(edge);
        }
        joinStartEnd(startNode);
        joinStartEnd(endNode);

        //calculate blocking
        calculateBlocking();
        return true;
    }

    /**
     * If there is an adjacent NodeType.OPPONENT node, set the node to be blocked. Update all nodes.
     *
     * @return boolean indicating successful operation
     */
    private boolean calculateBlocking(){
        if(useBlocking){
            for(Node node : nodesMap.values()){
                if(node.getType() == NodeType.UNSET){
                    boolean found = false;
                    int i = 0;
                    List<Coordinate> adjacents = node.getCoords().getAdjactents(xSize, ySize);
                    //Check adjacents for opponent nodes, set blocked if found
                    while(i < adjacents.size() && !found){
                        if(nodesMap.get(adjacents.get(i)).getType() == NodeType.OPPONENT){
                            node.setBlocked(true);
                            found = true;
                        }
                        else{
                            node.setBlocked(false);
                        }
                        i++;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Calculate the standard edges for a node
     *
     * @param node The node to calculate edges for
     *
     * @return boolean indicating successful operation
     */
    private boolean calculateEdges(Node node){
        //Add standard edges between non-opponents
        Coordinate coords = node.getCoords();
        List<Coordinate> adjacents = coords.getAdjactents(xSize, ySize);
        for(Coordinate newCoords : adjacents){
            Node node2 = nodesMap.get(newCoords);
            if(node2 != null && node2.getType() != NodeType.OPPONENT){
                try{
                    new Edge(node, node2, EdgeType.STANDARD, Graph.STANDARD_EDGE_WEIGHT_MODIFIER);
                }
                catch(NodeWeightNotSetException e){
                    System.out.println("Node weight not properly set during population of graph");
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * Calculate the bridge edges (except those connected to the start and end nodes)
     *
     * @param node The node to calculate bridges for
     *
     * @return boolean indicating successful operation
     */
    private boolean calculateBridges(Node node){
        if(useBridges){
            ArrayList<Node> allNodes = new ArrayList<Node>(nodesMap.values());

            if(node.getType() == NodeType.UNSET || node.getType() == NodeType.PLAYER){
                List<Coordinate> bridgeCoords = node.getCoords().getBridges(xSize, ySize);
                //Iterate through the nodes that could have edges to the node passed
                for(Coordinate coords : bridgeCoords){
                    Node node2 = nodesMap.get(coords);
                    if(node2.getType() == NodeType.UNSET || node2.getType() == NodeType.PLAYER){
                        ArrayList<Node> connectedNodes = new ArrayList<Node>();
                        List<Edge> node1ConnectedEdges = node.getConnectedEdges();
                        //List node 1' adjacent nodes
                        for(Edge edge : node1ConnectedEdges){
                            if(edge.getType() == EdgeType.STANDARD){
                                Node otherNode = edge.getOtherNode(node);
                                if(otherNode.getType() == NodeType.UNSET){
                                    connectedNodes.add(edge.getOtherNode(node));
                                }
                            }
                        }
                        int i = 0;
                        List<Edge> node2Connectededges = node2.getConnectedEdges();
                        //Increment i for each NodeType.UNSET node adjacent to both node 1 and node 2
                        for(Edge edge : node2Connectededges){
                            if(edge.getType() == EdgeType.STANDARD){
                                Node otherNode = edge.getOtherNode(node2);
                                if(otherNode.getType() == NodeType.UNSET){
                                    if(connectedNodes.contains(edge.getOtherNode(node2))){
                                        i++;
                                    }
                                }
                            }
                        }
                        //If there are two unset nodes which are adjacent to both node1 and node2, there is a bridge between them
                        if(i == 2){
                            try{
                                //Create a new edge using the correct weight modifier depending on the type of bridge
                                if(node.getType() == NodeType.PLAYER && node2.getType() == NodeType.PLAYER){
                                    new Edge(node, node2, EdgeType.BRIDGE, Graph.OBTAINED_BRIDGE_WEIGHT_MODIFIER);
                                }
                                else if(node.getType() == NodeType.UNSET && node2.getType() == NodeType.UNSET){
                                    new Edge(node, node2, EdgeType.BRIDGE, Graph.UNOBTAINED_BRIDGE_WEIGHT_MODIFIER);
                                }
                                else if(node.getType() == NodeType.UNSET && node2.getType() == NodeType.PLAYER){
                                    new Edge(node, node2, EdgeType.BRIDGE, Graph.SEMI_OBTAINED_BRIDGE_WEIGHT_MODIFIER);
                                }
                                else if(node2.getType() == NodeType.UNSET && node.getType() == NodeType.PLAYER){
                                    new Edge(node, node2, EdgeType.BRIDGE, Graph.SEMI_OBTAINED_BRIDGE_WEIGHT_MODIFIER);
                                }
                            }
                            catch(NodeWeightNotSetException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Create all bridges and standard edges for the start/end node.
     *
     * @param node The node to calculate bridges for.
     *
     * @throws InvalidPositionException The node passed is not startNode or endNode. (The nodeType is not NodeType.STARTEND)
     */
    private boolean joinStartEnd(Node node) throws InvalidPositionException{
        if(node.getType() != NodeType.STARTEND){
            throw new InvalidPositionException();
        }

        if(colour == Piece.RED){
            //add standard edges to NodeType.Player and NodeType.UNSET nodes along the top and bottom
            for(int i = 0; i < xSize; i++){
                try{
                    if(nodesMap.get(new Coordinate(i, 0)).getType() != NodeType.OPPONENT){
                        new Edge(startNode, nodesMap.get(new Coordinate(i, 0)), EdgeType.STARTEND, Graph.STANDARD_EDGE_WEIGHT_MODIFIER);
                    }
                    if(nodesMap.get(new Coordinate(i, ySize - 1)).getType() != NodeType.OPPONENT){
                        new Edge(endNode, nodesMap.get(new Coordinate(i, ySize - 1)), EdgeType.STARTEND, Graph.STANDARD_EDGE_WEIGHT_MODIFIER);
                    }
                }
                catch(NodeWeightNotSetException e){
                    e.printStackTrace();
                }
            }
        }
        else if(colour == Piece.BLUE){
            //add standard edges to NodeType.Player and NodeType.UNSET nodes along the left and right
            for(int i = 0; i < ySize; i++){
                try{
                    if(nodesMap.get(new Coordinate(0, i)).getType() != NodeType.OPPONENT){
                        new Edge(startNode, nodesMap.get(new Coordinate(0, i)), EdgeType.STARTEND, Graph.STANDARD_EDGE_WEIGHT_MODIFIER);
                    }
                    if(nodesMap.get(new Coordinate(xSize - 1, i)).getType() != NodeType.OPPONENT){
                        new Edge(endNode, nodesMap.get(new Coordinate(xSize - 1, i)), EdgeType.STARTEND, Graph.STANDARD_EDGE_WEIGHT_MODIFIER);
                    }
                }
                catch(NodeWeightNotSetException e){
                    e.printStackTrace();
                }
            }

        }
        if(useBridges){
            //Add bridges to the start and end nodes
            ArrayList<Node> otherNodes = new ArrayList<Node>();
            if(node == startNode){
                //Create list of nodes that bridges exist for
                if(colour == Piece.RED){
                    for(int i = 1; i < xSize - 1; i++){
                        otherNodes.add(nodesMap.get(new Coordinate(i, 1)));
                    }
                }
                if(colour == Piece.BLUE){
                    for(int i = 1; i < ySize - 1; i++){
                        otherNodes.add(nodesMap.get(new Coordinate(1, i)));
                    }
                }
            }
            else if(node == endNode){
                //Create list of nodes that bridges exist for
                if(colour == Piece.RED){
                    for(int i = 1; i < xSize - 1; i++){
                        otherNodes.add(nodesMap.get(new Coordinate(i, xSize - 2)));
                    }
                }
                if(colour == Piece.BLUE){
                    for(int i = 1; i < ySize - 1; i++){
                        otherNodes.add(nodesMap.get(new Coordinate(ySize - 2, i)));
                    }
                }
            }
            //Run the bridge code for the nodes listed
            for(Node node2 : otherNodes){
                ArrayList<Node> connectedNodes = new ArrayList<Node>();
                for(Edge edge : node.getConnectedEdges()){
                    if(edge.getType() == EdgeType.STARTEND){
                        Node otherNode = edge.getOtherNode(node);
                        if(otherNode.getType() == NodeType.UNSET){
                            connectedNodes.add(edge.getOtherNode(node));
                        }
                    }
                }
                int i = 0;
                for(Edge edge : node2.getConnectedEdges()){
                    if(edge.getType() == EdgeType.STANDARD){
                        Node otherNode = edge.getOtherNode(node2);
                        if(otherNode.getType() == NodeType.UNSET){
                            if(connectedNodes.contains(edge.getOtherNode(node2))){
                                i++;
                            }
                        }
                    }
                }
                if(i == 2){
                    try{
                        if(node.getType() == NodeType.STARTEND && node2.getType() == NodeType.PLAYER){
                            new Edge(node, node2, EdgeType.BRIDGE, Graph.OBTAINED_BRIDGE_WEIGHT_MODIFIER);
                        }
                        else if(node.getType() == NodeType.UNSET && node2.getType() == NodeType.UNSET){
                            new Edge(node, node2, EdgeType.BRIDGE, Graph.UNOBTAINED_BRIDGE_WEIGHT_MODIFIER);
                        }
                        else if(node.getType() == NodeType.UNSET && node2.getType() == NodeType.PLAYER){
                            new Edge(node, node2, EdgeType.BRIDGE, Graph.SEMI_OBTAINED_BRIDGE_WEIGHT_MODIFIER);

                        }
                        else if(node2.getType() == NodeType.UNSET && node.getType() == NodeType.STARTEND){
                            new Edge(node, node2, EdgeType.BRIDGE, Graph.SEMI_OBTAINED_BRIDGE_WEIGHT_MODIFIER);
                        }
                    }
                    catch(NodeWeightNotSetException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    /**
     * Calculate the distance to the endNode from the startNode using djikstra's algorithm.
     *
     * @return boolean indicating successful operation
     */
    private boolean calculateDistance(){
        /*
        This bit of the code is quite slow as it's called so often in the AI. In an effort to speed it up, it contains some optimisations that can make it hard to read.
        For each loops have been replaced with for loops which deincrement to allow for comparison to 0 as opposed to a variable
        Nodes are added to lists at the appropriate place to maintain sorting, to remove the need for repeated full sorting.
         */
        //The nodes to visit
        ArrayList<Node> testingNodes = new ArrayList<Node>();
        //The nodes already visisted
        ArrayList<Node> testedNodes = new ArrayList<Node>();
        //Never visit endNode
        testedNodes.add(endNode);
        //Start with startNode
        testingNodes.add(startNode);
        //It takes 0 distance to get from startNode to startNode
        startNode.setDistance(0.0);
        //Keep looking until found (returns) or runs out of nodes to test
        while(testingNodes.size() > 0){
            //get the top node, add it to testedNodes and remove it
            Node testingNode = testingNodes.get(0);
            testedNodes.add(testingNode);
            testingNodes.remove(0);
            //Look at the nodes it's connected to
            ArrayList<Edge> edges = testingNode.getConnectedEdges();
            for(int i = edges.size(); i > 0; --i){
                //Try updating their distance
                Node newNode = edges.get(i - 1).addDistance(testingNode);
                //Test the connected node next if it hasn't already been tested
                if(newNode != null && !testingNodes.contains(newNode) && !testedNodes.contains(newNode)){
                    //work through the testingNodes list and place the new node in the correct location
                    int j = 0;
                    int size = testingNodes.size();
                    boolean found = false;
                    while(!found && j < size){
                        if(newNode.compareTo(testingNodes.get(j)) != 1){
                            testingNodes.add(j, newNode);
                            found = true;
                        }
                        ++j;
                    }
                    if(!found){
                        testingNodes.add(newNode);
                    }
                }
            }
        }
        return true;
    }

    /**
     * @return The end node
     */
    public Node getEndNode(){
        return endNode;
    }

    /**
     * Update the endNode to a new value. Useful for calculating the distance across parts of the graph.
     *
     * @param endNode The endNode to set to.
     */
    public void setEndNode(Node endNode){
        this.endNode = endNode;
    }

    /**
     * @return The map of coordinates to nodes
     */
    public HashMap<Coordinate, Node> getNodesMap(){
        return nodesMap;
    }

    /**
     * Set the nodes map to a new map of coordinate to nodes
     *
     * @param nodesMap the nodes map to set
     */
    public void setNodesMap(HashMap<Coordinate, Node> nodesMap){
        this.nodesMap = nodesMap;
    }

    /**
     * @return The start node
     */
    public Node getStartNode(){
        return startNode;
    }

    /**
     * Update the startNode to a new value. Useful for calculating the distance across parts of the graph.
     *
     * @param startNode The startNode to set to.
     */
    public void setStartNode(Node startNode){
        this.startNode = startNode;
    }
}
