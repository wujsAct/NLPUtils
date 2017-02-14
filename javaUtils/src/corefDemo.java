import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class corefDemo {

	public static String dirPath = "D:/Users/DELL/Workspaces/MyEclipse Professional 2014/protobuf/";
	public static String dataPath = dirPath + "msnbc/";
	public static String fcorefret = dataPath+"corefRet.txt";
	public static Properties props = new Properties();
	static {
		props.setProperty("annotators",
				"tokenize,ssplit,pos,lemma,ner,parse,dcoref");
		props.setProperty("ssplit.eolonly", "true"); // one line is a sentence;
														// we need to utilize
														// this formation
	}
	public static StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	public static void main(String[] args) {
		try {
			getDoc();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static String getDoc() throws IOException {
		String fsentid2aidsNoid = dataPath + "sentid2aNosNoid.txt";
		String text = IOUtils.slurpFileNoExceptions(fsentid2aidsNoid);
		String[] ids = text.split("\n");
		System.out.println(ids.length);
		//append 
		Writer fcoref = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(new File(fcorefret),true), "UTF8"));
		

		String fileN = dataPath + "msnbcData.txt";
		text = IOUtils.slurpFileNoExceptions(fileN);
		String[] sents = text.split("\n\n");
		System.out.println(sents.length);

		// key: aId,ArrayList<String> senti
		HashMap<String, ArrayList<String>> doc2sents = new HashMap<String, ArrayList<String>>();
		for (Integer i = 0; i < sents.length; i++) {
			String[] senti = sents[i].split("\n");
			String iline = "";
			for (Integer j = 0; j < senti.length; j++) {
				String word = senti[j].split("\t")[0];
				iline = iline + word + "\t";
			}
			iline = iline.trim();

			String docId = ids[i].split("_")[0];
			if (doc2sents.containsKey(docId)) {
				ArrayList<String> temp = doc2sents.get(docId);
				temp.add(iline);
				doc2sents.put(docId, temp);
			} else {
				ArrayList<String> temp = new ArrayList<String>();
				temp.add(iline);
				doc2sents.put(docId, temp);
			}
		}

		for (Integer i = 0; i < doc2sents.size(); i++) {
			String docs = "";
			ArrayList<String> sentsi = doc2sents.get(String.valueOf(i));
			for (Integer j = 0; j < sentsi.size(); j++) {
				docs = docs + sentsi.get(j) + "\n";
			}
			getDocCoref(i, docs,fcoref);
		}
		return null;
	}

	/**
	 * @time 2017/2/12
	 * @param text
	 * @throws IOException 
	 * @function generate document coreference resolution using stanford
	 *           corenlp2016-10-31
	 */
	public static void getDocCoref(Integer docId, String text,Writer fcoref) throws IOException {
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		// System.out.println("docId:"+docId+" senLent:"+sentences.size()+"\t"+text.split("\n").length);
		// //one line for one sentence (ssplit)

		for (CorefChain cc : document.get(
				CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
			// System.out.println("\t"+cc);
			CorefMention repMent = cc.getRepresentativeMention();
			String repMentStr = docId + "_" + Integer.toString(repMent.sentNum-1)
					+ "\t" + Integer.toString(repMent.startIndex-1) + "\t"
					+ Integer.toString(repMent.endIndex-1) + "\t"
					+ repMent.mentionSpan + "\t" + repMent.mentionType;

			System.out.println(repMentStr);
			List<CorefMention> ments = cc.getMentionsInTextualOrder();

			for (CorefMention m : ments) {
				System.out.println(m);
				String mentStr = docId + "_"
						+ Integer.toString(m.sentNum-1) + "\t"
						+ Integer.toString(m.startIndex-1) + "\t"
						+ Integer.toString(m.endIndex-1) +"\t"
						+ m.mentionSpan;
				fcoref.flush();
				fcoref.append(repMentStr+"\t\t"+mentStr+"\n");
				fcoref.flush();
			}
		}
	}

}
