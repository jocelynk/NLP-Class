ROOT_DIR="$( cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PYTHON_DIR="${ROOT_DIR}/Python"
TWEEBO_DIR="${ROOT_DIR}/TweeboParser"
LUCENE_DIR="${ROOT_DIR}/lucene_files"
LUCENE_INDEXER="${ROOT_DIR}/lucene_indexer"
DATA_DIR="${ROOT_DIR}/data"

TRAIN_FILE=$1
TEST_FILE=$2
KAGGLE_DATA=$3

TWEEBO_TRAIN="./data/train_tweebo_input.txt"
TWEEBO_TEST="./data/test_tweebo_input.txt"

# Setup input files for TweeboParser
python ${PYTHON_DIR}/file_setup.py -train ${TRAIN_FILE} -test ${TEST_FILE} -prep-tweebo 1

# Get Dependency Trees from TweeboParser
${TWEEBO_DIR}/run.sh ${TWEEBO_TRAIN}
${TWEEBO_DIR}/run.sh ${TWEEBO_TEST}

# Combine TweeboParser files with original files
python ${PYTHON_DIR}/file_setup.py -train ${TRAIN_FILE} -test ${TEST_FILE} -train-tweebo ${TWEEBO_TRAIN}.predict -parse-tweebo-train 1
python ${PYTHON_DIR}/file_setup.py -train ${TRAIN_FILE} -test ${TEST_FILE} -test-tweebo ${TWEEBO_TEST}.predict -parse-tweebo-test 1 -kaggle-data ${KAGGLE_DATA}

rm -rfv ${LUCENE_INDEXER} && mkdir ${LUCENE_INDEXER}

cd "${ROOT_DIR}/Code"
TWEEBO_TRAIN="${DATA_DIR}/train-tweebo.processed.txt"
TWEEBO_TEST="${DATA_DIR}/test-tweebo.processed.txt"
FEATURES_TRAIN="${DATA_DIR}/features/train-features.txt"
FEATURES_TEST="${DATA_DIR}/features/test-features.txt"
MODEL_FILE="${DATA_DIR}/models/necrf-model"
RESULT_FILE="${DATA_DIR}/results/kaggle-results.txt"

# Extract Features
if [ ${KAGGLE_DATA} -eq 1 ]
then
  java -mx5000m -cp "*" edu.cornell.cs.nlp.assignments.NamedEntityExtractor -train ${TWEEBO_TRAIN} -test ${TWEEBO_TEST} -train-output ${FEATURES_TRAIN} -test-output ${FEATURES_TEST} -lucene-files ${LUCENE_DIR} -lucene-index ${LUCENE_INDEXER} -init-lucene-indexes -kaggle
else
  java -mx5000m -cp "*" edu.cornell.cs.nlp.assignments.NamedEntityExtractor -train ${TWEEBO_TRAIN} -test ${TWEEBO_TEST} -train-output ${FEATURES_TRAIN} -test-output ${FEATURES_TEST} -lucene-files ${LUCENE_DIR} -lucene-index ${LUCENE_INDEXER} -init-lucene-indexes
fi

cd "${ROOT_DIR}"
# Run CRFTagger to get results
java -mx5000m -cp "*" cc.mallet.fst.SimpleTagger --train true --model-file ${MODEL_FILE} --result-file ${RESULT_FILE} ${FEATURES_TRAIN}
java -mx5000m -cp "*" cc.mallet.fst.SimpleTagger --train false --model-file ${MODEL_FILE} --result-file ${RESULT_FILE} ${FEATURES_TEST}
