
# coding: utf-8

# In[1]:

import sys
from itertools import izip
import HTMLParser
import argparse

TRAIN_FILE = "../data/train.txt"
TEST_FILE = "../data/DevNoLabels.txt"
#TEST_FILE = "../tweebo/inputs/test.nolabels.txt"


# In[5]:

# Setup Twitter input files for TweeboParser

#formated inputs for Tweebo
def process_files_for_tweebo(train_file, test_file):
    tweebo_input_train_file = "./data/train_tweebo_input.txt"
    tweebo_input_test_file = "./data/test_tweebo_input.txt"
    with open(train_file) as train:
        input_lines = train.readlines()
        tweets = []
        arr = []
        for line in input_lines:
            try:
                if line in ['\n', '\r\n']:
                    sentence = ' '.join(str(x) for x in arr) 
                    tweets.append(sentence.strip())
                    arr = []
                else:
                    (word, tag) = line.split()
                    arr.append(word)
            except ValueError:
                continue
        #open(tweebo_input_train_file, 'w').close()
        with open (tweebo_input_train_file, 'w') as f: 
            for tweet in tweets:
                f.write(tweet)
                f.write("\n")

    with open(test_file) as test:
        input_lines = test.readlines()
        tweets = []
        arr = []
        for line in input_lines:
            try:
                if line in ['\n', '\r\n']:
                    sentence = ' '.join(str(x) for x in arr) 
                    tweets.append(sentence.strip())
                    arr = []
                else:
                    word = line.strip()
                    arr.append(word)
            except ValueError:
                continue

        #open(tweebo_input_test_file, 'w').close()
        with open (tweebo_input_test_file, 'w') as f: 
            for tweet in tweets:
                f.write(tweet)
                f.write("\n")            


# In[2]:

# Setup Train/Test Tweebo files to extract features

#train_tweebo_file = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/inputs/train_input.txt.predict"
#test_tweebo_file = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/inputs/tweebo_test.nolabels.txt.predict"
#file_to_train = test_file
#file_to_train_tweebo = test_tweebo_file
#result_file = "C:/Users/User/Documents/Cornell/Courses/NLP/HW4/tweebo/inputs/test.nolabels.processed.txt"

