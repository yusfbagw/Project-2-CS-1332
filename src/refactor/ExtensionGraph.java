package refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ExtensionGraph<T> extends MutableGraph<T> {
    private final Map<Vertex<T>, List<VertexDistance<T>>> adjList;

    public ExtensionGraph(Set<Vertex<T>> vertices, Set<Edge<T>> edges){
        super(vertices, edges);
        //Validation Loop
        for (Edge<T> edge : edges) {
            if (!vertices.contains(edge.u()) || !vertices.contains(edge.v())) {
                throw new IllegalArgumentException("One or both of the endpoints aren't in the vertices.");
            }
        }

        adjList = new HashMap<>();
        for (Vertex<T> vertex : vertices) {
            adjList.put(vertex, new ArrayList<>());
        }

        for (Edge<T> edge : edges) {
            adjList.get(edge.u()).add(new VertexDistance<>(edge.v(), edge.weight()));
            adjList.get(edge.v()).add(new VertexDistance<>(edge.u(), edge.weight()));
        }
 
}
    @Override
    public Map<Vertex<T>, List<VertexDistance<T>>> getAdjList() {
        return adjList;
    }


    @Override
    public List<VertexDistance<T>> getNeighbors(Vertex<T> vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex can't be null.");
        }

        if (!adjList.containsKey(vertex)) {
            throw new IllegalArgumentException("Vertex isn't in the graph.");
        }
        return adjList.get(vertex);
    }

    public void addVertex(Vertex<T> vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex can't be null.");
        }
        if (adjList.containsKey(vertex)) {
            throw new IllegalArgumentException("Vertex already in graph.");
        }

        vertices.add(vertex);
        adjList.put(vertex, new ArrayList<>());
    }

    public void removeVertex(Vertex<T> vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex can't be null.");
        }
        if (!adjList.containsKey(vertex)) {
            throw new IllegalArgumentException("Vertex isnt in the graph.");
        }
        for (VertexDistance<T> neighbor : new ArrayList<>(getNeighbors(vertex))) {
            adjList.get(neighbor.vertex()).remove(new VertexDistance<>(vertex, neighbor.distance()));
        }

        edges.removeIf(edge -> edge.u().equals(vertex) || edge.v().equals(vertex));
        adjList.remove(vertex);
        vertices.remove(vertex);
    }
    
    //edge
    public void addEdge(Edge<T> edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge can't be null.");
        }

        if (edge.u().equals(edge.v())) {
            throw new IllegalArgumentException("Edges can't be a self loop");
        }

        //adjList contains the verticies so we wouldn't check for the verticies similar to add vertex above
        //since that contains keys.
        //instead we would look at the edges set.
        if (edges.contains(edge)) {
            throw new IllegalArgumentException("Edge already in graph.");
        }

        //we check for the endpoints existing before touching their lists.
        //Because the two population lines read from the lists via adjList.get(...)
        //otherwise returns null if endpoint was never added as a key.
        if (!containsVertex(edge.u())) {
            addVertex(edge.u());
        }
        if (!containsVertex(edge.v())) {
            addVertex(edge.v());
        }

        edges.add(edge);
        adjList.get(edge.u()).add(new VertexDistance<>(edge.v(), edge.weight()));
        adjList.get(edge.v()).add(new VertexDistance<>(edge.u(), edge.weight()));
    }

    public void removeEdge(Edge<T> edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge can't be null.");
        }
        if (!edges.contains(edge)) {
            throw new IllegalArgumentException("Edge isnt in the graph.");
        }

        //Same thing as from addEdge just flipped to remove.
        edges.remove(edge);
        adjList.get(edge.u()).remove(new VertexDistance<>(edge.v(), edge.weight()));
        adjList.get(edge.v()).remove(new VertexDistance<>(edge.u(), edge.weight()));
    }
}