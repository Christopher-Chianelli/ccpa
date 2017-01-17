#!/bin/bash
#Usage: cat file | bash translate_code "key1 value1" "key2 value2"... > out
lookup=()
index=0

#Get register assignments
for i in $@
do
    i=`echo $i | tr ':' ' '`
    key=`echo $i | sed -e 's/ .*$//'`
    keylength=${#key}
    keylength=$((keylength + 2))
    arglength=${#i}
    value=`echo $i | cut -c$keylength-$arglength`
    lookup[$index]="$key:$value"
    index=$((index + 1))
done

data=$(cat)

#Replace registers
for pair in "${lookup[@]}" ; do
    KEY="${pair%%:*}"
    VALUE="${pair##*:}"
    data=`echo "$data" |sed -e "s/\\[$KEY\\]/$VALUE/g"`
done

echo "$data"