def process_tweebo_files(kaggle, file_to_train, file_to_train_tweebo, result_file):
    with open(file_to_train) as train:
        input_lines = train.readlines()
        labeled_tweets = []
        arr = []
        for line in input_lines:
            try:
                if line in ['\n', '\r\n']:
                    labeled_tweets.append(arr)
                    arr = []
                else:
                    if not kaggle:
                        (word, tag) = line.split()
                        arr.append((word, tag))
                    else:
                        arr.append([line.strip()])

            except ValueError:
                continue

    with open(file_to_train_tweebo) as train_tweebo:
        input_lines = train_tweebo.readlines()
        tweebo_tweets = []
        arr = []
        for line in input_lines:
            try:
                if line in ['\n', '\r\n']:
                    tweebo_tweets.append(arr)
                    arr = []
                else:
                    (word_id, word, lemma, cpostag, postag, feats, head, dep_relation) = line.split()
                    arr.append((word, word_id, postag, head, dep_relation));
            except ValueError:
                continue

    features = []

    for s1, s2 in zip(labeled_tweets, tweebo_tweets):
        tweebo_index = 0
        sentence = []
        head_diff = 0
        head_point = 0
        head_point_arr = []
        head_diff_arr = []
        for i in range(len(s1)):
            word1 = s1[i][0]
            escaped_word1 = HTMLParser.HTMLParser().unescape(s1[i][0]) 
            word2 = HTMLParser.HTMLParser().unescape(s2[tweebo_index][0]) 
            t_ind = tweebo_index
            if(word1 != word2 and escaped_word1 != word2):
                head_point = i+1
                head_point_arr.append(head_point)
                while True:
                    if(word2 == word1 or word2 == escaped_word1):
                        if((int(s2[tweebo_index][3]) != -1 and t_ind != tweebo_index)):
                            t_ind = tweebo_index
                        break;
                    else:
                        head_diff += 1
                        if((int(s2[tweebo_index][3]) != -1 and t_ind != tweebo_index)):
                            t_ind = tweebo_index
                        tweebo_index += 1
                        word2 += HTMLParser.HTMLParser().unescape(s2[tweebo_index][0])
                head_diff_arr.append(head_diff)

            if not kaggle:
                sentence.append([i+1, escaped_word1, s1[i][1], s2[t_ind][2], int(s2[t_ind][3]), s2[t_ind][4]])
            else:
                sentence.append([i+1, escaped_word1, s2[t_ind][2], int(s2[t_ind][3]), s2[t_ind][4]])
            tweebo_index += 1
        #head indexes reconciliation
        #print head_point_arr
        if len(head_diff_arr) > 0:
            #print sentence
            for word in sentence:
                if not kaggle:
                    old_head = word[4]
                else:
                    old_head = word[3]
                if(old_head == -1 or old_head == 0):
                    continue

                start = old_head - (len(s2) - len(s1)) - 2
                end = old_head + (len(s2) - len(s1))

                if(start < 0):
                    start = 0
                if(end > len(sentence)):
                    end = len(sentence)

                old_word = s2[old_head-1][0]

                if(old_head < len(s2)):
                    next_word = s2[old_head][0]
                else:
                    next_word = ''

                old_word_next = old_word + next_word

                if(old_head > 0):
                    prev_word = s2[old_head-2][0]
                else:
                    prev_word = ''

                prev_old_word = prev_word + old_word

                new_word = ''

                for i in xrange(start, end):
                    if(HTMLParser.HTMLParser().unescape(old_word) == sentence[i][1]                        or HTMLParser.HTMLParser().unescape(old_word_next) in sentence[i][1]                      or HTMLParser.HTMLParser().unescape(prev_old_word) in sentence[i][1]):                    
                        new_word = sentence[i]
                        break
                if not kaggle:
                    word[4] = new_word[0]
                else:
                    word[3] = new_word[0]

        features.append(sentence)

    open(result_file, 'w').close()
    with open (result_file, 'w') as f: 
        for tweet in features:
            for word in tweet:
                f.write(' '.join(str(x) for x in word).strip())
                f.write("\n")
            f.write("\n")


# In[ ]:

def main(argv):    
    parser = argparse.ArgumentParser()
    parser.add_argument('-train', help='Train File Path', dest='train', required=True)
    parser.add_argument('-test', help='Test File Path', dest='test', required=True)
    parser.add_argument('-train-tweebo', help='Tweebo-processed Train File Path', dest='train_tweebo')
    parser.add_argument('-test-tweebo', help='Tweebo-processed Test File Path', dest='test_tweebo')
    parser.add_argument('-prep-tweebo', help='1 = Prep Files to use for Tweebo Parser, 0 = Do not prep files', dest='prep_tweebo', default=0, type=int)
    parser.add_argument('-parse-tweebo-train', help='1 = Parse Tweebo Train Files, 0 = Do not parse file', dest='parse_tweebo_train', default=0, type=int)
    parser.add_argument('-parse-tweebo-test', help='1 = Parse Tweebo Test Files, 0 = Do not parse file', dest='parse_tweebo_test', default=0, type=int)
    parser.add_argument('-kaggle-data', help='1 = Parsing test data for Kaggle, 0 = test data not for Kaggle', dest='kaggle', default=0, type=int)

    args = parser.parse_args()
    
    if bool(args.prep_tweebo):
        process_files_for_tweebo(args.train, args.test)
    if bool(args.parse_tweebo_train):
        result_file = './data/train-tweebo.processed.txt'
        process_tweebo_files(args.kaggle, args.train, args.train_tweebo, result_file)
    if bool(args.parse_tweebo_test):
        result_file = './data/test-tweebo.processed.txt'
        process_tweebo_files(args.kaggle, args.test, args.test_tweebo, result_file)
        
    
             
if __name__ == '__main__':
    main(sys.argv[1:])
    
    

