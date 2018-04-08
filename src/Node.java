import java.util.ArrayList;

/**
 * Created by Steven on 13/04/2016.
 */
public class Node implements Comparable<Node>{
    /**
     * The coordinates of the node
     */
    private final Coordinate coords;

    /**
     * The edges connected to the node
     */
    private ArrayList<Edge> connectedEdges;

    /**
     * The weight of this node - entering/exiting the node incurs a cost of half this node's weight
     */
    private Double weight;

    /**
     * The type of node
     */
    private NodeType type;

    /**
     * How much distance it takes to get to this node from the startNode
     */
    private Double distanceFromStart;

    /**
     * Does this node have an adjacent NodeType.OPPONENT node?
     */
    private boolean blocked;

    /**
     * Create a new node and initialise all fields.
     *
     * @param weight The weight of the node
     * @param type The type of the node
     * @param coords The coordinates of the node
     */
    public Node(Double weight, NodeType type, Coordinate coords){
        this.type = type;
        connectedEdges = new ArrayList<Edge>();
        this.weight = weight;
        distanceFromStart = null;
        this.coords = coords;
        this.blocked = false;
    }

    /**
     * @return String in style: "type coords: distanceFromStart"
     */
    public String toString(){
        return type.toString() + " " + coords.toString() + ": " + distanceFromStart;
    }

    /**
     * @return boolean indicating blocking
     */
    public boolean isBlocked(){
        return blocked;
    }

    /**
     * Set blocking value to the value passed as a parameter
     *
     * @param blocked The new value for blocked
     */
    public void setBlocked(boolean blocked){
        this.blocked = blocked;
    }

    /**
     * @return The coordinates of the Node
     */
    public Coordinate getCoords(){
        return coords;
    }

    /**
     * @return The weight of the node
     *
     * @throws NodeWeightNotSetException The weight is equal to null - the weight has not been correctly set.
     */
    public Double getWeight() throws NodeWeightNotSetException{
        if(weight == null){
            throw new NodeWeightNotSetException();
        }
        else{
            return weight;
        }
    }

    /**
     * Set the weight to be equal to the weight parameter
     *
     * @param weight The new value of weight
     */
    public void setWeight(Double weight){
        this.weight = weight;
    }

    /**
     * @return The distance to get to this node from the startNode. Will be null if not yet calculated
     */
    public Double getDistance(){
        return distanceFromStart;
    }

    /**
     * Set the distance to this node to be equal to the value.
     *
     * @param distance The new value of distance
     */
    public void setDistance(Double distance){
        this.distanceFromStart = distance;
    }

    /**
     * @return The type of the node
     */
    public NodeType getType(){
        return type;
    }

    /**
     * Set the type of the node to be equal to the parameter
     *
     * @param type The new type for this node
     */
    public void setType(NodeType type){
        this.type = type;
    }

    /**
     * Add an edge to the list of edges. Doesn't add duplicates.
     *
     * @param edge The edge to add to the list
     */
    public void addEdge(Edge edge){
        if(!connectedEdges.contains(edge)){
            connectedEdges.add(edge);
        }
    }

    /**
     * Remove the specified edge from the list of edges.
     *
     * @param edge The edge to remove
     */
    public void removeEdge(Edge edge){
        connectedEdges.remove(edge);
    }

    /**
     * Remove all edges from the list of edges such that it becomes an empty list
     */
    public void removeEdges(){
        connectedEdges = new ArrayList<Edge>();
    }

    /**
     * @return The list of edges connected to this node.
     */
    public ArrayList<Edge> getConnectedEdges(){
        return connectedEdges;
    }

    /**
     * Provide a new list of edges and set the list of edges connected to this node to be equal to the provided value. Doesn't clone the list so be sure to provide a copy if
     * multiple nodes are going to share the same list originally and then change their lists.
     *
     * @param connectedEdges The new list.
     */
    private void setConnectedEdges(ArrayList<Edge> connectedEdges){
        this.connectedEdges = connectedEdges;
    }

    @Override public int compareTo(Node o){
        return distanceFromStart.compareTo(o.getDistance());
    }

    @Override public boolean equals(Object o){
        return this == o;
    }
}
