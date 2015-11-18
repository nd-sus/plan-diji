package com.dijiwise.reader;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jinstagram.auth.model.Token;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.social.oauth2.OAuth2Parameters;

import com.googlecode.googleplus.ActivityOperations;
import com.googlecode.googleplus.CommentOperations;
import com.googlecode.googleplus.GooglePlusFactory;
import com.googlecode.googleplus.Plus;
import com.googlecode.googleplus.model.activity.Activity;
import com.googlecode.googleplus.model.activity.ActivityCollection;
import com.googlecode.googleplus.model.activity.ActivityFeed;
import com.googlecode.googleplus.model.activity.ActivityObjectAttachments;
import com.googlecode.googleplus.model.comment.CommentFeed;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Channel;

public class BQGooglePlusListener  implements Runnable {
	// facebook handler
	// facebook handler
		 static Logger logger = Logger.getLogger(BQGooglePlusListener.class);
		 byte[] body;
		 String routingKey;
		ExecutorService executor;
		 com.rabbitmq.client.Connection connection1;
		 Properties prop;
		 Long counterValue;
		public BQGooglePlusListener( byte[] body,
				 String routingKey, ExecutorService executor, 
				 com.rabbitmq.client.Connection connection1,
				 Properties prop,  Long counterValue) {
			this.body=body;
			this.routingKey=routingKey;
			this.executor=executor;
			this.connection1=connection1;
			this.prop=prop;
			
			
			
		}
			
