package com.dijiwise.semaphore;

import org.apache.log4j.Logger;

import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class DijiShutDownListener implements ShutdownListener {
	final static Logger logger = Logger.getLogger(DijiShutDownListener.class);
	@Override
	public void shutdownCompleted(ShutdownSignalException cause) {
		// TODO Auto-generated method stub
		logger.info("\n***********Connection Got Closed*****************");
	}

}
