all: cparse

clean:
	rm c_parser.tab.c c_parser.tab.h lex.yy.c cparse

c_parser.tab.c c_parser.tab.h: c_parser.y
	bison -d --verbose c_parser.y

lex.yy.c: c_scanner.lex c_parser.tab.h
	flex c_scanner.lex

cparse: lex.yy.c c_parser.tab.c c_parser.tab.h
	gcc -ll -o cparse c_parser.tab.c lex.yy.c
