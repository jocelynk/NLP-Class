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
        }*/

    }

    public List<Pair> extractFeatures(int position, List<WordFeature> sentence) {
        List<Pair> features = new ArrayList<>();
        Pattern pattern;
        Matcher matcher;
        WordFeature feature = sentence.get(position);
        String word = feature.getWord();
        /***Features of word***/

        //first word in sentence
        if(position == 0)
            features.add(new Pair("FIRST WORD", true));

        //word itself
        features.add(new Pair("WORD", word.toLowerCase()));

        //previous word
        if(position > 0) {
            features.add(new Pair("i-1_WORD", sentence.get(position-1).getWord().toLowerCase()));
        }

        //pre previous word
        if(position - 1 > 0) {
            features.add(new Pair("i-2_WORD", sentence.get(position-2).getWord().toLowerCase()));
        }

        //following word
        if(position < sentence.size() - 1) {
            features.add(new Pair("i+1_WORD", sentence.get(position+1).getWord().toLowerCase()));
        }

        //word after following word
        if(position < sentence.size() - 2) {
            features.add(new Pair("i+2_WORD", sentence.get(position+2).getWord().toLowerCase()));
        }

        //Last three letters of word
        if (word.length() > 3) {
            Pair<String, String> suffix = new Pair("SUFFIX", word.substring(word.length() - 3).toLowerCase());
            features.add(suffix);
        }

        //First three letters of word
        if (word.length() > 3) {
            Pair<String, String> prefix = new Pair("PREFIX", word.substring(0, 4).toLowerCase());
            features.add(prefix);
        }

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

        //inline period
        pattern = Pattern.compile("[a-z][.][a-z]");
        matcher = pattern.matcher(word);
        if (matcher.find()) {
            Pair<String, Boolean> inline_period = new Pair("INLINE_PERIOD", true);
            features.add(inline_period);
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

        /**Capitalization**/
        /**change to if else? **/
        //All CAPS
        pattern = Pattern.compile("^[A-Z\\s\\d%s]+$");
        matcher = pattern.matcher(word);
        if (matcher.find()) {
            Pair<String, Boolean> allCaps = new Pair("ALLCAPS", true);
            features.add(allCaps);
        }

        //Title
        pattern = Pattern.compile("^[A-Z][a-z]{0,}$");
        matcher = pattern.matcher(word);
        if (matcher.find()) {
            Pair<String, Boolean> title = new Pair("TITLE", true);
            features.add(title);
        }

        //Mixcase
        pattern = Pattern.compile("^[a-zA-Z]*(?:[a-z][A-Z]|[A-Z][a-z])[a-zA-Z]*");
        matcher = pattern.matcher(word);
        if (matcher.find()) {
            Pair<String, Boolean> mixcase = new Pair("MIXCASE", true);
            features.add(mixcase);
        }

        //Initials
        pattern = Pattern.compile("^(?:[A-Z]{1}\\.{1}|[A-Z]{1}\\.{1}[A-Z]{1})+$");
        matcher = pattern.matcher(word);
        if (matcher.find()) {
            Pair<String, Boolean> initials = new Pair("INITIALS", true);
            features.add(initials);
        }


        //think more about Caps, all uppercase, mix, but what about words like INC. or U.K.


        /***Features of Labels***/
        /**NE**/
        //prevtag=='O' and current NE label is 'O'
        if((position > 0 && sentence.get(position - 1).getNeTag().toUpperCase().equals("O")) && feature.getNeTag().toUpperCase().equals("O")) {
            Pair<String, String> retweet = new Pair("NE_PREV+NE_CUR", "O");
            features.add(retweet);
        }

        if(position > 0) {
            //i-1 NE tag
            Pair<String, String> ne_prev = new Pair("NE_PREV", sentence.get(position - 1).getNeTag().toUpperCase());
            features.add(ne_prev);

            //i-1 NE tag + word
            Pair<String, String> ne_prev_word = new Pair("NE_PREV+WORD", sentence.get(position - 1).getNeTag().toUpperCase() + "_" + word);
            features.add(ne_prev_word);
        }

        if(position - 1 > 0) {
            //i-2 NE tag
            Pair<String, String> ne_prev = new Pair("NE_PREPREV", sentence.get(position - 2).getNeTag().toUpperCase());
            features.add(ne_prev);

            //i-1 NE tag + i-2 NE tag
            Pair<String, String> ne_prePrev = new Pair("NE_PRE+NE_PREV", sentence.get(position - 1).getNeTag().toUpperCase() + "_" + sentence.get(position - 2).getNeTag().toUpperCase());
            features.add(ne_prePrev);
        }

        /**POS TAG **/
        features.add(new Pair("POS_TAG", feature.getPosTag()));

        //previous pos tag
        if(position > 0) {
            features.add(new Pair("i-1_POS_TAG", sentence.get(position-1).getPosTag()));
        }

        //pre previous pos tag
        if(position - 1 > 0) {
            features.add(new Pair("i-2_POS_TAG", sentence.get(position-2).getPosTag()));
        }

        //word + POS Tag of following word
        if(position < sentence.size() - 1) {
            features.add(new Pair("WORD+POS_TAG_+1", word + "_" + sentence.get(position+1).getPosTag()));
        }

        /*Chunk Tags*/
        //current chunk tag
        features.add(new Pair("CHUNK_TAG", feature.getChunkTag()));

        //previous chunk tag
        if(position > 0) {
            features.add(new Pair("i-1_CHUNK_TAG", sentence.get(position-1).getChunkTag()));
        }

        //pre previous chunk tag
        if(position - 1 > 0) {
            features.add(new Pair("i-2_CHUNK_TAG", sentence.get(position-2).getChunkTag()));
        }

        /**COMBOS**/

        //POS tag of word combined with NE tag of preceding word
        if(position > 0) {
            features.add(new Pair("POS_TAG+NE_PREV", word + "_" + sentence.get(position-1).getNeTag()));
        }

        /**Relations**/
        //beginning of NE group
        if(feature.getNeTag().toUpperCase().equals("B")) {
            features.add(new Pair("BEGIN_NE_GROUP", true));
        }

        //end of NE group
        boolean NE_END = true;
        if(!feature.getNeTag().toUpperCase().equals("I")) {
            NE_END = false;
        } else if(position < sentence.size() - 1 && sentence.get(position + 1).getNeTag().toUpperCase().equals("I")) {
            NE_END = false;
        }

        if(NE_END) {
            features.add(new Pair("END_NE_GROUP", true));
        }

        //in between NE group
        boolean NE_BTWN = true;
        if(!feature.getNeTag().toUpperCase().equals("I")) {
            NE_BTWN = false;
        } else if(position == sentence.size() - 1 || position == 0) {
            NE_BTWN = false;
        } else if(!sentence.get(position + 1).getNeTag().toUpperCase().equals("I")) {
            NE_BTWN = false;
        }

        if(NE_BTWN) {
            features.add(new Pair("BTWN_NE_GROUP", true));
        }


        return features;
    }

    /*  if (word.length() > 0)
            features.incrementCount("i pref1" + word.charAt(0), 1);
        features.incrementCount("i-1 tag" + previousTag, 1);
        features.incrementCount("i-2 tag" + prePreviousTag, 1);
        features.incrementCount("i tag+i-2 tag" + previousTag + prePreviousTag, 1);
        features.incrementCount("" + context.get(position), 1);
        features.incrementCount("i-1 tag+i word" + previousTag + context.get(position), 1);
        //need to make sure to add start and end to context
        if (position > 0)
            features.incrementCount("i-1 word" + context.get(position - 1), 1);
        if (position > 0 && context.get(position - 1).length() > 3)
            features.incrementCount("i-1 suffix" + context.get(position - 1).substring(context.get(position - 1).length() - 3), 1);
        if (position > 1)
            features.incrementCount("i-2 word" + context.get(position - 2), 1);
        if (position < context.size() - 1)
            features.incrementCount("i+1 word" + context.get(position + 1), 1);
        if (position < context.size() - 1 && context.get(position + 1).length() > 3)
            features.incrementCount("i+1 suffix" + context.get(position + 1).substring(context.get(position + 1).length() - 3), 1);
        if (position < context.size() - 2)
            features.incrementCount("i+2 word" + context.get(position + 2), 1);*/

    /*SMALL = 'a|an|and|as|at|but|by|en|for|if|in|of|on|or|the|to|v\.?|via|vs\.?'
    PUNCT = r"""!"#$%&'‘()*+,\-./:;?@[\\\]_`{|}~"""

    SMALL_WORDS = re.compile(r'^(%s)$' % SMALL, re.I)
    INLINE_PERIOD = re.compile(r'[a-z][.][a-z]', re.I)
    UC_ELSEWHERE = re.compile(r'[%s]*?[a-zA-Z]+[A-Z]+?' % PUNCT)
    CAPFIRST = re.compile(r"^[%s]*?([A-Za-z])" % PUNCT)
    SMALL_FIRST = re.compile(r'^([%s]*)(%s)\b' % (PUNCT, SMALL), re.I)
    SMALL_LAST = re.compile(r'\b(%s)[%s]?$' % (SMALL, PUNCT), re.I)
    SUBPHRASE = re.compile(r'([:.;?!\-\—][ ])(%s)' % SMALL)
    APOS_SECOND = re.compile(r"^[dol]{1}['‘]{1}[a-z]+(?:['s]{2})?$", re.I)
    ALL_CAPS = re.compile(r'^[A-Z\s\d%s]+$' % PUNCT)
    UC_INITIALS = re.compile(r"^(?:[A-Z]{1}\.{1}|[A-Z]{1}\.{1}[A-Z]{1})+$")
    MAC_MC = re.compile(r"^([Mm]c)(\w.+)")*/


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

    /*The shape of the word (e.g., does it contain numbers? does it begin with a capital letter?)
    The length of the word

    The POS tag of the word
    The word itself
    Does the word exist in an English dictionary?
    The tag of the word that precedes this word (i.e., was the previous word identified as a NE)
    The POS tag of the preceding word
    The POS tag of the following word
    The word that precedes this word
    The word that follows this word
    The word combined with the POS tag of the following word
    The POS tag of the word combined with the tag of the preceding word
    The shape of the word combined with the tag of the preceding word*/

}
