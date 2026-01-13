grammar Enquanto;

programa : seqComando;     // sequência de comandos

seqComando: (comando ';')+;

comando: listaId ':=' listaExpressao                     # atribuicao
       | 'skip'                                          # skip
       | 'se' booleano 'entao' comando ('senaose' booleano 'entao' comando)* ('senao' comando)?   # se
       | 'enquanto' booleano 'faca' comando              # enquanto
       | 'para' ID 'de' expressao 'ate' expressao 'faca' comando # para
       | 'repita' expressao 'vezes' comando              # repita
       | 'escolha' expressao '{' caso* '}'               # escolha
       | 'exiba' (TEXTO | expressao)                     # exiba
       | 'escreva' expressao                             # escreva
       | '{' seqComando '}'                              # bloco
       ;

caso : (expressao | 'padrao' | '_') ':' comando ';';

listaId : ID (',' ID)*;
listaExpressao : expressao (',' expressao)*;

expressao: INT                                           # inteiro
         | 'leia'                                        # leia
         | ID                                            # id
         | '(' expressao ')'                             # expPar
         | <assoc=right> expressao '^' expressao         # opBin
         | expressao ('*' | '/') expressao               # opBin
         | expressao ('+' | '-') expressao               # opBin
         ;

booleano: BOOLEANO                                       # bool
        | expressao ('=' | '<=' | '>=' | '>' | '<' | '<>') expressao # opRel
        | 'nao' booleano                                 # naoLogico
        | booleano 'e' booleano                          # eLogico
        | booleano ('ou' | 'xor') booleano               # opLogico
        | '(' booleano ')'                               # boolPar
        ;


BOOLEANO: 'verdadeiro' | 'falso';
INT: ('0'..'9')+ ;
ID: ('a'..'z')+;
TEXTO: '"' .*? '"';

Comentario: '#' .*? '\n' -> skip;
Espaco: [ \t\n\r] -> skip;