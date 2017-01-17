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
    pattern = re.compile(r'J\[(.*?)\]\n')
    for m in re.finditer(pattern, data):
        label = m.group(1)
        labelIndex = lines.index(label)
        jumpIndex = findIndex(oldLines,m.start())
        difference = labelIndex - jumpIndex - 1
        if difference >= 0:
            lines[jumpIndex] = "CF+" + str(difference)
        else:
            lines[jumpIndex] = "CB+" + str(-difference)

    f = open(sys.argv[1], "w")
    for line in lines:
        f.write(line + "\n")
    f.close()
