import sys
import re

def findIndex(lines,index):
    i = 0
    lineNum = 0
    while i < index:
        i += len(lines[lineNum]) + 1
        lineNum += 1
    return lineNum

if __name__ == "__main__":
    f = open(sys.argv[1], "r")
    data = f.read() + "\n"
    f.close()
    lines = data.split('\n')
    oldLines = data.split('\n')
    pattern = re.compile(r'D\[(.*?)\]\n')
    returnIndex = 0
    for m in re.finditer(pattern, data):
        label = m.group(1)
        callIndex = findIndex(oldLines,m.start())
        lines[callIndex] = "N[OUT] " + str(returnIndex) + "\n" + \
            "-\n" + \
            "L[STACK_TOP]\n" + \
            "L[ZERO]\n" + \
            "S[T2]\n" + \
            "L[T2]\n" + \
            "L[ONE]\n" + \
            "S[T2]\n" + \
            "CF?5\n" + \
            "<1\n" + \
            "L[OUT]\n" + \
            "L[ZERO]\n" + \
            "S[OUT]\n" + \
            "CB+9\n" + \
            "+\n" + \
            "L[STACK_TOP]\n" + \
            "L[ONE]\n" + \
            "S[STACK_TOP]\n" + \
            "L[STACK]\n" + \
            "L[OUT]\n" + \
            "S[STACK]\n" + \
            "J[" + label + "]\n" + \
            ".return(" + str(returnIndex) + ")"
        returnIndex += 1

    f = open(sys.argv[1], "w")
    for line in lines:
        f.write(line + "\n")

    f.write(".return\n")
    f.write("-\n")
    f.write("L[STACK_TOP]\n")
    f.write("L[ZERO]\n")
    f.write("S[T2]\n")
    f.write("L[STACK]\n")
    f.write("L[ZERO]\n")
    f.write("S[T3]\n")
    f.write("L[T2]\n")
    f.write("L[ONE]\n")
    f.write("S[T2]\n")
    f.write("CF?5\n")
    f.write(">1\n")
    f.write("L[T3]\n")
    f.write("L[ZERO]\n")
    f.write("S[T3]\n")
    f.write("CB+9\n")
    f.write("/\n")
    f.write("L[T3]\n")
    f.write("L[CALL_LIMIT]\n")
    f.write("S[T3]\n")
    f.write("-\n")
    for i in range(returnIndex):
        f.write("L[T3]\n")
        f.write("L[ONE]\n")
        f.write("S[T3]\n")
        f.write("CF?1\n")
        f.write("J[.return(" + str(i) + ")]\n")
    f.close()
