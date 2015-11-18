package com.dijiwise.reader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;



import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jinstagram.exceptions.InstagramException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
 
 
/**
* Naive implementation of ThreadPool. Just for illustration
*/
 class ThreadPool1
{
    private final BlockingQueue<Runnable> workerQueue;
    private final Thread[] workerThreads;
    private volatile boolean shutdown;
 
    public ThreadPool1(int N)
    {
        workerQueue = new LinkedBlockingQueue<>();
        workerThreads = new Thread[N];
 
        //Start N Threads and keep them running
        for (int i = 0; i < N; i++) {
            workerThreads[i] = new Worker("Pool Thread " + i);
            workerThreads[i].start();
        }
    }
 
    public void addTask(Runnable r)
    {
        try {
            workerQueue.put(r);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
 
    public void shutdown()
    {
        while (!workerQueue.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //interruption
            }
        }
        shutdown = true;
        for (Thread workerThread : workerThreads) {
            workerThread.interrupt();
        }
    }
 
    private class Worker
            extends Thread
    {
        public Worker(String name)
        {
            super(name);
        }
 
        public void run()
        {
            while (!shutdown) {
                try {
                    //each thread wait for next runnable and executes it's run method
                    Runnable r = workerQueue.take();
                    r.run();
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
    }
 
}


 
 public class CopyOfProducerConsumerWithThreadPool
{
    private static AtomicInteger rnd = new AtomicInteger(0);
    private final BlockingQueue<Integer> queue;
 
    public CopyOfProducerConsumerWithThreadPool()
    {
        queue = new LinkedBlockingQueue<>(3);
    }
 
    public static void main(String[] args)
    {
        CopyOfProducerConsumerWithThreadPool pc = new CopyOfProducerConsumerWithThreadPool();
        pc.init();
    }
    final static Logger logger = Logger.getLogger(CopyOfProducerConsumerWithThreadPool.class);
    public static void defaultHandler() {
		logger.info("No socialmedial type provided");
	}
    private void init()
    {
        ThreadPool1 pool = new ThreadPool1(2);
        

		PropertyConfigurator.configure("/dijiwise/log4j.properties");
		logger.info("start of the program");
		QueueingConsumer.Delivery delivery =null;
		Properties prop = new Properties();
		InputStream input = null;
		try {
			File file = new File("/dijiwise/config.properties");
			input = new FileInputStream(file);
			prop.load(input);
		} catch (Exception e) {
			logger.error("Exception Info:" + e.getMessage() + " e.toSting: "
					+ e.toString());
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		final String QUEUE_NAME = prop.getProperty("QUEUE_NAME");
		final int NTHREDS = Integer.parseInt(prop.getProperty("NTHREDS"));
		final String RQUEUE_NAME = prop.getProperty("RQUEUE_NAME");
		final Integer PORT = Integer.parseInt(prop.getProperty("PORT"));
		final String HOST = prop.getProperty("HOST");
		final String USERNAME = prop.getProperty("USERNAME");
		final String PASSWORD = prop.getProperty("PASSWORD");

		ConnectionFactory factory = null;
		Connection connection = null;
		Channel channel = null;
		QueueingConsumer consumer=null;
		try {
			factory = new ConnectionFactory();
			factory.setNetworkRecoveryInterval(5000);
			factory.setHost(HOST);
			factory.setPort(PORT);
			factory.setUsername(USERNAME);
			factory.setPassword(PASSWORD);
			connection = factory.newConnection();
			channel = connection.createChannel();
			connection.addShutdownListener(new DijiShutDownListener());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-cancel-on-ha-failover", true);
		
		
		try {
			channel.queueDeclare(QUEUE_NAME, false, false, false, args);
			consumer = new QueueingConsumer(channel);
			channel.basicConsume(QUEUE_NAME, true, args, consumer);
		 delivery = consumer.nextDelivery();
			
		} catch (IOException | ShutdownSignalException | ConsumerCancelledException | InterruptedException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
		ArrayList<Future<?>> tokenResponse = new ArrayList();

		// while (true) {

		/*
		 * if(executor==null) executor = Executors.newFixedThreadPool(NTHREDS);
		 */
		Long token = 0L;
		Long count = 0L;
		
		
        
        
        for (int i = 0; i < 100; i++) {
        	
            pool.addTask(new Producer());
            pool.addTask(new Producer());
            pool.addTask(new Producer());
 
            pool.addTask(new Consumer());
            pool.addTask(new Consumer());
            pool.addTask(new Consumer());
        }

    		
        pool.shutdown();
    }
 
    private class Producer implements Runnable
    {
        @Override
        public void run()
        {
            Integer e = rnd.incrementAndGet();
            System.out.println("Inserting Element " + e);
            try {
                queue.put(e);
                Thread.sleep(1000);
            } catch (InterruptedException e2) {
                Thread.currentThread().interrupt();
            }
        }
    }
 
    private class Consumer implements Runnable
    {
        @Override
        public void run()
        {
            try {
                System.out.println("Removing Element " + queue.take());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
