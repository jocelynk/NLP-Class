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
 * Created by Jocelyn on 3/22/2016.
 */
public class NamedEntityExtractor {
    private static List<ArrayList<WordFeature>> trainingSet = new ArrayList<>();
    private static List<ArrayList<WordFeature>> testSet = new ArrayList<>();
    private static final String TRAIN_FILE_NAME_RAW = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/data-raw/train.txt";
    private static final String TEST_FILE_NAME_RAW = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/data-raw/dev1.txt";
    private static final String MAXENT_DATA = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/models/twitter-maxent-data-train.txt";
    private static final String MAXENT_MODEL = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/models/twitter-model.maxent.gz";

    private static final String TRAIN_FILE_NAME = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/inputs/train_processed_testing.txt";
    //private static final String TEST_FILE_NAME = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/inputs/test_processed.txt";
    private static final String TEST_FILE_NAME = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/inputs/test.nolabels.processed.txt";

    private static final String CRF_TRAIN_DATA = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/features/train_crf_data.txt";
    private static final String CRF_TEST_DATA = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/features/test_crf_data.txt";


    private static final String CV_TRAIN_DATA = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/features/cv/train_cv_data.txt";
    private static final String CV_TEST_DATA = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/features/cv/test_cv_data.txt";
    private static final String CV_TRAIN_LABELED = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/features/cv/train_cv_labeled.txt";
    private static final String CV_TEST_LABELED = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/features/cv/test_cv_labeled.txt";

    private static final String EXTRA_DATA = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/Python/extra_tweebo_features.txt";
    private static final String EXTRA_CRF_DATA = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/Python/extra_crf.txt";

    private static MaxentModel maxentModel;
    static final String START_TAG = "<S>";
    static final String START_WORD = "<S>";
    static final String STOP_TAG = "</S>";
    static final String STOP_WORD = "</S>";


    //Lucene Indexer
    static final String INDEX_DIRECTORY = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/lucene_indexer";
    private static TextFileIndexer indexer  = null;
    static final String FILE_DIRECTORY = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/lucene_files";


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

    private static void initTextFileIndexer(boolean addFiles) throws IOException {

        try {
            indexer = new TextFileIndexer(INDEX_DIRECTORY);
            if(addFiles)
                indexer.indexFileOrDirectory(FILE_DIRECTORY);
            indexer.closeIndex();

        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }
    }

