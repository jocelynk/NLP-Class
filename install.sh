ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Download data files needed

rm ${ROOT_DIR}/lucene_files.tar.gz
curl -L -k --insecure "https://www.dropbox.com/s/iit862a3zqmoi01/lucene_files.tar.gz?dl=0" -o ${ROOT_DIR}/lucene_files.tar.gz
tar xvf lucene_files.tar.gz

rm ${ROOT_DIR}/TweeboParser.tar.gz
curl -L -k --insecure "https://www.dropbox.com/s/ertemrunywp0o5e/TweeboParser.tar.gz?dl=0" -o ${ROOT_DIR}/TweeboParser.tar.gz
tar xvf TweeboParser.tar.gz

TWEEBO_DIR="${ROOT_DIR}/TweeboParser"

# Install the Tweebo Parser
cd ${TWEEBO_DIR}
chmod +x install.sh
cd ..
${TWEEBO_DIR}/install.sh