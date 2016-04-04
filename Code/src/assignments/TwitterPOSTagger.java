package assignments;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.InvalidFormatException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by User on 4/1/2016.
 */
public class TwitterPOSTagger {
    private static final String TWITTER_MODEL_MAXENT = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/models/twitter-en-pos-maxent.bin";
    private static final String OPENNLP_MODEL_MAXENT = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/models/opennlp_models/en-pos-maxent.bin";
    public static void main(String args[]) {

    }

    public static POSTaggerME getPOSTagger() {
        InputStream modelIn = null;

        try {
            modelIn = new FileInputStream(TWITTER_MODEL_MAXENT);
            POSModel model = new POSModel(modelIn);

            POSTaggerME tagger = new POSTaggerME(model);
            return tagger;
        }
        catch (IOException e) {
            // Model loading failed, handle the error
            e.printStackTrace();
            return null;
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                }
            }
        }

    }



}
