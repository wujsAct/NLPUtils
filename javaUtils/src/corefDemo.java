import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class corefDemo {
	
	public static String dirPath = "D:/Users/DELL/Workspaces/MyEclipse Professional 2014/protobuf/";
	public static String dataPath=dirPath+"ace/";
	public static Properties props = new Properties();
	static{
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,dcoref");
		props.setProperty("ssplit.eolonly", "true");
	}
    public static StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	public static void main(String[] args) {
		getDoc();
		System.exit(0);
	}
	public static String getDoc(){
		String fsentid2aidsNoid = dataPath+"sentid2aNosNoid.txt";
		String text = IOUtils.slurpFileNoExceptions(fsentid2aidsNoid);
		String[] ids = text.split("\n");
		System.out.println(ids.length);
		
		String fileN = dataPath+"aceData.txt";
		text = IOUtils.slurpFileNoExceptions(fileN);
		String[] sents = text.split("\n\n");
		System.out.println(sents.length);
		
		//key: aId,ArrayList<String> senti
		HashMap<String,ArrayList<String>> doc2sents = new HashMap<String, ArrayList<String>>();
		for(Integer i = 0;i<sents.length;i++){
			String[] senti = sents[i].split("\n");
			String iline = "";
			for(Integer j=0;j<senti.length;j++){
				String word = senti[j].split("\t")[0];
				iline = iline + word +"\t";
			}
			iline = iline.trim();
			
			String docId = ids[i].split("_")[0];
			if(doc2sents.containsKey(docId)){
				ArrayList<String> temp = doc2sents.get(docId);
				temp.add(iline);
				doc2sents.put(docId, temp);
			}
			else{
				ArrayList<String> temp = new ArrayList<String>();
				temp.add(iline);
				doc2sents.put(docId, temp);
			}
		}
		for(Integer i=0;i<doc2sents.size();i++){
			String docs ="";
			ArrayList<String> sentsi =  doc2sents.get(String.valueOf(i));
			for(Integer j=0; j<sentsi.size();j++){
				docs = docs + sentsi.get(j)+"\n";
			}
			getDocCoref(i, docs);
			
		}
		return null;
	}
	/**
	 * @time 2017/2/12
	 * @param text
	 * @function generate document coreference resolution using stanford corenlp2016-10-31
	 */
	public static void getDocCoref(Integer docId,String text){
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		System.out.println("docId:"+docId+" senLent:"+sentences.size()+"\t"+text.split("\n").length);
		/**
		for(CorefChain cc: document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()){
			//System.out.println("\t"+cc);
			System.out.println(cc.getRepresentativeMention());
			List<CorefMention> ments = cc.getMentionsInTextualOrder();
			for(CorefMention m:ments){
				if(m.mentionSpan.contains("Home Depot")){
					System.out.println(m.corefClusterID+"\t"+m.startIndex+"\t"+m.endIndex+"\t"+m.mentionSpan);
				}
				
			}
		}*/
	}

}
