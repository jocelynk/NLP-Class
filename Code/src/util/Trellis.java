package util;

/**
 * Created by User on 4/3/2016.
 */
public class Trellis<S> {
    CounterMap<S, S> backwardTransitions;
    S endState;
    CounterMap<S, S> forwardTransitions;
    S startState;

    public Trellis() {
        forwardTransitions = new CounterMap<S, S>();
        backwardTransitions = new CounterMap<S, S>();
    }

    /**
     * For a given state, returns a counter over what states can precede it
     * in
     * the markov process, along with the cost of that transition.
     */
    public Counter<S> getBackwardTransitions(S state) {
        return backwardTransitions.getCounter(state);
    }

    /**
     * Get the unique end state for this trellis.
     */
    public S getEndState() {
        return endState;
    }

    /**
     * For a given state, returns a counter over what states can be next in
     * the
     * markov process, along with the cost of that transition. Caution: a
     * state
     * not in the counter is illegal, and should be considered to have cost
     * Double.NEGATIVE_INFINITY, but Counters score items they don't contain
     * as
     * 0.
     */
    public Counter<S> getForwardTransitions(S state) {
        return forwardTransitions.getCounter(state);

    }

    /**
     * Get the unique start state for this trellis.
     */
    public S getStartState() {
        return startState;
    }

    public void setStartState(S startState) {
        this.startState = startState;
    }

    public void setStopState(S endState) {
        this.endState = endState;
    }

    public void setTransitionCount(S start, S end, double count) {
        forwardTransitions.setCount(start, end, count);
        backwardTransitions.setCount(end, start, count);
    }
}
