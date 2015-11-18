package com.digiwise.reader;

import static java.lang.System.out;
import com.mongodb.MongoClient;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.restfb.types.Comment;
import com.restfb.types.Comment.Comments;
import com.restfb.types.FacebookType;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Post;
import com.restfb.types.Post.Likes;

import hello.DijiWiseResultsArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jinstagram.Instagram;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.media.MediaInfoFeed;
import org.jinstagram.entity.users.basicinfo.UserInfo;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class JavaListener {
	final static Logger logger = Logger.getLogger(JavaListener.class);
	//public static final String MONGO_HOST = "104.130.31.144";
	public static final String MONGO_HOST = "104.130.31.144";

	@Autowired
	public static DijiWiseResultsArray getResult(  String parent_id,String child_user_id,String access_token, String oauth_token_secret, String social_media_type,String social_site_user_id, DijiWiseResultsArray array ) throws Exception {
		PropertyConfigurator.configure("/dijiwise/log4j.properties");
		logger.info("start of the program");
	/*	Properties prop = new Properties();
		InputStream input = null;
		try {
			File file = new File("/dijiwise/config.properties");
			input = new FileInputStream(file);
			prop.load(input);
		} catch (Exception e) {
			logger.error("Exception Info:" + e.getMessage() + " e.toSting: "
					+ e.toString());
		} finally {
			input.close();
		}
		*/
		int count = 0;

					if (social_media_type.equalsIgnoreCase("facebook")) {
					
						logger.info("From Facebook");
						return FacebookListner.newQueueMessage(  parent_id, child_user_id, access_token, social_media_type, social_site_user_id,array);
					}
					if (social_media_type.equalsIgnoreCase("instagram")) {
					
						logger.info("From instagram");
						return InstagramListner.instraGramMain(parent_id, child_user_id, access_token, social_media_type, social_site_user_id,array);
						
					}
					if (social_media_type.equalsIgnoreCase("google_plus1")) {
						
						logger.info("From GooglePlus");
						return GooglePlusListener.getUserGoogleFeeds( parent_id, child_user_id, access_token, oauth_token_secret, social_media_type, social_site_user_id,array);
					}
					if (social_media_type.equalsIgnoreCase("twitter")) {
						
						logger.info("From twitter");
						return TwitterListener.getTwitterTweets(  parent_id, child_user_id, access_token, oauth_token_secret, social_media_type, social_site_user_id,array);
					}
					
	
				return null;
				}
	

}
