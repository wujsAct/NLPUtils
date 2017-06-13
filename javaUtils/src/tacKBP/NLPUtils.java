package tacKBP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;

import sun.nio.ch.IOUtil;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class NLPUtils {
	
	
	public static String convertXMLFileToString(String fileName) throws FileNotFoundException, JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new FileInputStream(new File( fileName)));

        Format format = Format.getCompactFormat();
        format.setEncoding("UTF-8");// 设置xml文件的字符为UTF-8，解决中文问题
        XMLOutputter xmlout = new XMLOutputter();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        xmlout.output(document, bo);
        return bo.toString().trim();
		
		
    }
	public static ArrayList<ArrayList<ArrayList<String>>> getTokenPOS(
			List<CoreMap> sentences) {
		ArrayList<ArrayList<String>> tokenArray= new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> tokenIndexArray= new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> posArray= new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> neArray= new ArrayList<ArrayList<String>>();
		ArrayList<String> tokens;
		ArrayList<String> tokenIndexs;
		ArrayList<String> poses ;
		ArrayList<String> nes ;
		
		Integer sentId = 0;
		for (CoreMap sentence : sentences) {
			tokens = new ArrayList<String>();
			tokenIndexs = new ArrayList<String>();
			poses = new ArrayList<String>();
			nes= new ArrayList<String>();
			String newString = "";
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);

				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				Integer start = token
						.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
				Integer end = token
						.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
				tokenIndexs.add(start + "_" + end);
				// newString += word+":"+start+" ";
				String ne = token.get(NamedEntityTagAnnotation.class);
				//System.out.println(word+"\t"+start+"\t"+end);
				tokens.add(word);
				poses.add(pos);
				nes.add(ne);
			}
			tokenArray.add(tokens); tokenIndexArray.add(tokenIndexs);posArray.add(poses);
			newString = newString.trim();
			//System.out.println(sentId+": "+newString.length()+":"+newString);
			//System.out.println("--------------------------------");
			sentId += 1;
		}

		ArrayList<ArrayList<ArrayList<String>>> retParams = new ArrayList<ArrayList<ArrayList<String>>>();
		retParams.add(tokenArray);
		retParams.add(posArray);
		retParams.add(tokenIndexArray);
		return retParams;
	}
}
