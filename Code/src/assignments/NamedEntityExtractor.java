package assignments;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPInputStream;

import math.DoubleArrays;
import opennlp.maxent.GIS;
import opennlp.maxent.io.GISModelReader;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;
import opennlp.model.*;
import util.State;
import util.Trellis;

/**
 * Created by User on 3/22/2016.
 */
public class NamedEntityExtractor {
    private static List<ArrayList<String[]>> trainingSet = new ArrayList<>();
    private static List<ArrayList<String[]>> testSet = new ArrayList<>();
    private static final String TRAIN_FILE_NAME = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/train.txt";
    private static MaxentModel maxentModel;

    public static void splitData() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(TRAIN_FILE_NAME))) {
            String line = br.readLine();
            List<ArrayList<String[]>> tweets = new ArrayList<>();
            List<String[]> tweet = new ArrayList<>();
            while (line != null) {
                if (line.length() < 1) {
                    //System.out.println("newline: " + line);
                    tweets.add(new ArrayList<>(tweet));
                    tweet = new ArrayList<>();
                } else {
                    //System.out.println(line);
                    String[] wordPair = line.split("\\s+");
                    tweet.add(wordPair);
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

    public static void maxEnt() throws IOException {
         // here are the input training samples

        String trainingFileName = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/realTeam.dat";//"training-file.txt";
        String modelFileName = "trained-team-model.maxent.gz";
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

        String[] context = {"home", "pdiff=0.6875", "ptwins=0.5"};
        double[] outcomeProbs = maxentModel.eval(context);
        String outcome = maxentModel.getBestOutcome(outcomeProbs);

    }

    private Trellis<State> buildTrellis(List<String> sentence) {
        final Trellis<State> trellis = new Trellis<State>();
        trellis.setStartState(State.getStartState());
        final State stopState = State.getStopState(sentence.size() + 2);
        trellis.setStopState(stopState);
        Set<State> states = Collections.singleton(State.getStartState());
        for (int position = 0; position <= sentence.size()
                + 1; position++) {
            final Set<State> nextStates = new HashSet<State>();
            for (final State state : states) {
                if (state.equals(stopState)) {
                    continue;
                }

                //build context
                String[] context = {"home", "pdiff=0.6875", "ptwins=0.5"};
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




    //tokenizer
    //postagger based off maxent
    //named entity
    public static void main(String args[]) throws IOException {

        //splitData();
        maxEnt();

    }




}







