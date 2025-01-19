package psa.pagerank;

import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        int nV, nE, seed;
        double d;
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the number of vertices and edges: ");
        nV = sc.nextInt();
        nE = sc.nextInt();
        if (nV < 1 || nE < 1 || nE > nV * (nV - 1)) {
            System.out.println("Invalid input");
            sc.close();
            return;
        }
        System.out.print("Enter the damping factor: ");
        d = sc.nextDouble();
        if (d < 0 || d > 1) {
            System.out.println("Invalid input");
            sc.close();
            return;
        }
        System.out.print("Enter the random seed: ");
        seed = sc.nextInt();
        Random rand = new Random(seed);

        boolean[][] adj = new boolean[nV][nV];
        for (int i = 0; i < nE;) {
            int u = rand.nextInt(nV);
            int v = rand.nextInt(nV);
            if (u == v || adj[u][v]) {
                continue;
            }
            adj[u][v] = true;
            i++;
        }
        PageRank pr = new PageRank(adj, d);
        int iter = 0;
        while (pr.iter() > 1e-5) {
            for (int j = 0; j < pr.rank.length; j++) {
                System.out.print(pr.rank[j] + " ");
            }
            System.out.println();
            iter++;
        }
        System.out.println("Converged in " + iter + " iterations");
        System.out.print("Enter filename to save the graph as csv (empty for no save): ");
        String filename = sc.next();
        if (!filename.isEmpty()) {
            saveGraph(adj, filename);
        }

        System.out.print("Enter filename to save the ranks as csv (empty for no save): ");
        filename = sc.next();
        if (!filename.isEmpty()) {
            saveRanks(pr.rank, filename);
        }
        sc.close();
    }

    public static void saveGraph(boolean[][] adj, String filename) {
        try {
            java.io.FileWriter fw = new java.io.FileWriter(filename);
            fw.write("u,v\n");
            for (int i = 0; i < adj.length; i++) {
                for (int j = 0; j < adj[i].length; j++) {
                    if (adj[i][j]) {
                        fw.write(i + "," + j + "\n");
                    }
                }
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveRanks(double[] rank, String filename) {
        try {
            java.io.FileWriter fw = new java.io.FileWriter(filename);
            fw.write("vertex,score\n");
            for (int i = 0; i < rank.length; i++) {
                fw.write(i + "," + rank[i] + "\n");
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
