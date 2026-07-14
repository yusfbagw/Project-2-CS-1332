package apply;
import java.util.List;
import java.util.Map;

import javax.lang.model.type.IntersectionType;

import implement.GraphAlgorithms;

import refactor.DisjointSet;
import refactor.DisjointSetNode;
import refactor.Edge;
import refactor.ExtensionGraph;
import refactor.MutableGraph;
import refactor.StaticGraph;
import refactor.Vertex;
import refactor.VertexDistance;

public class WaddleWorks implements StaticWaddleWorks { 
    //The 
    private final MutableGraph<Intersection> roads;
    private final MutableGraph<Building> grid;
    private final DisjointSet<Intersection> neighborhoods;
    
    public WaddleWorks(MutableGraph<Intersection> initialRoad, MutableGraph<Building> initialGrid) {
        if (initialRoad == null) {
            throw new IllegalArgumentException("Initial road is null.");
        }
        if (initialGrid == null) {
            throw new IllegalArgumentException("Initial grid is null.");
        }

        //Store both graphs
        this.roads = initialRoad;
        this.grid = initialGrid;
        this.neighborhoods = new DisjointSet<>();

        // We register every intersection first (handles isolated vertices + primes union)
        for (Vertex<Intersection> v : roads.getVertices()) {
            neighborhoods.find(v.data());
        }   

        Map<Vertex<Intersection>, List<VertexDistance<Intersection>>> adjList = roads.getAdjList();
        for (Map.Entry<Vertex<Intersection>, List<VertexDistance<Intersection>>> entry : adjList.entrySet()) {
            for (VertexDistance<Intersection> vd : entry.getValue()) {
                Intersection from = entry.getKey().data();
                Intersection to = vd.vertex().data();

                neighborhoods.union(from, to);
            }
        }
    }
    /**
     * Gets the number of neighborhoods in the road network.
     * <p>
     * A group of Intersections is considered to be in the same
     * neighborhood if each Intersection can reach every other
     * Intersection.
     *
     * @return the number of neighborhoods
     * @implSpec {@code O(1)} runtime
     */
    public int getNeighborhoodCount() {
        return neighborhoods.getRoots().size();
    }

    /**
     * Adds a road to the road network.
     * <p>
     * If the added road connects two neighborhoods,
     * then return a true. Otherwise, return false.
     *
     * @param a the Intersection at one end of the road
     * @param b the Intersection at the other end of the road
     * @param duration the time it takes to traverse the road
     * @return whether the edge connects two neighborhoods
     * @implSpec {@code O(1)} runtime
     */
    public boolean addRoad(Intersection a, Intersection b, int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Duration can't be negative.");
        }
        if (a == null || b == null) {
            throw new IllegalArgumentException("Intersection can't be null.");
        }

        //We create the verticies for the intersections.
        Vertex<Intersection> vertexA = new Vertex<>(a);
        Vertex<Intersection> vertexB = new Vertex<>(b);
        //We gotta look at if it contains both.
        boolean containsBoth = roads.containsVertex(vertexB) && roads.containsVertex(vertexA);
        
        //They have to be in different neighborhoods.
        //We figure out if a and b already existed in roads.
        boolean connectsTwo = containsBoth && !(neighborhoods.find(a).equals(neighborhoods.find(b)));
        
        Edge<Intersection> newEdge = new Edge<>(vertexA, vertexB, duration);
        
        roads.addEdge(newEdge);
        neighborhoods.union(a, b);
        return connectsTwo;
    }

    /**
     * Adds a wire to the electrical grid.
     *
     * @param a the Building at one end of the wire
     * @param b the Building at the other end of the wire
     * @param length the length of the wire
     * @implSpec {@code O(1)} runtime
     */
    public void addWire(Building a, Building b, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("The length can't be negative.");
        }
        if (a == null) {
            throw new IllegalArgumentException("The buidling A can't be negative.");
        }
        if (b == null) {
            throw new IllegalArgumentException("The buidling B can't be negative.");
        }

        
    }
}  
