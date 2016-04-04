import sys
from tageval import  convert_bio_to_spans

def getTag(line):
     if line.strip():
          fields = line.rstrip().split("-")
          return fields[0]
     else:
          return "O"
    
def main():
    owplFile = sys.argv[1]
    lines = open(owplFile).readlines()
    tags = map(getTag,lines)
    chunks = convert_bio_to_spans(tags)
    print "Id,Prediction"
    count = 0
    sys.stdout.write("0,")
    for c in chunks:
        sys.stdout.write(" %s-%d-%d" % c)
    sys.stdout.write("\n")    


#            fields = line.rstrip().split("\t")
#            token = fields[0]
#            return fields[1]
if __name__ == "__main__":
    main()
