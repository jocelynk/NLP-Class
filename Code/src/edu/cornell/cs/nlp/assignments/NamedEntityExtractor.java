package edu.cornell.cs.nlp.assignments;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import edu.cornell.cs.nlp.assignments.util.CommandLineUtils;
import edu.cornell.cs.nlp.assignments.util.Pair;
import edu.cornell.cs.nlp.assignments.util.WordFeature;

/**
 * Created by Jocelyn on 3/22/2016.
 */
public class NamedEntityExtractor {
    private static List<ArrayList<WordFeature>> trainingSet = new ArrayList<>();
    private static List<ArrayList<WordFeature>> testSet = new ArrayList<>();
    private static Map<String, Integer> brownClusters = new HashMap<>();

    static final String START_TAG = "<S>";
    static final String START_WORD = "<S>";
    static final String STOP_TAG = "</S>";
    static final String STOP_WORD = "</S>";


    //Lucene Indexer
    private static TextFileIndexer indexer  = null;

    public static void main(String args[]) {
        // Parse command line flags and arguments.
        final Map<String, String> argMap = CommandLineUtils
                .simpleCommandLineParser(args);

        // Read commandline parameters.
        String trainDataPath = "";
        if (!argMap.containsKey("-train")) {
            System.out.println("-train flag required.");
            System.exit(0);
        } else {
            trainDataPath = argMap.get("-train");
        }

        String testDataPath = "";
        if (!argMap.containsKey("-test")) {
            System.out.println("-test flag required.");
            System.exit(0);
        } else {
            testDataPath = argMap.get("-test");
        }

        String trainOutputPath = "";
        if (!argMap.containsKey("-train-output")) {
            System.out.println("-train-output flag required.");
            System.exit(0);
        } else {
            trainOutputPath = argMap.get("-train-output");
        }


        String testOutputPath = "";
        if (!argMap.containsKey("-test-output")) {
            System.out.println("-test-output flag required.");
            System.exit(0);
        } else {
            testOutputPath = argMap.get("-test-output");
        }

        if (argMap.containsKey("-brown")) {
            getBrownDictionary(argMap.get("-brown"));
        }

        String luceneFiles = "";
        if (argMap.containsKey("-init-lucene-indexes") && !argMap.containsKey("-lucene-files")) {
            System.out.println("-lucene-files flag required.");
            System.exit(0);
        } else {
            luceneFiles = argMap.get("-lucene-files");
        }

        String luceneIndex = "";
        if (!argMap.containsKey("-lucene-index")) {
            System.out.println("-lucene-index flag required.");
            System.exit(0);
        } else {
            luceneIndex = argMap.get("-lucene-index");
        }

        if (argMap.containsKey("-init-lucene-indexes")) {
            try {
                initTextFileIndexer(true, luceneFiles, luceneIndex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                initTextFileIndexer(false, luceneFiles, luceneIndex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long start = System.currentTimeMillis();

        if (argMap.containsKey("-crossvalidation")) {
            String cvTestOutputPath = "";
            if (!argMap.containsKey("-cv-test-output-labeled")) {
                System.out.println("-cv-test-output-labeled flag required.");
                System.exit(0);
            } else {
                cvTestOutputPath = argMap.get("-cv-test-output-labeled");
            }

            try {
                splitDataTrainOnly(trainDataPath, cvTestOutputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            long cvStartTrain = System.currentTimeMillis();

            createFeatures(trainOutputPath, true, luceneIndex);

            long cvEndTrain = System.currentTimeMillis() - cvStartTrain;
            double cvTrainMin = (cvEndTrain / 1000.0)  / 60.0;
            System.out.println("Time to get train features: " + cvTrainMin + " minutes");

            long cvStartTest = System.currentTimeMillis();

            createFeatures(testOutputPath, false, luceneIndex);

            long cvEndTest = System.currentTimeMillis() - cvStartTest;
            double cvTestMin = (cvEndTest / 1000.0)  / 60.0;
            System.out.println("Time to get train features: " + cvTestMin + " minutes");

        } else {
            if (argMap.containsKey("-kaggle")) {
                splitData(trainDataPath, testDataPath, true);
            } else {
                splitData(trainDataPath, testDataPath, false);
            }
            long startTrain = System.currentTimeMillis();

            createFeatures(trainOutputPath, true, luceneIndex);

            long endTrain = System.currentTimeMillis() - startTrain;
            double trainMin = (endTrain / 1000.0)  / 60.0;
            System.out.println("Time to get train features: " + trainMin + " minutes");

            long startTest = System.currentTimeMillis();

            createFeatures(testOutputPath, false, luceneIndex);

            long endTest = System.currentTimeMillis() - startTest;
            double testMin = (endTest / 1000.0)  / 60.0;
            System.out.println("Time to get test features: " + testMin + " minutes");
        }

        long time = System.currentTimeMillis() - start;
        double minutes = (time / 1000.0)  / 60.0;
        System.out.println("Time to train model: " + minutes + " minutes");


    }


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


    private static void initTextFileIndexer(boolean addFiles, String fileDirectory, String indexDirectory) throws IOException {

        try {
            indexer = new TextFileIndexer(indexDirectory);
            if(addFiles)
                indexer.indexFileOrDirectory(fileDirectory);
            indexer.closeIndex();

        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }
    }

    /**End Helpers**/
    public static void splitData(String trainFileName, String testFileName, Boolean kaggle) {
        try (BufferedReader br = new BufferedReader(new FileReader(trainFileName))) {
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

        try (BufferedReader br = new BufferedReader(new FileReader(testFileName))) {
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

    public static void splitDataTrainOnly(String trainFileName, String cvTestFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(trainFileName))) {
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

                File file = new File(cvTestFile);
            /* This logic will make sure that the file
            * gets created if it is not present at the
            * specified location*/
                if(!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw_test = new FileWriter(cvTestFile);
                bw_test = new BufferedWriter(fw_test);

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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void getBrownDictionary(String brownFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(brownFile))) {
            String line = br.readLine();

            while (line != null) {
                line.replace("\n", "").replace("\r", "");
                String[] pair = line.split("\\s+");
                int bits = Integer.parseInt(pair[1]);
                brownClusters.put(pair[0], bits);
                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void createFeatures(String fileName, Boolean isTrain, String luceneIndex) {
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
                    List<Pair> features = TwitterFeatureExtractor.extractFeatures(i, sentence, indexer, brownClusters, luceneIndex);
                    int ind = 0;
                    for(Pair pair : features) {
                        line.append(pair.getFirst() + "=" + pair.getSecond());
                        if(ind != features.size()-1) {
                            line.append(" ");
                        }
                    }

                    if(isTrain)
                        line.append(sentence.get(i).getNeTag() + " ");

                    bw.write(line.toString() + "\n");
                }
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
    }
}






