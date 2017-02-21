package parsers;

import exceptions.IllegalCharacterException;
import exceptions.ParserException;
import lambdaTree.Abstraction;
import lambdaTree.Application;
import lambdaTree.LambdaExpression;
import lambdaTree.LambdaVariable;

public final class LambdaParser extends Parser {
    private static LambdaParser localInstance = new LambdaParser();

    private Token currentToken;

    //No instances for you
    private LambdaParser() {
    }

    public static LambdaParser getInstance() {
        return localInstance;
    }

    public LambdaExpression parse(String string) throws ParserException {
        expression = string;
        position = 0;
        currentToken = nextToken();
        return readExpression();
    }

    private LambdaExpression readVariable() throws ParserException {
        //Переменная = ('a'..'z'){'a'...'z'| '0'..'9' | \'}*
        if (currentToken != Token.LOWER_LETTER) {
            throw new IllegalCharacterException(position);
        }
        String varName = currentString;
        currentToken = nextToken();
        return new LambdaVariable(varName);
    }

    private LambdaExpression readAtom() throws ParserException {
        //(атом) = '(' (выражение) ')' | (Переменная)
        if (currentToken == Token.LBRACE) {
            currentToken = nextToken();
            LambdaExpression result = readExpression();
            if (currentToken != Token.RBRACE) {
                throw new ParserException(") expected but found " + currentString);
            }
            currentToken = nextToken();
            return result;
        }
        return readVariable();
    }

    private LambdaExpression readApplicative() throws ParserException {
        //(применение) ::= (применение) (атом) | (атом)
        LambdaExpression result = readAtom();
        while (currentToken == Token.LOWER_LETTER || currentToken == Token.LBRACE) {
            LambdaExpression nextAtom = readAtom();
            result = new Application(result, nextAtom);
        }
        return result;
    }

    private LambdaExpression readAbstraction() throws ParserException {
        //(абстракция) ::= (переменная) '.' (Выражение)
        LambdaExpression variable = readVariable();
        if (currentToken != Token.DOT) {
            throw new ParserException("Expected dot '.' but found " + currentString);
        }
        currentToken = nextToken();
        LambdaExpression expression = readExpression();
        return new Abstraction(variable, expression);
    }

    private LambdaExpression readExpression() throws ParserException {
        //(выражение)  ::= [(применение)] '\' (абстракция) | (применение)
        if (currentToken == Token.LAMBDA) {
            currentToken = nextToken();
            return readAbstraction();
        }
        LambdaExpression result = readApplicative();
        if (currentToken == Token.LAMBDA) {
            currentToken = nextToken();
            LambdaExpression abstraction = readAbstraction();
            result = new Application(result, abstraction);
        }
        return result;
    }
}
