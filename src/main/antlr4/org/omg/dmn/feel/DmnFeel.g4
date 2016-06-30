grammar DmnFeel;
import DmnFeelTokens;

//start_rule: expression;

expression
    : //functionDefinition #lblFunctionDefinition
    //| forExpression #lblForExpression
    //|
    ifExpression #lblIfExpression
    | quantifiedExpression #lblQuantifiedExpression
    // 4. Arithmetic
    |<assoc=right> a=expression s=POW b=expression  #lblExponentiation
    //51.a
    | a=expression s=BETWEEN '(' b=expression ',' c=expression ')' #lblBetween
    //| a=expression s=BETWEEN  b=expression AND c=expression #lblBetween
    //51.c + 51.d
    | a=expression s=IN ('(' positiveUnaryTests ')' | positiveUnaryTests) #lblIn
    | a=expression s=DIV b=expression #lblDivision
    | a=expression s=MUL b=expression #lblMultiplication
    | a=expression s=ADD b=expression #lblAddition
    | a=expression s=SUB b=expression #lblSubtraction
    | SUB a=expression #lblNegation
    /** 53. instance of */
    | a=expression s=INSTANCE OF type #lblInstanceOf //instanceOf
    //| expression '.' Name //pathExpression
    /** 52. filter expression */
    | a=expression '[' b=expression ']' #lblFilterExpression
    | a=expression ',' b=parameters #lblFunctionInvocation
    | simpleUnaryTests #lblSimpleUnaryTests
    | a=literal #lblLiteral
    | '(' a=expression ')' #lblBrackets
    //51.a
    | a=expression s=COMP b=expression #lblComparison
    /** 50. conjunction */
    | a=expression s=OR b=expression #lblConjunction //conjunction
    /** 49. disjunction */
    | a=expression s=AND b=expression #lblDisjunction //disjunction
    //| boxedExpression #lblBoxedExpression
    ;

/** 2. textual expression => left recrusion moved to top rule */
/** 4. arithmetic expression => left recrusion moved to top rule */

/** 3. textual expressions =>
 * FIXME: never uesed
 */
/*
textualExpressions
    : textualExpression (',' textualExpression)*
    ;
*/

/** 5. simple expression */
simpleExpression
    // 4. Arithmetic
    : <assoc=right> a=simpleExpression POW b=simpleExpression #lblSimpleExponentiation
    | a=simpleExpression DIV b=simpleExpression #lblSimpleDivision
    | a=simpleExpression MUL b=simpleExpression #lblSimpleMultiplication
    | a=simpleExpression ADD b=simpleExpression #lblSimpleAddition
    | a=simpleExpression SUB b=simpleExpression #lblSimpleSubtraction
    | SUB a=simpleExpression #lblSimpleNegation
    | simpleUnaryTests #lblSimpleUnaryTest
    | a=simpleValue #lblSimpleValue
    ;

/** 6. simple expressions */
simpleExpressions
    : (simpleExpression ','?)+ #lblSimpleExpressions
    | simpleExpression #lblSimpleExpression
    ;

/** 7. simple positive unary test */
simplePositiveUnaryTest
    : UNARYTEST endpoint #lblUnaryTestEndpoint
    | interval #lblUnaryTestInterval
    ;

/** 8. interval */
interval
    : ( openIntervalStart | closedIntervalStart )
        startInterval= endpoint '..' endInterval= endpoint
      ( openIntervalEnd | closedIntervalEnd )
    ;

/** 9. open interval start */
openIntervalStart
    : '('
    | ']'
    ;

/** 10. closed interval start */
closedIntervalStart
    : '['
    ;

/** 11. open interval end */
openIntervalEnd
    : ')'
    | '['
    ;

/** 12. closed interval end */
closedIntervalEnd
    : ']'
    ;

/** 13. simple positive unary tests */
simplePositiveUnaryTests
    : (simplePositiveUnaryTest ','?)+ #lblSimplePositiveUnaryTests
    | simplePositiveUnaryTest #lblSimplePositiveUnaryTest
    ;

/** 14. simple unary tests */
simpleUnaryTests
	: simplePositiveUnaryTests #lblSimpleUnaryTestsPositive
	| NOT '(' simplePositiveUnaryTests ')' #lblSimpleUnaryTestsNeg
	| '-' #lblSimpleUnaryTestsNil
	;


/** 15. positive unary test */
positiveUnaryTest
    : simplePositiveUnaryTest
    | NULL
    ;

/** 16. positive unary tests */
positiveUnaryTests
    : (positiveUnaryTest ','?)+
    ;

/** 17. unary tests */
/*
unaryTests
    : positiveUnaryTests
    | NOT '(' simpleUnaryTests ')'
	| '-'
	;
*/

/** 18. endpoint */
endpoint
    : simpleValue
    ;

/** 19. simple value */
simpleValue
    : simpleLiteral
    | qualifiedName
    ;

/** 20. qualified name */
qualifiedName
    : ( '.'? Name )+
    ;



/** 33. literal */
literal
    : simpleLiteral #typeLiteral
    | Name #lblName
    | NULL #typeNull
    ;

/** 34. simple literal */

simpleLiteral
    : BooleanLiteral #typeBoolean
    | NumericLiteral #typeNumeric
    | StringLiteral #typeString
    | DateTimeLiteral #typeDate
    ;


/** 40. function invocation */
/*
functionInvocation
    : expression ',' parameters
    ;
*/

/** 41. parameters*/
parameters
    : '(' ( namedParameters | positionalParameters ) ')'
    ;
/** 42. named parameters */
namedParameters
    : parameterName ':' expression
    | '{' (namedParameters ','?)+ '}' ;

/** 43. parameter name */
parameterName
    : Name
    ;

/** 44. positional parameters */
positionalParameters
    : '[' (expression ','?)+  ']'
    ;

/** 45. path expression */
/*
pathExpression
    : expression '.' Name
    ;
*/

/** 46. for expression */
/*
forExpression
    //for expression = "for" , name , "in" , expression { "," , name , "in" , expression } , "return" , expression ;
    : FOR Name IN expression (',' Name IN expression)* RETURN expression
    ;
*/

/** 47. if expression */
ifExpression
    : IF expression THEN expression ELSE expression
    ;

/** 48. quantified expression */
quantifiedExpression
    //("some" | "every") , name , "in" , expression , { name , "in" , expression } , "satisfies" , expression ;
    : QUANTIFIER (Name IN expression)+ SATISFIES expression
    ;

/** 54. type */
type
    : qualifiedName
    ;

/** 55. boxed expression */
/*
boxedExpression
    : list 
	| functionDefinition 
	| context
    ;
*/

/** 56. list */
/*
list
    : '[' (expression ','?)+  ']'
    ;
*/

/** 57. function definition */
/*
functionDefinition
    : 'function' '(' (formalParameter ','?)+ ')' 'external'? expression
    ;
*/

/** 58. formal parameter */
/*
formalParameter
    : parameterName
    ;
*/

/** 59. context */
/*
context
    : '{' (contextEntry ','?)+  '}'
    ;
*/

/** 60. context entry */
/*
contextEntry
    : key ':' expression
    ;
*/

/** 61. key */
/*
key
    : Name
    | StringLiteral
    ;
*/