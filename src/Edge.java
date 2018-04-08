/**
 * An Edge connects two Nodes. Edges are traversed when running djikstra's algorithm.
 *
 * @author Steven Lowes
 */
public class Edge{

    /**
     * A node which the edge connects to another node.
     */
    private final Node node1;

    /**
     * A node which the edge connects to another node.
     */
    private final Node node2;

    /**
     * For additional flexibility, weight modifiers can be added. This value is added to the weight when weight is calculated. This means that bridges can have a different weight
     * to standard edges.
     */
    private final double weightModifier;

    /**
     * The type of edge.
     */
    private final EdgeType type;

    /**
     * The "cost" of traversing the edge. Calculated as the average of the weight of the two connecting nodes, plus the weightModifier.
     */
    private double weight;

    /**
     * Create a new node, initialising the fields. The weight is set, equal to the average of the weights of the nodes, plus the weightModifier. Add this edge to the list of
     * connected edges on both nodes.
     *
     * @param node1 One node to connect
     * @param node2 Another node to connect
     * @param type The type of edge
     * @param weightModifier The amount to increase the weight by
     *
     * @throws NodeWeightNotSetException Either node has null weight
     */
    public Edge(Node node1, Node node2, EdgeType type, double weightModifier) throws NodeWeightNotSetException{
        this.weightModifier = weightModifier;
        this.node1 = node1;
        this.node2 = node2;
        this.weight = (node1.getWeight() + node2.getWeight()) / 2 + weightModifier;
        node1.addEdge(this);
        node2.addEdge(this);
        this.type = type;
    }

    /**
     * Update the weight field. The weight is set equal to the average of the weights of the nodes, plus the weightModifier.
     *
     * @return boolean indicating successful execution.
     *
     * @throws NodeWeightNotSetException Either node has null weight
     */
    public boolean updateWeight() throws NodeWeightNotSetException{
        this.weight = (node1.getWeight() + node2.getWeight()) / 2 + weightModifier;
        return true;
    }

    /**
     * @return String, style "edgeType node1 - node2"
     */
    public String toString(){
        return type + " " + node1 + " - " + node2;
    }

    /**
     * @return edge type
     */
    public EdgeType getType(){
        return type;
    }

    /**
     * Add the distance of this edge to the other node (as opposed to the callingNode parameter). If node1 and node2 are "blocked" i.e. have an adjacent NodeType.OPPONENT node, add
     * the distance of this edge + Graph.BLOCKED_WEIGHT_MODIFIER. Only calls setDistance on the node if the new distance is less than its current distance.
     *
     * @param callingNode the node calling addDistance
     *
     * @return The other node, or null if callingNode is not either node1 or node2. If returning null, no other code has been excecuted.
     */
    public Node addDistance(Node callingNode){
        Node otherNode;
        if(callingNode == node1){
            otherNode = node2;
        }
        else if(callingNode == node2){
            otherNode = node1;
        }
        else{
            return null;
        }

        //Calculate the distance to get to that node from this one
        double testingDistance;
        Double prevDistance = callingNode.getDistance();
        if(prevDistance == null){
            prevDistance = 0.0;
        }
        testingDistance = prevDistance + weight;

        //If the calculated value is less than its current value, or it has yet to be visited, update its distance (Adding extra distance for blocked standard edges)
        if(callingNode.isBlocked() && otherNode.isBlocked() && type == EdgeType.STANDARD){
            if(otherNode.getDistance() == null || testingDistance + Graph.BLOCKED_WEIGHT_MODIFIER < otherNode.getDistance()){
                otherNode.setDistance(testingDistance + Graph.BLOCKED_WEIGHT_MODIFIER);
            }
        }
        else{
            if(otherNode.getDistance() == null || testingDistance < otherNode.getDistance()){
                otherNode.setDistance(testingDistance);
            }

        }
        return otherNode;
    }

    /**
     * Return node1 or node2, whichever is not passed as a parameter.
     *
     * @param node The node you don't want - returns the other node.
     *
     * @return The node you didn't provide as a parameter, or null if the node provided is neither node1 or node2.
     */
    public Node getOtherNode(Node node){
        if(node == node1){
            return node2;
        }
        else if(node == node2){
            return node1;
        }
        else{
            return null;
        }
    }

    /**
     * @return node1
     */
    public Node getNode1(){
        return node1;
    }

    /**
     * @return node2
     */
    public Node getNode2(){
        return node2;
    }

    /**
     * Remove this node from both of its nodes' connectedEdges lists.
     *
     * @return boolean indicating successful completion
     */
    public boolean remove(){
        node1.removeEdge(this);
        node2.removeEdge(this);
        return true;
    }

    @Override
    /**
     * Compare: weight, node1 and node2 equal in any combination (node1 == node2 and vice versa is also acceptable)
     */ public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }

        Edge edge = (Edge) o;

        if(Double.compare(edge.weight, weight) != 0){
            return false;
        }
        if(node1.equals(edge.getNode1())){
            return node2.equals(edge.getNode2());
        }
        else{
            return node1.equals(edge.getNode2()) && node2.equals(edge.getNode1());
        }
    }
}
