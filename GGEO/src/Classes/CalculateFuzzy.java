/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Sk8er
 */
public class CalculateFuzzy {

    private ExecutorService exec;
    private int numThreads;
    private double[][] grid;

    public CalculateFuzzy(double[][] grid) {
        this.grid = grid;
        exec =  Executors.newFixedThreadPool(8);
    }

    private class Job implements Callable<Boolean> {


        public Job() {
            
        }

        @Override
        public Boolean call() throws Exception {
            String a[][] = new String[1000][1000];
            for(int i=1;i<2000;i++)
            {
                a[i/2][i/2]= Integer.toString(i);
            }
            return true;
        }
    }

    public void runMultiThread() {

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 70; i++) {
            for (int j = 0; j < 70; j++) {
                for (int k = 0; k < 400; k++) {
                    try {
                        tasks.add(new Job());
                    } catch (Exception e) {
                    }
                }
            }
        }
        try {
            List<Future<Boolean>> results = this.exec.invokeAll(tasks);
            for (Future<Boolean> result : results) {
                if (!result.get().booleanValue()) {
                    throw new RuntimeException();
                }
            }
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}