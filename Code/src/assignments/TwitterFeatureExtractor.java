package assignments;

import util.Pair;
import util.WordFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by User on 4/1/2016.
 */
public class TwitterFeatureExtractor {

    //for all features need to append on label (B, I, O)
    public static void main(String args[]) {
        /*Pattern punctuationPattern = Pattern.compile("[\\w,.!:\\'\\\"&]{2,}");
        Matcher m = punctuationPattern.matcher("''");
        if(m.find()) {
            System.out.println("matches");
        }
*/
    }

    public List<Pair> extractFeatures(int position, List<WordFeature> sentence) {
        List<Pair> features = new ArrayList<>();
        Pattern pattern;
        Matcher matcher;
        String word = sentence.get(position).getWord();
        //Features of word
        //All punctuation, i.e. !!!! or , or .
        if (word.matches("^([^\\w\\d\\s]{2,})$")) {
            Pair<String, Boolean> punct = new Pair("PCT", true);
            features.add(punct);
        } else {
            pattern = Pattern.compile("[,.!:\\'\\\"&]");
            matcher = pattern.matcher(word);
            //contains punctuation other than ~
            if (word.length() > 1 && matcher.find()) {
                Pair<String, Boolean> punct = new Pair("PCTINWORD", true);
                features.add(punct);
            }
        }

        //Hashtag
        if (word.charAt(0) == '#') {
            Pair<String, Boolean> hashTag = new Pair("HASHTAG", true);
            features.add(hashTag);
        }
        //Twitter Handle
        if (word.charAt(0) == '@') {
            Pair<String, Boolean> hashTag = new Pair("HANDLE", true);
            features.add(hashTag);
        }

        //URL
        pattern = Pattern.compile("^[(www)|(http)]");
        matcher = pattern.matcher(word);
        if (matcher.find()) {
            Pair<String, Boolean> url = new Pair("URL", true);
            features.add(url);
        }

        //Retweet
        if (word.startsWith("RT")) {
            Pair<String, Boolean> retweet = new Pair("RT", true);
            features.add(retweet);
        }

        //think more about Caps, all uppercase, mix, but what about words like INC.

        return null;
    }


    /*
    10.125 bias==True and label is 'O'
    6.631 suffix3=='day' and label is 'O'
    -6.207 bias==True and label is 'I-GSP'
    5.628 prevtag=='O' and label is 'O'
    -4.740 shape=='upcase' and label is 'O'
    4.106 shape+prevtag=='+O' and label is 'O'
    -3.994 shape=='mixedcase' and label is 'O'
    3.992 pos+prevtag=='NNP+B-PERSON' and label is 'I-PERSON'
    3.890 prevtag=='I-ORGANIZATION' and label is 'I-ORGANIZATION'
    3.879 shape+prevtag=='+I-ORGANIZATION' and label is 'I-ORGANIZATION'*/

}
