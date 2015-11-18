package com.dijiwise.reader;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
 
public class CachedThreadPoolExample {
    public static void main(String[] args) {
        // Obtain a cached thread pool
        ExecutorService cachedPool = Executors.newCachedThreadPool();
 
        // Create an anonymous Callable<T> object. You need to
        // override the call() method
        Callable<String> aCallable = new Callable<String>(){
            String message = "Callable is done !";
            @Override
            public String call() throws Exception {
                for(int i = 0; i < 10; i++){
                    System.out.println("Callable is doing something");
                    Thread.sleep(500); // make it sleep a little
                }
                return message;
            }
        };
 
        // Create an anonymous instance of Runnable
        Thread aRunnable = new Thread(){
            @Override
            public void run() {
                try{
                	
                   for(int i = 0; i < 10; i++){
                	
                        System.out.println("\tRunnable is doing something "+this.getName()+" "+(i++));
                        Thread.sleep(100000);
                    }
                }catch(Exception e){
 
                }
            }
        };
 
        // Time to run these
        // Future<> gets parameterized based on how Callable is parameterized
        // Since Runnable is not parameterized, you get a Future <?>
      //  Future<String> callableFuture = cachedPool.submit(aCallable);
        Future<?> runnableFuture = cachedPool.submit(aRunnable);
 
        // check if tasks are done or not
      /*  if(callableFuture.isDone()){
            System.out.println("\t\tCallable is done !");
        }else{
            System.out.println("\t\tCallable is not done !");
        }*/
 
        if(runnableFuture.isDone()){
            System.out.println("\t\tRunnable is done !");
        }else{
            System.out.println("\t\tRunnable is not done !");
        }
 
        /*try {
            // get() waits for the task to finish and then gets the result
            String returnedValue = callableFuture.get();
            System.out.println(returnedValue);
        } catch (InterruptedException e) {
            // thrown if task was interrupted before completion
            e.printStackTrace();
        } catch (ExecutionException e) {
            // thrown if the task threw an execption while executing
            e.printStackTrace();
        }*/
 
       // cachedPool.shutdown(); // shutdown the pool.
    }
}
