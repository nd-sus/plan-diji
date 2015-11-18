package com.dijiwise.semaphore;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.social.oauth2.OAuth2Parameters;

import com.dijiwise.reader.Person;
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

public class GoogleplusWorker {
	// facebook handler
	// facebook handler
	static Logger logger = Logger.getLogger(GoogleplusWorker.class);
	byte[] body;
	String routingKey;
	ExecutorService executor;
	com.rabbitmq.client.Connection connection1;
	Properties prop;
	Long counterValue;
	public GoogleplusWorker(){}
	public GoogleplusWorker(byte[] body, String routingKey,
			ExecutorService executor,
			com.rabbitmq.client.Connection connection1, Properties prop,
			Long counterValue) {
		this.body = body;
		this.routingKey = routingKey;
		this.executor = executor;
		this.connection1 = connection1;
		this.prop = prop;

	}

	public static void processData(final byte[] body, final String routingKey,
			
			final com.rabbitmq.client.Connection connection1,
			final Properties prop, final Long counterValue,
			final MongoClient mongo1) {

		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					logger.setLevel(Level.INFO);
					System.setProperty("DEBUG.MONGO", "false");
					//SocialNetworkService.getInstance().getThreaPoolLock();
					PersonLock.getInstance().getThreaPoolLock();
					String accessTokenValue = null;
					String refreshTokenValue = null;
					MongoOperations mongoOps = null;
					Plus plus = null;
					List<Activity> data = null;
					String PERSON_COLLECTION = "";
					String FB_COMMENT_COLLECTION = "";
					String PERSON_COLLECTION_TEMP="";
					String FB_COMMENT_COLLECTION_TEMP = "";

					com.rabbitmq.client.ConnectionFactory factory1 = null;
					MongoClient mongo = null;
					DateTimeZone zone = DateTimeZone.getDefault();

					Channel writechannel = null;
					Person errorPerson = null;
					JSONObject obj = null;
					Person p = null;
					Person p1 = null;

					PropertyConfigurator
							.configure("/dijiwise/log4j.properties");

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

					String message = new String(body);
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject) jsonParser
							.parse(message);

					accessTokenValue = ((String) jsonObject.get("access_token") != null ? (String) jsonObject
							.get("access_token") : "");
					refreshTokenValue = ((String) jsonObject
							.get("refresh_token") != null ? (String) jsonObject
							.get("refresh_token") : "");
					/*
					 * refreshTokenValue=
					 * "1/P7XmbWY3BE8o57a5yAvDckiwOAZkRVIah4tTXqnrxCY";
					 * accessTokenValue =
					 * "ya29.BAEpHzyQH3yIoNTb00AuAGbHET4bpaZOBSI_t94n06U8-zJhcmlFla3E1pPgzucS86C719w9L3c19Q"
					 * ;
					 */

					String secret = "wMkZAYYeKTgXMFr_A-0EUPnT";
					String id = "881464315694-225kilo20ne8rg0j42n21fkprgk87pnq.apps.googleusercontent.com";
					OAuth2Parameters oAuthParams = new OAuth2Parameters();

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
						Thread.sleep(5000);
						/*
						 * if (mongo != null && mongo instanceof MongoClient) {
						 * try { mongo.close(); } catch (Exception e) { // TODO
						 * Auto-generated catch block
						 * logger.error("mongo connection exception : " +
						 * e.toString()); } }
						 */
						mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
						mongoOps = new MongoTemplate(mongo, DB_NAME);

						// plus = factory.getApi(accessTokenValue);
						plus = factory.getApi(accessTokenValue,
								refreshTokenValue, null);

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
						
