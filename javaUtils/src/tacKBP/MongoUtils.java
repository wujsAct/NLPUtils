package tacKBP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoUtils {
	private MongoDatabase db;
	public MongoUtils(String dbName){
		@SuppressWarnings("resource")
		MongoClient mongoClient = new MongoClient( "192.168.3.196" , 27017 );
		this.setDb(mongoClient.getDatabase(dbName));
		System.out.println("connect to database successfully!");
	}
	
	public void insertEntity(String collectionName, HashMap<String,String> data){
		MongoCollection<Document> collection = db.getCollection(collectionName);
		 //�����ĵ�  
        /** 
        * 1. �����ĵ� org.bson.Document ����Ϊkey-value�ĸ�ʽ ;
        * 2. �����ĵ�����List<Document> 
        * 3. ���ĵ����ϲ������ݿ⼯���� mongoCollection.insertMany(List<Document>) ���뵥���ĵ������� mongoCollection.insertOne(Document) 
        * */
        Document document;
        String entId = data.get("entId");
        List<Document> documents = new ArrayList<Document>();
        for(String key:data.keySet()){
        	if(!key.equals("entId")){
        		document = new Document();
        		document.append("head", entId);
        		document.append("rel", key);
        		document.append("tail", data.get(key));
        		documents.add(document);
        	}
        }
        collection.insertMany(documents); 
        //System.out.println("�ĵ�����ɹ�");  
	}
	public static void main(String[] args){
		MongoUtils mongoUtils = new MongoUtils("wiki");
		HashMap<String,String> data = null;
		mongoUtils.insertEntity("demos", data);
	}


	public MongoDatabase getDb() {
		return db;
	}


	public void setDb(MongoDatabase db) {
		this.db = db;
	}
	
	
}
