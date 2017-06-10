package tacKBP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
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
import tacKBP.NLPUtils;
import tacKBP.KBPQuery;

public class TrainTextProcess {
	public static Properties props = new Properties();
	
	//deep learning coreference is good 
	static {
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
		
		//props.setProperty("coref.algorithm", "neural");
		//props.setProperty("coref.neural.greedyness", "0.2");
	}
	
	public static StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	
	/**
	 * TAC KBP annotation: whole document
	 * parse: only text
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		NLPUtils nlpUitls = new NLPUtils();
		String dataTag ="training";
		String rawPath = "D:/Users/DELL/Workspaces/MyEclipse Professional 2014/protobuf/data/TACKBP/2014/";
		String path =rawPath +dataTag+"/";
		String docPath =path +"source_documents/";
		//String path = "data/kbp/LDC2017EDL/data/2014/training/source_documents/";
		System.out.println("path: "+path);
		
		String rawDataName = rawPath+dataTag+"Data.txt";
		String sentid2aNosNoName = rawPath +dataTag +"_sentid2aNosNoid.txt";
		String entMen2aNosNoName =rawPath+dataTag+"_entMen2aNosNoid.txt";
		String aid2Name = rawPath +dataTag+"_aid2Name.txt";
		
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();  
        factory.setNamespaceAware(true);
        SAXParserHandler handler = new SAXParserHandler();
        parser.parse(new InputSource(path+"tac_kbp_2014_english_EDL_"+dataTag+"_queries.xml"), handler);
        HashMap<String,List<String>> queryMap =handler.getQueryMap();
    	HashMap<String,KBPQuery> ents = handler.getEnts();
    	
    	System.out.println("training docs number:"+handler.getQueryMap().size());
        System.out.println("all ents:"+handler.getEnts().size());
    	
        Annotation document;List<CoreMap> sentences; List<String> entidList;
        ArrayList<ArrayList<String>> retParams; 
        String entId; KBPQuery q; Integer start,end;
        Integer sid = 0; Integer sNo = 0; Integer aNo = 0;
        for(String key:queryMap.keySet()){
        //String key = "bolt-eng-DF-170-181122-8794909";{
        	System.out.println(docPath+key+".txt");
	        String text = IOUtils.slurpFile(docPath+key+".txt");
	        text = text.replace("<", "-");   //to avoid the transfer
	        text = text.replace(">", "-");
        	//String text= NLPUtils.readXML(docPath+key+".txt");
			document = new Annotation(text);
			pipeline.annotate(document);
			sentences = document.get(SentencesAnnotation.class);
			System.out.println(sentences.size());
			retParams = nlpUitls.getTokenPOS(sentences);
			ArrayList<String> tokenArray = retParams.get(0);
			ArrayList<String> tokenIndexArray = retParams.get(2);
			ArrayList<String> posArray = retParams.get(1);
			/**
			 * get entity mentions
			 */
			for(Integer j=0;j<tokenArray.size();j++){
				String[] entIndex = tokenIndexArray.get(j).split("_");
				String s = entIndex[0]; String e = entIndex[1];
				System.out.println(Integer.parseInt(s)+"\t"+Integer.parseInt(e)+"\t"+tokenArray.get(j));
			}
			
			entidList = queryMap.get(key);
			Integer si=0;Integer ei=0;
			for(Integer i=0;i<entidList.size();i++){
				entId = entidList.get(i);
				q = ents.get(entId);
				si=0;ei=0;
				start = q.getBeg(); end = q.getEnd();
				System.out.println(start+"-"+end+"\t"+q.getName());
				for(Integer j=0;j<tokenArray.size();j++){
					String[] entIndex = tokenIndexArray.get(j).split("_");
					Integer s = Integer.parseInt(entIndex[0]); Integer e = Integer.parseInt(entIndex[1]);
					if(s.intValue() == start.intValue()){
						si = j;
					}
					if(e.intValue() == end.intValue()){
						ei = j+1;
					}
				}
				if(si ==ei){
					System.out.println("parse entity error...\t FileName:"+key);
					System.exit(0);
				}
				System.out.println("mention index:"+si+"\t"+ei);
			}
			System.out.println("--------------------");
			System.exit(0);
        }
        System.exit(0);
        //System.out.println(SAXParserHandler.text);
        //System.out.println(SAXParserHandler.text.substring(0,7));
	}
	
	
}

class SAXParserHandler extends DefaultHandler{
	private HashMap<String,List<String>> queryMap; //存放document: entId
	private HashMap<String,KBPQuery> ents;
	KBPQuery q; //构建KBPQuery对象
	private String entId;
    private String tagName; //用来存放每次遍历后的元素名称(节点名称)  
    
	@Override
	public void startDocument() throws SAXException {
		System.out.println("start parse...");
		queryMap = new HashMap<String, List<String>>();
		ents = new HashMap<String, KBPQuery>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(qName.equals("query")){
			q = new KBPQuery();
			System.out.println(attributes.getValue(0));
			this.entId = attributes.getValue(0);
		}
		this.tagName = qName;
	}
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(qName.equals("query")){
			this.ents.put(this.entId, q);
			if(queryMap.get(q.getDocid())!=null){
				List<String> temp = queryMap.get(q.getDocid());
				temp.add(this.entId);
				queryMap.put(q.getDocid(), temp);
			}else{
				List<String> temp = new ArrayList<String>();
				temp.add(this.entId);
				queryMap.put(q.getDocid(), temp);
			}
		}
	}
	@Override
	public void endDocument() throws SAXException {
		System.out.println("parse end!");
	}
	@Override  //调用多次
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(this.tagName!=null){
			String data = new String(ch,start,length);
			if(this.tagName.equals("name")){
				this.q.setName(data);
			}
			else if(this.tagName.equals("docid")){
				this.q.setDocid(data);
			}
			else if(this.tagName.equals("beg")){
				this.q.setBeg(Integer.parseInt(data));
			}
			else if(this.tagName.equals("end")){
				this.q.setEnd(Integer.parseInt(data)+1);
			}
		}
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public HashMap<String, List<String>> getQueryMap() {
		return queryMap;
	}

	public HashMap<String, KBPQuery> getEnts() {
		return ents;
	}
	
}

