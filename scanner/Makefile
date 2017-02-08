all: cparse treegen

clean:
	rm c_parser.tab.c c_parser.tab.h lex.yy.c cparse treegen

c_parser.tab.c c_parser.tab.h: c_parser.y
	bison -d c_parser.y

lex.yy.c: c_scanner.lex c_parser.tab.h
	flex c_scanner.lex

cparse: lex.yy.c c_parser.tab.c c_parser.tab.h cparse.c
	gcc -ll -o cparse -g c_parser.tab.c lex.yy.c cparse.c

treegen: treegen.c cparse.c
	gcc -o treegen -g treegen.c cparse.c
