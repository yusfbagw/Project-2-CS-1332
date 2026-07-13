package refactor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExtensionGraph<T> extends MutableGraph<T> {
    public ExtensionGraph(Set<Vertex<T>> vertices, Set<Edge<T>> edges){
        if (vertices == null || edges == null) {
            throw new IllegalArgumentException("vertices and or edges cannot be null.");
        }
        this.vertices = new HashSet<>(vertices);
        this.edges = new HashSet<>(edges);
    }
}
