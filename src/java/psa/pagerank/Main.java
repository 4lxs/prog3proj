package psa.pagerank;

import java.util.Random;
import java.util.Scanner;

import mpi.MPI;

public class Main {
    enum Mode {
        SEQUENTIAL, PARALLEL, DISTRIBUTED
    }

    public static void main(String[] args) {
        Mode mode = Mode.SEQUENTIAL;
        int nV, nE, seed;
        double d;
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter mode (0: SEQUENTIAL, 1: PARALLEL, 2: DISTRIBUTED): ");
        int modeInput = sc.nextInt();
        switch (modeInput) {
            case 0:
                mode = Mode.SEQUENTIAL;
                break;
            case 1:
                mode = Mode.PARALLEL;
                break;
            case 2:
                mode = Mode.DISTRIBUTED;
                break;
            default:
                System.out.println("Invalid mode, defaulting to SEQUENTIAL");
                sc.close();
                return;
        }

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

        if (mode == Mode.DISTRIBUTED) {
            String[] cmd = {
                "java",
                "-Djava.library.path=C:\\mpj\\lib",
                "-DMPJ_HOME=C:\\mpj",
                "-jar", "C:\\mpj\\lib\\starter.jar",
                "-np", "12",
                "-cp", "C:\\Users\\svens\\Downloads\\89231433_pagerank\\pagerank\\out\\production\\pagerank;C:\\mpj\\lib\\mpj.jar",
                "psa.pagerank.Distributed",
                String.valueOf(nV),
                String.valueOf(nE),
                String.valueOf(d),
                String.valueOf(seed)
            };

            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.inheritIO();
                pb.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error launching distributed process");
            }
            sc.close();
            return;
        }

        // we don't parallelize the graph generation to only measure the PageRank algorithm performance
        boolean[][] adj = generateGraph(nV, nE, seed);

        PageRank pr = new PageRank(adj, d);
        int iter = 0;
        
        while (true) {
            double convergence;
            switch (mode) {
                case SEQUENTIAL:
                    convergence = pr.sequentialIter();
                    break;
                case PARALLEL:
                    convergence = pr.parallelIter();
                    break;
                case DISTRIBUTED:
                    convergence = pr.distributedIter();
                    break;
                default:
                    throw new IllegalStateException("Unexpected mode: " + mode);
            }
            
            if (convergence <= 1e-5) {
                break;
            }
            
            // Print current ranks
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

    public static boolean[][] generateGraph(int nV, int nE, int seed) {
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
        
        return adj;
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
