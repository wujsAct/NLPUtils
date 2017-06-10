package tacKBP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

import edu.stanford.nlp.international.arabic.process.IOBUtils;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

class myRet{
	String newText;
	Hashtable<Character,Integer> doc2index;
	Hashtable<Integer,Character> index2doc;
	Hashtable<Integer,Integer> newIndex2oldIndex;
	Hashtable<Integer,Integer> oldIndex2newIndex;
}

public class TrainTextProcess {
	public static Properties props = new Properties();
	
	//deep learning coreference is good 
	static {
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
		
		//props.setProperty("tokenize.whitespace", "true");  //attention to those properties.
		//props.setProperty("ssplit.eolonly", "true"); // one line is a sentence;
		//props.setProperty("coref.algorithm", "neural");
		//props.setProperty("coref.neural.greedyness", "0.2");
	}
	
	public static StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	
	public static ArrayList<ArrayList<String>> getTokenPOS(List<CoreMap> sentences){
		ArrayList<String> tokenArray = new ArrayList<String>();
		ArrayList<String> posArray = new ArrayList<String>();
		ArrayList<String> neArray = new ArrayList<String>();
		Integer sentId = 0;
		for(CoreMap sentence: sentences) {
			String newString="";
		      // traversing the words in the current sentence
		      // a CoreLabel is a CoreMap with additional token-specific methods
		      for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		        // this is the text of the token
		        String word = token.get(TextAnnotation.class);
		        
		        // this is the POS tag of the token
		        String pos = token.get(PartOfSpeechAnnotation.class);
		        Integer start = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
		        Integer end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
		        newString += word+":"+start+" ";
		        String ne = token.get(NamedEntityTagAnnotation.class);
		        tokenArray.add(word+":"+start);
		        posArray.add(pos);
		        neArray.add(ne);
		      }
		      newString = newString.trim();
		      System.out.println(sentId+": "+newString.length()+":"+newString);
		      System.out.println("--------------------------------");
		      sentId += 1;
		}
		
		ArrayList<ArrayList<String>> retParams = new ArrayList<ArrayList<String>>();
		retParams.add(tokenArray);
		retParams.add(posArray);
		return retParams;
	}
	
	public static Hashtable<Integer,Integer> getTagIndex(String text,Hashtable<Integer,Integer> delete,String tag){
		Integer matchIndex = 0;
		while(true){
			Integer t = text.indexOf(tag,matchIndex);
			if(t==-1) break;
			for(Integer i=t; i<t+tag.length(); i++){
				delete.put(i, 1);
			}
			matchIndex = t+tag.length();
			//System.out.println(matchIndex);
		}
		return delete;
	}
	
	public static myRet getRawText(String text){
		
		Hashtable<Character,Integer> doc2index = new Hashtable<Character,Integer>();
		Hashtable<Integer,Character> index2doc = new Hashtable<Integer, Character>();
		Hashtable<Integer,Integer> newIndex2oldIndex = new Hashtable<Integer,Integer>();
		Hashtable<Integer,Integer> oldIndex2newIndex = new Hashtable<Integer,Integer>();
		for(Integer i=0;i<text.length();i++){
			doc2index.put(text.charAt(i), i);
			index2doc.put(i, text.charAt(i));
		}
		
		Integer beginText = text.indexOf("<HEADLINE>") + 6;
		System.out.println(beginText);
		Hashtable<Integer,Integer> delete= new Hashtable<Integer,Integer>();
		delete = getTagIndex(text,delete,"</DATELINE>");
		delete = getTagIndex(text,delete,"<P>");
		delete = getTagIndex(text,delete,"</P>");
		delete = getTagIndex(text,delete,"</TEXT>");
		delete = getTagIndex(text,delete,"</DOC>");
		
		String newText = "";
		Integer newIndex =0;
		for(Integer i=beginText;i<text.length();i++){
			if(!delete.containsKey(i)){
				newText += text.charAt(i);
				newIndex2oldIndex.put(newIndex, i);
				oldIndex2newIndex.put(i, newIndex);
				newIndex +=1;
			}
		}
		myRet rets = new myRet();
		rets.newText = newText;
		rets.doc2index = doc2index;
		rets.index2doc = index2doc;
		rets.oldIndex2newIndex = oldIndex2newIndex;
		rets.newIndex2oldIndex = newIndex2oldIndex;
		return rets;
	}
	/**
	 * TAC KBP annotation: whole document
	 * parse: only text
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		String path ="D:/Users/DELL/Workspaces/MyEclipse Professional 2014/protobuf/data/TACKBP/2014/training/source_documents/";
		//String path = "data/kbp/LDC2017EDL/data/2014/training/source_documents/";
		System.out.println("path: "+path);
		SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();  
        
        String shortText = "Mike Macnamee, chief executive of Bourn Hall, near Cambridge, the IVF clinic which Edwards founded, said: Bob Edwards is one of our greatest scientists.";
        System.out.println(shortText.length());
        Annotation document = new Annotation(shortText);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		getTokenPOS(sentences);
        
		String text = IOUtils.slurpFile(path+"AFP_ENG_20090802.0401.txt");
		System.out.println(text.length());
		Integer annotS = 2864; Integer annotE = 2871;
		
		document = new Annotation(text);
		pipeline.annotate(document);
		sentences = document.get(SentencesAnnotation.class);
		getTokenPOS(sentences);
		
		
		System.exit(0);
        /*//String text = IOUtils.slurpFile(path+"bolt-eng-DF-183-195681-7949372.txt");
        //Integer annotS = 2864; Integer annotE = 2871;
		
		System.out.println(text.substring(101,108+1));
		
		myRet rets = getRawText(text);
		//System.out.println(rets.newText);
		String newText = rets.newText;
		System.out.println(newText.length());
		String newEnts = "";
		for(Integer i=annotS;i<=annotE;i++){
			System.out.println(i);
			newEnts+=newText.substring(rets.oldIndex2newIndex.get(i),rets.oldIndex2newIndex.get(i)+1);
		}
		System.out.println(newEnts);*/
		
		System.exit(0);
		
		//we need to revise text 
		
        parser.parse(new File(path+"AFP_ENG_20090802.0401.txt"), new SAXParserHandler());
        //System.out.println(SAXParserHandler.text);
        System.out.println(SAXParserHandler.text.substring(0,7));
	}
}

class SAXParserHandler extends DefaultHandler{
	private Hashtable<String, Integer> tags;
	static String text="";
	static Integer pi =0;
	boolean isP=true;
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String content = new String(ch, start, length);
		content = content.replace("\n", " ");
		if(isP){
			text += ' '+content;
			System.out.println(pi + content);
			
		}
	}
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		Enumeration<String> e = tags.keys();
		while (e.hasMoreElements()) {
            String tag = (String)e.nextElement();
            int count = ((Integer)tags.get(tag)).intValue();
            System.out.println("Local Name \"" + tag + "\" occurs " 
                               + count + " times");
        }   
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		tags = new Hashtable<String, Integer>();
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		String key = name;
	    Object value = tags.get(key);
	    
	    if (value == null) {
	        tags.put(key, new Integer(1));
	    } 
	    else {
	        int count = ((Integer)value).intValue();
	        count++;
	        System.out.println("localName:"+name);
	        tags.put(key, new Integer(count));
	        if(key.equals("P")){
		    	isP = true;
		    }
	    }
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if(qName.equals("P")){
	    	isP = false;
	    	pi += 1;
	    }
	}
}