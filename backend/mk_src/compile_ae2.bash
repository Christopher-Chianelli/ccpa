#!/bin/bash
#
# backend/mk_src/compile_ae2.bash
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
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cat > $SCRIPT_DIR/out/temp
if [ -s $SCRIPT_DIR/out/temp ]
then
        :
else
        exit 1
fi
tempRegs=`head -n 1 $SCRIPT_DIR/out/temp | sed 's/^.//'`
cat $SCRIPT_DIR/builtin_registers.txt > $SCRIPT_DIR/register_assignment.txt

for ((i=0;i<=$tempRegs;i++)); do
    n=$((979 - $i - 1))
    echo "T$i $n" >> $SCRIPT_DIR/register_assignment.txt
done

$SCRIPT_DIR/replace_calls.py $SCRIPT_DIR/out/temp
$SCRIPT_DIR/replace_jumps.py $SCRIPT_DIR/out/temp
bash $SCRIPT_DIR/replace_registers.bash `cat $SCRIPT_DIR/register_assignment.txt | tr ' ' ':'` < $SCRIPT_DIR/out/temp
