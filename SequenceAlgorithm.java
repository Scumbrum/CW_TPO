import java.util.Arrays;

public class SequenceAlgorithm {
    static int countVertex = 1_000;
    static int countEdge = 800_000;

    static int averowEdge;
    static int extraEdge;

    public static void main(String[] args) {

        averowEdge = countEdge / countVertex;
        extraEdge = countEdge % countVertex;

        int[][] graph = new int[countVertex][];

        for (int i = 0; i < countVertex; i++) {
            int edgeCount = (i < extraEdge) ? averowEdge + 1 : averowEdge;
            graph[i] = new int[countVertex];
            for (int j = 0; j < edgeCount; j++) {
                double rand = Math.random();
                int dist = (int) (rand * 25);
                int node = (int) (rand * countVertex);
                graph[i][node] = dist;
            }
        }

        DijkstraAlgorithm dijkstraAlgorithm = new DijkstraAlgorithm(countVertex);

        double startTime = System.currentTimeMillis();

        dijkstraAlgorithm.dijkstra(graph, 0);

        double endTime = System.currentTimeMillis();

        System.out.println("Time: " + (endTime - startTime));

//        System.out.println("Matrix: ");
//        for(int i = 0; i <graph.length; i++) {
//            System.out.println(Arrays.toString(graph[i]));
//        }
//
//        System.out.println("The shortest path to all vertices from the source vertex " + countVertex + " is: ");
//        for (int i = 0; i < countVertex; i++) {
//            System.out.println("Vertex " + i + ": " + dijkstraAlgorithm.distance[i]);
//        }
    }
}