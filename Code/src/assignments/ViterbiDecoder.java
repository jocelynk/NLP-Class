package assignments;

import util.Counter;
import util.Trellis;
import util.TrellisDecoder;

import java.util.*;

/**
 * Created by User on 4/3/2016.
 */
public class ViterbiDecoder<S> implements TrellisDecoder<S> {

    private Set<S> visited;
    private Stack<S> states;

    private void topologicalSort(S currentState, Trellis<S> trellis) {
        // Mark the current node as visited
        visited.add(currentState);

        // Recur for all the vertices adjacent to this vertex
        for (S state : trellis.getForwardTransitions(currentState).keySet()) {
            if (!visited.contains(state))
                topologicalSort(state, trellis);
        }

        // Push current vertex to stack which stores topological sort
        states.push(currentState);
    }

    /**Todo: figure out why first word is getting tagged as start**/
    @Override
    public List<S> getBestPath(Trellis<S> trellis) {
        List<S> result = new ArrayList<S>();
        S startState = trellis.getStartState();
        states = new Stack<>();
        visited = new HashSet<>();
        topologicalSort(startState, trellis);

        HashMap<S, Double> distances = new HashMap<>();
        HashMap<S, S> backPointers = new HashMap<>();

        distances.put(startState, 0.0);
        backPointers.put(startState, null);
        while (!states.isEmpty()) {
            S ver = states.pop();
            if (distances.containsKey(ver)) {
                Counter<S> transitions = trellis.getForwardTransitions(ver);
                for (S st : transitions.keySet()) {
                    double prevScore = distances.get(ver);
                    double newScore = prevScore + transitions.getCount(st);
                    if (!distances.containsKey(st)) {
                        distances.put(st, newScore);
                        backPointers.put(st, ver);
                    } else {
                        if (distances.get(st) < newScore) {
                            distances.put(st, newScore);
                            backPointers.put(st, ver);
                        }
                    }

                }
            }
        }

        S currentState = trellis.getEndState();
        result.add(currentState);
        while (currentState != trellis.getStartState()) {
            S st = backPointers.get(currentState);
            result.add(st);
            currentState = st;
        }

        Collections.reverse(result);

        return result;
    }


}