						if (writechannel != null && writechannel.isOpen()) {
							writechannel.close();
						}
						writechannel = connection1.createChannel();
						writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);
						writechannel.basicPublish("", RQUEUE_NAME, null, jsonObject.toString().getBytes());
						if (mongo != null && mongo instanceof MongoClient) {
							try {
								mongo.close();
							} catch (Exception ex) {
								// TODO Auto-generated catch block
								logger.error("mongo connection exception : " + ex.toString());
							}
						}
						if (writechannel != null && writechannel.isOpen()) {
							writechannel.close();
						}
					} finally {

					}
					try {
						/*
						 * logger.info("Thread  Got data  w.r.t accesstoken :" +
						 * (String) jsonObject.get("access_token") +
						 * "\n Thread Name:" + this.getName());
						 */
						for (Activity i : data) {

							logger.info("\n STGOOGLEPLUSstart processing "
									+ i.getId() + " is "
									+ (System.currentTimeMillis()));
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

							Date local = i.getPublished();
							long utc = zone.convertLocalToUTC(local.getTime(),
									false);
							p.setCreatedDate(utc);

							p.setPostType((i.getKind() != null ? i.getKind()
									: ""));

							p.setName((i.getActor().getDisplayName() != null ? i
									.getActor().getDisplayName() : ""));

							p.setPhotoUrl((i.getActor().getImage().getUrl()) != null ? (i
									.getActor().getImage().getUrl()) : "");

							p.setLink((i.getObject().getContent() != null ? i
									.getObject().getContent() : ""));

							p.setSocialMediaType((String) jsonObject
									.get("social_media_type"));

							p.setChild_user_id((String) jsonObject
									.get("child_user_id"));

							DateTime retriveDate = new DateTime(i.getUpdated());
							DateTime sendingDate = new DateTime(retriveDate
									.getYear(), retriveDate.getMonthOfYear(),
									retriveDate.getDayOfMonth(), 0, 0, 0);
							p.setUpdatedDate(sendingDate.getMillis());
							p.setUpdateDateTime(retriveDate.getMillis());

							p.setCaption((i.getTitle() != null ? i.getTitle()
									: ""));

							p.setDescription((i.getObject().getContent() != null ? i
									.getObject().getContent() : ""));

							p.setStory((i.getVerb() != null ? i.getVerb() : ""));

							p.setFromId((i.getActor().getId() != null ? i
									.getActor().getId() : ""));

							p.setAttribution((i.getGeocode() != null ? i
									.getGeocode() : ""));

							p.setReadFlag(0);
							p.setSaveFlag(0);
							p.setDeleteFlag(0);
							p.setErrorFlag(0);
							p.setErrorMessage("");
							p.setNotificationFlag(0);
							p1 = new Person();

							try {
								if (mongoOps.findById(i.getId(), Person.class,
										PERSON_COLLECTION) != null) {
									p1 = mongoOps.findById(i.getId(),
											Person.class, PERSON_COLLECTION);
									p.setReadFlag(p1.getReadFlag());
									p.setSaveFlag(p1.getSaveFlag());
									p.setDeleteFlag(p1.getDeleteFlag());
									p.setErrorFlag(p1.getErrorFlag());
									p.setErrorMessage("");
									p.setNotificationFlag(p1
											.getNotificationFlag());
								}
							} catch (Exception e) {
								logger.info("Googleplus flag modification exception.."
										+ e.toString());
							}

							p.setParentId((String) jsonObject.get("parent_id"));
							p.setSocialSiteUserId((String) jsonObject
									.get("social_site_user_id"));
							logger.info("google plus testing person object creted successfully.");

							// synchronized (this) {
							try {

								if (writechannel != null
										&& writechannel.isOpen()) {

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
							
							mongoOps.save(p, PERSON_COLLECTION);
							logger.info("After save  googlePlus post timeStamp  "
									+ p.getId()
									+ " is"
									+ (System.currentTimeMillis()));
							obj = new JSONObject();
							obj.put("uuid", p.getUuid());
							
							obj.put("socialmediaType", p.getSocialMediaType());
							obj.put("child_user_id", p.getChild_user_id());
							obj.put("parent_id", p.getParentId());
							
							writechannel.basicPublish("", RQUEUE_NAME, null,
									obj.toString().getBytes());

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

					} catch (UnknownHostException e) {
						logger.error("UnknownHostException: e.toSting: "
								+ e.toString());
					} finally {
						if (mongo != null && mongo instanceof MongoClient) {
							try {
								mongo.close();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								logger.error("mongo connection exception : "
										+ e.toString());
							}
						}
						if (writechannel != null && writechannel.isOpen()) {
							writechannel.close();
						}
					}

				} catch (Exception e) {
					logger.error("Exception in processing a post:"
							+ e.getMessage());
				} finally {

					//SocialNetworkService.getInstance().releaseThreaPoolLock();
					PersonLock.getInstance().releaseThreaPoolLock();
					/*try {
						Thread.currentThread().stop();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						
					}*/

				}

			}

		});
		t1.start();
	}
}
