import mpi.MPI;
import mpi.Request;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    static final int INF = Integer.MAX_VALUE;
    static int[] dist;
    static boolean[] visited;
    static int[] parallelVisited;
    static  int[][] graph;
    static int sizeMatrix = 5;
    static int n = 0;
    static int averow = 0;
    static int extra = 0;
    public static void main(String[] args) {


        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if(size < 2) {
            throw new IllegalArgumentException("Number of processes should be greater than 1");
        }

        n = sizeMatrix;

        averow = n/size;
        extra = n%size;

        dist = new int[n];
        Arrays.fill(dist, INF);
        dist[0] = 0;

        if(rank == 0) {
            graph = new int[sizeMatrix][sizeMatrix];
            for(int i = 0; i < sizeMatrix; i++) {
                for(int j = 0; j < sizeMatrix; j++) {
                    double rand = Math.random();

                    if(rand > 0.8) {
                        graph[i][j] = INF;
                    } else {
                        graph[i][j] = j;
                    }

                }
            }

            visited = new boolean[n];
            parallelVisited = new int[n];
            Arrays.fill(parallelVisited, -1);

            for (int i = 0; i < n; i++) {
                visited[i] = false;
            }

            double startTime = System.currentTimeMillis();
            NodeDistance[] childrens = new NodeDistance[0];
            Request[] requests = new Request[size];
            int u = INF;
            int minDist;
            for (int i = 0; i < n; i++) {
                int offset = 0;
                minDist = INF;
                if( i > 0 && childrens != null) {
                    NodeDistance[] received = new NodeDistance[(size - 1) * n];
                    for(int j = 1; j < size; j++) {
                        requests[j - 1] = MPI.COMM_WORLD.Irecv(received, offset, n, MPI.OBJECT, j, i-1);
                        offset += n;
                    }
                    Request.Waitall(requests);


                    for(int j = 0; j < received.length; j++) {
                        if(received[j] != null && dist[received[j].index] > received[j].distance) {
                            dist[received[j].index] = received[j].distance;
                        }
                    }
                }

                for (int v = 0; v < n; v++) {
                    if (!visited[v] && dist[v] < minDist) {
                        minDist = dist[v];
                        u = v;
                    }
                }

                visited[u] = true;

                if(parallelVisited[u] == minDist) {
                    for(int j = 1; j < size; j++)
                        MPI.COMM_WORLD.Isend(new boolean[]{false}, 0, 1, MPI.BOOLEAN, j, 4*i + 1);
                    childrens = null;
                    continue;
                }

                ArrayList<NodeDistance> childrenArray = new ArrayList<>();

                int newDist;
                for (int v = 0; v < n; v++) {
                    newDist = dist[u] + graph[u][v];
                    if (!visited[v] && graph[u][v] != INF && newDist < dist[v]) {
                        if (i < n - 1) {
                            childrenArray.add(new NodeDistance(v, newDist));
                            parallelVisited[v] = newDist;
                        }
                        dist[v] = newDist;
                    }
                }


                if (i == n - 1) break;

                childrens = new NodeDistance[childrenArray.size()];
                for(int j = 0; j < childrens.length; j++){
                    childrens[j] = childrenArray.get(j);
                }
                ;
                extra = childrens.length%(size-1);
                averow = childrens.length / (size-1);
                offset = 0;
                for(int j = 1; j < size; j++) {
                    int rows = (j <= extra) ? averow + 1: averow;
                    MPI.COMM_WORLD.Isend(new boolean[]{true}, 0, 1, MPI.BOOLEAN, j, 4*i + 1);
                    MPI.COMM_WORLD.Isend(new int[]{rows}, 0, 1, MPI.INT, j, 4*i + 2);
                    MPI.COMM_WORLD.Isend(childrens, offset, rows, MPI.OBJECT, j, 4*i + 3);
                    int[][] subGraph = new int[rows][n];
                    for(int k = offset; k < offset + rows; k++) {
                        subGraph[k-offset] = graph[childrens[k].index];
                    }
                    MPI.COMM_WORLD.Isend(subGraph, 0, rows, MPI.OBJECT, j, 4*i + 4);
                    offset += rows;
                }

            }

            double time = System.currentTimeMillis() - startTime;
           System.out.println("Matrix: ");
            for(int i = 0; i <graph.length; i++) {
                System.out.println(Arrays.toString(graph[i]));
            }
            for(int i = 0; i <dist.length; i++) {
                System.out.print("Distance from 0 to " + i + ": ");
                if(dist[i] == INF) {
                    System.out.println("No way");
                } else {
                    System.out.println(dist[i]);
                }
            }

            System.out.println("Time: " + time);
        } else {
            int[] rows = new int[1];
            NodeDistance[] childrens;
            NodeDistance[] sendNodes;
            HashMap<Integer, Integer> pushed;
            int[][] subGraph;
            for(int i =0; i<n-1; i++) {
                boolean[] next = new boolean[1];
                sendNodes = new NodeDistance[n];
                pushed = new HashMap<>();
                MPI.COMM_WORLD.Recv(next, 0, 1, MPI.BOOLEAN, 0, 4*i+1);
                if (!next[0]) continue;
                MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, 0, 4*i+2);
                childrens = new NodeDistance[rows[0]];

                subGraph = new int[rows[0]][n];
                MPI.COMM_WORLD.Recv(childrens, 0, rows[0], MPI.OBJECT, 0, 4*i+3);
                MPI.COMM_WORLD.Recv(subGraph, 0, rows[0], MPI.OBJECT, 0, 4*i + 4);
                int newDist;
                int currentIndex = 0;
                for (int j = 0; j < rows[0]; j++) {
                    for(int k = 0; k < n; k++) {
                        if (subGraph[j][k] == INF) continue;
                        newDist = childrens[j].distance + subGraph[j][k];
                        if(pushed.get(k) == null) {
                            sendNodes[currentIndex] = new NodeDistance(k, newDist);
                            pushed.put(k, currentIndex);
                            currentIndex++;
                        } else {
                            sendNodes[pushed.get(k)].distance = newDist;
                        }
                    }
                }


                MPI.COMM_WORLD.Send(sendNodes, 0, n, MPI.OBJECT, 0, i);
            }

        }



        MPI.Finalize();

        // виведення результатів
//        if (rank == 0) {
//            for (int i = 0; i < n; i++) {
//                System.out.println("Shortest distance from node 0 to node " + i + ": " + globalDist[i]);
//            }
//        }
    }
}