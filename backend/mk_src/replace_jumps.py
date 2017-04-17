#!/usr/bin/env python3
#
# backend/mk_src/replace_jumps.py
# Copyright (C) 2017 Christopher Chianelli
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#
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