    /**End Helpers**/
    public static void splitData(Boolean kaggle) {
        try (BufferedReader br = new BufferedReader(new FileReader(TRAIN_FILE_NAME))) {
            String line = br.readLine();
            List<WordFeature> trainTweet = new ArrayList<>();
            trainTweet.add(new WordFeature(-1, START_WORD, START_TAG, START_TAG, -1, null));
            while (line != null) {
                if (line.length() < 1) {
                    //System.out.println("newline: " + line);
                    trainTweet.add(new WordFeature(-1, STOP_WORD, STOP_TAG, STOP_TAG, -1, null));
                    trainingSet.add(new ArrayList<>(trainTweet));
                    trainTweet = new ArrayList<>();
                    trainTweet.add(new WordFeature(-1, START_WORD, START_TAG, START_TAG, -1, null));
                } else {
                    //System.out.println(line);
                    String[] wordPair = line.split("\\s+");
                    WordFeature feature = new WordFeature(Integer.parseInt(wordPair[0]), wordPair[1], wordPair[2], wordPair[3], Integer.parseInt(wordPair[4]), wordPair[5]);

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
            testTweet.add(new WordFeature(-1, START_WORD, START_TAG, START_TAG, -1, null));
            while (line != null) {
                if (line.length() < 1) {
                    //System.out.println("newline: " + line);
                    testTweet.add(new WordFeature(-1, STOP_WORD, STOP_TAG, STOP_TAG, -1, null));
                    testSet.add(new ArrayList<>(testTweet));
                    testTweet = new ArrayList<>();
                    testTweet.add(new WordFeature(-1, START_WORD, START_TAG, START_TAG, -1, null));
                } else {
                    //System.out.println(line);
                    String[] wordPair = line.split("\\s+");
                    WordFeature feature = null;
                    if(!kaggle)
                        feature = new WordFeature(Integer.parseInt(wordPair[0]), wordPair[1], wordPair[2], wordPair[3], Integer.parseInt(wordPair[4]), wordPair[5]);
                    else
                        feature = new WordFeature(Integer.parseInt(wordPair[0]), wordPair[1], null, wordPair[2], Integer.parseInt(wordPair[3]), wordPair[4]);

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
            tweet.add(new WordFeature(-1, START_WORD, START_TAG, START_TAG, -1, null));
            while (line != null) {
                if (line.length() < 1) {
                    //System.out.println("newline: " + line);
                    tweet.add(new WordFeature(-1, STOP_WORD, STOP_TAG, STOP_TAG, -1, null));
                    tweets.add(new ArrayList<>(tweet));
                    tweet = new ArrayList<>();
                    tweet.add(new WordFeature(-1, START_WORD, START_TAG, START_TAG, -1, null));
                } else {
                    //System.out.println(line);
                    String[] wordPair = line.split("\\s+");
                    WordFeature feature = new WordFeature(Integer.parseInt(wordPair[0]), wordPair[1], wordPair[2], wordPair[3], Integer.parseInt(wordPair[4]), wordPair[5]);

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
            BufferedWriter bw_train = null;
            BufferedWriter bw_test = null;
            try{

                File file1 = new File(CV_TRAIN_LABELED);
                File file2 = new File(CV_TEST_LABELED);
            /* This logic will make sure that the file
            * gets created if it is not present at the
            * specified location*/
                if(!file1.exists()) {
                    file1.createNewFile();
                    file2.createNewFile();
                }

                FileWriter fw_train = new FileWriter(CV_TRAIN_LABELED);
                FileWriter fw_test = new FileWriter(CV_TEST_LABELED);
                bw_train = new BufferedWriter(fw_train);
                bw_test = new BufferedWriter(fw_test);
                for(ArrayList<WordFeature> sentence : trainingSet) {
                    for(int i = 0; i < sentence.size(); i++) {
                        String word = sentence.get(i).getWord();
                        String ne = sentence.get(i).getNeTag();

                        bw_train.write(word.trim() + "\t" + ne + "\n");
                    }
                    bw_train.write("\n");
                }
                System.out.println("Train Labeled File written Successfully");

                for(ArrayList<WordFeature> sentence : testSet) {
                    for(int i = 0; i < sentence.size(); i++) {
                        String word = sentence.get(i).getWord();
                        String ne = sentence.get(i).getNeTag();

                        if(!word.trim().equals("<S>") && !word.trim().equals("</S>"))
                            bw_test.write(word.trim() + "\t" + ne + "\n");
                    }
                    bw_test.write("\n");
                }
                System.out.println("Test Labeled File written Successfully");


            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            finally
            {
                try{
                    if(bw_train!=null)
                        bw_train.close();
                    if(bw_test!=null)
                        bw_test.close();
                }catch(Exception ex){
                    System.out.println("Error in closing the BufferedWriter"+ex);
                }
            }

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


    public static Boolean createFeatures(String fileName, Boolean isTrain, Boolean isMaxEnt) {
        BufferedWriter bw = null;
        try{

            File file = new File(fileName);

            /* This logic will make sure that the file
            * gets created if it is not present at the
            * specified location*/
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            List<ArrayList<WordFeature>> list = isTrain? trainingSet : testSet;

            for(ArrayList<WordFeature> sentence : list) {
                for(int i = 0; i < sentence.size(); i++) {
                    StringBuilder line = new StringBuilder();
                    if(isMaxEnt)
                        line.append(sentence.get(i).getNeTag() + " ");
                    List<Pair> features = TwitterFeatureExtractor.extractFeatures(i, sentence, indexer, true);
                    int ind = 0;
                    for(Pair pair : features) {
                        line.append(pair.getFirst() + "=" + pair.getSecond());
                        if(ind != features.size()-1) {
                            line.append(" ");
                        }
                    }

                    if(isTrain && !isMaxEnt)
                        line.append(sentence.get(i).getNeTag() + " ");

                    bw.write(line.toString() + "\n");
                }
            }
            System.out.println("File written Successfully");
            if(isMaxEnt)
                return true;
            else
                return false;

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

        String trainingFileName = MAXENT_DATA;
        String modelFileName = MAXENT_MODEL;
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

    }

    private static Trellis<State> buildTrellis(List<WordFeature> sentence) {
        final Trellis<State> trellis = new Trellis<State>();
        trellis.setStartState(State.getStartState());
        final State stopState = State.getStopState(sentence.size()+2);
        trellis.setStopState(stopState);
        Set<State> states = Collections.singleton(State.getStartState());
        for (int position = 0; position <= sentence.size() + 1; position++) {
            final Set<State> nextStates = new HashSet<State>();
            for (final State state : states) {
                if (state.equals(stopState)) {
                    continue;
                }

                //build context
                List<Pair> features = null;
                try {
                    features = TwitterFeatureExtractor.extractFeatures(position, sentence, indexer, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        sentence.remove(0);
        sentence.remove(sentence.size()-1);
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



        //CV
        //splitDataTrainOnly();

        //Kaggle
        splitData(true);

        //CV
        /*initTextFileIndexer(false);
        Boolean featuresCreated = createFeatures(CV_TRAIN_DATA, true, false);
        createFeatures(CV_TEST_DATA, false, false);*/

        //Kaggle
        initTextFileIndexer(false);
        Boolean featuresCreated = createFeatures(CRF_TRAIN_DATA, true, false);
        createFeatures(CRF_TEST_DATA, false, false);

        //Maxent
        //Boolean featuresCreated = createFeatures(MAXENT_DATA, true, true);

        if(featuresCreated) {
            createMaxEnt();
            double numTagsCorrect = 0;
            double numTags = 0;
            double numNETagsCorrect = 0;
            double numNETags = 0;
            BufferedWriter bw = null;
            try{

                File file = new File("C:/Users/User/Documents/Cornell/Courses/NLP/HW4/results/dev_result_maxent.txt");

                if(!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                for(ArrayList<WordFeature> sentence : testSet) {
                    List<String> tags = tag(sentence);

                    for(int i = 0; i < sentence.size(); i++) {
                        bw.write(tags.get(i) + "\n");
                        String wordNETag = sentence.get(i).getNeTag();
                        if (wordNETag.equals(tags.get(i))) numTagsCorrect++;
                        numTags++;

                        if(wordNETag.equals("B") || wordNETag.equals("I")) {
                            if (wordNETag.equals(tags.get(i))) {
                                numNETagsCorrect++;
                            }
                            numNETags++;
                        }
                    }
                    bw.write("\n");

                }

                System.out.println("File written Successfully");

            }
            catch (IOException ioe) {
                ioe.printStackTrace();
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


            System.out.println("Tag Accuracy: " + numTagsCorrect / numTags);
            System.out.println("Named Entity Accuracy: " + numNETagsCorrect / numNETags);
        }

    }




}







