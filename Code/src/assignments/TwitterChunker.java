package assignments;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.Sequence;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by User on 3/31/2016.
 */
public class TwitterChunker {
    private static final String TWITTER_CHUNKER_MODEL = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/models/twitter-en-chunker.bin";
    private static final String OPENNLP_CHUNKER_MODEL = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/models/opennlp_models/en-chunker.bin";

    public static void main(String args[]) {
        InputStream modelIn = null;
        ChunkerModel model = null;

        try {
            modelIn = new FileInputStream(TWITTER_CHUNKER_MODEL);
            model = new ChunkerModel(modelIn);

            /**testing model**/
            ChunkerME chunker = new ChunkerME(model);

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
            double probs[] = chunker.probs();
            for(int i = 0; i < tag.length; i++) {
                System.out.println(sent[i] + " " + pos[i] + " " + tag[i] + " " + probs[i]);
            }
            System.out.println();

            Sequence topSequences[] = chunker.topKSequences(sent, pos);

            for(int i = 0; i < topSequences.length; i++) {
                System.out.println(topSequences[i].toString());
            }
            System.out.println();
        } catch (IOException e) {
            // Model loading failed, handle the error
            e.printStackTrace();
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static ChunkerME getChunkerModel() {
        InputStream modelIn = null;
        ChunkerModel model = null;

        try {
            modelIn = new FileInputStream(TWITTER_CHUNKER_MODEL);
            model = new ChunkerModel(modelIn);

            ChunkerME chunker = new ChunkerME(model);

            return chunker;
        } catch (IOException e) {
            // Model loading failed, handle the error
            e.printStackTrace();
            return null;
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
