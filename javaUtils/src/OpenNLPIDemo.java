import edu.stanford.nlp.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import opennlp.uima.tokenize.Tokenizer;

public class OpenNLPIDemo {
	static String dirPath = "D:/Users/DELL/Workspaces/MyEclipse Professional 2014/protobuf/";
	static String dataPath=dirPath+"ace/";
	static String ACEFileName = dataPath+"aceData.txt";
	static String sentid2aNosNoName = dataPath + "sentid2aNosNoid.txt";
	static String entMen2aNosNoName =dataPath+"entMen2aNosNoid.txt";
	static String aid2Name = dataPath + "aid2Name.txt";
	static Integer aNo = 0; 
	static Integer allMentions = 0;
	static Integer maxsent = 0;
	static String sentmax = "";
	
	
	public static void main(String[] args) throws Exception {
		HashMap<String,ArrayList<String[]>> docData = parseXMl(dataPath+"ace2004.xml");
		
		InputStream chunkModelIn = new FileInputStream(dirPath+ "model/en-chunker.bin");
		ChunkerModel chunkModel = new ChunkerModel(chunkModelIn);
		ChunkerME chunker = new ChunkerME(chunkModel);
		
		
		InputStream sentModelIn = new FileInputStream(dirPath+"model/en-sent.bin");
		SentenceModel sentModel = new SentenceModel(sentModelIn);
		SentenceDetectorME sentDetector = new SentenceDetectorME(sentModel);
		
		InputStream tokenModelIn = new FileInputStream(dirPath+"model/en-token.bin");
		TokenizerModel model = new TokenizerModel(tokenModelIn);
		TokenizerME tokenizer = new TokenizerME(model);
		
		
		InputStream posModelIn = new FileInputStream(dirPath+"model/en-pos-perceptron.bin");
		POSModel posModel = new POSModel(posModelIn);
		POSTaggerME tagger = new POSTaggerME(posModel);
		
		
	    
		//String fileN = "20001115_AFP_ARB.0061.eng";
		
		//String fileN="chtb_267.eng";
		//String fileN="chtb_165.eng";
		/**
		//String fileN="20001115_AFP_ARB.0212.eng";
		String text = IOUtils.slurpFile(dirPath + "model/RawTexts/" + fileN);
		System.out.println(text);
		FileWriter entmfileWriter = new FileWriter(entMen2aNosNoName, true);
		for(int i=0;i<text.length();i++){
			entmfileWriter.write(text.substring(i,i+1)+"\n");
			entmfileWriter.flush();
		}
		entmfileWriter.close();
		ArrayList<String[]> val = docData.get(fileN);*/
		//annoteText(fileN,sentDetector,chunker,tokenizer,tagger,val);
		//System.out.println(allMentions);
		
		
		//ArrayList<String> sents = annoteText(fileN,pipeline);
	    //getEntMent2aNosNo(sents,fileN,pipeline,val);   //not all the sentence has the entity mentions!
		
		
		Integer allentMents = 0;
		Iterator<Entry<String, ArrayList<String[]>>> iter = docData.entrySet().iterator();
		try{
			while (iter.hasNext()) {
				Map.Entry<String, ArrayList<String[]>> entry = iter.next();
			    String fileN = (String) entry.getKey();
			    System.out.println(fileN);
			    ArrayList<String[]> val = ( ArrayList<String[]>) entry.getValue();
			    annoteText(fileN,sentDetector,chunker,tokenizer,tagger,val);
			    allentMents+=val.size();
			    aNo += 1;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("all entity mention numbers:"+allentMents);
		System.out.println(allMentions);
		System.out.println(maxsent);
		System.out.println(sentmax);
		
		String[] maxsentItems = sentDetector.sentDetect(sentmax);
		for(String str: maxsentItems){
			System.out.println(str);
		}
		chunkModelIn.close();  //do not forget close the inputstream
		sentModelIn.close();
		tokenModelIn.close();
		posModelIn.close();
		
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
	
	public static int getPreSpace(String str){
		int i=0;
		while (str.charAt(i)==' ')
		{
			i += 1;
		}
		return i;
	}

	public static void annoteText(String fileN,
			SentenceDetectorME sentDetector, ChunkerME chunker,
			TokenizerME tokenizer, POSTaggerME tagger, ArrayList<String[]> val)
			throws IOException {
		String text = IOUtils.slurpFile(dataPath + "RawTexts/" + fileN);
		System.out.println(text);
		/**
		FileWriter entmfileWriter = new FileWriter(entMen2aNosNoName, true);
		FileWriter aceWriter = new FileWriter(ACEFileName, true);
		FileWriter sentiWriter = new FileWriter(sentid2aNosNoName, true);
		FileWriter aiWriter = new FileWriter(aid2Name, true);
		**/
		
		Writer aiWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(aid2Name),true), "UTF8"));
		Writer aceWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(ACEFileName),true), "UTF8"));
		Writer sentiWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(sentid2aNosNoName),true), "UTF8"));
		Writer entmfileWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(entMen2aNosNoName),true), "UTF8"));
		aiWriter.append(fileN + '\n');
		aiWriter.flush();
		aiWriter.close();
		String[] sents = text.split("   ");
		int sentid = -1;
		
		for (int i = 0; i < sents.length; i++) {
			String stri = sents[i];
			String[] strSents;
			if(stri.contains("Minneapolis-St. Paul")){
				strSents = new String[1];
				strSents[0] = stri;
			}
			else
			{
			strSents = sentDetector.sentDetect(stri);
			}
			/**
			 * str 最前面有空格啦！
			 */
			// String[] strSents = str.split("\\.");
			for (int k = 0; k < strSents.length; k++) {
				sentid += 1;
				String str = strSents[k];
				ArrayList<String> sentL = new ArrayList<String>();
				
				Span[] tokenSpans = tokenizer.tokenizePos(str);
				for (Span tokenSpan : tokenSpans) {
					int s = tokenSpan.getStart();
					int e = tokenSpan.getEnd();
					String word = str.substring(s, e);
					if (!word.equals("_")) {
						sentL.add(word); // 影响tag的效果啦！
					}
				}
				if (sentL.size() >= maxsent) {
					maxsent = sentL.size();
					sentmax = str;
				}

				String[] sentArray = sentL.toArray(new String[sentL.size()]);
				String[] tagArray = tagger.tag(sentArray);
				String[] chunkerArray = chunker.chunk(sentArray, tagArray);

				for (int j = 0; j < sentArray.length; j++) {
					String line = sentArray[j] + "\t" + tagArray[j] + "\t"
							+ chunkerArray[j] + "\n";
					aceWriter.write(line);
					aceWriter.flush();
				}
				aceWriter.write("\n");
				aceWriter.flush();
				sentiWriter.write(aNo + "_" + sentid + "\n");
				sentiWriter.flush();
			}
		}
		// 这样的话，咱们mention完全就对的上了呢
		int thisents = 0;
		System.out.println("start-------------" + fileN + " ------------");
		for (int j = 0; j < val.size(); j++) {
			Integer ment_s = Integer.MAX_VALUE, ment_e = Integer.MIN_VALUE; // 标记在句子中的位置啦！
			String mentInSent = "";
			Boolean flag = false;
			String[] vali = val.get(j);
			String gold_mention = vali[0];
			String linking_ent = vali[1];
			Integer start = Integer.parseInt(vali[2]);
			Integer end = Integer.parseInt(vali[3]) + start;
			String mention = text.substring(start, end);
			System.out.println("enti:" + start.toString() + '\t'
					+ end.toString() + '\t' + mention);
			int s = 0, e = 0;
			int temp = 0;
			Integer sid = 0;
			Integer sentNo = -1;
			for (int i = 0; i < sents.length; i++) {
				String str = sents[i];
				//System.out.println("split senti:" + i + "\t" + str);
				s = temp + 3 * i;
				// 这个规则不适用了啊！
				// e = s + str.length();
				/**
				 * str可能还需要进一步split
				 */
				String [] strSents;
				if(str.contains("Minneapolis-St. Paul")){
					strSents = new String[1];
					strSents[0] = str;
				}
				else
				{
				strSents = sentDetector.sentDetect(str);
				}
				/**
				 * str 最前面有空格啦！
				 */
				int prefix = getPreSpace(str);
				s += prefix;
				// String[] strSents = str.split("\\.");
				for (int k = 0; k < strSents.length; k++) {
					sentNo += 1;
					s = s + 1 * (k == 0 ? 0 : 1);
					e = s + strSents[k].length();
					//System.out.println("strSents:" + k + "\ts:" + s + '\t'
					//		+ strSents[k]);
					// System.out.println("s:"+s+"\te"+e);
					if (start >= s && start <= e && end >= s && end <= e) {
						String ment = strSents[k].substring(start - s, end - s);
						// System.out.println(mention+'\t'+ment);
						Span[] tokenSpans = tokenizer.tokenizePos(strSents[k]);
						int tokeni = 0;
						for (Span tokenSpan : tokenSpans) {
							int s1 = tokenSpan.getStart();
							int e1 = tokenSpan.getEnd();
							String subs = strSents[k].substring(s1, e1);
							
							if (subs.contains("Calif")) {
								System.out.println("subs:" + subs);
							}
							
							// int s2 = s1 + i * 3 + temp;
							// int e2 = e1 + i * 3 + temp;
							int s2 = s1 + s;
							int e2 = e1 + s;
							System.out.println("subs:"+subs+" s2: "+s2+" e2: "+e2);
							//System.out.println("subs:" + subs+" text:"+text.substring(s2, e2));
							if (mention.contains(subs) && e2 <= end && s2 >=start
							) { // 最后的一个限制吧！
								if (tokeni <= ment_s) {
									ment_s = tokeni;
								}
								if (tokeni > ment_e) {
									ment_e = tokeni;
								}
								mentInSent = mentInSent + subs + " ";
								flag = true;
								System.out.println("right: " + s1 + '\t' + e1
										+ "\t" + strSents[k].substring(s1, e1)
										+ '\t' + text.substring(s2, e2));
								sid = sentNo;
							}else if(subs.contains(mention) && s2 <= start && e2>=end) { // 最后的一个限制吧！
								flag = true;
								System.out.println("right: " + s1 + '\t' + e1
										+ "\t" +strSents[k].substring(s1, e1) + '\t'
										+ text.substring(s2, e2));
								if (tokeni <= ment_s) {
									ment_s = tokeni;
								}
								if (tokeni > ment_e) {
									ment_e = tokeni;
								}
								mentInSent = mentInSent + subs + " ";
								sid = sentNo;
							}
							if (!subs.equals("_")) {
								tokeni += 1;
							}
						}
					}
					s += strSents[k].length();
				}
				temp += str.length();
			}
			if (flag) {
				allMentions += 1;
				thisents += 1;
			} else {
				System.out.println(fileN);
				System.out.println("mention:"+mention);
				System.out.println("not inlucded in:" + mention);
				System.exit(-1);
			}
			ment_e += 1;
			String aNosNo = aNo.toString() + '_' + sid.toString();
			if(ment_e-ment_s > mention.split(" ").length+2)
			{
				ment_e = ment_s + mention.split(" ").length;
			}
			String line = mention + '\t' + linking_ent + '\t' + aNosNo + '\t'
					+ ment_s.toString() + '\t' + ment_e.toString();
			
			System.out.println(line);
			entmfileWriter.write(line + "\n");
			entmfileWriter.flush();
		}
		System.out.println("end------" + fileN + " ------------");
		if (thisents != val.size()) {
			System.out.println(fileN + '\t' + thisents + '\t' + val.size());
			System.out.println(aNo);
			System.exit(-1);
		}
		/*
		 * generate words,pos, chunker
		 */

		// System.out.println(text);
		aceWriter.close();
		sentiWriter.close();
		entmfileWriter.close();
	}
}