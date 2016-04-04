package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 4/3/2016.
 */
public class State {
    static final String START_TAG = "<S>";
    static final String START_WORD = "<S>";
    static final String STOP_TAG = "</S>";
    static final String STOP_WORD = "</S>";

    private static transient Interner<State> stateInterner = new Interner<State>(
            state -> new State(state));

    private static transient State tempState = new State();

    int position;

    String previousPreviousTag;

    String previousTag;

    private State() {
    }

    private State(State state) {
        setState(state.getPreviousPreviousTag(), state.getPreviousTag(),
                state.getPosition());
    }

    public static State buildState(String previousPreviousTag,
                                   String previousTag, int position) {
        tempState.setState(previousPreviousTag, previousTag, position);
        return stateInterner.intern(tempState);
    }

    public static State getStartState() {
        return buildState(START_TAG, START_TAG, 0);
    }

    public static State getStopState(int position) {
        return buildState(STOP_TAG, STOP_TAG, position);
    }

    public static List<String> toTagList(List<State> states) {
        final List<String> tags = new ArrayList<String>();
        if (states.size() > 0) {
            tags.add(states.get(0).getPreviousPreviousTag());
            for (final State state : states) {
                tags.add(state.getPreviousTag());
            }
        }
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof State)) {
            return false;
        }

        final State state = (State) o;

        if (position != state.position) {
            return false;
        }
        if (previousPreviousTag != null
                ? !previousPreviousTag.equals(state.previousPreviousTag)
                : state.previousPreviousTag != null) {
            return false;
        }
        if (previousTag != null ? !previousTag.equals(state.previousTag)
                : state.previousTag != null) {
            return false;
        }

        return true;
    }

    public State getNextState(String tag) {
        return State.buildState(getPreviousTag(), tag, getPosition() + 1);
    }

    public int getPosition() {
        return position;
    }

    public String getPreviousPreviousTag() {
        return previousPreviousTag;
    }

    public State getPreviousState(String tag) {
        return State.buildState(tag, getPreviousPreviousTag(),
                getPosition() - 1);
    }

    public String getPreviousTag() {
        return previousTag;
    }

    @Override
    public int hashCode() {
        int result;
        result = position;
        result = 29 * result
                + (previousTag != null ? previousTag.hashCode() : 0);
        result = 29 * result + (previousPreviousTag != null
                ? previousPreviousTag.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[" + getPreviousPreviousTag() + ", " + getPreviousTag()
                + ", " + getPosition() + "]";
    }

    private void setState(String previousPreviousTag, String previousTag,
                          int position) {
        this.previousPreviousTag = previousPreviousTag;
        this.previousTag = previousTag;
        this.position = position;
    }
}
