1. In currenct directory:
Run these commands:
chmod +x install.sh
chmod +x run.sh

2. To install neccessary files in current directory run following commands:
./install.sh

3. To run the models run the following commands:
./run-model.sh <path to train file> <path to test file> <1 if test file is unlabeled, 0 if test file is labeled>

Example:
./run-model.sh ./data/train.txt ./data/DevNoLabels.txt 1

4. The result file will be located in ./data/results/kaggle-results.txt

