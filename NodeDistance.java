import java.io.Serializable;

public class NodeDistance implements Serializable {
    public int index;
    public int distance;

    public NodeDistance(int index, int distance) {
        this.index = index;
        this.distance = distance;
//        System.out.println("Matrix: ");
//            for(int i = 0; i <graph.length; i++) {
//                System.out.println(Arrays.toString(graph[i]));
//            }
//            for(int i = 0; i <dist.length; i++) {
//                System.out.print("Distance from 0 to " + i + ": ");
//                if(dist[i] == INF) {
//                    System.out.println("No way");
//                } else {
//                    System.out.println(dist[i]);
//                }
//            }
    }


    @Override
    public String toString() {
        return "NodeDistance{" +
                "index=" + index +
                ", distance=" + distance +
                '}';
    }
}
