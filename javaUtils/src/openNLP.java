import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;


public class openNLP {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
    String dirPaht = "D:/Users/DELL/Workspaces/MyEclipse Professional 2014/protobuf/model/";	
	InputStream chunkModelIn = new FileInputStream(dirPaht+"en-chunker.bin");
	ChunkerModel chunkModel = new ChunkerModel(chunkModelIn);
    ChunkerME chunker = new ChunkerME(chunkModel);
    String sent[] = new String[] { "Rockwell", "International", "Corp.", "'s",
				    "Tulsa", "unit", "said", "it", "signed", "a", "tentative", "agreement",
				    "extending", "its", "contract", "with", "Boeing", "Co.", "to",
				    "provide", "structural", "parts", "for", "Boeing", "'s", "747",
				    "jetliners", "." };

	String pos[] = new String[] { "NNP", "NNP", "NNP", "POS", "NNP", "NN",
				    "VBD", "PRP", "VBD", "DT", "JJ", "NN", "VBG", "PRP$", "NN", "IN",
				    "NNP", "NNP", "TO", "VB", "JJ", "NNS", "IN", "NNP", "POS", "CD", "NNS",
				    "." };

    String tag[] = chunker.chunk(sent, pos);
	  int i=0;
	  for(String ti: tag){
		System.out.println(sent[i++]+'\t'+ti);	
	  }
						
	  chunkModelIn.close();
					
	}

}
