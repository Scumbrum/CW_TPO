import java.util.*;

public class DijkstraAlgorithm {
    public int[] distance;
    public Set<Integer> settled;
    public PriorityQueue<Node> pq;
    public int numVertices;
    int[][] graph;

    public DijkstraAlgorithm(int numVertices) {
        this.numVertices = numVertices;
        distance = new int[numVertices];
        settled = new HashSet<>();
        pq = new PriorityQueue<>(numVertices, new Node());

        for (int i = 0; i < numVertices; i++) {
            distance[i] = Integer.MAX_VALUE;
        }
    }

    public void dijkstra(int[][] graph, int sourceVertex) {
        this.graph = graph;

        pq.add(new Node(sourceVertex, 0));
        distance[sourceVertex] = 0;

        while (settled.size() != numVertices) {
            if (pq.size() == 0) break;
            int currentVertex = pq.remove().node;
            settled.add(currentVertex);
            evaluateNeighbors(currentVertex);
        }
    }

    private void evaluateNeighbors(int currentVertex) {
        for (int neighbor = 0; neighbor < graph[currentVertex].length; neighbor++ ) {
            if (!settled.contains(neighbor)) {
                int edgeWeight = graph[currentVertex][neighbor];
                if (edgeWeight == 0) continue;

                int newDistance = distance[currentVertex] + edgeWeight;

                if (newDistance < distance[neighbor]) {
                    distance[neighbor] = newDistance;

                    int finalNeighbor = neighbor;

                    var existed = pq.stream()
                            .filter(node -> node.node == finalNeighbor)
                            .findFirst();

                    existed.ifPresent(node -> pq.remove(node));

                    pq.add(new Node(neighbor, distance[neighbor]));
                }
            }
        }
    }
}
