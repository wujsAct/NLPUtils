import java.io.IOException;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
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
		
		String sent = "";
		String senti = "";
		String word="";
		Integer docId=0;
		for(Integer i=0; i<sents.length;i++){
			String[] iline = sents[i].split("\n");
			Integer sentiLent = iline.length;
			for(Integer j=0;j<sentiLent;j++){
				//System.out.println("iline: "+j+" "+iline[j]);
				word = iline[j].split("\t")[0];
				senti = senti + word+" ";
			}
			senti = senti.substring(0, senti.length()-1);
			String aid = ids[i].split("_")[0];
			
			if(Integer.parseInt(aid)==docId){
				sent = sent+senti+'\n';
				senti="";
			}
			else{
				getDocCoref(sent);
				docId += 1;
				sent="";
				senti="";
			}
			
		}
		
		return null;
	}
	/**
	 * @time 2017/2/12
	 * @param text
	 * @function generate document coreference resolution using stanford corenlp2016-10-31
	 */
	public static void getDocCoref(String text){
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		
		System.out.println(sentences.size()+"\t"+text.split("\n").length);
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
