package util;

import java.util.List;

/**
 * Created by User on 4/3/2016.
 */
public interface TrellisDecoder<S> {
    List<S> getBestPath(Trellis<S> trellis);
}
