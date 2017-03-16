#!/bin/bash
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
