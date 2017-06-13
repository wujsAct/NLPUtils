package tacKBP;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tacKBP.KBPEntity;

public class ParsekbpKG {
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
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		String path="E:/datasets/entity linking/tac_kbp_ref_know_base/data/";
		Writer aiWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File("data/tacKBP/"+"kbpentid2wiki.txt"), false), "UTF8"));
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		factory.setNamespaceAware(true);
		EntityHandler handler = new EntityHandler();
		HashMap<String, String> kbpentid2wiki;
		
        ArrayList<String> listFileName = new ArrayList<String>(); 
        getAllFileName(path,listFileName);
        
        for(String e:listFileName){
        	parser.parse(new InputSource(path+"kb_part-0001.xml"), handler);
        	kbpentid2wiki = handler.getKbpentid2wikiName();
        	for(String key:kbpentid2wiki.keySet()){
        		aiWriter.write(key+"\t"+kbpentid2wiki.get(key)+"\n");
        		aiWriter.flush();
        	}
        }
        
		aiWriter.close();
		System.exit(0);
	}
}

class EntityHandler extends DefaultHandler {
	private HashMap<String, String> kbpentid2wikiName;
	private String entid;
	private String wikiName;
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
			System.out.println(entid+"\t"+wikiName);
			kbpentid2wikiName.put(entid, wikiName);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("parse finish.");
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
	}

	public HashMap<String, String> getKbpentid2wikiName() {
		return kbpentid2wikiName;
	}

	public void setKbpentid2wikiName(HashMap<String, String> kbpentid2wikiName) {
		this.kbpentid2wikiName = kbpentid2wikiName;
	}
	
	
	
}