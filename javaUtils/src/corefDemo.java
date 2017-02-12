import java.io.IOException;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class corefDemo {

	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);



		System.out.println("right");

		String fileN = "16451112";
		String dirPath = "D:/Users/DELL/Workspaces/MyEclipse Professional 2014/protobuf/";
		String dataPath = dirPath + "msnbc/";
		String text = null;
		try {
			text = IOUtils.slurpFile(dataPath + "RawTexts/" + fileN);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(text);

		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		System.out.println("---");
		System.out.println("coref chains");
		
		for(CorefChain cc: document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()){
			System.out.println("\t"+cc);
		}
		System.exit(0);
	}

}
