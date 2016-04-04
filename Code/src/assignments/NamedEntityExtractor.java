package assignments;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPInputStream;

import opennlp.maxent.GIS;
import opennlp.maxent.io.GISModelReader;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;
import opennlp.model.*;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.postag.POSTaggerME;
import util.Pair;
import util.State;
import util.Trellis;
import util.WordFeature;

/**
 * Created by User on 3/22/2016.
 */
public class NamedEntityExtractor {
    private static List<ArrayList<WordFeature>> trainingSet = new ArrayList<>();
    private static List<ArrayList<WordFeature>> testSet = new ArrayList<>();
    private static final String TRAIN_FILE_NAME = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/data-raw/train.txt";
    private static final String TEST_FILE_NAME = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/data-raw/dev1.txt";
    private static MaxentModel maxentModel;
    static final String START_TAG = "<S>";
    static final String START_WORD = "<S>";
    static final String STOP_TAG = "</S>";
    static final String STOP_WORD = "</S>";

    /**Helpers**/
    //Durstenfeld shuffle
    private static void shuffleArray(int[] ar)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private static List<String> stripBoundaryTags(List<String> tags) {
        try {
            return tags.subList(2, tags.size() - 2);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }

    /**End Helpers**/
    public static void splitData() {
        try (BufferedReader br = new BufferedReader(new FileReader(TRAIN_FILE_NAME))) {
            String line = br.readLine();
            List<WordFeature> trainTweet = new ArrayList<>();
            trainTweet.add(new WordFeature(START_WORD, START_TAG, START_TAG, START_TAG));
            while (line != null) {
                if (line.length() < 1) {
                    //System.out.println("newline: " + line);
                    trainTweet.add(new WordFeature(STOP_WORD, STOP_TAG, STOP_TAG, STOP_TAG));
                    trainingSet.add(new ArrayList<>(trainTweet));
                    trainTweet = new ArrayList<>();
                    trainTweet.add(new WordFeature(START_WORD, START_TAG, START_TAG, START_TAG));
                } else {
                    //System.out.println(line);
                    String[] wordPair = line.split("\\s+");
                    WordFeature feature = new WordFeature(wordPair[0], null, null, wordPair[1]);

                    trainTweet.add(feature);
                }
                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(TEST_FILE_NAME))) {
            String line = br.readLine();
            List<WordFeature> testTweet = new ArrayList<>();
            testTweet.add(new WordFeature(START_WORD, START_TAG, START_TAG, START_TAG));
            while (line != null) {
                if (line.length() < 1) {
                    //System.out.println("newline: " + line);
                    testTweet.add(new WordFeature(STOP_WORD, STOP_TAG, STOP_TAG, STOP_TAG));
                    testSet.add(new ArrayList<>(testTweet));
                    testTweet = new ArrayList<>();
                    testTweet.add(new WordFeature(START_WORD, START_TAG, START_TAG, START_TAG));
                } else {
                    //System.out.println(line);
                    String[] wordPair = line.split("\\s+");
                    WordFeature feature = new WordFeature(wordPair[0], null, null, wordPair[1]);

                    testTweet.add(feature);
                }
                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void splitDataTrainOnly() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(TRAIN_FILE_NAME))) {
            String line = br.readLine();
            List<ArrayList<WordFeature>> tweets = new ArrayList<>();
            List<WordFeature> tweet = new ArrayList<>();
            tweet.add(new WordFeature(START_WORD, START_TAG, START_TAG, START_TAG));
            while (line != null) {
                if (line.length() < 1) {
                    //System.out.println("newline: " + line);
                    tweet.add(new WordFeature(STOP_WORD, STOP_TAG, STOP_TAG, STOP_TAG));
                    tweets.add(new ArrayList<>(tweet));
                    tweet = new ArrayList<>();
                    tweet.add(new WordFeature(START_WORD, START_TAG, START_TAG, START_TAG));
                } else {
                    //System.out.println(line);
                    String[] wordPair = line.split("\\s+");
                    WordFeature feature = new WordFeature(wordPair[0], null, null, wordPair[1]);

                    tweet.add(feature);
                }
                line = br.readLine();
            }

            int[] indexes = new int[tweets.size()];
            int n = 0;
            while(n < indexes.length) indexes[n] = n++;

            shuffleArray(indexes);
            int trainSize = (int)(tweets.size() * .8);
            int testSize = tweets.size() - trainSize;
            int[] trainArray = new int[trainSize];
            int[] testArray = new int[testSize];

            System.arraycopy(indexes, 0, trainArray, 0, trainSize);
            System.arraycopy(indexes, trainSize, testArray, 0, testSize);

            for(int i = 0; i < trainArray.length; i++) {
                trainingSet.add(tweets.get(trainArray[i]));
            }

            for(int i = 0; i < testArray.length; i++) {
                testSet.add(tweets.get(testArray[i]));
            }

            System.out.println(trainingSet.size());
            System.out.println(testSet.size());

            /*for(int i = 0; i < trainingSet.size(); i++) {
                ArrayList<String[]> sentence = trainingSet.get(i);
                for(int j = 0; j < sentence.size(); j++) {
                    System.out.println("Word: " + sentence.get(j)[0] + ", Tag: " + sentence.get(j)[1]);
                }
                System.out.println();
            }

            for(int i = 0; i < testSet.size(); i++) {
                ArrayList<String[]> sentence = testSet.get(i);
                for(int j = 0; j < sentence.size(); j++) {
                    System.out.println("Word: " + sentence.get(j)[0] + ", Tag: " + sentence.get(j)[1]);
                }
                System.out.println();
            }*/

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void posTagData() {

        POSTaggerME tagger = TwitterPOSTagger.getPOSTagger();

        for(ArrayList<WordFeature> sentence : trainingSet) {
            String[] sent = new String[sentence.size()-2];
            for(int i = 1; i < sentence.size() - 1; i++) {
                sent[i-1] = sentence.get(i).getWord();
            }

            String tags[] = tagger.tag(sent);

            for(int i = 0; i < tags.length; i++) {
                sentence.get(i+1).setPosTag(tags[i]);
            }
        }

        for(ArrayList<WordFeature> sentence : testSet) {
            String[] sent = new String[sentence.size()-2];
            for(int i = 1; i < sentence.size() - 1; i++) {
                sent[i-1] = sentence.get(i).getWord();
            }

            String tags[] = tagger.tag(sent);

            for(int i = 0; i < tags.length; i++) {
                sentence.get(i+1).setPosTag(tags[i]);
            }
        }
    }

    public static void chunkTagData() {
        ChunkerME chunker = TwitterChunker.getChunkerModel();
        for(ArrayList<WordFeature> sentence : trainingSet) {
            String[] sent = new String[sentence.size()-2];
            String[] pos = new String[sentence.size()-2];

            for(int i = 1; i < sentence.size() - 1; i++) {
                sent[i-1] = sentence.get(i).getWord();
                pos[i-1] = sentence.get(i).getPosTag();
            }

            String tags[] = chunker.chunk(sent, pos);

            for(int i = 0; i < tags.length; i++) {
                sentence.get(i+1).setChunkTag(tags[i]);
            }

        }

        for(ArrayList<WordFeature> sentence : testSet) {
            String[] sent = new String[sentence.size()-2];
            String[] pos = new String[sentence.size()-2];

            for(int i = 1; i < sentence.size() - 1; i++) {
                sent[i-1] = sentence.get(i).getWord();
                pos[i-1] = sentence.get(i).getPosTag();
            }

            String tags[] = chunker.chunk(sent, pos);

            for(int i = 0; i < tags.length; i++) {
                sentence.get(i+1).setChunkTag(tags[i]);
            }

        }

    }

    public static Boolean createMaxEntModel() {
        BufferedWriter bw = null;
        try{

            File file = new File("C:/Users/User/Documents/Cornell/Courses/NLP/HW4/models/twitter-maxent-data.txt");

            /* This logic will make sure that the file
            * gets created if it is not present at the
            * specified location*/
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for(ArrayList<WordFeature> sentence : trainingSet) {
                for(int i = 0; i < sentence.size(); i++) {
                    StringBuilder line = new StringBuilder();
                    line.append(sentence.get(i).getNeTag() + " ");
                    List<Pair> features = TwitterFeatureExtractor.extractFeatures(i, sentence);
                    int ind = 0;
                    for(Pair pair : features) {
                        line.append(pair.getFirst() + "=" + pair.getSecond());
                        if(ind != features.size()-1) {
                            line.append(" ");
                        }

                        /*if(pair.getSecond() instanceof Boolean) {

                        }*/

                    }

                    bw.write(line.toString() + "\n");
                }
            }
            System.out.println("File written Successfully");
            return true;

        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
        finally
        {
            try{
                if(bw!=null)
                    bw.close();
            }catch(Exception ex){
                System.out.println("Error in closing the BufferedWriter"+ex);
            }
        }
    }

    public static void createMaxEnt() throws IOException {
         // here are the input training samples

        String trainingFileName = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/models/twitter-maxent-data.txt";
        String modelFileName = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/models/trained-twitter-model.maxent.gz";
        DataIndexer indexer = null;
        try {
            indexer = new OnePassDataIndexer( new FileEventStream(trainingFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        MaxentModel trainedMaxentModel = GIS.trainModel(100, indexer); // 100 iterations

        // Storing the trained model into a file for later use (gzipped)
        File outFile = new File(modelFileName);
        AbstractModelWriter writer = new SuffixSensitiveGISModelWriter((AbstractModel) trainedMaxentModel, outFile);
        writer.persist();

        // Loading the gzipped model from a file
        FileInputStream inputStream = new FileInputStream(modelFileName);
        InputStream decodedInputStream = new GZIPInputStream(inputStream);
        DataReader modelReader = new PlainTextFileDataReader(decodedInputStream);
        maxentModel = new GISModelReader(modelReader).getModel();

        // Now predicting the outcome using the loaded model
        //String[] context = {"a=1", "b=0"};

        /*String[] context = {"home", "pdiff=0.6875", "ptwins=0.5"};
        double[] outcomeProbs = maxentModel.eval(context);
        String outcome = maxentModel.getBestOutcome(outcomeProbs);
*/
    }

    private static Trellis<State> buildTrellis(List<WordFeature> sentence) {
        final Trellis<State> trellis = new Trellis<State>();
        trellis.setStartState(State.getStartState());
        final State stopState = State.getStopState(sentence.size());
        trellis.setStopState(stopState);
        Set<State> states = Collections.singleton(State.getStartState());
        for (int position = 0; position < sentence.size(); position++) {
            final Set<State> nextStates = new HashSet<State>();
            for (final State state : states) {
                if (state.equals(stopState)) {
                    continue;
                }

                //build context
                List<Pair> features = TwitterFeatureExtractor.extractFeatures(position, sentence);
                String[] context = new String[features.size()];
                int ind = 0;
                for(Pair pair : features) {
                    String f = pair.getFirst() + "=" + pair.getSecond();
                    context[ind++] = f;
                }

                double[] outcomeProbs = maxentModel.eval(context);

                for(int i = 0; i < maxentModel.getNumOutcomes(); i++) {
                    String outcome = maxentModel.getOutcome(i);
                    double prob = outcomeProbs[i];
                    final State nextState = state.getNextState(outcome);
                    trellis.setTransitionCount(state, nextState, prob);
                    nextStates.add(nextState);

                }


                /*final LocalTrigramContext localTrigramContext = new LocalTrigramContext(
                        sentence, position, state.getPreviousPreviousTag(),
                        state.getPreviousTag());
                final Counter<String> tagScores = localTrigramScorer
                        .getLogScoreCounter(localTrigramContext);
                for (final String tag : tagScores.keySet()) {
                    final double score = tagScores.getCount(tag);
                    final State nextState = state.getNextState(tag);
                    trellis.setTransitionCount(state, nextState, score);
                    nextStates.add(nextState);
                }*/
            }
            // System.out.println("States: "+nextStates);
            states = nextStates;
        }
        return trellis;
    }

    public static List<String> tag(ArrayList<WordFeature> sentence) {
        final Trellis<State> trellis = buildTrellis(sentence);
        ViterbiDecoder decoder = new ViterbiDecoder();
        final List<State> states = decoder.getBestPath(trellis);
        List<String> tags = State.toTagList(states);
        tags = stripBoundaryTags(tags);
        return tags;
    }




    //tokenizer
    //postagger based off maxent
    //named entity
    public static void main(String args[]) throws IOException {

        splitData();
        posTagData();
        chunkTagData();

       /* createMaxEnt();
        String[] context = {"WORD=</S>"};
        double[] outcomeProbs = maxentModel.eval(context);
        String outcome = maxentModel.getBestOutcome(outcomeProbs);*/

        Boolean modelCreated = true;//createMaxEntModel();
        if(modelCreated) {
            createMaxEnt();
            for(ArrayList<WordFeature> sentence : testSet) {
                List<String> tags = tag(sentence);
            }
        }

    }




}







