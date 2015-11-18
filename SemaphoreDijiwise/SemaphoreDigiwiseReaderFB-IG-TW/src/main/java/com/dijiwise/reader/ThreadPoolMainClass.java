package com.dijiwise.reader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolMainClass{
	public static void main(String [] args){
	try {
		new NetworkService(20).run();
		 System.out.println("\n\n ThreadPoolMainClass's main  method is executing  ");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
}

class NetworkService implements Runnable {
	  
	   private final ExecutorService pool;

	   public NetworkService( int poolSize)
	       throws IOException {
	    
	     pool = Executors.newFixedThreadPool(poolSize);
	   }

	   public void run() { // run the service
	     int i=0;
      for (;;) {
    	  
		   try {
			System.out.println("\n\n newWorkService  run method is executing  ");
			 pool.execute(new Handler(""+(i++)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   finally{
			   pool.shutdown();
		   }
		 
		 
      }
	   }
	 }

	 class Handler extends Thread {
	   private final String socket;
	   Handler(String socket) { this.socket = socket; }
	   public void run() {
	     // read and service request on socket
		   try {
			System.out.println("\n\n Handler is executing  "+socket);
			   //this.sleep(30000);
			   System.out.println("\n\n Thread is executing  "+this.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	 }
	 
	 
