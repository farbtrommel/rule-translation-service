lexer grammar ProvaTokens;

DOT: '.';
PLUS: '+' ;
MINUS: '-' ;
MULT: '*' ;
DIV: '/' ;
COMMA: ',';
IF: ':-';
CUT: '!';
OPEN: '(';
CLOSE: ')';
BAR: '|';
BRA: '[';
KET: ']';
EQUAL: '=';
NOT_EQUAL1: '<>';
NOT_EQUAL2: '!=';
LT: '<';
GT: '>';
LE: '<=';
GE: '>=';
/*
	CLAUSE;
	RULEBASE;
	STATEMENT;
	QUERY;
	LITERAL;
	METADATA;
	RELATION;
	SEMANTIC_ATTACHMENT;
	ARITHMETIC_RELATION;
	PREDICATE;
	TERM;
	INSTANCE_JAVA_CALL;
	STATIC_JAVA_CALL;
	LIST_BODY;
	PROVA_LIST;
	ARGS;
	TYPED_VARIABLE;
	USWORD;
	QUALIFIED_JAVA_CLASS;
	ANNOTATION;
	*/
REM: 'mod';
/*
	DOLLARWORD;
	GUARD;
	ATERM;
	EXPR;
	PROVA_MAP;
	KEY_VALUE;
	FUNCTION;
	FUNCTION_CALL;
*/
	/*------------------------------------------------------------------
     * LEXER RULES
     *------------------------------------------------------------------*/

    EXPONENT
    	:	'e' '-'? DIGIT+
    ;
    LONG_LITERAL	:	DIGIT+ 'L';

    INT_LITERAL:
        DIGIT+
    ;

    NEWLINE :	('%' (~('\r'|'\n') )*)? ('\r'? '\n')+?;

    fragment LC 	:	'a'..'z';

    fragment UC 	:	'A'..'Z';

    LCWORD 	:	LC WORD;

    DOLLARWORD
    	:	DOLLAR WORD;

    fragment WORD
    	: (LC | UC | UNDERSCORE | DIGIT)*;

    fragment UNDERSCORE
    	:	'_'
    	;

    fragment DOLLAR
    	:	'$'
    	;

    UCWORD 	:	UC (LC | UC | UNDERSCORE | ':' | DIGIT)*;

    USWORD 	:	UNDERSCORE (LC | UC | UNDERSCORE | DIGIT)*;

    ML_COMMENT
       :   '/*' .*? '*/' -> channel(HIDDEN)
        ;

    WS : ( '\t' | ' ' | '\u000C' )+ -> channel(HIDDEN) ;

    fragment DIGIT	: '0'..'9' ;

    STRING1	:	'\'' CLEAN_STRING* (STRING2 CLEAN_STRING*)* '\'';

    STRING2	:	'"' CLEAN_STRING* (STRING1 CLEAN_STRING*)* '"';

    CLEAN_STRING
    	:	~('\''|'\"') | CHAR_ESC;

    fragment CHAR_ESC://	returns [Character ret]:
        '\\'
        ( 'n'  //{ $ret='\n'; }
        | 'r'  //{ $ret='\r'; }
        | 't'  //{ $ret='\t'; }
        | 'b'  //{ $ret='\b'; }
        | 'f'  //{ $ret='\f'; }
        | '\"' //{ $ret='\"'; }
        | '\'' //{ $ret='\''; }
        | '\\' //{ $ret='\\'; }
        )
    ;