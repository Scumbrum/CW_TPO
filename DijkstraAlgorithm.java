import java.util.*;

public class DijkstraAlgorithm {
    public int distance[];
    public Set<Integer> settled;
    public PriorityQueue<Node> pq;
    public int numVertices;
    List<List<Node>> adjacencylist;

    public DijkstraAlgorithm(int numVertices) {
        this.numVertices = numVertices;
        distance = new int[numVertices];
        settled = new HashSet<>();
        pq = new PriorityQueue<>(numVertices, new Node());
    }

    private List<List<Node>> graphToList(int[][] graph) {
        List<List<Node>> adjacencylist = new ArrayList<>();
        for(int i = 0; i < graph.length; i++) {
            adjacencylist.add(new ArrayList<>());
            for (int j = 0; j <graph.length; j++ ) {
                if(graph[i][j] < Integer.MAX_VALUE) {
                    adjacencylist.get(i).add(new Node(j, graph[i][j]));
                }

            }
        }
        return adjacencylist;
    }

    public void dijkstra(int[][] graph, int sourceVertex) {
        this.adjacencylist = graphToList(graph);

        for (int i = 0; i < numVertices; i++) {
            distance[i] = Integer.MAX_VALUE;
        }

        pq.add(new Node(sourceVertex, 0));
        distance[sourceVertex] = 0;

        while (settled.size() != numVertices) {
            int currentVertex = pq.remove().node;
            settled.add(currentVertex);
            evaluateNeighbors(currentVertex);
        }
    }

    private void evaluateNeighbors(int currentVertex) {
        for (Node neighbor : adjacencylist.get(currentVertex)) {
            if (!settled.contains(neighbor.node)) {
                int edgeWeight = neighbor.cost;
                int newDistance = distance[currentVertex] + edgeWeight;

                if (newDistance < distance[neighbor.node]) {
                    distance[neighbor.node] = newDistance;

                    Collection collection = pq.stream()
                            .filter(node -> node.node == neighbor.node).toList();

                    pq.remove(collection);

                    pq.add(new Node(neighbor.node, distance[neighbor.node]));
                }
            }
        }
    }
}
