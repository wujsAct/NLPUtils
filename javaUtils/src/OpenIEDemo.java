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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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
	static String dirPaht = "D:/Users/DELL/Workspaces/MyEclipse Professional 2014/protobuf/model/";
	
	public static void main(String[] args) throws Exception {
		
		parseXMl(dirPaht+"ace2004.xml");
		
		Properties props = PropertiesUtils.asProperties("annotators",
				"tokenize,ssplit,pos,lemma,depparse,natlog,openie");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		// Annotate an example document.
		String text;
		// String fileN = "C:/Users/DELL/Desktop/chtb_227.eng";
		// text = IOUtils.slurpFile(fileN);
		String text1 = "West Indian all-rounder Phil Simmons took four for 38 on Friday";
		Annotation doc = new Annotation(text1);
		pipeline.annotate(doc);
		// Loop over sentences in the document
		int sentNo = 0;
		for (CoreMap sentence : doc
				.get(CoreAnnotations.SentencesAnnotation.class)) {
			List<CoreLabel> tokens = sentence
					.get(CoreAnnotations.TokensAnnotation.class);

			System.out.println("Sentence #" + ++sentNo + ": "
					+ sentence.get(CoreAnnotations.TextAnnotation.class));

			ArrayList<String> sentL = new ArrayList<String>();
			ArrayList<String> posL = new ArrayList<String>();
			for (CoreLabel token : tokens) {
				Integer ment = token
						.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
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
			
			InputStream chunkModelIn = new FileInputStream(dirPaht
					+ "en-chunker.bin");
			ChunkerModel chunkModel = new ChunkerModel(chunkModelIn);
			ChunkerME chunker = new ChunkerME(chunkModel);
			int size = sentL.size();
			String[] senta = (String[]) sentL.toArray(new String[size]);
			size = posL.size();
			String[] posa = (String[]) posL.toArray(new String[size]);
			String tag[] = chunker.chunk(senta, posa);
			int i = 0;
			for (String ti : tag) {
				System.out.println(senta[i] + ' ' + posa[i] + ' ' + ti);
				i += 1;
			}

			chunkModelIn.close();

			System.out.println(sentence.toString());
			// Print SemanticGraph
			StanfordCoreNLP.clearAnnotatorPool();
		}
		
	}
	/**
	 * 
	 * @param xmlfileName
	 * @return HashMap(fileName, mentions)
	 */
	public static HashMap<String, ArrayList> parseXMl(String xmlfileName) {
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
		HashMap<String,ArrayList> docData = new HashMap<String, ArrayList>();
		NodeList docs = data.getElementsByTagName("document");
		for (int i=0; i<docs.getLength(); i++){
			Node doc = docs.item(i);
			//»ñÈ¡docµÄname
			String name = doc.getAttributes().getNamedItem("docName").getNodeValue();
			docNames.add(name);
			System.out.println("****"+name+"***");
			NodeList annots = doc.getChildNodes();
			ArrayList ments = new ArrayList();
			for(int j=0;j<annots.getLength();j++){
				 Node annot = annots.item(j);
				 NodeList mentsInfos = annot.getChildNodes();
				 String[] mention = new String[4];
				 for(int k=0;k<mentsInfos.getLength();k++){
					 Node mentinfo = mentsInfos.item(k);
					 mention[k] = mentinfo.getTextContent();
				 }
				 ments.add(mention);
			}
			docData.put(name, ments);
		}
		return docData;
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
