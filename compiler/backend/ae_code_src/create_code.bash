#!/bin/bash
num=1023
echo "-"
    echo "N[T1] 512"
    echo "L[MEMADD]"
    echo "L[T1]"
    echo "CF?4348"
    echo "S[MEMADD]"
    for b in `seq 2`#256
    do
        echo "N[T1] 256"
        echo "L[MEMADD]"
        echo "L[T1]"
        echo "CF?2172"
        echo "S[MEMADD]"
        for c in `seq 2`#128
        do
            echo "N[T1] 128"
            echo "L[MEMADD]"
            echo "L[T1]"
            echo "CF?1084"
            echo "S[MEMADD]"
            for d in `seq 2`#64
            do
                echo "N[T1] 64"
                echo "L[MEMADD]"
                echo "L[T1]"
                echo "CF?540"
                echo "S[MEMADD]"
                for e in `seq 2`#32
                do
                    echo "N[T1] 32"
                    echo "L[MEMADD]"
                    echo "L[T1]"
                    echo "CF?268"
                    echo "S[MEMADD]"
                    for f in `seq 2`#16
                    do
                        echo "N[T1] 16"
                        echo "L[MEMADD]"
                        echo "L[T1]"
                        echo "CF?132"
                        echo "S[MEMADD]"
                        for g in `seq 2`#8
                        do
                            echo "N[T1] 8"
                            echo "L[MEMADD]"
                            echo "L[T1]"
                            echo "CF?64"
                            echo "S[MEMADD]"
                            for h in `seq 2`#4
                            do
                                echo "N[T1] 4"
                                echo "L[MEMADD]"
                                echo "L[T1]"
                                echo "CF?30"
                                echo "S[MEMADD]"
                                for i in `seq 2`#2
                                do
                                    echo "N[T1] 2"
                                    echo "L[MEMADD]"
                                    echo "L[T1]"
                                    echo "CF?12"
                                    echo "S[MEMADD]"
                                    for j in `seq 2`
                                    do
                                        echo "N[T1] 1"
                                        echo "L[MEMADD]"
                                        echo "L[T1]"
                                        echo "CF?4"
                                        echo "L$num"
                                        echo "L[ZERO]"
                                        echo "S[OUT]"
                                        echo "J[.return]"
                                        num=$((num - 1))
                                        echo "L$num"
                                        echo "L[ZERO]"
                                        echo "S[OUT]"
                                        echo "J[.return]"
                                        num=$((num - 1))
                                    done
                                done
                            done
                        done
                    done
                done
            done
        done
    done
