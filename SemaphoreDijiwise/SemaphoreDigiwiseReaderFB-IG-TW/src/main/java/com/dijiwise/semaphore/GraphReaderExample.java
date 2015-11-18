package com.dijiwise.semaphore;

/*
 * Copyright (c) 2010-2014 Mark Allen.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */



import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.Facebook;
import com.restfb.FacebookClient;
import com.restfb.JsonMapper;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.Page;
import com.restfb.types.Post;
import com.restfb.types.Url;
import com.restfb.types.User;

/**
 * Examples of RestFB's Graph API functionality.
 * 
 * @author <a href="http://restfb.com">Mark Allen</a>
 */
//public class GraphReaderExample extends Example {
public class GraphReaderExample  {
  
  private final FacebookClient facebookClient;
  final static Logger logger = Logger.getLogger(SocialNetworkService.class);
  

  
  
  
  GraphReaderExample(String accessToken) {
	  //TODO Senthil - Changed the constructor signature
   facebookClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_3);
	 // facebookClient = new DefaultFacebookClient( accessToken,  "336dc5c1671e843e5bcc551c6f0b028d",  Version.Version_2_2) ;
  }

  public com.restfb.Connection<User> friendsList(){
	  User user = facebookClient.fetchObject("me", User.class);
	  String userName = user.getFirstName();
	  if (userName == null){
	  userName = user.getLastName();
	  }
	  String userEmail = user.getEmail();
	  com.restfb.Connection<User> myFriends = facebookClient.fetchConnection("me/friends", User.class);
	  return myFriends;
 }

  void fetchObjectsAsJsonObject() {
    out.println("* Fetching multiple objects at once as a JsonObject *");

    List<String> ids = new ArrayList<String>();
    ids.add("btaylor");
    ids.add("http://www.imdb.com/title/tt0117500/");

    // Make the API call
    JsonObject results = facebookClient.fetchObjects(ids, JsonObject.class);

    // Pull out JSON data by key and map each type by hand.
    JsonMapper jsonMapper = new DefaultJsonMapper();
    User user = jsonMapper.toJavaObject(results.getString("btaylor"), User.class);
    Url url = jsonMapper.toJavaObject(results.getString("http://www.imdb.com/title/tt0117500/"), Url.class);

    out.println("User is " + user);
    out.println("URL is " + url);
  }

  void fetchObjects() {
    out.println("* Fetching multiple objects at once *");

    FetchObjectsResults fetchObjectsResults =
        facebookClient.fetchObjects(Arrays.asList("me", "cocacola"), FetchObjectsResults.class);

    out.println("User name: " + fetchObjectsResults.me.getName());
    out.println("Page likes: " + fetchObjectsResults.page.getLikes());
  }

  void fetchDifferentDataTypesAsJsonObject() {
    out.println("* Fetching different types of data as JsonObject *");

    JsonObject btaylor = facebookClient.fetchObject("btaylor", JsonObject.class);
    out.println(btaylor.getString("name"));

    JsonObject photosConnection = facebookClient.fetchObject("me/photos", JsonObject.class);
    JsonArray photosConnectionData = photosConnection.getJsonArray("data");

    if (photosConnectionData.length() > 0) {
      String firstPhotoUrl = photosConnectionData.getJsonObject(0).getString("source");
      out.println(firstPhotoUrl);
    }

    String query = "SELECT uid, name FROM user WHERE uid=220439 or uid=7901103";
    List<JsonObject> queryResults = facebookClient.executeQuery(query, JsonObject.class);

    if (queryResults.size() > 0)
      out.println(queryResults.get(0).getString("name"));
  }

  /**
   * Holds results from a "fetchObjects" call.
   */
  public static class FetchObjectsResults {
    @Facebook
    User me;

    @Facebook(value = "cocacola")
    Page page;
  }

  void fetchConnections() {
    out.println("* Fetching connections *");

   // Connection<User> myFriends = facebookClient.fetchConnection("me/friends", User.class);
    Connection<Post> myFeed = facebookClient.fetchConnection("me/feed", Post.class);

    //out.println("Count of my friends: " + myFriends.getData().size());

    if (myFeed.getData().size() > 0)
      out.println("First item in my feed: " + myFeed.getData().get(0).getMessage());
  }

  
  ArrayList<Post> fetchConnectionsJson1() {
	    out.println("* Fetching connections *");
	    System.out.println("\n Fetching connections ");

	   // Connection<User> myFriends = facebookClient.fetchConnection("me/friends", User.class);
	    Connection<Post> myFeed = facebookClient.fetchConnection("me/feed", Post.class);

	    //out.println("Count of my friends: " + myFriends.getData().size());
	   
	    //System.out.println(DateTime.Compare(new DateTime(2014,11,16,10,10), DateTime.now()) < 2);
	    //System.out.println( (new DateTime(2014,11,16,10,10)) - DateTime.now() );
	   /* 
	    DateTime t1 = new DateTime(100);
	    DateTime t2 = new DateTime(20);
	    DateTime dt = new DateTime(2005, 3, 26, 12, 0, 0, 0);
	  //if (DateTime.Compare(t1, t2) >  0){}
	    
	    Days d = Days.daysBetween(DateTime.now(), dt);
	    int days = d.getDays();*/
	 //   System.out.println("\n\n\n\ndatas betweeen  :"+days+"\n\n\n "+myFeed.getData()+"  nextpageurl"+myFeed.getNextPageUrl()+" ");
	    for (Post i: myFeed.getData() )
	    {
	    	//DateTime createdDate=new DateTime(i.getCreatedTime());
	    	 //System.out.println("\n\n\n\ndatas betweeen  :"+(Days.daysBetween(new DateTime(i.getCreatedTime()),DateTime.now()).getDays())+"\n\n\n");
	    	
	    	//System.out.println("\n\n\n\ndatas betweeen  :"+(Days.daysBetween(new DateTime(i.getCreatedTime()),DateTime.now()).getDays())+"\n\n\n");
	    	//System.out.println("\n\n\n\ndatas betweeen  :"+(Days.
	    			//daysBetween(new DateTime(i.getCreatedTime()),DateTime.now()).getDays())+"\n\n\n");
	    	//String contentUrl=(i.getType()=="picture"?i.getPicture():(i.getType()=="video"?i.getSource():"null"));
	    	//if((Days.daysBetween(new DateTime(i.getCreatedTime()),DateTime.now()).getDays() <= 2))
	    	//{
	    		
	    	//	System.out.println("ClienttId="+i.getId()+" post="+i.getMessage()+" createdDate="+i.getCreatedTime()+" type="+i.getType()+" Createdby"+i.getName()+" commentscount="+i.getCommentsCount()+" Lkescount="+i.getLikesCount()+" photorvideourl="+contentUrl+" updated:"+i.getUpdatedTime());
	    //	}
	    //	else{
	    //		System.out.println("ClienttId="+i.getId()+" post="+i.getMessage()+"  in else part createdDate="+i.getCreatedTime()+" type="+i.getType()+" Createdby"+i.getName()+" commentscount="+i.getCommentsCount()+" Lkescount="+i.getLikesCount()+" photorvideourl="+contentUrl+" updated:"+i.getUpdatedTime());
	    //	}
	    		
	    	
	    }
	   
	    if (myFeed.getData().size() > 0)
	      out.println("First item in my feed: " + myFeed.getData().get(0).getMessage());
	    
	   // return (ArrayList<Post>) myFeed.getData();
	    return new ArrayList<Post>(myFeed.getData());
	    
	    
	  }
  
  void query() {
    out.println("* FQL Query *");

    List<FqlUser> users =
        facebookClient.executeQuery("SELECT uid, name FROM user WHERE uid=220439 or uid=7901103", FqlUser.class);

    out.println("User: " + users);
  }

  void multiquery() {
    out.println("* FQL Multiquery *");

    Map<String, String> queries = new HashMap<String, String>();
    queries.put("users", "SELECT uid, name FROM user WHERE uid=220439 OR uid=7901103");
    queries.put("likers", "SELECT user_id FROM like WHERE object_id=122788341354");

    MultiqueryResults multiqueryResults = facebookClient.executeMultiquery(queries, MultiqueryResults.class);

    out.println("Users: " + multiqueryResults.users);
    out.println("People who liked: " + multiqueryResults.likers);
  }

  /**
   * Holds results from an "executeQuery" call.
   * <p>
   * Be aware that FQL fields don't always map to Graph API Object fields.
   */
  public static class FqlUser {
    @Facebook
    String uid;

    @Facebook
    String name;

    @Override
    public String toString() {
      return format("%s (%s)", name, uid);
    }
  }

  /**
   * Holds results from an "executeQuery" call.
   * <p>
   * Be aware that FQL fields don't always map to Graph API Object fields.
   */
  public static class FqlLiker {
    @Facebook("user_id")
    String userId;

    @Override
    public String toString() {
      return userId;
    }
  }

  /**
   * Holds results from a "multiquery" call.
   */
  public static class MultiqueryResults {
    @Facebook
    List<FqlUser> users;

    @Facebook
    List<FqlLiker> likers;
  }

  void search() {
    out.println("* Searching connections *");

    Connection<Post> publicSearch =
        facebookClient.fetchConnection("search", Post.class, Parameter.with("q", "watermelon"),
          Parameter.with("type", "post"));

    Connection<User> targetedSearch =
        facebookClient.fetchConnection("me/home", User.class, Parameter.with("q", "Mark"),
          Parameter.with("type", "user"));

    if (publicSearch.getData().size() > 0)
      out.println("Public search: " + publicSearch.getData().get(0).getMessage());

    out.println("Posts on my wall by friends named Mark: " + targetedSearch.getData().size());
    
  }

  void metadata() {
    out.println("* Metadata *");

    User userWithMetadata = facebookClient.fetchObject("me", User.class, Parameter.with("metadata", 1));

    out.println("User metadata: has albums? " + userWithMetadata.getMetadata().getConnections().hasAlbums());
  }

  void paging() {
    out.println("* Paging support *");

    Connection<User> myFriends = facebookClient.fetchConnection("me/friends", User.class);
    Connection<Post> myFeed = facebookClient.fetchConnection("me/feed", Post.class, Parameter.with("limit", 300));

    out.println("Count of my friends: " + myFriends.getData().size());

    if (myFeed.getData().size() > 0)
      out.println("First item in my feed: " + myFeed.getData().get(0));

    for (List<Post> myFeedConnectionPage : myFeed)
      for (Post post : myFeedConnectionPage)
        out.println("Post from my feed: " + post);
  }

  void selection() {
    out.println("* Selecting specific fields *");

    User user = facebookClient.fetchObject("me", User.class, Parameter.with("fields", "id,name"));

    out.println("User name: " + user.getName());
  }

  void parameters() {
    out.println("* Parameter support *");

    Date oneWeekAgo = new Date(currentTimeMillis() - 1000L * 60L * 60L * 24L * 7L);

    Connection<Post> filteredFeed =
        facebookClient.fetchConnection("me/home", Post.class, Parameter.with("limit", 3),
          Parameter.with("until", "yesterday"), Parameter.with("since", oneWeekAgo));

    out.println("Filtered feed count: " + filteredFeed.getData().size());
  }
  ArrayList<Post> FilteredPosts() {
	    out.println("* Parameter support *");
	    
	    
//	    Properties prop = new Properties();
//		InputStream input = null;
//		try {
//			//TODO Senthil - Commented by
////			File file = new File("/dijiwise/config.properties");
//			File file = new File(SocialNetworkService.CONFIG_PROP);
//			input = new FileInputStream(file);
//			prop.load(input);
//		} catch (IOException  e) {
//			logger.error("Exception Info:" + e.getMessage());
//		}
//		finally{
//			try {
//				input.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		final String DAYS_OLD = prop.getProperty("DAYS_OLD");
//		Long daysOld=new Long(Long.parseLong(DAYS_OLD));
		

	 //   Date oneWeekAgo = new Date(currentTimeMillis() - 1000L * 60L * 60L * 24L * 7L);
//	    Date daysAgo = new Date(currentTimeMillis() - 1000L * 60L * 60L * 24L * daysOld);
	    Date daysAgo = new Date(currentTimeMillis() - 1000L * 60L * 60L * 24L * 10);

	    Connection<Post> filteredFeed = 
	        facebookClient.fetchConnection("me/feed", Post.class,
	           Parameter.with("since", daysAgo),Parameter.with("limit", 100),
	        		Parameter.with("since", daysAgo));
	    out.println("***********beforehomefeed**********************************");
	    
	    Connection<Post> filteredHomefeed=null;
	  /*  synchronized (filteredHomefeed) {
			filteredHomefeed = facebookClient.fetchConnection("me/home",
					Post.class, Parameter.with("since", daysAgo));
		}

	    out.println("Filtered feed count: " + filteredFeed.getData().size());
	    
	    List<Post> allFeeds=filteredHomefeed.getData();
	    Boolean joinOperation=allFeeds.addAll(filteredFeed.getData());
	    out.println("Filtered home count: "+filteredHomefeed.getData().size());
	   */
	   // return new ArrayList<Post>(allFeeds);
	    return new ArrayList<Post>(filteredFeed.getData());
	  }
  ArrayList<Post> FilteredFeedPosts() {
	    out.println("* Parameter support *");
	    
	    
//	    Properties prop = new Properties();
//		InputStream input = null;
//		try {
//			//TODO Senthil - Commented by
//			File file = new File("/dijiwise/config.properties");
//			File file = new File(SocialNetworkService.CONFIG_PROP);
//			input = new FileInputStream(file);
//			prop.load(input);
//		} catch (IOException  e) {
//			logger.error("Exception Info:" + e.getMessage());
//		}
//		finally{
//			try {
//				input.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		final String DAYS_OLD = prop.getProperty("DAYS_OLD");
//		Long daysOld=new Long(Long.parseLong(DAYS_OLD));
		

	 //   Date oneWeekAgo = new Date(currentTimeMillis() - 1000L * 60L * 60L * 24L * 7L);
	    
//	    Date daysAgo = new Date(currentTimeMillis() - 1000L * 60L * 60L * 24L * daysOld)
	    Date daysAgo = new Date(currentTimeMillis() - 1000L * 60L * 60L * 24L * 10);

	    User myuser = facebookClient.fetchObject("me", User.class);
	    
	    Connection<Post> filteredFeed = facebookClient.fetchConnection("me/feed", Post.class,
	           Parameter.with("since", daysAgo),Parameter.with("limit", 100));
	    out.println("***********beforehomefeed**********************************");
	    
	    Connection<Post> filteredHomefeed=null;
	  
	    return new ArrayList<Post>(filteredFeed.getData());
	  }
  

  void rawJsonResponse() {
    out.println("* Raw JSON *");
    out.println("User object JSON: " + facebookClient.fetchObject("me", String.class));
  }
}
