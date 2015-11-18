package com.dijiwise.semaphore;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

/**
 * This class will allow thread to acquire and release locks as required
 * 
 * @author dinuka.arseculeratne
 * 
 */
public class PersonLock {

	/**
	 * We do not want multiple lock objects lying around so we make ths class
	 * singleton
	 */
	/*private PersonLock() {

	}*/
	
	final static Logger logger = Logger.getLogger(SocialNetworkService.class);
	private int semaphorePermitLimit;
	private Semaphore threadSemaphore=new Semaphore(semaphorePermitLimit);
	
	private PersonLock() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
//			File file = new File("/dijiwise/config.properties");
			File file = new File(SocialNetworkService.CONFIG_PROP);
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
		
		final int NTHREDS = Integer.parseInt(prop.getProperty("NTHREDS"));
		this.semaphorePermitLimit=NTHREDS;
	}

	/**
	 * Bill Pugh's way of lazy initializing the singleton instance
	 * 
	 * @author dinuka.arseculeratne
	 * 
	 */
	private static class SingletonHolder {
		public static final PersonLock INSTANCE = new PersonLock();
	}

	/**
	 * Use this method to get a reference to the singleton instance of
	 * {@link PersonLock}
	 * 
	 * @return the singleton instance
	 */
	public static PersonLock getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * In this sample, we allow only one thread at at time to update the cache
	 * in order to maintain consistency
	 */
	private Semaphore writeLock = new Semaphore(1);

	/**
	 * We allow 10 concurrent threads to access the cache at any given time
	 */
	
	
	private Semaphore threaPoolLock=new Semaphore(10);
	public int getThreadpoolLockStatus(){
		return threaPoolLock.availablePermits();
	}
public  int getThreadsWaitingToAcquire(){
		return threaPoolLock.getQueueLength();
	}

public  Boolean getWhetherAnyThreadsAreWaiting (){
	return threaPoolLock.hasQueuedThreads();
}
	public void getThreaPoolLock() throws InterruptedException {
		threaPoolLock.acquire();
	}

	public void releaseThreaPoolLock() {
		threaPoolLock.release();
	}

	
	public void getWriteLock() throws InterruptedException {
		writeLock.acquire();
	}

	public void releaseWriteLock() {
		writeLock.release();
	}

}
