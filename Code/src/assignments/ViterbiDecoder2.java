package assignments;

import opennlp.model.MaxentModel;
import util.Pair;
import util.WordFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 4/7/2016.
 */
public class ViterbiDecoder2 {
    private String[] states = { "B", "I", "O" };

    private MaxentModel maxentModel = null;

    public ViterbiDecoder2(MaxentModel maxentModel) {
        this.maxentModel = maxentModel;
    }

    public List<String> decode(ArrayList<WordFeature> sentence) {
        // observations
        int N = states.length;
        int T = sentence.size();
        double[][] viterbi = new double[N + 1][T + 1];
        String[][] backtrack = new String[N + 1][T + 1];
        // initialize first step
        for (int s = 0; s < N; s++) {
            String state = states[s];
            double v = 1.0;
            // calculate posterior probability by maximum entropy

            //build context
            List<Pair> features = null;
            try {
                features = TwitterFeatureExtractor.extractFeatures(0, sentence, null, false);
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

            int stateIndex = maxentModel.getIndex(state);
            // max probability
            double likelihood = v * outcomeProbs[stateIndex];
            double log = Math.abs(Math.log10(likelihood));
            viterbi[s][0] = log;
            backtrack[s][0] = "";
        }

        // recursive step
        for (int t = 1; t < T; t++) {
            for (int s = 0; s < N; s++) {
                double argmax = Double.MAX_VALUE;
                String backtrackArg = "";
                String state = states[s];
                for (int s1 = 0; s1 < N; s1++) {
                    double v = viterbi[s1][t - 1];
                    if (v == 0.0) {
                        continue;
                    }
                    String previousState = states[s1];
                    // build features

                    //build context
                    List<Pair> features = null;
                    try {
                        features = TwitterFeatureExtractor.extractFeatures(t, sentence, null, false);
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
                    int stateIndex = maxentModel.getIndex(state);
                    // max probability
                    double log = v + Math.abs(Math.log10(outcomeProbs[stateIndex]));
                    if (log < argmax) {
                        argmax = log;
                        backtrackArg = previousState;
                    }
                }
                viterbi[s][t] = argmax;
                backtrack[s][t] = backtrackArg;
            }
        }

        // terminate step
        viterbi[N][T] = Double.MAX_VALUE;
        for (int s = 0; s < N; s++) {
            String state = states[s];
            // max probability
            double likelihood = viterbi[s][T - 1];
            if (likelihood < viterbi[N][T]) {
                viterbi[N][T] = likelihood;
                backtrack[N][T] = state;
            }
        }
        // backtracking
        List<String> res = new ArrayList<>();
        int tagIndex = N;
        int stepIndex = T;
        String tag = backtrack[tagIndex][stepIndex];
        while (tag != null && tag.length() != 0) {
            // set tag
            String token = sentence.get(stepIndex - 1).getWord();
            res.add(tag);
            for (int i = 0; i < states.length; i++) {
                if (states[i].equals(tag)) {
                    tagIndex = i;
                    break;
                }
            }
            tag = backtrack[tagIndex][--stepIndex];
        }

        return res;
    }
}
