
# coding: utf-8

# In[124]:
import sys
from itertools import chain
import pycrfsuite
import argparse
import time


# In[128]:

def process_features(train_file, test_file):
    features_train = []
    labels = []
    with open(train_file) as f:
        inputlines = f.readlines()
        arr1 = []
        arr2 = []
        for line in inputlines:
            l = line.strip().split()
            feat = l[:-1]
            label = l[-1]
            arr1.append(feat)
            arr2.append(label)
            if(label == "</S>"):
                features_train.append(arr1)
                labels.append(arr2)
                arr1 = []
                arr2 = []

    features_test = []           
    with open(test_file) as f:
        inputlines = f.readlines()
        arr1 = []
        prev_tag = "POS_TAG=<S>"
        for line in inputlines:
            l = line.strip().split()
            arr1.append(l)

            if(l[0] == "POS_TAG=<S>" and prev_tag != "POS_TAG=<S>"):
                features_test.append(arr1)
                arr1 = []

            prev_tag = l[0]
    return features_train, labels, features_test
    

# In[129]:

def crf_tagger(X_train, Y_train, X_test, model_name, output_file):
    t0 = time.time()
    trainer = pycrfsuite.Trainer(verbose=False)

    for xseq, yseq in zip(X_train, Y_train):
        trainer.append(xseq, yseq)
        
    trainer.set_params({
        'c1': 0,   # coefficient for L1 penalty
        'c2': 1,  # coefficient for L2 penalty
        'max_iterations': 500,  # stop earlier
        # include transitions that are possible, but not observed
        'feature.possible_transitions': True
    })
    #trainer.params()
    
    trainer.train(model_name)
    #!ls -lh ./necrf-suite
    
    tagger = pycrfsuite.Tagger()
    tagger.open(model_name)
    
    with open(output_file, 'w') as f:
        for tweet in X_test:
            guesses = tagger.tag(tweet)
            for tag in guesses:
                if tag != '<S>' and tag != '</S>':
                    f.write(tag + "\n")
            f.write("\n")

    t1 = time.time()
    print 'Completed tagging. Process took', (t1 - t0), 'seconds' 
# In[122]:

def main(argv):    
    parser = argparse.ArgumentParser()
    parser.add_argument('-train', help='Path to features training file', dest='train', required=True)
    parser.add_argument('-test', help='Path to features test file', dest='test', required=True)
    parser.add_argument('-model', help='Path to output model', dest='model', required=True)
    parser.add_argument('-result', help='Path to output results', dest='result', required=True)
    
    args = parser.parse_args()
    
    X_train, Y_train, X_test = process_features(args.train, args.test)
    
    crf_tagger(X_train, Y_train, X_test, args.model, args.result)
    
             
if __name__ == '__main__':
    main(sys.argv[1:])

