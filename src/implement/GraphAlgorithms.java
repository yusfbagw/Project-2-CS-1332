package implement;
import refactor.DisjointSet;
import refactor.Edge;
import refactor.StaticGraph;
import refactor.Vertex;
import refactor.VertexDistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Your implementation of various different graph algorithms.
 */
public class GraphAlgorithms {
    /**
     * Performs a breadth-first search (BFS) on the input graph, starting at
     * the parameterized starting vertex.
     * <p>
     * When exploring a vertex, explore in the order of neighbors returned by
     * the adjacency list. Failure to do so may cause you to lose points.
     *
     * @param <T>   the generic typing of the data
     * @param start the vertex to begin the BFS on
     * @param graph the graph to search through
     * @return list of vertices in visited order
     * @throws IllegalArgumentException if any input is null, or if start
     *                                  doesn't exist in the graph
     */
    public static <T> List<Vertex<T>> bfs(Vertex<T> start, StaticGraph<T> graph) {
        if (start == null) {
            throw new IllegalArgumentException("Start is null");
        }
        if (graph == null) {
            throw new IllegalArgumentException("Graph is null");
        }
        if (!graph.containsVertex(start)) {
            throw new IllegalArgumentException("Graph doesn't contain start vertex");
        }

        Set<Vertex<T>> visitedSet = new HashSet<>();
        Queue<Vertex<T>> queue = new LinkedList<>();
        List<Vertex<T>> list = new ArrayList<>();

        queue.add(start);
        visitedSet.add(start);

        while (!queue.isEmpty()) {
            Vertex<T> curr = queue.remove();
            list.add(curr);

            for (VertexDistance<T> vd : graph.getNeighbors(curr)) {
                Vertex<T> neighbor = vd.vertex();
                if (!visitedSet.contains(neighbor)) {
                    visitedSet.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return list;
    }

    /**
     * Performs a depth-first search (DFS) on the input graph, starting at
     * the parameterized starting vertex.
     * <p>
     * When exploring a vertex, explore in the order of neighbors returned by
     * the adjacency list. Failure to do so may cause you to lose points.
     *
     * @param <T>   the generic typing of the data
     * @param start the vertex to begin the DFS on
     * @param graph the graph to search through
     * @return list of vertices in visited order
     * @throws IllegalArgumentException if any input is null, or if start
     *                                  doesn't exist in the graph
     * @implSpec You MUST implement this method recursively, or else you will
     * lose all points for this method.
     */
    public static <T> List<Vertex<T>> dfs(Vertex<T> start, StaticGraph<T> graph) {
        if (start == null) {
            throw new IllegalArgumentException("Start is null");
        }
        if (graph == null) {
            throw new IllegalArgumentException("Graph is null");
        }
        if (!graph.containsVertex(start)) {
            throw new IllegalArgumentException("Graph doesn't contain start vertex");
        }

        Set<Vertex<T>> vSet = new HashSet<>();
        List<Vertex<T>> list = new ArrayList<>();

        dfs(start, graph, vSet, list);
        return list;
    }

    /**
     * Recursive helper method for DFS.
     *
     * @param <T>   the generic typing of the data
     * @param curr  the current vertex being explored
     * @param g     the graph being searched through
     * @param vSet  the set of vertices that have already been visited
     * @param list  the list of vertices in visited order
     */
    private static <T> void dfs(Vertex<T> curr, StaticGraph<T> g, Set<Vertex<T>> vSet, List<Vertex<T>> list) {
        if (!vSet.contains(curr)) {
            vSet.add(curr);
            list.add(curr);
            //For each vertex distance get the adjacent list and get the current vertex from that.
            for (VertexDistance<T> vd : g.getNeighbors(curr)) {
                //then if the vertex distance's vertex isn't in the visited set then recursive call
                //dfs
                if (!vSet.contains(vd.vertex())) {
                    dfs(vd.vertex(), g, vSet, list);
                }
            }
        }
    }

    /**
     * Finds the single-source shortest distance between the start vertex and
     * all vertices given a weighted graph (you may assume non-negative edge
     * weights).
     * <p>
     * Return a map of the shortest distances such that the key of each entry
     * is a node in the graph and the value for the key is the shortest distance
     * to that node from start, or {@link Integer#MAX_VALUE} (representing
     * infinity) if no path exists.
     *
     * @param <T>   the generic typing of the data
     * @param start the vertex to begin the Dijkstra's on (source)
     * @param graph the graph we are applying Dijkstra's to
     * @return a map of the shortest distances from start to every
     * other node in the graph
     * @throws IllegalArgumentException if any input is null, or if start
     *                                  doesn't exist in the graph.
     */
    public static <T> Map<Vertex<T>, Integer> dijkstras(Vertex<T> start, StaticGraph<T> graph) {
        if (start == null) {
            throw new IllegalArgumentException("Start is null");
        }
        if (graph == null) {
            throw new IllegalArgumentException("Graph is null");
        }
        if (!graph.containsVertex(start)) {
            throw new IllegalArgumentException("Graph doesn't contain start vertex");
        }

        //You need a distance hashmap of type which you're returning
        Map<Vertex<T>, Integer> dist = new HashMap<>();
        //You need a visited set of type vertex<T>
        Set<Vertex<T>> vSet = new HashSet<>();
        //You need a PrioirityQueue holds vertexdistance not vertex
        PriorityQueue<VertexDistance<T>> pq = new PriorityQueue<>();

        //For each vertex in the graph we loop through and
        //put the distance from that into the dist HashMap w/ the max value of the weights
        for (Vertex<T> v : graph.getVertices()) {
            dist.put(v, Integer.MAX_VALUE);
        }

        //Now put dist start with 0 to start here
        dist.put(start, 0);
        pq.add(new VertexDistance<T>(start, 0));

        //Loop while pq is NOT empty AND vSet.size() is less than graph's verticies size
        while (!pq.isEmpty() && vSet.size() < graph.getVertices().size()) {
            VertexDistance<T> curr = pq.poll();
            Vertex<T> u = curr.vertex();

            //If visited doesn't contain u then add u to visitedSet
            if (!vSet.contains(u)) {
                vSet.add(u);

                for (VertexDistance<T> next : graph.getAdjList().get(u)) {
                    //If the visited set doesn't contain the next vertex
                    if (!vSet.contains(next.vertex())) {
                        //new distance = curr dist + next dist
                        int newDist = dist.get(u) + next.distance();
                        if (newDist < dist.get(next.vertex())) {
                            dist.put(next.vertex(), newDist);
                            pq.add(new VertexDistance<>(next.vertex(), newDist));
                        }
                    }
                }
            }
        }
        return dist;
    }

    /**
     * Runs Prim's algorithm on the given graph and returns the Minimum
     * Spanning Tree (MST) in the form of a set of Edges. If the graph is
     * disconnected and therefore no valid MST exists, return null.
     * <p>
     * You may assume that the passed in graph is undirected.
     * <p>
     * @param <T> the generic typing of the data
     * @param start the vertex to begin Prims on
     * @param graph the graph we are applying Prims to
     * @return the MST of the graph or null if there is no valid MST
     * @throws IllegalArgumentException if any input is null, or if start
     *                                  doesn't exist in the graph.
     */
    public static <T> Set<Edge<T>> prims(Vertex<T> start, StaticGraph<T> graph) {
        if (start == null) {
            throw new IllegalArgumentException("Start is null");
        }
        if (graph == null) {
            throw new IllegalArgumentException("Graph is null");
        }
        if (!graph.containsVertex(start)) {
            throw new IllegalArgumentException("Graph doesn't contain start vertex");
        }
        //init Set vSet
        Set<Vertex<T>> vSet = new HashSet<>();

        //init MST
        Set<Edge<T>> mst = new HashSet<>();

        //init pq
        PriorityQueue<Edge<T>> pq = new PriorityQueue<>();
        vSet.add(start);
        for (VertexDistance<T> vd : graph.getNeighbors(start)) {
            pq.add(new Edge<T>(start, vd.vertex(), vd.distance()));
        }

        while (!pq.isEmpty() && vSet.size() < graph.getVertices().size()) {
            Edge<T> curr = pq.poll();
            Vertex<T> vertex = curr.v();

            if (!vSet.contains(vertex)) {
                vSet.add(vertex);
                mst.add(curr);

                for (VertexDistance<T> next : graph.getNeighbors(vertex)) {
                    if (!vSet.contains(next.vertex())) {
                        pq.add(new Edge<T>(vertex, next.vertex(), next.distance()));
                    }
                }
            }

        }

        if (vSet.size() != graph.getVertices().size()) {
            return null;
        }

        return mst;
    }

    /**
     * Runs Kruskal's algorithm on the given graph and returns the Minimal
     * Spanning Tree (MST) in the form of a set of Edges. If the graph is
     * disconnected and therefore no valid MST exists, return null.
     * <p>
     * You may assume that the passed in graph is undirected.
     * <p>
     * @param <T>   the generic typing of the data
     * @param graph the graph we are applying Kruskals to
     * @return the MST of the graph or null if there is no valid MST
     * @throws IllegalArgumentException if any input is null
     */
    public static <T> Set<Edge<T>> kruskals(StaticGraph<T> graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph is null can't be null.");
        }

        DisjointSet<Vertex<T>> ds = new DisjointSet<>();
        Set<Edge<T>> mst = new HashSet<>();
        PriorityQueue<Edge<T>> pq = new PriorityQueue<>(graph.getEdges());

        //Similar mst.size() < (graph.getVertices().size() - 1) * 2
        while (!pq.isEmpty() && mst.size() < graph.getVertices().size() - 1) {
            Edge<T> e = pq.poll();

            if (!ds.find(e.u()).equals(ds.find(e.v()))) {
                mst.add(e);
                ds.union(e.u(), e.v());
            }
        }

        if (mst.size() != (graph.getVertices().size() - 1)) {
            return null;
        }
        return mst;
    }
}