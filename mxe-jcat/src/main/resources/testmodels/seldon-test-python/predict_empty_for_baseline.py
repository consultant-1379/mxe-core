import sys
import getopt

import time
from SeldonPythonReferenceModelV1 import SeldonPythonReferenceModelV1



def test_prediction(count):
    secondsSinceEpoch = time.time()
    for i in range(count):
        w = SeldonPythonReferenceModelV1()
        w.predict([i], "test")
    print("Number of prediction(s): ", count)
    print("Sum prediction time: ", (time.time() - secondsSinceEpoch))


def printHelp():
    print(sys.argv[0], '-c <count>')


if __name__ == "__main__":
    count = 0
    try:
        opts, args = getopt.getopt(sys.argv[1:], "c:", ["count="])
    except getopt.GetoptError:
        printHelp()
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            printHelp()
            sys.exit()
        elif opt in ("-c", "--count"):
            count = int(arg)
    if (count == 0):
        printHelp();
        sys.exit()
    test_prediction(count)
