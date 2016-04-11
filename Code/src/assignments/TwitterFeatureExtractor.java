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

    //Todo: Search in database for movies, names, music, etc (Companies, products, brands, people, locations )
    //Todo: Days of week, month?
    //Todo: create file where emoticons aren't separate entities
    //Todo: model dependencies
    //Todo: Number of inbound links
    //Todo:


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

        //Abbreviations
        pattern = Pattern.compile("[A-Z]([A-Z]|\\.|&)+");
        matcher = pattern.matcher(word);
        if (matcher.find())
            features.add(new Pair("ABBREVIATIONS", true));
        else
            features.add(new Pair("ABBREVIATIONS", false));

        //Words with optional internal hyphens
        pattern = Pattern.compile("\\w+(-\\w+)*");
        matcher = pattern.matcher(word);
        if (matcher.find())
            features.add(new Pair("INTERNAL_HYPHEN", true));
        else
            features.add(new Pair("INTERNAL_HYPHEN", false));

        //Currency and Percentages e.g. $12.40, 82%
        pattern = Pattern.compile("\\$?\\d+(\\.\\d+)?%?");
        matcher = pattern.matcher(word);
        if (matcher.find())
            features.add(new Pair("CURRENCY_PER", true));
        else
            features.add(new Pair("CURRENCY_PER", false));

        //Ellipsis
        pattern = Pattern.compile("\\.\\.\\. ");
        matcher = pattern.matcher(word);
        if (matcher.find())
            features.add(new Pair("ELLIPSIS", true));
        else
            features.add(new Pair("ELLIPSIS", false));

        //inline period
        pattern = Pattern.compile("[a-z][.][a-z]");
        matcher = pattern.matcher(word);
        if (matcher.find())
            features.add(new Pair("INLINE_PERIOD", true));
        else
            features.add(new Pair("INLINE_PERIOD", false));


        //Hashtag
        if (word.charAt(0) == '#')
            features.add(new Pair("HASHTAG", true));
        else
            features.add(new Pair("HASHTAG", false));

        //Twitter Handle
        if (word.charAt(0) == '@')
            features.add(new Pair("HANDLE", true));
        else
            features.add(new Pair("HANDLE", false));

        //URL
        pattern = Pattern.compile("^[(www)|(http)]");
        matcher = pattern.matcher(word);
        if (matcher.find())
            features.add(new Pair("URL", true));
        else
            features.add(new Pair("URL", false));

        //Retweet
        Pair<String, Boolean> retweet;
        if (word.startsWith("RT"))
            features.add(new Pair("RT", true));
        else
            features.add(new Pair("RT", false));

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
        if (matcher.find())
            features.add(new Pair("TITLE", true));
        else
            features.add(new Pair("TITLE", false));

        //Mixcase
        pattern = Pattern.compile("^[a-zA-Z]*(?:[a-z][A-Z]|[A-Z][a-z])[a-zA-Z]*");
        matcher = pattern.matcher(word);
        Pair<String, Boolean> mixcase;
        if (matcher.find()) {
            features.add(new Pair("MIXCASE", true));
        } else
            features.add(new Pair("MIXCASE", false));

        //Initials
        pattern = Pattern.compile("^(?:[A-Z]{1}\\.{1}|[A-Z]{1}\\.{1}[A-Z]{1})+$");
        matcher = pattern.matcher(word);
        Pair<String, Boolean> initials;
        if (matcher.find())
            features.add(new Pair("INITIALS", true));
        else
            features.add(new Pair("INITIALS", false));

        //think more about Caps, all uppercase, mix, but what about words like INC. or U.K.


        /***Features of Labels***/

        /**Dependency Features**/
        //word is a root
        if (feature.getHead() == 0)
            features.add(new Pair("ROOT", true));
        else
            features.add(new Pair("ROOT", false));

        //word not included in dependency tree
        if (feature.getHead() == -1)
            features.add(new Pair("EXCLUDED", true));
        else
            features.add(new Pair("EXCLUDED", false));

        //Proper Noun
        if (feature.getPosTag().equals("^")) {
            features.add(new Pair("PROPER_NOUN", true));
        } else
            features.add(new Pair("PROPER_NOUN", false));

        //Noun
        if (feature.getPosTag().equals("N")) {
            features.add(new Pair("NOUN", true));
        } else
            features.add(new Pair("NOUN", false));

        //Dependency Tree POS relation
        features.add(new Pair("POS_TAG", feature.getPosTag()));

        //POSTag of head
        if (feature.getHead() != -1 && feature.getHead() != 0) {
            if (START_STOP_INCLUDED)
                features.add(new Pair("POS_TAG_HEAD", sentence.get(feature.getHead()).getPosTag()));
            else
                features.add(new Pair("POS_TAG_HEAD", sentence.get(feature.getHead() - 1).getPosTag()));
        }

       /* //Word of head
        if (feature.getHead() != -1 && feature.getHead() != 0) {
            if (START_STOP_INCLUDED)
                features.add(new Pair("POS_TAG_HEAD_WORD", sentence.get(feature.getHead()).getWord()));
            else
                features.add(new Pair("POS_TAG_HEAD_WORD", sentence.get(feature.getHead() - 1).getWord()));
        }*/

        //MWE
        if (feature.getDepRelationship().equals("MWE") && (feature.getPosTag().equals("^") || feature.getPosTag().equals("N"))) {
            features.add(new Pair("MWE_WITH_NOUN", true));
        } else {
            features.add(new Pair("MWE_WITH_NOUN", false));
        }


       /* if(feature.getDepRelationship().equals("MWE") && (feature.getPosTag().equals("^") || feature.getPosTag().equals("N"))) {
            if (START_STOP_INCLUDED) {
                //MWE and head is a Noun or Proper Noun
                if (sentence.get(feature.getHead()).getPosTag().equals("N") || sentence.get(feature.getHead()).getPosTag().equals("^"))
                    features.add(new Pair("MWE_HEAD_NOUN", true));
                else
                    features.add(new Pair("MWE_HEAD_NOUN", false));

               *//* //MWE and head is a Noun or Proper Noun and MWE
                if (sentence.get(feature.getHead()).getDepRelationship().equals("MWE") && (sentence.get(feature.getHead()).getPosTag().equals("N") || sentence.get(feature.getHead()).getPosTag().equals("^")))
                    features.add(new Pair("MWE_HEAD_NOUN_MWE", true));
                else
                    features.add(new Pair("MWE_HEAD_NOUN_MWE", false));*//*
            } else {
                //MWE and head is a Noun or Proper Noun
                if (sentence.get(feature.getHead()-1).getPosTag().equals("N") || sentence.get(feature.getHead()-1).getPosTag().equals("^"))
                    features.add(new Pair("MWE_HEAD_NOUN", true));
                else
                    features.add(new Pair("MWE_HEAD_NOUN", false));

              *//*  //MWE and head is a Noun or Proper Noun and MWE
                if (sentence.get(feature.getHead()-1).getDepRelationship().equals("MWE") && (sentence.get(feature.getHead()-1).getPosTag().equals("N") || sentence.get(feature.getHead()-1).getPosTag().equals("^")))
                    features.add(new Pair("MWE_HEAD_NOUN_MWE", true));
                else
                    features.add(new Pair("MWE_HEAD_NOUN_MWE", false));*//*
            }

            if(position != 0 && sentence.get(position-1).getHead() == feature.getId() && (sentence.get(position-1).getPosTag().equals("N") || sentence.get(position-1).getPosTag().equals("^"))) {
                features.add(new Pair("MWE_PREV_NOUN", true));
            } else {
                features.add(new Pair("MWE_PREV_NOUN", false));
            }
        }*/

        //get Entire MWE

        //need to possibly include prev head or id in extractFeatures function or would have to parse through whole tree


        //CONJ
        Pair<String, Boolean> conj;
        if (feature.getDepRelationship().equals("CONJ") && (feature.getPosTag().equals("^") || feature.getPosTag().equals("N"))) {
            conj = new Pair("CONJ_WITH_NOUN", true);
        } else {
            conj = new Pair("CONJ_WITH_NOUN", false);
        }
        features.add(conj);

        //Entire Dependencies
        //brute force
        int start = 0;
        int end = 0;
        if (START_STOP_INCLUDED) {
            start = 1;
        }

        /*String longestTag = "";
        for (int i = start ; i < sentence.size(); i++) {
            int head = START_STOP_INCLUDED ? sentence.get(i).getHead() : (sentence.get(i).getHead() - 1);
            boolean add = head == feature.getHead()? true : false;
            StringBuilder tag = new StringBuilder();
            tag.append(sentence.get(i).getPosTag() + "_");
            int numWords = 0;
            while (head > 0) {
                if(numWords > sentence.size()) break;
                numWords++;
                if (head == feature.getHead())
                    add = true;
                tag.append(sentence.get(head).getPosTag() + "_");

                if(sentence.get(head).getHead() == 0 || sentence.get(head).getHead() == -1)
                    break;
                head = START_STOP_INCLUDED ? sentence.get(head).getHead() : (sentence.get(head).getHead() - 1);
            }
            if (add) {
                longestTag = tag.toString().trim().length() > longestTag.length() ?  tag.toString().trim() : longestTag;
            }
        }

        if(longestTag.length() > 0) {
            //features.add(new Pair("POS_TAGS", longestTag));
            features.add(new Pair("NUM_EDGES", longestTag.length()));
        }*/

        //Number words between it and root
        //number of edges

        /**TRIGRAM POS TAG **/
        if (position > 0) {
            //previous pos tag
            features.add(new Pair("i-1_POS_TAG", sentence.get(position - 1).getPosTag()));
            //previous pos tag + current pos tag
            features.add(new Pair("i-1_POS_TAG_i_POS_TAG", sentence.get(position - 1).getPosTag() + "_" + sentence.get(position).getPosTag()));
            //previous pos tag + word
            features.add(new Pair("i-1_POS_TAG_i_WORD", sentence.get(position - 1).getPosTag() + "_" + sentence.get(position).getWord()));
        }

        if (position - 1 > 0) {
            //pre previous pos tag
            features.add(new Pair("i-2_POS_TAG", sentence.get(position - 2).getPosTag()));
            //previous pos tag + previous_tag + current pos tag
            features.add(new Pair("i-2_i-1_i_POS_TAG", sentence.get(position - 2).getPosTag() + "_" + sentence.get(position - 1).getPosTag() + "_" + sentence.get(position).getPosTag()));
            //previous pos tag + previous_tag + current word
            features.add(new Pair("i-2_i-1_i_WORD", sentence.get(position - 2).getPosTag() + "_" + sentence.get(position - 1).getPosTag() + "_" + sentence.get(position).getWord()));

        }

        //word + POS Tag of following word
        if (position < sentence.size() - 1) {
            features.add(new Pair("WORD+POS_TAG_+1", word + "_" + sentence.get(position + 1).getPosTag()));
        }


        return features;
    }

    /*

        features.incrementCount("i tag+i-2 tag" + previousTag + prePreviousTag, 1);
        features.incrementCount("i-1 tag+i word" + previousTag + context.get(position), 1);
        //need to make sure to add start and end to context


    /*SMALL = 'a|an|and|as|at|but|by|en|for|if|in|of|on|or|the|to|v\.?|via|vs\.?'
    PUNCT = r"""!"#$%&'�()*+,\-./:;?@[\\\]_`{|}~"""

    SMALL_WORDS = re.compile(r'^(%s)$' % SMALL, re.I)
    INLINE_PERIOD = re.compile(r'[a-z][.][a-z]', re.I)
    UC_ELSEWHERE = re.compile(r'[%s]*?[a-zA-Z]+[A-Z]+?' % PUNCT)
    CAPFIRST = re.compile(r"^[%s]*?([A-Za-z])" % PUNCT)
    SMALL_FIRST = re.compile(r'^([%s]*)(%s)\b' % (PUNCT, SMALL), re.I)
    SMALL_LAST = re.compile(r'\b(%s)[%s]?$' % (SMALL, PUNCT), re.I)
    SUBPHRASE = re.compile(r'([:.;?!\-\�][ ])(%s)' % SMALL)
    APOS_SECOND = re.compile(r"^[dol]{1}['�]{1}[a-z]+(?:['s]{2})?$", re.I)
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
