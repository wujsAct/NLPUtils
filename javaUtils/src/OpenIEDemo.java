import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.ChunkAnnotationUtils;
import edu.stanford.nlp.pipeline.LabeledChunkIdentifier;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Interval;
import edu.stanford.nlp.util.PropertiesUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import edu.stanford.nlp.util.StringUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

public class OpenIEDemo {
	static String dirPath = "D:/Users/DELL/Workspaces/MyEclipse Professional 2014/protobuf/";
	static String ACEFileName = dirPath+"model/aceData.txt";
	static String sentid2aNosNoName = dirPath + "model/sentid2aNosNoid.txt";
	static String entMen2aNosNoName = dirPath + "model/entMen2aNosNoid.txt";
	static Integer aNo = 0; 
	public static void main(String[] args) throws Exception {
		HashMap<String,ArrayList<String[]>> docData = parseXMl(dirPath+"model/ace2004.xml");
		
		Properties props = PropertiesUtils.asProperties("annotators",
				"tokenize,ssplit,pos,lemma,depparse,natlog,openie");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		//String text ="July 1996 CDU / CSU SPD FDP Greens PDS Emind July 7 39.0 32.0 7.0 11.0 5.0";
		//annoteText(text,pipeline);
		/*String fileN = "NYT20001123.1511.0062";
		ArrayList<String[]> val = docData.get(fileN);
		ArrayList<String> sents = annoteText(fileN,pipeline);
	    getEntMent2aNosNo(sents,fileN,pipeline,val);   //not all the sentence has the entity mentions!
*/		
		Iterator<Entry<String, ArrayList<String[]>>> iter = docData.entrySet().iterator();
		try{
			while (iter.hasNext()) {
				Map.Entry<String, ArrayList<String[]>> entry = iter.next();
			    String fileN = (String) entry.getKey();
			    System.out.println(fileN);
			    ArrayList<String[]> val = ( ArrayList<String[]>) entry.getValue();
			    ArrayList<String> sents = annoteText(fileN,pipeline);
			    getEntMent2aNosNo(sents,fileN,pipeline,val);   //not all the sentence has the entity mentions!
			    aNo+=1;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			StanfordCoreNLP.clearAnnotatorPool();
		}
		StanfordCoreNLP.clearAnnotatorPool();
		System.exit(0);
	}
		
	/**
	 * 
	 * @param xmlfileName
	 * @return HashMap(fileName, mentions)
	 */
	public static HashMap<String, ArrayList<String[]>> parseXMl(String xmlfileName) {
		File f=new File(xmlfileName); 
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance(); 
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
		Document data = null;
		try {
			data = builder.parse(f);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<String> docNames = new ArrayList<String>();
		HashMap<String,ArrayList<String[]>> docData = new HashMap<String, ArrayList<String[]>>();
		NodeList docs = data.getElementsByTagName("document");
		for (int i=0; i<docs.getLength(); i++){
			Node doc = docs.item(i);
			//get doc name
			String name = doc.getAttributes().getNamedItem("docName").getNodeValue();
			docNames.add(name);
			System.out.println("****"+name+"***");
			NodeList annots = doc.getChildNodes();
			ArrayList<String[]> ments = new ArrayList<String[]>();
			for(int j=1;j<annots.getLength();j+=2){
				 Node annot = annots.item(j);
				 NodeList mentsInfos = annot.getChildNodes();
				 String[] mention = new String[4];
				 int d =0;
				 /**
				  * @author wujs time: 2016/12/29, k+=2
				  */
				 for(int k=1;k<mentsInfos.getLength();k+=2){
					 Node mentinfo = mentsInfos.item(k);
					 String ctx = mentinfo.getTextContent();
					 System.out.println("k"+":"+k+" "+ctx);
					 mention[d++] = ctx;
				 }
				 ments.add(mention);
			}
			docData.put(name, ments);
		}
		
		return docData;
    }
	
	public static void getEntMent2aNosNo(ArrayList<String> sents,String fileN,StanfordCoreNLP pipeline,ArrayList<String[]> val) throws IOException{
		String text = IOUtils.slurpFile(dirPath+"model/RawTexts/"+fileN);
		FileWriter fileWriter = new FileWriter(entMen2aNosNoName,true);
		Annotation doc = new Annotation(text);
		pipeline.annotate(doc);
		// Loop over sentences in the document
		Integer sentNo = sents.size();
		Integer i = 0;
		Integer lentrel = 0;
		for(String si : sents){
			System.out.println(si);
		}
		for(String[] vali : val){
			String gold_mention = vali[0];
			String linking_ent = vali[1];
			Integer start = Integer.parseInt(vali[2]);
			Integer end = Integer.parseInt(vali[3]) +start;
			String mention = text.substring(start, end);
			Integer mentlent = mention.split(" |'").length;
			System.out.println(mention+'\t'+mentlent.toString());
			for (i = 0; i < sents.size(); i++) {
				String senti = sents.get(i);
				/*
				 * if(senti.contains(mention)){ //利用这个函数能够处理那种一个实体出现多次的情况,
				 * if(gold_mention.equals(mention)) lentrel +=1;
				 * System.out.println
				 * (senti.indexOf(mention,0)+'\t'+gold_mention+
				 * '\t'+mention+'\t'+senti); break; }
				 */
				String[] senti_items = senti.split(" ");
				Integer si = 0;
				for (si = 0; si < senti_items.length; si++) {
					String newentsi = String.join("",
							Arrays.copyOfRange(senti_items, si, si + mentlent));
					String newment = mention.replace(" ", "");
					if (newentsi.contains(newment)) {
						String aNosNo = aNo.toString() + '_' + si.toString();
						Integer send = si + mentlent;
						lentrel += 1;
						String line = mention + '\t' + linking_ent + '\t'
								+ aNosNo + '\t' + si.toString() + '\t'
								+ send.toString();
						System.out.println(line);
						fileWriter.write(line + '\n');
						fileWriter.flush();
						break;
					}
				}
			}
			
		}
		Integer valsize= val.size();
		System.out.println("sentNo:"+sentNo.toString()+'\t'+valsize.toString());
		if(lentrel<val.size()){
			System.out.println(lentrel.toString()+"not equal"+valsize.toString());
			System.exit(0);
		}
		System.out.println("--------------------");
		fileWriter.flush();
		fileWriter.close();
	}
	public static ArrayList<String> annoteText(String fileN,StanfordCoreNLP pipeline) throws IOException{
		String text = IOUtils.slurpFile(dirPath+"model/RawTexts/"+fileN);
		System.out.println(text);
		FileWriter aceWriter = new FileWriter(ACEFileName,true);
		FileWriter sentiWriter = new FileWriter(sentid2aNosNoName,true);
		
		//String text = fileN;
		Annotation doc = new Annotation(text);
		pipeline.annotate(doc);
		// Loop over sentences in the document
		Integer sentNo = 0;
		ArrayList<String> sents = new ArrayList<String>();
		for (CoreMap sentence : doc
				.get(CoreAnnotations.SentencesAnnotation.class)) {
			sentiWriter.write(aNo.toString()+'_'+sentNo.toString()+'\n');
			sentiWriter.flush();
			List<CoreLabel> tokens = sentence
					.get(CoreAnnotations.TokensAnnotation.class);

			/*System.out.println("Sentence #" + ++sentNo + ": "
					+ sentence.get(CoreAnnotations.TextAnnotation.class));
			System.out.println(StringUtils.getNotNullString(sentence.get(CoreAnnotations.AfterAnnotation.class)));*/
			ArrayList<String> sentL = new ArrayList<String>();
			ArrayList<String> posL = new ArrayList<String>();
			Integer lastIndex=0;
			for (CoreLabel token : tokens) {
				Integer ment = token
						.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
				lastIndex = ment;
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token
						.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				sentL.add(word);
				posL.add(pos);
			}
			
			/**
			 * generate chunk using opennlp
			 * 
			 */
			InputStream chunkModelIn = new FileInputStream(dirPath
					+ "model/en-chunker.bin");
			ChunkerModel chunkModel = new ChunkerModel(chunkModelIn);
			ChunkerME chunker = new ChunkerME(chunkModel);
			int size = sentL.size();
			String[] senta = sentL.toArray(new String[size]);
			sents.add(String.join(" ", senta));
			size = posL.size();
			String[] posa = posL.toArray(new String[size]);
			String tag[] = chunker.chunk(senta, posa);
			int i = 0;
			for (String ti : tag) {
				if(!senta.equals("_"))
				{
					aceWriter.write(senta[i] + ' ' + posa[i] + ' ' + ti+'\n');
					aceWriter.flush();
				}
				//System.out.println(senta[i] + ' ' + posa[i] + ' ' + ti);
				i += 1;
			}
			aceWriter.write("\n");
			aceWriter.flush();
			chunkModelIn.close();

			// Print SemanticGraph
			StanfordCoreNLP.clearAnnotatorPool();
		}
		aceWriter.flush();
		aceWriter.close();
		sentiWriter.flush();
		sentiWriter.close();
		return sents;
	}
}
/***
 * System.out.println(sentence.get(SemanticGraphCoreAnnotations.
 * EnhancedDependenciesAnnotation
 * .class).toString(SemanticGraph.OutputFormat.LIST));
 * 
 * // Get the OpenIE triples for the sentence Collection<RelationTriple> triples
 * = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
 * 
 * // Print the triples for (RelationTriple triple : triples) {
 * System.out.println(triple.confidence + "\t" + triple.subjectLemmaGloss() +
 * "\t" + triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss()); }
 * 
 * // Alternately, to only run e.g., the clause splitter: List<SentenceFragment>
 * clauses = new OpenIE(props).clausesInSentence(sentence); for
 * (SentenceFragment clause : clauses) {
 * System.out.println(clause.parseTree.toString
 * (SemanticGraph.OutputFormat.LIST)); } System.out.println("finished");
 **/
