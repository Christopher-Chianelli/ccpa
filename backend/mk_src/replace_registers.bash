#!/bin/bash
#
# backend/mk_src/replace_registers.bash
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
