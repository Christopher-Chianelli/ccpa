#!/bin/bash
#
# install.bash - Installs and compiles ccpa
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
if [ $# -ne 1 ]
then
    echo "Usage: install INSTALL_DIRECTORY"
    exit 1
fi

if [ -d $1 ]
then
    cp -R . $1

    file_contents=$(<ccpa)
    echo "${file_contents//-PATH-/$1}" > $1/ccpa
    file_contents=$(<backend/mk_src/compile_ae2.bash)
    echo "${file_contents//-PATH-/$1/backend/mk_src}" > $1/backend/mk_src/compile_ae2.bash

    mkdir $1/temp
    mkdir $1/backend/mk_src/out
    mkdir $1/backend/treeToAE2/bin
    mkdir $1/optimization/bin

    cd $1/scanner
    make
    cd $1/aesource
    make
    cd $1/backend/treeToAE2
    ant
    cd $1/optimization
    ant
else
    if [ -e $1 ]
    then
        echo "$1 is not a directory"
    else
        echo "$1 does not exist"
    fi
fi
