
# coding: utf-8

# In[1]:

import sys
import argparse


# In[27]:

def get_dependency_data(labeled_data, result_data, dep_tree): #Get dependency data
    with open(labeled_data) as t:
        input_lines = t.readlines()
        labeled_temp = []
        lbl = []
        arr = []
        arr2 = []
        for line in input_lines:
            try:
                if line in ['\n', '\r\n']:
                    sentence = ' '.join(str(x) for x in arr) 
                    labeled_temp.append(sentence.strip())
                    lbl.append(arr2)
                    arr = []
                    arr2 = []
                else:
                    (word, tag) = line.strip().split()
                    arr.append(word)
                    arr2.append((word, tag))
            except ValueError:
                continue

    with open(result_data) as t:
        input_lines = t.readlines()
        result_temp = []
        arr = []
        for line in input_lines:
            try:
                if line in ['\n', '\r\n']:
                    result_temp.append(arr)
                    arr = []
                else:
                    tag = line.strip()
                    arr.append(tag)
            except ValueError:
                continue

    with open(dep_tree) as t:
        input_lines = t.readlines()
        tree_temp = []
        tree = []
        arr = []
        arr2 = []
        for line in input_lines:
            try:
                if line in ['\n', '\r\n']:
                    sentence = ' '.join(str(x) for x in arr) 
                    tree_temp.append(sentence.strip())
                    tree.append(arr2)
                    arr = []
                    arr2 = []
                else:
                    node = line.strip().split()
                    arr.append(node[1])
                    arr2.append(node)
            except ValueError:
                continue

    labeled_indexes = []

    for line in labeled_temp:
        ind = tree_temp.index(line)
        labeled_indexes.append(ind)
        
    return lbl, labeled_indexes, tree, result_temp
                


# In[37]:

def get_error_analysis(lbl, labeled_indexes, tree, result_temp):
    tweets = []
    arr = []
    ind = 0
    match = 1
    for t in range(len(lbl)):
        node = tree[labeled_indexes[ind]]
        for t1, t2, t3 in zip(lbl[t], result_temp[t], node):
            (word, tag) = t1
            guess = t2
            (w_id, word, tag2, pos_tag, head, rel) = t3
            if tag != guess.strip():
                match = 0
            arr.append((w_id, word, pos_tag, head, rel, tag, guess.strip()))
        ind += 1

        if not match:
            tweets.append(arr)
        match = 1
        arr = []

    return tweets


# In[38]:

def write_error_analysis_file(file_name, tweets):
    open(file_name, 'w').close()
    with open (file_name, 'w') as f: 
        for tweet in tweets:
            for word in tweet:
                f.write(word[0] + "\t" + word[1] + "\t\t" + word[2] + "\t" +word[3] + "\t" + word[4] + "\t" + word[5] + "\t" + word[6] +"\n")
            f.write("\n")


# In[ ]:

def main(argv):    
    parser = argparse.ArgumentParser()
    parser.add_argument('-labeled-data', help='Path to labeled data', dest='lbl', required=True)
    parser.add_argument('-guessed-data', help='Path to guessed tags', dest='guess', required=True)
    parser.add_argument('-dep-tree-data', help='Path fo dependency tree file', dest='dep', required=True)
    parser.add_argument('-output-file', help='Path to error analysis output file', dest='output', required=True)
    
    args = parser.parse_args()
    
    lbl, labeled_indexes, tree, result_temp = get_dependency_data(args.lbl, args.guess, args.dep)
    tweets = get_error_analysis(lbl, labeled_indexes, tree, result_temp)
    write_error_analysis_file(args.output, tweets)
        
             
if __name__ == '__main__':
    main(sys.argv[1:])
