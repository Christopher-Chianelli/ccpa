#!/bin/bash
cat $1.ae2 > out/$1.ae
for i in grep -E "D[\\._(memget)]"
do
    cat ../ae_code_src/memget.ae2 >> out/$1.ae
done
python replace_calls.py out/$1.ae
python replace_jumps.py out/$1.ae
(bash replace_registers.bash `cat register_assignment.txt | tr ' ' ':'` < out/$1.ae) > out/$1.txt
