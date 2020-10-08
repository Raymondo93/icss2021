grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

// Properties
BACKGROUND_COLOR: 'background-color';
SELECTOR_COLOR: 'color';
HEIGHT: 'height';
WIDTH: 'width';

//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MULTIPLY: '*';
DIVISION: '/';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet:(variableAssignment | styleRule )+ EOF;
selector: (idSelector | tagSelector | classSelector);
idSelector: ID_IDENT;
classSelector: CLASS_IDENT;
tagSelector: LOWER_IDENT;
variableAssignment:  variableReference ASSIGNMENT_OPERATOR literal SEMICOLON;
styleRule: selector declaration;
declaration: OPEN_BRACE (styleBlock | ifClause)* CLOSE_BRACE;
styleBlock: propertyName COLON expression SEMICOLON;
expression: (operation | value);
operation: (sum | sub | multiply);
literal: (color | percentage | pixel | scalar | bool);
color: COLOR;
value: (literal | variableReference);
propertyName: BACKGROUND_COLOR | SELECTOR_COLOR | HEIGHT | WIDTH;
variableReference: (bool | CAPITAL_IDENT);
bool: TRUE | FALSE;
elseClause: ELSE declaration;
ifClause: IF BOX_BRACKET_OPEN value BOX_BRACKET_CLOSE declaration elseClause?;
pixel: PIXELSIZE;
percentage: PERCENTAGE;
scalar: SCALAR;
sum: value PLUS expression;
sub: value MIN expression;
multiply: value MULTIPLY expression;