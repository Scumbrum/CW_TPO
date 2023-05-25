import java.util.ArrayList;
import java.util.List;

public class SequenceAlgorithm {
    static final int INF = Integer.MAX_VALUE;
    static int sizeMatrix = 15000;

    public static void main(String[] args) {
        int numVertices = sizeMatrix;
        int sourceVertex = 0;

        List<List<Node>> adjacencylist = new ArrayList<>();
        for(int i = 0; i < sizeMatrix; i++) {
            adjacencylist.add(new ArrayList<>());
            for (int j = 0; j <sizeMatrix; j++ ) {
                double rand = Math.random();
                if(rand <0.5) {
                    adjacencylist.get(i).add(new Node(j, j));
                }

            }
        }
        double startTime = System.currentTimeMillis();

        DijkstraAlgorithm dijkstraAlgorithm = new DijkstraAlgorithm(numVertices);

        dijkstraAlgorithm.dijkstra(adjacencylist, sourceVertex);

        double endTime = System.currentTimeMillis();

        System.out.println("Time: " + (endTime - startTime));

//        System.out.println("The shortest path to all vertices from the source vertex " + sourceVertex + " is: ");
//        for (int i = 0; i < numVertices; i++) {
//            System.out.println("Vertex " + i + ": " + dijkstraAlgorithm.distance[i]);
//        }
    }
}