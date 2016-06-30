lexer grammar DmnFeelTokens;

/**
 * Lexer Rules
 */
MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
POW: ('**' | '^');
IF: I F;
THEN: T H E N;
ELSE: E L S E;
OR: (O R | '|' | '||');
AND: (A N D | '&' | '&&');
NOT: N O T;
NULL: N U L L;
UNARYTEST: ( LT | LT EQ | GT | GT EQ );
COMP: (EQ | '!' EQ | LT | LT EQ | GT | GT EQ);
BETWEEN: B E T W E E N;
IN: I N;
INSTANCE: I N S T A N C E;
OF:  O F;
FOR: F O R;
RETURN: R E T U R N;
QUANTIFIER: (S O M E | E V E R Y);
SATISFIES: S A T I S F I E S;

/** 36. Boolean literal */
BooleanLiteral
    : T R U E
    | F A L S E
    ;

/** 37. numeric literal */
/** 38. digit */
/** 39. digits */
NumericLiteral
    : '-'? ('.' DIGIT+ | DIGIT+ ('.' DIGIT*)? )
    ;
fragment
DIGIT
    : [0-9]
    ;

/** 27. name */
/** 29. name part */
/** 28. name start */
Name
    : NameStartChar (NamePartChar+ | AdditionalNameSymbols)*
    ;

/** 30. name start char */
NameStartChar
    : '?'
    | ('A'..'Z')
    | '_'
    | ('a'..'z')
    //| ('\uC0'..'\uD6')
    //| ('\uD8'..'\uF6')
    //| ('\uF8'..'\u2FF')
    //| ('\u370'..'\u37D')
    //| ('\u37F'..'\u1FFF')
    | ('\u200C'..'\u200D')
    | ('\u2070'..'\u218F')
    | ('\u2C00'..'\u2FEF')
    | ('\u3001'..'\uD7FF')
    | ('\uF900'..'\uFDCF')
    | ('\uFDF0'..'\uFFFD')
    | ('\u10000'..'\uEFFFF')
    ;


/** 31. name part char */
NamePartChar
    : NameStartChar
    | DIGIT
    //| '\uB7'
    | ('\u0300'..'\u036F')
    | ('\u203F'..'\u2040')
    ;

/** 32. additional name symbols */
AdditionalNameSymbols
    : '.'
    /*| '/'
    | '-'
    | '’'
    | '+'
    | '*'
    */
    ;

/** 35. string literal */
StringLiteral : '"' ( ESC | . )*? '"' ;
fragment ESC : '\\' [btnr"\\] ; // \b, \t, \n etc...

/** 62. date time literal */
DateTimeLiteral
    :('date' | 'time' | 'duration' )  '(' StringLiteral ')'
    ;

LINE_COMMENT : '//' .*? '\n' -> skip ;
COMMENT : '/*' .*? '*/' -> skip ;

// case insensitive chars
fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');
fragment GT:('>');
fragment LT:('<');
fragment EQ:('=');

WS:
  [ \r\n\t]+ -> channel(HIDDEN)
  ;
