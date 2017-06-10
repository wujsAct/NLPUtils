package tacKBP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class NLPUtils {
	public static String readXML(String fileName) throws IOException {
		File xmlFile = new File(fileName); // Let's get XML file as String
												// using BufferedReader
		// FileReader uses platform's default character encoding
		// if you need to specify a different encoding, use InputStreamReader
		Reader fileReader = new FileReader(xmlFile);
		BufferedReader bufReader = new BufferedReader(fileReader);
		StringBuilder sb = new StringBuilder();
		String line = bufReader.readLine();
		while (line != null) {
			sb.append(line).append("\n");
			line = bufReader.readLine();
		}
		String xml2String = sb.toString();
		System.out.println("XML to String using BufferedReader : ");
		System.out.println(xml2String);
		return xml2String;
	}

	public static ArrayList<ArrayList<String>> getTokenPOS(
			List<CoreMap> sentences) {
		ArrayList<String> tokenArray = new ArrayList<String>();
		ArrayList<String> tokenIndexArray = new ArrayList<String>();
		ArrayList<String> posArray = new ArrayList<String>();
		ArrayList<String> neArray = new ArrayList<String>();
		Integer sentId = 0;
		for (CoreMap sentence : sentences) {
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
				tokenIndexArray.add(start + "_" + end);
				// newString += word+":"+start+" ";
				String ne = token.get(NamedEntityTagAnnotation.class);
				tokenArray.add(word);
				posArray.add(pos);
				neArray.add(ne);
			}
			newString = newString.trim();
			// System.out.println(sentId+": "+newString.length()+":"+newString);
			// System.out.println("--------------------------------");
			sentId += 1;
		}

		ArrayList<ArrayList<String>> retParams = new ArrayList<ArrayList<String>>();
		retParams.add(tokenArray);
		retParams.add(posArray);
		retParams.add(tokenIndexArray);
		return retParams;
	}
}
