"""
Run tests with:

    py.test -vs tageval.py

py.test is from the pytest package: http://pytest.org/
get it with something like "pip install pytest"
see the website for more info, and/or py.test --help

Convention for spans in this code
start-inclusive, end-exclusive
same as python slicing conventions
"""

from __future__ import division
import re, sys

def warning(msg):
    print>>sys.stderr, "WARNING:", msg

def convert_bio_to_spans(bio_sequence):
    spans = []  # (label, startindex, endindex)
    cur_start = None
    cur_label = None
    N = len(bio_sequence)
    for t in range(N+1):
        if ((cur_start is not None) and
                (t==N or re.search("^[BO]", bio_sequence[t]))):
            assert cur_label is not None
            spans.append((cur_label, cur_start, t))
            cur_start = None
            cur_label = None
        if t==N: continue
        assert bio_sequence[t] and bio_sequence[t][0] in ("B","I","O")
        if bio_sequence[t].startswith("B"):
            cur_start = t
            cur_label = re.sub("^B-?","", bio_sequence[t]).strip()
        if bio_sequence[t].startswith("I"):
            if cur_start is None:
                warning("BIO inconsistency: I without starting B. Rewriting to B.")
                newseq = bio_sequence[:]
                newseq[t] = "B" + newseq[t][1:]
                return convert_bio_to_spans(newseq)
            continuation_label = re.sub("^I-?","",bio_sequence[t])
            if continuation_label != cur_label:
                newseq = bio_sequence[:]
                newseq[t] = "B" + newseq[t][1:]
                warning("BIO inconsistency: %s but current label is '%s'. Rewriting to %s" % (bio_sequence[t], cur_label, newseq[t]))
                return convert_bio_to_spans(newseq)

    # should have exited for last span ending at end by now
    assert cur_start is None
    spancheck(spans)
    return spans

def test_bio_conversion():
    spans = convert_bio_to_spans(["B"])
    assert spans==[("",0,1)]
    spans = convert_bio_to_spans(["B","I"])
    assert spans==[("",0,2)]
    spans = convert_bio_to_spans(["B","I","O"])
    assert spans==[("",0,2)]
    spans = convert_bio_to_spans(["O","B","I","O","O"])
    assert spans==[("",1,3)]
    spans = convert_bio_to_spans(["B","B"])
    assert spans==[("",0,1), ("",1,2)]
    spans = convert_bio_to_spans(["B","I","B"])
    assert spans==[("",0,2), ("",2,3)]
    spans = convert_bio_to_spans(["B-asdf","I-asdf","B"])
    assert spans==[("asdf",0,2), ("",2,3)]
    spans = convert_bio_to_spans(["B-asdf","I-difftype","B"])
    assert spans==[("asdf",0,1), ("difftype",1,2), ("",2,3)]
    spans = convert_bio_to_spans(["I","I"])
    assert spans==[("",0,2)]
    spans = convert_bio_to_spans(["B-a","I-b"])
    assert spans==[("a",0,1), ("b",1,2)]


def spancheck(spanlist):
    s = set(spanlist)
    assert len(s)==len(spanlist), "spans are non-unique ... is this a bug in the eval script?"

def kill_labels(bio_seq):
    ret = []
    for x in bio_seq:
        if re.search("^[BI]", x):
            x = re.sub("^B.*","B", x)
            x = re.sub("^I.*","I", x)
        ret.append(x)
    return ret

def evaluate_taggings(goldseq_predseq_pairs, ignore_labels=False):
    """a list of (goldtags,predtags) pairs.  goldtags and predtags are both lists of strings, of the same length."""
    num_sent = 0
    num_tokens= 0
    num_goldspans = 0
    num_predspans = 0

    tp, fp, fn = 0,0,0

    for goldseq,predseq in goldseq_predseq_pairs:
        N = len(goldseq)
        assert N==len(predseq)
        num_sent += 1
        num_tokens += N

        if ignore_labels:
            goldseq = kill_labels(goldseq)
            predseq = kill_labels(predseq)

        goldspans = convert_bio_to_spans(goldseq)
        predspans = convert_bio_to_spans(predseq)

        num_goldspans += len(goldspans)
        num_predspans += len(predspans)

        goldspans_set = set(goldspans)
        predspans_set = set(predspans)

        tp += len(goldspans_set & predspans_set)
        fp += len(predspans_set - goldspans_set)
        fn += len(goldspans_set - predspans_set)

    prec = tp/(tp+fp) if (tp+fp)>0 else 0
    rec =  tp/(tp+fn) if (tp+fn)>0 else 0
    f1 = 2*prec*rec / (prec + rec)
    print "F = {f1:.4f},  Prec = {prec:.4f} ({tp}/{tpfp}),  Rec = {rec:.4f} ({tp}/{tpfn})".format(
            tpfp=tp+fp, tpfn=tp+fn, **locals())
    print "({num_sent} sentences, {num_tokens} tokens, {num_goldspans} gold spans, {num_predspans} predicted spans)".format(**locals())

def read_tokens_tags_file(filename):
    """Returns list of sentences.  each sentence is a pair (tokens, tags), each
    of which is a list of strings of the same length."""
    sentences = open(filename).read().strip().split("\n\n")
    ret = []
    for sent in sentences:
        sent = sent.strip()
        lines = sent.split("\n")
        pairs = [L.split("\t") for L in lines]
        for pair in pairs:
            assert len(pair)==2, "Was expecting 2 tab-separated items per line."
        tokens = [tok for tok,tag in pairs]
        tags = [tag for tok,tag in pairs]
        ret.append( (tokens,tags) )
    return ret

def read_tags_file(filename):
    sentences = open(filename).read().strip().split("\n\n")
    ret = []
    for sent in sentences:
        sent = sent.strip()
        lines = sent.split("\n")
        for line in lines:
            assert len(line.split())==1, "Was expecting 1 item per line"
        ret.append( [line.strip() for line in lines] )
    return ret

def evaluate_tagging_file(gold_tags_file, predicted_tags_file):
    tokens_and_tags = read_tokens_tags_file(gold_tags_file)
    goldseqs = [tags for tokens,tags in tokens_and_tags]

    # assume predicted_tags_file is the simple crfsuite output format
    # every line is just a tag by itself, blank lines separating sentences
    predtags = read_tags_file(predicted_tags_file)

    # commented out code for a different prediction format
    # tokens_and_tags = read_tokens_tags_file(predicted_tags_file)
    # predtags = [tags for tokens,tags in tokens_and_tags]

    print "Span-level NER evaluation"
    # print "Evaluation including NER types"
    # evaluate_taggings( list(zip(goldseqs, predtags)) )
    # print "Evaluation without types (is the span a name or not?)"
    evaluate_taggings( list(zip(goldseqs, predtags)), ignore_labels=True )


if __name__=='__main__':
    evaluate_tagging_file(sys.argv[1], sys.argv[2])

