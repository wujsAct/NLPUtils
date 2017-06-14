package tacKBP;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.sound.midi.SysexMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tacKBP.KBPEntity;
import tacKBP.MongoUtils;
public class ParsekbpKG {
	static String path="E:/datasets/entity linking/tac_kbp_ref_know_base/data/";
	
	static public MongoUtils mongoUtils = new MongoUtils("kbpwiki");
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		insertData();
		System.exit(0);
	}
	
	public static String [] getFileName(String path)
    {
        File file = new File(path);
        String [] fileName = file.list();
        return fileName;
    }
	
	public static void getAllFileName(String path,ArrayList<String> fileName)
    {
        File file = new File(path);
        File [] files = file.listFiles();
        String [] names = file.list();
        if(names != null)
        fileName.addAll(Arrays.asList(names));
        for(File a:files)
        {
            if(a.isDirectory())
            {
                getAllFileName(a.getAbsolutePath(),fileName);
            }
        }
        
    }
	
	public static void getEntid2wiki() throws SAXException, IOException, ParserConfigurationException{
		Writer aiWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File("data/tacKBP/"+"kbpentid2wiki.txt"), false), "UTF8"));
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		factory.setNamespaceAware(true);
		EntityHandlerEnt2Wiki handler = new EntityHandlerEnt2Wiki();
		HashMap<String, String> kbpentid2wiki;
		
        ArrayList<String> listFileName = new ArrayList<String>(); 
        getAllFileName(path,listFileName);
        
        for(String e:listFileName){
        	System.out.println(e);
        	parser.parse(new InputSource(path+e), handler);
        	kbpentid2wiki = handler.getKbpentid2wikiName();
        	for(String key:kbpentid2wiki.keySet()){
        		aiWriter.write(key+"\t"+kbpentid2wiki.get(key)+"\n");
        		aiWriter.flush();
        	}
        }
		aiWriter.close();
	}
	
	public static void insertData() throws ParserConfigurationException, SAXException, IOException{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		factory.setNamespaceAware(true);
		
		EntityHandler handler = new EntityHandler(mongoUtils);
		ArrayList<String> listFileName = new ArrayList<String>(); 
        getAllFileName(path,listFileName);
        
        for(String e:listFileName){
        	System.out.println(e);
        	parser.parse(new InputSource(path+e), handler);
        	//System.exit(0);
        }
	}
}

class EntityHandler extends DefaultHandler{
	private MongoUtils mutils;
	private HashMap<String,String> entity;
	private String rel=""; private String relctx = "";
	private String linkText="";private String predictEntId="";
	private String wikiText=""; private boolean iswikiText;
	private boolean isRel;
	private boolean isLink;
	public EntityHandler(MongoUtils utils){
		mutils = utils;
	}
	
 	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
		//System.out.println("finish parse.");
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(qName.equals("entity")){
			entity = new HashMap<String, String>();
			entity.put("wiki_title", attributes.getValue("wiki_title"));
			entity.put("type",attributes.getValue("type"));
			entity.put("name", attributes.getValue("name"));
			entity.put("entId", attributes.getValue("id"));
		}
		if(qName.equals("facts")){
			entity.put("InfoClass",attributes.getValue("class"));
		}
		if(qName.equals("fact")){
			rel = attributes.getValue("name");
			isRel = true;
		}
		if(qName.equals("link")){
			isLink = true;
			predictEntId = attributes.getValue("entity_id");
		}
		if(qName.equals("wiki_text")){
			rel = "wiki_text";
			iswikiText=true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(qName.equals("fact")){
			isRel=false;
			isLink=false;
			entity.put(rel, relctx);
			//System.out.println(rel+"\t"+relctx.replace("\n", "\t"));
			relctx = "";
		}
		if(qName.equals("entity")){
			mutils.insertEntity("entity", entity);
			//System.out.println("-------------------");
			//System.exit(0);
		}
		if(qName.equals("wiki_text")){
			iswikiText=false;
			entity.put(rel, relctx);
			//System.out.println(rel+"\t"+relctx.replace("\n", " "));
			relctx="";
			wikiText="";
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String obj = new String(ch, start, length);
		if(iswikiText){
			if(wikiText.length()!=0){
				wikiText +=" ";
			}
			wikiText +=obj;
			relctx = wikiText;
		}
		if(isRel && !isLink){
			if (relctx.length()!=0){
				relctx += "\t";
			}
			relctx += obj;
		}
		if(isLink){
			linkText = obj;
			if (relctx.length()!=0){
				relctx += "\t";
			}
			if(predictEntId!=null){
				relctx += predictEntId;
				if(linkText.length()!=0){
					relctx += "---"+new String(ch, start, length).trim();
				}
			}else{
				relctx += new String(ch, start, length).trim();
			}
		}
		
	}
	
}

class EntityHandlerEnt2Wiki extends DefaultHandler {
	private HashMap<String, String> kbpentid2wikiName;
	private String entid;
	private String wikiName;
	private String wikiTitle;
	private String infoClass;
	/**
	 * only recall one time
	 */
	@Override
	public void startDocument() throws SAXException {
		System.out.println("start to parse document...");
		kbpentid2wikiName = new HashMap<String, String>();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		/**
		 * In our paper, we just need to extract the kbpEntId to wiki_name
		 * <entity wiki_title="Mike_Quigley_(footballer)" type="PER" id="E0000001" name="Mike Quigley (footballer)">
		 */
		if(qName.equals("entity")){
			entid = attributes.getValue("id");
			wikiName = attributes.getValue("name");
			wikiTitle = attributes.getValue("wiki_title");
			//System.out.println(entid+"\t"+wikiName);
			kbpentid2wikiName.put(entid, wikiName+"\t"+wikiTitle);
		}
	}
	public HashMap<String, String> getKbpentid2wikiName() {
		return kbpentid2wikiName;
	}

	public void setKbpentid2wikiName(HashMap<String, String> kbpentid2wikiName) {
		this.kbpentid2wikiName = kbpentid2wikiName;
	}
	
	
	
}