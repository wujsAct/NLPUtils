package tacKBP;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom2.JDOMException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.IO;

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

	// deep learning coreference is good
	static {
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
		props.setProperty("tokenize.language", "English");
		
		props.setProperty("ssplit.tokenPatternsToDiscard", "Inc/.");
		// props.setProperty("coref.algorithm", "neural");
		// props.setProperty("coref.neural.greedyness", "0.2");
	}

	public static StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	/**
	 * TAC KBP annotation: whole document parse: only text
	 * 
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException, JDOMException {
		NLPUtils nlpUitls = new NLPUtils();
		String dataTag = "training";
		String rawPath = "data/TACKBP/2014/";
		// String rawPath = "data/kbp/LDC2017EDL/data/2014/";
		String path = rawPath + dataTag + "/";
		String docPath = path + "source_documents/";
		// String path =
		// "data/kbp/LDC2017EDL/data/2014/training/source_documents/";
		System.out.println("path: " + path);

		String rawDataName = rawPath + dataTag + "Data.txt";
		String sentid2aNosNoName = rawPath + dataTag + "_sentid2aNosNoid.txt";
		String entMen2aNosNoName = rawPath + dataTag + "_entMen2aNosNoid.txt";
		String aid2Name = rawPath + dataTag + "_aid2Name.txt";
		String wrongEntName = rawPath + dataTag + "_nonRecognizedEnts.txt";
		InputStream chunkModelIn = new FileInputStream("model/en-chunker.bin");
		ChunkerModel chunkModel = new ChunkerModel(chunkModelIn);
		ChunkerME chunker = new ChunkerME(chunkModel);

		Writer aiWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(aid2Name), false), "UTF8"));
		Writer rawDataWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(rawDataName), false), "UTF8"));
		Writer sentiWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(
						sentid2aNosNoName), false), "UTF8"));
		Writer entmfileWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(
						entMen2aNosNoName), false), "UTF8"));

		Writer nonRecogWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(wrongEntName), false), "UTF8"));

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		factory.setNamespaceAware(true);
		SAXParserHandler handler = new SAXParserHandler();
		String qXML = "";
		if(dataTag.equals("eval")){
			qXML="evaluation";
		}else{
			qXML = dataTag;
		}
		parser.parse(new InputSource(path + "tac_kbp_2014_english_EDL_"
				+ qXML + "_queries.xml"), handler);
		HashMap<String, List<String>> queryMap = handler.getQueryMap();
		HashMap<String, KBPQuery> ents = handler.getEnts();

		System.out.println("training docs number:"
				+ handler.getQueryMap().size());
		System.out.println("all ents:" + handler.getEnts().size());

		Annotation document;
		List<CoreMap> sentences;
		List<String> entidList;
		String entId;
		KBPQuery q;
		Integer start, end;
		Integer sid = 0;
		Integer sNo = 0;
		Integer aNo = 0;

		List<String> sortedKeys = new ArrayList(queryMap.keySet());
		Collections.sort(sortedKeys);

		for(String key:sortedKeys){
		//String key = "bolt-eng-DF-199-192783-6834959";{
			// String key = "bolt-eng-DF-170-181137-9034171";{

			String filename = docPath + key + ".txt";
			String text = IOUtils.slurpFile(filename);
			text = text.replace("-", " ");
			text = text.replace("<", "."); // to avoid the transfer
			text = text.replace(">", ".");
			//text = text.replace("...", "   ");
			text = text.replace("/", " ");
			text = text.replace("&quot;", "      "); // we need to revise as regex methods
			text = text.replace("&amp;", "     ");
			text = text.replace("&lt;", "    ");
			text = text.replace("&gt;", "    ");
			text = text.replace("&apos;", "      ");
			text = text.replace("ø", " ");
			text = text.replace("¤", " ");
			text = text.replace("°", " ");
			text = text.replace("º", " ");
			text = text.replace("@"," "); //wikipedia entity recognition
			//text = text.replace("", " ");
			//text = text.replace("", " ");
			// String text =
			// NLPUtils.convertXMLFileToString(docPath+key+".txt");

			document = new Annotation(text);
			pipeline.annotate(document);
			sentences = document.get(SentencesAnnotation.class);
			System.out.println(aNo + "\t" + docPath + key + ".txt"
					+ "\t sentNum:" + sentences.size());
			ArrayList<ArrayList<ArrayList<String>>> retParams = NLPUtils
					.getTokenPOS(sentences);

			ArrayList<ArrayList<String>> tokenArray = retParams.get(0);
			ArrayList<ArrayList<String>> tokenIndexArray = retParams.get(2);
			ArrayList<ArrayList<String>> posArray = retParams.get(1);
			/**
			 * process the different line
			 */
			for (Integer i = 0; i < tokenArray.size(); i++) {
				sentiWriter.write(aNo + "_" + sNo + "\n");
				sentiWriter.flush();
				sNo += 1;
				ArrayList<String> tokens = tokenArray.get(i);
				ArrayList<String> tokenIndexs = tokenIndexArray.get(i);
				ArrayList<String> poses = posArray.get(i);
				String[] chunkers = chunker.chunk(
						(String[]) tokens.toArray(new String[0]),
						(String[]) poses.toArray(new String[0]));
				for (Integer j = 0; j < tokens.size(); j++) {
					rawDataWriter.write(tokens.get(j) + "\t" + poses.get(j)
							+ "\t" + chunkers[j] + "\t" + tokenIndexs.get(j)
							+ "\n");
					rawDataWriter.flush();
				}
				rawDataWriter.write("\n");
				rawDataWriter.flush();
			}

			entidList = queryMap.get(key);
			Integer starti = 0;
			Integer endi = 0;
			Integer sent_s = 0, sent_e = 0;
			for (Integer i = 0; i < entidList.size(); i++) {
				entId = entidList.get(i);
				q = ents.get(entId);
				starti = 0;
				endi = 0;
				sent_s = 0;
				sent_e = 0;
				start = q.getBeg();
				end = q.getEnd();
				// System.out.println(start+"-"+end+"\t"+q.getName());

				for (Integer j = 0; j < tokenArray.size(); j++) {
					ArrayList<String> tokens = tokenArray.get(j);
					ArrayList<String> tokenIndexs = tokenIndexArray.get(j);
					ArrayList<String> poses = posArray.get(j);

					for (Integer k = 0; k < tokens.size(); k++) {
						String[] entIndex = tokenIndexs.get(k).split("_");
						Integer s = Integer.parseInt(entIndex[0]);
						Integer e = Integer.parseInt(entIndex[1]);
						if (s.intValue() == start.intValue()) {
							starti = k;
							sent_s = j;
						}
						if (e.intValue() == end.intValue()) {
							endi = k + 1;
							sent_e = j;
						}
					}
				}
				/**
				 * we need to record the wrong entity mentions!
				 */

				if (starti == endi || endi == 0 || sent_s != sent_e) {
					System.out.println(start + "\t" + end + "\t" + entId + "\t"
							+ q.getName());
					nonRecogWriter.write(key + "\t" + entId + "\t"
							+ q.getName() + "\t" + start + "\t" + end + "\n");
					nonRecogWriter.flush();
					System.out.println("parse entity error...\t FileName:"
							+ key);
					// System.exit(0);
				} else {
					entmfileWriter.write(String.join(" ", tokenArray
							.get(sent_s).subList(starti, endi))
							+ "\t"
							+entId
							+"\t"
							+ starti
							+ "\t"
							+ endi
							+ "\t"
							+ aNo
							+ "_"
							+ sent_s + "\n");
					entmfileWriter.flush();
					// System.out.println("mention index:"+starti+"\t"+endi+"\t"+sent_s+"\t"+sent_e+"\n");
				}
			}
			System.out.println("--------------------");
			// System.exit(0);

			aiWriter.write(aNo + "\t" + key + ".txt" + "\n");
			aiWriter.flush();
			aNo += 1;
		}
		rawDataWriter.close();
		aiWriter.close();
		sentiWriter.close();
		entmfileWriter.close();
		nonRecogWriter.close();
		System.exit(0);
		// System.out.println(SAXParserHandler.text);
		// System.out.println(SAXParserHandler.text.substring(0,7));
	}

}
class SAXParserHandler extends DefaultHandler {
	private HashMap<String, List<String>> queryMap; // 存放document: entId
	private HashMap<String, KBPQuery> ents;
	KBPQuery q; // 构建KBPQuery对象
	private String entId;
	private String tagName; // 用来存放每次遍历后的元素名称(节点名称)

	@Override
	public void startDocument() throws SAXException {
		System.out.println("start parse...");
		queryMap = new HashMap<String, List<String>>();
		ents = new HashMap<String, KBPQuery>();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (qName.equals("query")) {
			q = new KBPQuery();
			System.out.println(attributes.getValue(0));
			this.entId = attributes.getValue(0);
		}
		this.tagName = qName;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("query")) {
			this.ents.put(this.entId, q);
			if (queryMap.get(q.getDocid()) != null) {
				List<String> temp = queryMap.get(q.getDocid());
				temp.add(this.entId);
				queryMap.put(q.getDocid(), temp);
			} else {
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

	@Override
	//调用多次
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (this.tagName != null) {
			String data = new String(ch, start, length);
			if (this.tagName.equals("name")) {
				this.q.setName(data);
			} else if (this.tagName.equals("docid")) {
				this.q.setDocid(data);
			} else if (this.tagName.equals("beg")) {
				this.q.setBeg(Integer.parseInt(data));
			} else if (this.tagName.equals("end")) {
				this.q.setEnd(Integer.parseInt(data) + 1);
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