			public void run() {
		
				String accessTokenValue = null;
				MongoOperations mongoOps = null;
				Plus plus = null;
				List<Activity> data = null;
				String PERSON_COLLECTION = "";
				String FB_COMMENT_COLLECTION = "";
			
				com.rabbitmq.client.ConnectionFactory factory1 = null;
				MongoClient mongo = null;
				DateTimeZone zone = DateTimeZone.getDefault();
				
				Channel writechannel = null;
				Person errorPerson = null;
				JSONObject obj = null;
				Person p = null;
				Person p1 = null;

				try {

					PropertyConfigurator
							.configure("/dijiwise/log4j.properties");
					/* logger.info("start of the Thread: " + this.getName()); */
					/*
					 * Properties prop = new Properties(); InputStream input =
					 * null; try { File file = new
					 * File("/dijiwise/config.properties"); input = new
					 * FileInputStream(file); prop.load(input);
					 * logger.info("Thread  read the propfile its name is :" +
					 * this.getName()); // load a properties file } catch
					 * (Exception e) {
					 * logger.info("Thread  Error in reading file name is  :" +
					 * this.getName()); e.printStackTrace(); } finally {
					 * input.close(); }
					 */
					final String RQUEUE_NAME = prop.getProperty("RQUEUE_NAME");
					
					final String DB_NAME = prop.getProperty("DB_NAME");
					PERSON_COLLECTION = prop.getProperty("PERSON_COLLECTION");
					FB_COMMENT_COLLECTION = prop
							.getProperty("FB_COMMENT_COLLECTION");
					final String MONGO_HOST = prop.getProperty("MONGO_HOST");
					final int MONGO_PORT = Integer.parseInt(prop
							.getProperty("MONGO_PORT"));
					Map<String, Object> args = new HashMap<String, Object>();
					args.put("x-cancel-on-ha-failover", true);
					/*
					 * factory1 = new com.rabbitmq.client.ConnectionFactory();
					 * factory1.setNetworkRecoveryInterval(5000);
					 * factory1.setHost(HOST); factory1.setPort(PORT);
					 */
					/*
					 * com.rabbitmq.client.Connection connection1 = factory1
					 * .newConnection(); Channel writechannel =
					 * connection1.createChannel();
					 * writechannel.queueDeclare(RQUEUE_NAME, false, false,
					 * false, args);
					 */

					String message = new String(body);
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject) jsonParser
							.parse(message);

					accessTokenValue = ((String) jsonObject.get("access_token") != null ? (String) jsonObject
							.get("access_token") : "");
					/*
					 * refreshTokenValue = ((String)
					 * jsonObject.get("refresh_token") != null ? (String)
					 * jsonObject .get("refresh_token") : "");
					 */
					// accessTokenValue
					// ="ya29.7wBjP19A_Bcdmmu-n1WwG_j6mtOh_tas2UCp-SvRytIE4qoi9vRAeXaizOMvviNkFx1NUlGg_Ia4Ag";
					String secret = "Fmi5r-MP--MXwi5ErGQ1I_oW";
					String id = "693488445005-chme8sakk0qg5slmcnqgpara7diiim5a.apps.googleusercontent.com";
					OAuth2Parameters oAuthParams = new OAuth2Parameters();

					/*
					 * oAuthParams.setRedirectUri(
					 * "<https-url>/googleplus/authenticate");
					 * oAuthParams.setScope(
					 * "https://www.googleapis.com/auth/plus.me https://www.googleapis.com/auth/plus.moments.write"
					 * ); oAuthParams.put("access_type",
					 * Lists.newArrayList("offline"));
					 */

					GooglePlusFactory factory = null;
					try {
						factory = new GooglePlusFactory(id, secret);
						/*
						 * logger.info("****secret***** "+secret+
						 * "**********clientid*********: "+id);
						 */
					} catch (Exception e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						/*
						 * logger.info("exception******"+e2.toString()+
						 * "****secret***** "
						 * +secret+"**********clientid*********: "+id);
						 */
					}

					/*
					 * ArrayList<Post> data = new GraphReaderExample( (String)
					 * jsonObject.get("access_token")) .FilteredPosts();
					 */
					try {
						mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
						mongoOps = new MongoTemplate(mongo, DB_NAME);

						plus = factory.getApi(accessTokenValue);

						// plus =
						// factory.getApi("ya29.7wAvTpZft8nFOXjYHz9OE8G3FNjkF6_4erGVx0RNNf5O3CoMZRW5gCHJUtNWK2Fwc7dXpxBdmXbUFQ",
						// "1/zPDxCU4JdkEtQY_wCWcbdExAuJAPigaBcZEnHwBOTy190RDknAdJa_sgfheVM0XT",
						// new OAuth2RefreshListener());
						// factory.getApi(accessTokenValue, refreshToken, new
						// OAuth2RefreshListener());
						com.googlecode.googleplus.model.person.Person user = plus
								.getPeopleOperations().get("me");
						ActivityOperations activities = plus
								.getActivityOperations();

						ActivityFeed ac = activities.list(user.getId(),
								ActivityCollection.PUBLIC);
						data = ac.getItems();

					} catch (Exception e) {
						logger.error("Exception: " + e.getMessage() + "--"
								+ " e.tostring: " + e.toString());
						errorPerson = new Person();

						errorPerson.setUuid((UUID.randomUUID() + ""));
						errorPerson.setParentId((String) jsonObject
								.get("parent_id"));
						errorPerson.setSocialSiteUserId((String) jsonObject
								.get("social_site_user_id"));
						errorPerson.setErrorFlag(1);
						errorPerson.setErrorMessage(e.toString());
						errorPerson.setSocialMediaType((String) jsonObject
								.get("social_media_type"));
						mongoOps.save(errorPerson, PERSON_COLLECTION);
					}
					try {
						/*
						 * logger.info("Thread  Got data  w.r.t accesstoken :" +
						 * (String) jsonObject.get("access_token") +
						 * "\n Thread Name:" + this.getName());
						 */
						for (Activity i : data) {

							logger.info("start processing " + i.getId()
									+ " is " + (System.currentTimeMillis()));
							/*
							 * logger.info("###############Start#############" +
							 * i.getId());
							 * logger.info("Thread started reading post with id : "
							 * + i.getId() + "   updatged ptime " +
							 * i.getUpdated().getTime());
							 * logger.info("google plus testing..."
							 * +i.toString());
							 */
							p = new Person();
							p.setUuid((UUID.randomUUID() + ""));
							p.setId(i.getId());
							p.setName(i.getActor().getDisplayName());

							p.setInsertDate((new DateTime().toDateTimeISO()
									.toString()));

							if (i.getObject().getAttachments() != null) {

								for (ActivityObjectAttachments e : i
										.getObject().getAttachments()) {

									if (e.getImage() != null) {
										// System.out.println("\n e.getObjectType() :"+e.getObjectType()+" e.getUrl(): "+e.getUrl()+"  e.getImage().getUrl()"+e.getImage().getUrl());
										p.setGoogleImageAttachment(e.getImage()
												.getUrl());
									}
									if (e.getEmbed() != null) {
										p.setGoogleVideoAttachment(e.getEmbed()
												.getUrl());
										p.setGoogleEmbedType(e.getEmbed()
												.getType());
									}
									// System.out.println("e.getEmbed().getType(): "+e.getEmbed().getType()+
									// " e.getEmbed().getUrl(): "+e.getEmbed().getUrl()
									// );
									if (e.getContent() != null) {

										p.setGoogleAlbumAttachment(e
												.getContent());
									}// System.out.println("e.getContent(): "+e.getContent());

								}
							}

							p.setGoogleActivityUrl((i.getUrl() != null ? i
									.getUrl() : ""));
							p.setGoogleOriginalContent((i.getObject() != null ? (i
									.getObject().getOriginalContent() != null ? i
									.getObject().getOriginalContent() : "")
									: ""));
							p.setGoogleContent(i.getObject() != null ? i
									.getObject().getContent() != null ? i
									.getObject().getContent() : "" : "");
							p.setGoogleCrossPost(i.getCrosspostSource() != null ? i
									.getCrosspostSource() : "");
							/*
							 * DateTime insertDate = new DateTime()
							 * .toDateTimeISO(); //
							 * p.setInsertDate(insertDate.getDayOfMonth
							 * ()+"-"+insertDate
							 * .getMonthOfYear()+"-"+insertDate.getYear());
							 * p.setInsertDate(insertDate.getYear() + "-" +
							 * insertDate.getMonthOfYear() + "-" +
							 * insertDate.getDayOfMonth());
							 */
							p.setMessage(i.getTitle());

							/*
							 * logger.info(
							 * "google plus testing for toLocaleString...");
							 */

							CommentOperations commentsOp = plus
									.getCommentOperations();

							CommentFeed ac = commentsOp.list(i.getId());
							p.setCommentCount((long) ac.getItems().size());

							try {
								// logger.info("google plus testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
								if (ac != null & ac.getItems().size() > 0) {
									String uuidCommentId = UUID.randomUUID()
											+ "";
									int j = 0;
									for (com.googlecode.googleplus.model.comment.Comment c : ac
											.getItems()) {
										FBcomments comment = new FBcomments();
										comment.setUuidComment(uuidCommentId);
										comment.setPostId(i.getId());

										comment.setCommentId((c.getId() != null ? c
												.getId() : ""));
										comment.setCommentMessage((c
												.getObject().getContent() != null ? c
												.getObject().getContent() : ""));
										comment.setCommentFrom((c.getActor() != null ? c
												.getActor().getDisplayName() != null ? c
												.getActor().getDisplayName()
												: ""
												: ""));
										comment.setProfileImage((c.getActor()
												.getImage().getUrl() != null ? c
												.getActor().getImage().getUrl()
												: ""));
										comment.setCommentedById((c.getActor()
												.getId() != null ? c.getActor()
												.getId() : ""));
										comment.setAttachmentType((c
												.getObject().getObjectType() != null ? c
												.getObject().getObjectType()
												: ""));
										comment.setAttachmentUrl((c.getVerb() != null ? c
												.getVerb() : ""));
										/*
										 * logger.info("lfjsadkfjdslkfjsdlkaj "+c
										 * .toString()); if
										 * (c.getAttachment()!=null){
										 * 
										 * logger.info("Attachment url: "+
										 * c.getAttachment()
										 * +"  c.getAttachment().getType(): "+
										 * c.getAttachment().getType()+
										 * "  c.getAttachment().getUrl(): "
										 * +c.getAttachment().getUrl());
										 * if(c.getAttachment
										 * ().getMedia()!=null) logger.info(
										 * " c.getAttachment().getMedia().getType(): "
										 * +
										 * c.getAttachment().getMedia().getType(
										 * )+
										 * " c.getAttachment().getMedia().getImage().getSrc():  "
										 * +
										 * c.getAttachment().getMedia().getImage
										 * ().getSrc()); }
										 * 
										 * try { comment.setAttachmentType((c.
										 * getAttachment()!=null
										 * ?(c.getAttachment
										 * ().getMedia()!=null?c
										 * .getAttachment().
										 * getMedia().getType():""):""));
										 * comment
										 * .setAttachmentUrl((c.getAttachment
										 * ()!=null ?
										 * (c.getAttachment().getMedia
										 * ()!=null?c.
										 * getAttachment().getMedia().
										 * getImage().getSrc():""):"")); } catch
										 * (Exception e) { // TODO
										 * Auto-generated catch block
										 * e.getMessage(); }
										 */
										j++;
										mongoOps.save(comment,
												FB_COMMENT_COLLECTION);
									}
									p.setFbComments(uuidCommentId);
									// p.setCommentCount((long)i.getComments().getData().size());
									// p.setCommentCount((long)j);
								}
							} catch (Exception e1) {
								logger.info("google plus Comments list exception..."
										+ e1.toString());
								// TODO Auto-generated catch block

							}

							/*
							 * p.setCreatedDate(new DateTime(i.getCreatedTime())
							 * .toDateTimeISO().getMillis());
							 */
							/* p.setCreatedDate(i.getCreatedTime().getTime()); */

							/*
							 * final DateTime dateTime = new
							 * DateTime(i.getCreatedTime().getTime(),
							 * DateTimeZone.forID("UTC"));
							 * p.setCreatedDate(dateTime.getMillis());
							 */
							// p.setCreatedDate(new
							// DateTime(i.getCreatedTime()).withZone(DateTimeZone.UTC).getMillis());

							Date local = i.getPublished();
							long utc = zone.convertLocalToUTC(local.getTime(),
									false);
							p.setCreatedDate(utc);

							/*
							 * if (i.getCreatedTime() != null) { DateTime
							 * createdDate = new DateTime( i.getCreatedTime());
							 * 
							 * p.setCreatedDate(createdDate.toDateTimeISO()
							 * .getYear() + "-" + createdDate.toDateTimeISO()
							 * .getMonthOfYear() + "-" +
							 * createdDate.toDateTimeISO() .getDayOfMonth());
							 * 
							 * }
							 */

							p.setPostType((i.getKind() != null ? i.getKind()
									: ""));

							p.setName((i.getActor().getDisplayName() != null ? i
									.getActor().getDisplayName() : ""));

							/*
							 * try { if (i.getComments()!=null &&
							 * i.getComments() instanceof com.restgoogle
							 * plus.types.Post.Comments ||
							 * i.getComments().getData() != null) { Long
							 * commentCountFromComments = i.getComments()
							 * .getTotalCount(); List<Comment> comments =
							 * (i.getComments() .getData() != null ?
							 * i.getComments() .getData() : null); if (comments
							 * != null) { long dataCount = comments.size();
							 * p.setLikeCount(commentCountFromComments >=
							 * dataCount ? commentCountFromComments :
							 * dataCount); } } else { p.setCommentCount(0L); } }
							 * catch (Exception e) { // TODO Auto-generated
							 * catch block
							 * logger.error("Comment count not exception...."
							 * +e.toString());
							 * 
							 * }
							 */

							p.setPhotoUrl((i.getActor().getImage().getUrl()) != null ? (i
									.getActor().getImage().getUrl()) : "");

							p.setLink((i.getObject().getContent() != null ? i
									.getObject().getContent() : ""));

							p.setSocialMediaType((String) jsonObject
									.get("social_media_type"));

							/*
							 * p.setDeviceType((String) jsonObject
							 * .get("device_type"));
							 * 
							 * p.setDeviceToken((String) jsonObject
							 * .get("device_token"));
							 * 
							 * p.setRegisterId((String) jsonObject
							 * .get("registration_id"));
							 * 
							 * p.setApiKey((String) jsonObject.get("apikey"));
							 */

							p.setChild_user_id((String) jsonObject
									.get("child_user_id"));

							/*
							 * p.setUpdatedDate(new DateTime(i.getUpdatedTime())
							 * .toDateTimeISO().getMillis());
							 */
							/* p.setUpdatedDate(i.getUpdatedTime().getTime()); */

							/*
							 * final DateTime updateTime = new
							 * DateTime(i.getUpdatedTime().getTime(),
							 * DateTimeZone.forID("UTC"));
							 * p.setUpdatedDate(updateTime.getMillis());
							 */
							// Date updatedLocal = i.getUpdatedTime();
							// long updatedutc =
							// zone.convertLocalToUTC(updatedLocal.getTime(),
							// false);
							/*
							 * DateTime updateTime=new
							 * DateTime(i.getUpdatedTime().g);
							 * 
							 * p.setUpdatedDate(updatedutc);
							 */

							DateTime retriveDate = new DateTime(i.getUpdated());
							DateTime sendingDate = new DateTime(
									retriveDate.getYear(),
									retriveDate.getMonthOfYear(),
									retriveDate.getDayOfMonth(), 0, 0, 0);
							p.setUpdatedDate(sendingDate.getMillis());
							p.setUpdateDateTime(retriveDate.getMillis());

							/*
							 * if (i.getUpdatedTime() != null) { DateTime
							 * updateDate = new DateTime( i.getUpdatedTime());
							 * 
							 * p.setUpdatedDate((updateDate.toDateTimeISO()
							 * .getYear() + "-" + updateDate.toDateTimeISO()
							 * .getMonthOfYear() + "-" + updateDate
							 * .toDateTimeISO().getDayOfMonth()));
							 * 
							 * }
							 */

							p.setCaption((i.getTitle() != null ? i.getTitle()
									: ""));

							p.setDescription((i.getObject().getContent() != null ? i
									.getObject().getContent() : ""));

							p.setStory((i.getVerb() != null ? i.getVerb() : ""));

							/*
							 * p.setIcon((i.getIcon() != null ? i.getIcon() :
							 * ""));
							 * 
							 * p.setPicture((i.getPicture() != null ? i
							 * .getPicture() : ""));
							 */

							p.setFromId((i.getActor().getId() != null ? i
									.getActor().getId() : ""));

							p.setAttribution((i.getGeocode() != null ? i
									.getGeocode() : ""));
							p.setReadFlag(0);
							p.setSaveFlag(0);
							p.setDeleteFlag(0);
							p.setErrorMessage("");
							p.setErrorFlag(0);
							p.setNotificationFlag(0);
							p.setParentId((String) jsonObject.get("parent_id"));
							p.setSocialSiteUserId((String) jsonObject
									.get("social_site_user_id"));
							logger.info("google plus testing person object creted successfully.");

							synchronized (this) {
								try {

									if (writechannel != null
											&& writechannel.isOpen()) {
										/*
										 * logger.info(
										 * "write channel getting closed");
										 */
										writechannel.close();
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block

									logger.info("Exception in closing the channel. "
											+ e.toString());
								}

								writechannel = connection1.createChannel();
								writechannel.queueDeclare(RQUEUE_NAME, false,
										false, false, args);
								p1 = new Person();
								if (mongoOps.findById(i.getId(), Person.class,
										PERSON_COLLECTION) != null) {
									p1 = mongoOps.findById(i.getId(),
											Person.class, PERSON_COLLECTION);
								}
								if (p1.getUuid() != null
										&& p1.toString().length() > 0) {
									/*
									 * logger.info("Check Same Post data : " +
									 * (p1.toString()).equals(p .toString()) +
									 * "@@" + i.getId());
									 */
									if (!(p1.toString()).equals(p.toString())) {
										/*
										 * logger.info("Update the post with Data: "
										 * + p.toString() + " for social type:"
										 * + p.getSocialMediaType());
										 */
										mongoOps.save(p, PERSON_COLLECTION);
										logger.info("After update update googlePlus post timeStamp  "
												+ p.getId()
												+ " is"
												+ (System.currentTimeMillis()));
										obj = new JSONObject();
										obj.put("uuid", p.getUuid());
										/*
										 * obj.put("apikey", p.getApiKey());
										 * obj.put("deviceToken",
										 * p.getDeviceToken());
										 * obj.put("deviceType",
										 * p.getDeviceType());
										 * obj.put("registeredId",
										 * p.getRegisterId());
										 */
										obj.put("socialmediaType",
												p.getSocialMediaType());
										obj.put("child_user_id",
												p.getChild_user_id());
										obj.put("parent_id", p.getParentId());
										/*
										 * logger.info(
										 * "Json object that will be sent to response queue for id:    "
										 * + p.getId() + " is" +
										 * obj.toJSONString());
										 */
										writechannel.basicPublish("",
												RQUEUE_NAME, null, obj
														.toString().getBytes());
									}
								} else {
									/*
									 * logger.info("New post with Data: " +
									 * p.toString() + " for social type:" +
									 * p.getSocialMediaType());
									 */
									mongoOps.save(p, PERSON_COLLECTION);
									logger.info("After save  googlePlus post timeStamp  "
											+ p.getId()
											+ " is"
											+ (System.currentTimeMillis()));
									obj = new JSONObject();
									obj.put("uuid", p.getUuid());
									/*
									 * obj.put("apikey", p.getApiKey());
									 * obj.put("deviceToken",
									 * p.getDeviceToken());
									 * obj.put("deviceType", p.getDeviceType());
									 * obj.put("registeredId",
									 * p.getRegisterId());
									 */
									obj.put("socialmediaType",
											p.getSocialMediaType());
									obj.put("child_user_id",
											p.getChild_user_id());
									obj.put("parent_id", p.getParentId());
									/*
									 * logger.info(
									 * "Json object that will be sent to response queue :    "
									 * + obj.toJSONString());
									 */
									writechannel.basicPublish("", RQUEUE_NAME,
											null, obj.toString().getBytes());
								}

							}
							/*
							 * logger.info("###############End#############" +
							 * i.getId());
							 */
						}
					} catch (UnknownHostException e) {
						logger.error("UnknownHostException: e.toSting: "
								+ e.toString());
					} finally {
						if (writechannel != null && writechannel.isOpen()) {
							writechannel.close();
						}
					}
					
				} catch (Exception e) {
					logger.error("Exception in processing a post:"
							+ e.getMessage());
				} finally {
					errorPerson = null;
					data = null;
					obj = null;
					if (mongo != null && mongo instanceof MongoClient) {
						try {
							mongo.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					if (writechannel != null && writechannel.isOpen()) {
						try {
							writechannel.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							logger.info("Write channel closing exception:"
									+ e.getMessage() + " e.tostring"
									+ e.toString());
						}
					}

				}
		
		
	}

}
