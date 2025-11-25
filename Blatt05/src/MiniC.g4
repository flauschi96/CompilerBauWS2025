grammar MiniC;

// Parser rules
program : stmt+ EOF ;

stmt
  : vardecl
  | assign
  | fndecl
  | expr ';'
  | block
  | whileStmt
  | cond
  | returnStmt
  ;

vardecl : type ID ('=' expr)? ';' ;
assign  : ID '=' expr ';' ;

fndecl  : type ID '(' params? ')' block ;
params  : type ID (',' type ID)* ;
returnStmt  : 'return' expr ';' ;

fncall  : ID '(' args? ')' ;
args    : expr (',' expr)* ;

block   : '{' stmt* '}' ;
whileStmt   : 'while' '(' expr ')' block ;
cond    : 'if' '(' expr ')' block ('else' block)? ;

expr
  : fncall
  | expr ('*' | '/') expr
  | expr ('+' | '-') expr
  | expr ('>' | '<') expr
  | expr ('==' | '!=') expr
  | ID
  | NUMBER
  | STRING
  | 'T'
  | 'F'
  | '(' expr ')'
  ;

type : 'int' | 'string' | 'bool' ;

// Lexer rules
ID      : [a-zA-Z] [a-zA-Z0-9]* ;
NUMBER  : [0-9]+ ;
STRING  : '"' (~[\n\r"])* '"' ;

COMMENT : '#' ~[\n\r]* -> skip ;
WS      : [ \t\n\r]+   -> skip ;
