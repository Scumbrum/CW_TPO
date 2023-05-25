import java.util.*;

public class DijkstraAlgorithm {
    public int[] distance;
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

    public void dijkstra(List<List<Node>> graph, int sourceVertex) {
        this.adjacencylist = graph;

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

                    var existed = pq.stream()
                            .filter(node -> node.node == neighbor.node)
                            .findFirst();

                    existed.ifPresent(node -> pq.remove(node));

                    pq.add(new Node(neighbor.node, distance[neighbor.node]));
                }
            }
        }
    }
}
