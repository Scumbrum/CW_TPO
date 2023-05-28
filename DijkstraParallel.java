import mpi.MPI;

import java.util.Arrays;

public class DijkstraParallel {
    static final int INF = Integer.MAX_VALUE;
    static int[] dist;
    static boolean[] visited;
    static  int[][] graph;
    static int countVertex = 15_000;
    static int countEdge = 2_250_000;
    static int n = 0;
    static int averow = 0;
    static int vertexPerProcess = 4;
    static int extra = 0;
    static int averowEdge;
    static int extraEdge;
    static int[] counts;
    static int[] displs;
    static int currentRows;
    static int[][] subGraph = new int[0][];
    static int offset = 0;
    static int u = INF;
    static int minDist;
    static int newDist;
    public static void main(String[] args) {

        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (size < 2) {
            throw new IllegalArgumentException("Number of processes should be greater than 1");
        }

        prepareData(rank, size);

        double startTime = System.currentTimeMillis();

        proceed(rank, size);

        double time = System.currentTimeMillis() - startTime;
        if (rank == 0)
            System.out.println("Time:" + time);

        MPI.Finalize();

    }

    public static boolean checkIfInBound(int start, int end, int target) {
        return start <= target && end > target;
    }

    public static int[] proceed(int rank, int size) {
        int recvCount = Math.min(currentRows, vertexPerProcess);

        if (rank == 0) {
            visited = new boolean[n];

            for (int i = 0; i < n; i++) {
                visited[i] = false;
            }

            int[] initialNodes = new int[size];

            for (int i = 0; i < size; i++) {

                minDist = INF;

                for (int v = 0; v < n; v++) {
                    if (!visited[v] && dist[v] < minDist) {
                        minDist = dist[v];
                        u = v;
                    }
                }

                if (minDist == INF) {
                    initialNodes = null;
                    break;
                }

                visited[u] = true;
                initialNodes[i] = u;

                for (int v = 0; v < graph[u].length; v++) {
                    if (visited[v] || graph[u][v] == INF || graph[u][v] == 0) continue;

                    newDist = dist[u] + graph[u][v];

                    if (newDist < dist[v]) {
                        dist[v] = newDist;
                    }
                }

            }

            if (initialNodes == null) return dist;

            offset = 0;

            for (int i = 0; i < size; i++) {
                int rows = (i < extra) ? averow + 1 : averow;
                int[] tempNode = graph[offset];
                int tempDist = dist[offset];
                boolean tempVisited = visited[offset];

                graph[offset] = graph[initialNodes[i]];
                visited[offset] = visited[initialNodes[i]];
                dist[offset] = dist[initialNodes[i]];
                graph[initialNodes[i]] = tempNode;
                visited[initialNodes[i]] = tempVisited;
                dist[initialNodes[i]] = tempDist;

                for (int j = 0; j < n; j++) {
                    int tempItem = graph[j][offset];
                    graph[j][offset] = graph[j][initialNodes[i]];
                    graph[j][initialNodes[i]] = tempItem;
                }

                offset += rows;
            }

            offset = 0;

            for (int i = 0; i < size; i++) {
                int rows = (i < extra) ? averow + 1 : averow;
                counts[i] = rows;
                displs[i] = offset;
                offset += rows;
            }

            offset = currentRows;

            for (int i = 1; i < size; i++) {
                int rows = (i < extra) ? averow + 1 : averow;
                int sendCount = Math.min(rows, vertexPerProcess);
                subGraph = new int[sendCount][n];
                for (int k = offset; k < offset + sendCount; k++) {
                    subGraph[k-offset] = graph[k];
                }
                MPI.COMM_WORLD.Isend(subGraph, 0, sendCount, MPI.OBJECT, i, 1);
                MPI.COMM_WORLD.Isend(dist, offset, 1, MPI.INT, i, 2);
                offset += rows;
            }

            offset = 0;

            subGraph = new int[currentRows][n];

            for(int k = 0; k < currentRows; k++) {
                subGraph[k] = graph[k];
            }
        }

        boolean[] localVisited = new boolean[currentRows];

        MPI.COMM_WORLD.Scatterv(visited, 0, counts, displs, MPI.BOOLEAN, localVisited, 0 , currentRows, MPI.BOOLEAN, 0);

        if (rank != 0) {
            offset = 0;
            for (int i =0; i < rank; i++) {
                int rows = (i < extra) ? averow + 1 : averow;
                offset += rows;
            }
            subGraph = new int[recvCount][n];
            MPI.COMM_WORLD.Recv(subGraph, 0, recvCount , MPI.OBJECT, 0, 1);

            MPI.COMM_WORLD.Recv(dist, offset, 1, MPI.INT, 0, 2);
        }

        localVisited[0] = false;

        for (int i = 0; i < subGraph.length; i++) {
            minDist = INF;
            u = INF;
            for (int v = 0; v < subGraph.length; v++) {
                if (!localVisited[v] && dist[offset + v] < minDist) {
                    minDist = dist[offset + v];
                    u = v;
                }
            }

            if (u == INF) break;

            localVisited[u] = true;

            for (int v = 0; v < subGraph[u].length; v++) {
                if (subGraph[u][v] == 0 || subGraph[u][v] == INF) continue;
                if (checkIfInBound(offset, offset + currentRows, v) && localVisited[v - offset]) continue;
                newDist = dist[offset + u] + subGraph[u][v];
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                }
            }
        }

        MPI.COMM_WORLD.Reduce(dist, 0, dist, 0 , n, MPI.INT, MPI.MIN, 0);

        if (rank == 0) {
            for (int i = 0; i < n; i++) {

                minDist = INF;
                u = INF;

                for (int v = 0; v < n; v++) {
                    if (!visited[v] && dist[v] < minDist) {
                        minDist = dist[v];
                        u = v;
                    }
                }

                if (u == INF) break;
                visited[u] = true;

                for (int v = 0; v < graph[u].length; v++) {
                    if (!visited[v] || graph[u][v] == INF || graph[u][v] == 0) continue;
                    newDist = dist[u] + graph[u][v];
                    if (newDist < dist[v]) {
                        dist[v] = newDist;
                    }
                }
            }
        }

        return dist;
    }

    public static void prepareData(int rank, int size) {
        n = countVertex;

        averowEdge = countEdge / countVertex;
        extraEdge = countEdge % countVertex;

        averow = n / size;
        extra = n % size;

        counts = new int[size];
        displs = new int[size];

        currentRows = (rank < extra) ? averow + 1 : averow;

        dist = new int[n];
        Arrays.fill(dist, INF);

        dist[0] = 0;
        graph = new int[countVertex][];

        if (rank == 0) {
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
        }
    }
}