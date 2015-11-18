package com.digiwise.reader;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class testCommetCount {

	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		try {
			MongoClient mongo = new MongoClient("173.244.67.213", 27017);
			DB db = mongo.getDB("dijiwisecdndb");
			DBCollection collection = db.getCollection("dijiwiseComment");
			//BasicDBObject whereQuery = new BasicDBObject();
			BasicDBObject whereQuery = new BasicDBObject();
			
			whereQuery.put("postId", "423670114403144_558292004274287_4");
			DBCursor cursor = collection.find(whereQuery);
			System.out.println(whereQuery+"running the file"+cursor.count());
			
			/*while (cursor.hasNext()) {
				
				 System.out.println(cursor.next());
				
			}*/
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
