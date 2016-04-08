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

    //ToDo: Search in database for movies, names, music, etc
    //ToDo: Days of week, month?

    /*1 Saturday O ^ 2 MWE
2 September O ^ 0 _
3 18th O $ 2 MWE
4 2010 O $ 5 _
5 8pm O $ 6 _
6 Hotel B N 7 _
7 Elegante I ^ 8 MWE
8 Steve B ^ 10 MWE
9 " I , -1 _
10 Stylez I ^ 0 _
11 " I , -1 _
12 A.K.A. O ~ -1 _
13 Desperado B ^ 14 _
14 Birthday O N 15 _
15 Bash O N 0 _
16 ! O , -1 _
17 " O , -1 _
18 Headrush O ^ 15 _
19 in O P 0 _
20 da O D 21 _
21 building O N 19 _
22 " O , -1 _*/
    //Find Group of words, go up dependency tree

    static final String START_TAG = "<S>";
    static final String START_WORD = "<S>";
    static final String STOP_TAG = "</S>";
    static final String STOP_WORD = "</S>";


    public static void main(String args[]) {

        System.out.println("" + true);
        /*Pattern punctuationPattern = Pattern.compile("[\\w,.!:\\'\\\"&]{2,}");
        Matcher m = punctuationPattern.matcher("''");
        if(m.find()) {
            System.out.println("matches");
        }*/

    }

    public static List<Pair> extractFeatures(int position, List<WordFeature> sentence, boolean train) {
        List<Pair> features = new ArrayList<>();
        Pattern pattern;
        Matcher matcher;
        WordFeature feature = position < sentence.size() ? sentence.get(position) : null;
        String word = feature == null ? STOP_WORD : feature.getWord();
        Boolean START_STOP_INCLUDED = sentence.get(0).getWord().equals(START_WORD) ? true : false;
        /***Features of word***/

        if (word.equals(START_WORD) || word.equals(STOP_WORD)) {
            features.add(new Pair("POS_TAG", START_TAG));
            features.add(new Pair("NE_TAG", START_TAG));
            return features;
        }

        if (word.equals(STOP_WORD)) {
            features.add(new Pair("POS_TAG", STOP_TAG));
            features.add(new Pair("NE_TAG", STOP_TAG));
            return features;
        }


        //first word in sentence
        if (feature.getId() == 1)
            features.add(new Pair("FIRST_WORD", true));

        //word itself
        features.add(new Pair("WORD", word));

        //previous word
        if (position > 0) {
            features.add(new Pair("i-1_WORD", sentence.get(position - 1).getWord()));
        }

        //pre previous word
        if (position - 1 > 0) {
            features.add(new Pair("i-2_WORD", sentence.get(position - 2).getWord()));
        }

        //following word
        if (position < sentence.size() - 1) {
            features.add(new Pair("i+1_WORD", sentence.get(position + 1).getWord()));
        }

        //word after following word
        if (position < sentence.size() - 2) {
            features.add(new Pair("i+2_WORD", sentence.get(position + 2).getWord()));
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
            Pair<String, String> punct = new Pair("PUCT", "ALLPUNC");
            features.add(punct);
        } else {
            pattern = Pattern.compile("[,.!:\\'\\\"&]");
            matcher = pattern.matcher(word);
            //contains punctuation other than ~
            if (word.length() > 1 && matcher.find()) {
                Pair<String, String> punct = new Pair("PUCT", "PUNCTINWORD");
                features.add(punct);
            }
        }

        //inline period
        pattern = Pattern.compile("[a-z][.][a-z]");
        matcher = pattern.matcher(word);
        Pair<String, Boolean> inline_period;
        if (matcher.find())
            inline_period = new Pair("INLINE_PERIOD", true);
        else
            inline_period = new Pair("INLINE_PERIOD", false);
        features.add(inline_period);

        //Hashtag
        Pair<String, Boolean> hashTag;
        if (word.charAt(0) == '#')
            hashTag = new Pair("HASHTAG", true);
        else
            hashTag = new Pair("HASHTAG", false);
        features.add(hashTag);

        //Twitter Handle
        Pair<String, Boolean> handle;
        if (word.charAt(0) == '@')
            handle = new Pair("HANDLE", true);
        else
            handle = new Pair("HANDLE", false);
        features.add(handle);


        //URL
        pattern = Pattern.compile("^[(www)|(http)]");
        matcher = pattern.matcher(word);
        Pair<String, Boolean> url;
        if (matcher.find())
            url = new Pair("URL", true);
        else
            url = new Pair("URL", false);

        features.add(url);

        //Retweet
        Pair<String, Boolean> retweet;
        if (word.startsWith("RT"))
            retweet = new Pair("RT", true);
        else
            retweet = new Pair("RT", false);

        features.add(retweet);


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
        Pair<String, Boolean> title;
        if (matcher.find())
            title = new Pair("TITLE", true);
        else
            title = new Pair("TITLE", false);
        features.add(title);


        //Mixcase
        pattern = Pattern.compile("^[a-zA-Z]*(?:[a-z][A-Z]|[A-Z][a-z])[a-zA-Z]*");
        matcher = pattern.matcher(word);
        Pair<String, Boolean> mixcase;
        if (matcher.find()) {
            mixcase = new Pair("MIXCASE", true);
        } else
            mixcase = new Pair("MIXCASE", false);

        features.add(mixcase);

        //Initials
        pattern = Pattern.compile("^(?:[A-Z]{1}\\.{1}|[A-Z]{1}\\.{1}[A-Z]{1})+$");
        matcher = pattern.matcher(word);
        Pair<String, Boolean> initials;
        if (matcher.find())
            initials = new Pair("INITIALS", true);
        else
            initials = new Pair("INITIALS", false);

        features.add(initials);


        //think more about Caps, all uppercase, mix, but what about words like INC. or U.K.


        /***Features of Labels***/

        /**Dependency Features**/
        //word is a root
        Pair<String, Boolean> root;
        if (feature.getHead() == 0)
            root = new Pair("ROOT", true);
        else
            root = new Pair("ROOT", false);
        features.add(root);

        //word not included in dependency tree
        Pair<String, Boolean> excluded;
        if (feature.getHead() == -1)
            excluded = new Pair("EXCLUDED", true);
        else
            excluded = new Pair("EXCLUDED", false);

        features.add(excluded);

        //Proper Noun
        Pair<String, Boolean> properNoun;
        if (feature.getPosTag().equals("^")) {
            properNoun = new Pair("PROPER_NOUN", true);
        } else
            properNoun = new Pair("PROPER_NOUN", false);

        features.add(properNoun);

        //Noun
        Pair<String, Boolean> noun;
        if (feature.getPosTag().equals("N")) {
            noun = new Pair("NOUN", true);
        } else
            noun = new Pair("NOUN", false);

        features.add(noun);

        //Dependency Tree POS relation
        features.add(new Pair("POS_TAG", feature.getPosTag()));

        //POSTag of head

        if (feature.getHead() != -1 && feature.getHead() != 0) {
            if (START_STOP_INCLUDED)
                features.add(new Pair("POS_TAG_HEAD", sentence.get(feature.getHead()).getPosTag()));
            else
                features.add(new Pair("POS_TAG_HEAD", sentence.get(feature.getHead() - 1).getPosTag()));
        }

        //MWE
        Pair<String, Boolean> mwe;
        if(feature.getDepRelationship().equals("MWE") && (feature.getPosTag().equals("^") || feature.getPosTag().equals("N"))) {
            mwe = new Pair("MWE_WITH_NOUN", true);
        } else {
            mwe = new Pair("MWE_WITH_NOUN", false);
        }
        features.add(mwe);

        //need to possibly include prev head or id in extractFeatures function or would have to parse through whole tree


        //CONJ
        Pair<String, Boolean> conj;
        if(feature.getDepRelationship().equals("CONJ") && (feature.getPosTag().equals("^") || feature.getPosTag().equals("N"))) {
            conj = new Pair("CONJ_WITH_NOUN", true);
        } else {
            conj = new Pair("CONJ_WITH_NOUN", false);
        }
        features.add(conj);




        /** NE for training**/
        /*if (train) {
            //prevtag=='O' and current NE label is 'O'
            if ((position > 0 && sentence.get(position - 1).getNeTag().toUpperCase().equals("O")) && feature.getNeTag().toUpperCase().equals("O")) {
                Pair<String, String> ne_prev_ne_curr = new Pair("NE_PREV+NE_CUR", "O");
                features.add(ne_prev_ne_curr);
            }

            if (position > 0) {
                //i-1 NE tag
                Pair<String, String> ne_prev = new Pair("NE_PREV", sentence.get(position - 1).getNeTag().toUpperCase());
                features.add(ne_prev);

                //i-1 NE tag + word
                Pair<String, String> ne_prev_word = new Pair("NE_PREV+WORD", sentence.get(position - 1).getNeTag().toUpperCase() + "_" + word);
                features.add(ne_prev_word);
            }

            if (position - 1 > 0) {
                //i-2 NE tag
                Pair<String, String> ne_prev = new Pair("NE_PREPREV", sentence.get(position - 2).getNeTag().toUpperCase());
                features.add(ne_prev);

                //i-1 NE tag + i-2 NE tag
                Pair<String, String> ne_prePrev = new Pair("NE_PRE+NE_PREV", sentence.get(position - 1).getNeTag().toUpperCase() + "_" + sentence.get(position - 2).getNeTag().toUpperCase());
                features.add(ne_prePrev);
            }

            *//**Relations**//*
            //beginning of NE group
            if (feature.getNeTag().toUpperCase().equals("B")) {
                features.add(new Pair("BEGIN_NE_GROUP", true));
            }

            //end of NE group
            boolean NE_END = true;
            if (!feature.getNeTag().toUpperCase().equals("I")) {
                NE_END = false;
            } else if (position < sentence.size() - 1 && sentence.get(position + 1).getNeTag().toUpperCase().equals("I")) {
                NE_END = false;
            }

            if (NE_END) {
                features.add(new Pair("END_NE_GROUP", true));
            }

            //in between NE group
            boolean NE_BTWN = true;
            if (!feature.getNeTag().toUpperCase().equals("I")) {
                NE_BTWN = false;
            } else if (position == sentence.size() - 1 || position == 0) {
                NE_BTWN = false;
            } else if (!sentence.get(position + 1).getNeTag().toUpperCase().equals("I")) {
                NE_BTWN = false;
            }

            if (NE_BTWN) {
                features.add(new Pair("BTWN_NE_GROUP", true));
            }

            //POS tag of word combined with NE tag of preceding word
            if (position > 0) {
                features.add(new Pair("POS_TAG+NE_PREV", word + "_" + sentence.get(position - 1).getNeTag()));
            }

        }*/


        /**TRIGRAM POS TAG **/
        //previous pos tag
        /*if (position > 0) {
            features.add(new Pair("i-1_POS_TAG", sentence.get(position - 1).getPosTag()));
        }

        //pre previous pos tag
        if (position - 1 > 0) {
            features.add(new Pair("i-2_POS_TAG", sentence.get(position - 2).getPosTag()));
        }

        //word + POS Tag of following word
        if (position < sentence.size() - 1) {
            features.add(new Pair("WORD+POS_TAG_+1", word + "_" + sentence.get(position + 1).getPosTag()));
        }
*/

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

    /*tokens = r"""(?x)      # set flag to allow verbose regexps "
     http://[^ ]+       #urls
   | \@[^ ]+            # Twitter usernames
   | \#[^ ]+            # Twitter hashtags
   | [A-Z]([A-Z]|\.|&)+        # abbreviations, e.g. U.S.A., AT&T
   | \w+(-\w+)*        # words with optional internal hyphens
   | \$?\d+(\.\d+)?%?  # currency and percentages, e.g. $12.40, 82%
   | \.\.\.            # ellipsis
   | \'s                # various things
   | \'t
   | n\'t
   | [][.,;"'?():-_`]  # these are separate tokens*/

}
