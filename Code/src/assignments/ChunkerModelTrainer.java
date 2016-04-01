package assignments;

import opennlp.tools.chunker.*;

import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by User on 3/30/2016.
 */
public class ChunkerModelTrainer {
    public static void main(String[] args) throws IOException {
        String fileName = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/test.txt";
        String openNLPTrain = "en-chunker.train";
        Charset charset = Charset.forName("UTF-8");
        String modelFile = "en-chunker.bin";
        ObjectStream<String> lineStream =
                new PlainTextByLineStream(new FileInputStream(fileName),charset);
        ObjectStream<ChunkSample> sampleStream = new ChunkSampleStream(lineStream);

        ChunkerModel model = null;

        try {
            model = ChunkerME.train("en", sampleStream,
                    new DefaultChunkerContextGenerator(), TrainingParameters.defaultParams());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sampleStream.close();
        }

        OutputStream modelOut = null;
        try {
            modelOut = new BufferedOutputStream(new FileOutputStream(modelFile));
            model.serialize(modelOut);
        } finally {
            if (modelOut != null)
                modelOut.close();
        }
    }
}
