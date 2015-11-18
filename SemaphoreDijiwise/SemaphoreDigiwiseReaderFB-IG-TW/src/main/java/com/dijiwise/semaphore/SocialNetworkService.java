package com.dijiwise.semaphore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class SocialNetworkService
{
	final static Logger logger = Logger.getLogger(SocialNetworkService.class);
	private int semaphorePermitLimit;
	private Semaphore threadSemaphore=new Semaphore(semaphorePermitLimit);
	
	private SocialNetworkService() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			File file = new File("/home/sushant/Projects/dijiwise-dev/dijiwise-dev/dijiwise-dev/Java/SemaphoreDijiwise/SemaphoreDigiwiseReaderFB-IG-TW/src/main/resources/config.properties");
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
	
	private static class SingletonHolder {
		public static final SocialNetworkService INSTANCE = new SocialNetworkService();
	}

	
	public static SocialNetworkService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	
	public int getThreadpoolLockStatus(){
		return threadSemaphore.availablePermits();
	}
public  int getThreadsWaitingToAcquire(){
		return threadSemaphore.getQueueLength();
	}

public  Boolean getWhetherAnyThreadsAreWaiting (){
	return threadSemaphore.hasQueuedThreads();
}
	public void getThreaPoolLock() throws InterruptedException {
		threadSemaphore.acquire();
	}

	public void releaseThreaPoolLock() {
		threadSemaphore.release();
	}


	public static final String CONFIG_PROP = "/home/sushant/Projects/dijiwise-dev/dijiwise-dev/dijiwise-dev/Java/SemaphoreDijiwise/SemaphoreDigiwiseReaderFB-IG-TW/src/main/resources/config.properties";
	public static final String LOG4J_PROP = "/home/sushant/Projects/dijiwise-dev/dijiwise-dev/dijiwise-dev/Java/SemaphoreDijiwise/SemaphoreDigiwiseReaderFB-IG-TW/src/main/resources/log4j.properties";
	
//	public static final String CONFIG_PROP = "D:\\Project\\Succeed_DEV\\digiwise\\Java\\DijiWebService\\resources\\config\\dijiwise.properties";
//	public static final String LOG4J_PROP = "D:\\Project\\Succeed_DEV\\digiwise\\Java\\SemaphoreDijiwise\\SemaphoreDigiwiseReaderFB-IG-TW\\src\\main\\resources\\log4j.properties";
	/**
	 * @param argv
	 * @throws Exception
	 */
	@Autowired
	public static void main(String[] argv) throws Exception {
//		PropertyConfigurator.configure("/dijiwise/log4j.properties");
		PropertyConfigurator.configure(LOG4J_PROP);
		logger.info("start of the program");
		Properties prop = new Properties();
		InputStream input = null;
		try {
//			File file = new File("/dijiwise/config.properties");
			File file = new File(CONFIG_PROP);
			input = new FileInputStream(file);
			prop.load(input);
		} catch (Exception e) {
			logger.error("Exception Info:" + e.getMessage() + " e.toSting: "
					+ e.toString());
		} finally {
			input.close();
		}
		final String QUEUE_NAME = prop.getProperty("QUEUE_NAME");
		final int NTHREDS = Integer.parseInt(prop.getProperty("NTHREDS"));
		final String RQUEUE_NAME = prop.getProperty("RQUEUE_NAME");
		final Integer PORT = Integer.parseInt(prop.getProperty("PORT"));
		final String HOST = prop.getProperty("HOST");
		final String USERNAME = prop.getProperty("USERNAME");
		final String PASSWORD = prop.getProperty("PASSWORD");
		final String MONGO_HOST = prop.getProperty("MONGO_HOST");
		final int MONGO_PORT = Integer.parseInt(prop
				.getProperty("MONGO_PORT"));
	

		ConnectionFactory factory = null;
		Connection connection = null;
		Channel channel = null;
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
			System.out.println("Successfully created the RabitMQ Connection!!!!!!!!!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("x-cancel-on-ha-failover", true);
		channel.queueDeclare(QUEUE_NAME, false, false, false, args);
		// Channel writechannel = connection.createChannel();
		// writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);
		QueueingConsumer consumer = new QueueingConsumer(channel);

		channel.basicConsume(QUEUE_NAME, true, args, consumer);
		
		ArrayList<Future<?>> tokenResponse = new ArrayList();

		MongoClientOptions options=MongoClientOptions.builder().connectionsPerHost(2000).connectTimeout(60000).autoConnectRetry(true).maxConnectionLifeTime(60000).build();
		ServerAddress mongoAddress=new ServerAddress(MONGO_HOST, MONGO_PORT);
		MongoClient mongo = new MongoClient(mongoAddress, options);
		Long token = 0L;
		Long count = 0L;
		for (;;) {
			System.out.println("Inside for loop!!!");
			logger.info("\n\n No of Locks available : "+PersonLock.getInstance().getThreadpoolLockStatus());
			logger.info("\n getThreadsWaitingToAcquire() : "+PersonLock.getInstance().getThreadsWaitingToAcquire());
			logger.info("\n getWhetherAnyThreadsAreWaiting() : "+PersonLock.getInstance().getWhetherAnyThreadsAreWaiting());
			logger.info("\n find no. of threads running java.lang.Thread.activeCount(): "+java.lang.Thread.activeCount());
			/*System.out.println("\n\n No of Locks available : "+SocialNetworkService.getInstance().getThreadpoolLockStatus());
			System.out.println("\n getThreadsWaitingToAcquire() : "+SocialNetworkService.getInstance().getThreadsWaitingToAcquire());
			System.out.println("\n getWhetherAnyThreadsAreWaiting() : "+SocialNetworkService.getInstance().getWhetherAnyThreadsAreWaiting());
			System.out.println("\n find no. of threads running java.lang.Thread.activeCount(): "+java.lang.Thread.activeCount());
			*/if (!connection.isOpen()) {
				try {
					channel.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.info("Problem opening rabbitMQ Connection : "
							+ e.toString());
				}
				connection = factory.newConnection();
				channel = connection.createChannel();
				connection.addShutdownListener(new DijiShutDownListener());

				channel.queueDeclare(QUEUE_NAME, false, false, false, args);
				// writechannel = connection.createChannel();
				// writechannel.queueDeclare(RQUEUE_NAME, false, false, false,
				// args);
				consumer = new QueueingConsumer(channel);
				// System.out.println("\n\n in if loop. \n"+count++);
			}
			System.out.println("Check if open -- "+connection.isOpen());
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery != null) {

				String routingKey = delivery.getEnvelope().getRoutingKey();
				String message = new String(delivery.getBody());
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(message);
				String socialMediaType = (String) jsonObject
						.get("social_media_type");

				// String socialMediaType ="twitter";
				switch (socialMediaType.toLowerCase()) {
				case "facebook":
					logger.info("From Facebook");
					 FacebookWorker.processData(
							delivery.getBody(), routingKey,
							connection, prop, count++, mongo);
					// tokenResponse.add(e);

					
					break;
				case "instagram":
					logger.info("From instagram");
					InstagramWorker.processData(
							delivery.getBody(), routingKey, 
							connection, prop, count++, mongo);
					// tokenResponse.add(e1);
					break;
				case "twitter":
					logger.info("From tweeter");
					 TwitterWorker.processData(
							delivery.getBody(), routingKey,
							connection, prop, count++, mongo);
					// tokenResponse.add(e2);
					break;
				case "google_plus1":
					logger.info("From googlePlus");
					 GoogleplusWorker.processData(
							delivery.getBody(), routingKey,
							connection, prop, count++, mongo);
					// tokenResponse.add(e3);
					break;
				default:
					defaultHandler();
					break;
				}

			}
			
			logger.info("\n\n No of Locks available : "+PersonLock.getInstance().getThreadpoolLockStatus());
			logger.info("\n getThreadsWaitingToAcquire() : "+PersonLock.getInstance().getThreadsWaitingToAcquire());
			logger.info("\n getWhetherAnyThreadsAreWaiting() : "+PersonLock.getInstance().getWhetherAnyThreadsAreWaiting());
			logger.info("\n find no. of threads running java.lang.Thread.activeCount(): "+java.lang.Thread.activeCount());
			
			
			}// end of for loop
		
	}// end of function
		

	public static void defaultHandler() {
		logger.info("No socialmedial type provided");
	}

		public static String fromJavaToJson(Serializable object)
				throws JsonGenerationException, JsonMappingException, IOException {
			ObjectMapper jsonMapper = new ObjectMapper();
			return jsonMapper.writeValueAsString(object);
		}

}
