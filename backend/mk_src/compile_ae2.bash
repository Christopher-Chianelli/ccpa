#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cat > $SCRIPT_DIR/out/temp
python $SCRIPT_DIR/replace_calls.py $SCRIPT_DIR/out/temp
python $SCRIPT_DIR/replace_jumps.py $SCRIPT_DIR/out/temp
bash $SCRIPT_DIR/replace_registers.bash `cat $SCRIPT_DIR/register_assignment.txt | tr ' ' ':'` < $SCRIPT_DIR/out/temp
