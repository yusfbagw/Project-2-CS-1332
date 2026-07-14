package apply;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.lang.model.type.IntersectionType;

import apply.Intersection.Type;
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
            throw new IllegalArgumentException("The buidling A can't be null.");
        }
        if (b == null) {
            throw new IllegalArgumentException("The buidling B can't be null.");
        }
        //No closest intersection
        if (a.closest() != null && !roads.containsVertex(new Vertex<>(a.closest()))) {
            throw new IllegalArgumentException("No route at all.");
        }
        if (b.closest() != null && !roads.containsVertex(new Vertex<>(b.closest()))) {
            throw new IllegalArgumentException("No route at all.");
        }
        
        boolean aInGrid = grid.containsVertex(new Vertex<>(a));
        boolean bInGrid = grid.containsVertex(new Vertex<>(b));
        boolean gridNonEmpty = grid.getVertexCount() != 0;

        if (!aInGrid && !bInGrid && gridNonEmpty) {
            throw new IllegalArgumentException("Adding this wire would disconect the grid.");
        }

        grid.addEdge(new Edge<>(new Vertex<>(a), new Vertex<>(b), length));
    }

    /**
     * Gets the current state of the road network.
     *
     * @return the current state of the road network
     */
    public StaticGraph<Intersection> getRoads() {
        return roads;
    }
     /**
     * Gets the current state of the electrical grid.
     *
     * @return the current state of the electrical grid
     */
    public StaticGraph<Building> getGrid() {
        return grid;
    }
    
    /**
     * Gets a view of certain Intersections organized by neighborhood,
     * specifically Intersections with a number of adjacent roads in an 
     * inclusive range [i, j].
     * <p>
     * This should return a map where the keys are the neighborhoods'
     * representative Intersections, and the values are a set of all
     * Intersections in the same neighborhood as the representative
     * Intersection that have a number of adjacent roads in the range.
     *
     * @param i the lower bound of the number adjacent of roads to consider
     * @param j the upper bound of the number adjacent of roads to consider
     * @return a curated map of the road network organized by neighborhoods
     * @implSpec
     * <p> {@code O(|R|+|I|)} runtime
     */
    @Override
    public Map<Intersection, Set<Intersection>> getNeighborhoodsByConnections(int i, int j) {
        if (i < 0) {
            throw new IllegalArgumentException("i can't be negative.");
        }
        if (j < 0) {
            throw new IllegalArgumentException("j can't be negative.");
        }
        if (i > j) {
            throw new IllegalArgumentException("lower bound can't be greater than the upper bound.");
        }
        //Create new map that is going to store the returned neighborhoods.
        Map<Intersection, Set<Intersection>> newMap = new HashMap<>();
        
        //Filter by degree loop
        Map<Vertex<Intersection>, List<VertexDistance<Intersection>>> adjList = roads.getAdjList();
        for (Map.Entry<Vertex<Intersection>, List<VertexDistance<Intersection>>> entry : adjList.entrySet()) {
            
            Vertex<Intersection> vertex = entry.getKey();
            int degree = entry.getValue().size();

            if (i <= degree && degree <= j) {
                Intersection root = neighborhoods.find(vertex.data());
                
                //If this root has no set yet then create an empty one
                if (!newMap.containsKey(root)) {
                    newMap.put(root, new HashSet<>());
                }

                //Now that the set exists just add the intersection to it.
                newMap.get(root).add(vertex.data());
            }
        }
        return newMap;
    }

    /**
     * Gets the minimum number of roads to travel between two Buildings.
     * If no path exists between the two Buildings, return -1.
     *
     * @param from the start Building
     * @param to the end Building
     * @return the minimum number of roads between the Buildings
     * @implSpec {@code O(|I|+|R|)} runtime
     */
    @Override
    public int getMinBetween(Building from, Building to) {
        if (from == null) {
            throw new IllegalArgumentException("From building can't be null.");
        }
        if (to == null) {
            throw new IllegalArgumentException("To building can't be null.");
        }

        Vertex<Building> fromBuild = new Vertex<>(from);
        Vertex<Building> toBuild = new Vertex<>(to);
        if (!grid.containsVertex(fromBuild)) {
            throw new IllegalArgumentException("From building is not in the graph.");
        }
        if (!grid.containsVertex(toBuild)) {
            throw new IllegalArgumentException("To building is not in the graph.");
        }
        //Different from addWire because here closest is NULL not if closest intersection isn't in the road graph.
        if (from.closest() == null) {
            throw new IllegalArgumentException("Building has no closest intersection.");
        }
        if (to.closest() == null) {
            throw new IllegalArgumentException("Building has no closest intersection.");
        }

        //We get the start and ending points.
        Intersection start = from.closest();
        Intersection end = to.closest();

        //Start is turned into a vertex
        Vertex<Intersection> startVertex = new Vertex<>(start);
        //End is turned into a vertex
        Vertex<Intersection> endVertex = new Vertex<>(end);

        //We use BFS so we use a queue and a distance map.
        // We use a dist map over a vSet because does double if a vertex is a key it's been visited and its value is the hop count to reach it
        Queue<Vertex<Intersection>> queue = new LinkedList<>();
        Map<Vertex<Intersection>, Integer> dist = new HashMap<>();

        //The distance map starts off knowing that the start is 0 hops away.
        dist.put(startVertex, 0);
        queue.add(startVertex);
        //BFS
        while (!queue.isEmpty()) {
            Vertex<Intersection> curr = queue.remove();
            for (VertexDistance<Intersection> vd : roads.getNeighbors(curr)) {
                Vertex<Intersection> neighbor = vd.vertex();
                if (!dist.containsKey(neighbor)) {
                    dist.put(neighbor, dist.get(curr)+ 1);
                    queue.add(neighbor);
                }
            }
        }
        //Checking no route found
        if (!dist.containsKey(endVertex)) {
            return -1;
        }
        return dist.get(endVertex);
    }

    @Override
    public List<Intersection> calculateRoute(Building from, Building to, Set<Type> avoid) {
        if (from == null) {
            throw new IllegalArgumentException("From building can't be null.");
        }
        if (to == null) {
            throw new IllegalArgumentException("To building can't be null.");
        }

        Vertex<Building> fromBuild = new Vertex<>(from);
        Vertex<Building> toBuild = new Vertex<>(to);
        if (!grid.containsVertex(fromBuild)) {
            throw new IllegalArgumentException("From building is not in the graph.");
        }
        if (!grid.containsVertex(toBuild)) {
            throw new IllegalArgumentException("To building is not in the graph.");
        }
        //Different from addWire because here closest is NULL not if closest intersection isn't in the road graph.
        if (from.closest() == null) {
            throw new IllegalArgumentException("Building has no closest intersection.");
        }
        if (to.closest() == null) {
            throw new IllegalArgumentException("Building has no closest intersection.");
        }


    }

     /**
     * Given a list of {@code k} candidate Buildings, select the one
     * that would be most "central" to the grid as a power site.
     * <p>
     * The most central power site is one that minimizes the average
     * distance to any arbitrary Building in the electrical grid graph.
     *
     * @param candidates the list of Building candidates
     * @return the best candidate to be a power site
     * @implSpec {@code O(k(|W|log(|W|)))} runtime
     */
    @Override
    public Building getBestPowerSite(List<Building> candidates) {
        
    }
}  
