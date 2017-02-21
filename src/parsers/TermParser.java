package parsers;

import exceptions.ParserException;
import termTree.Function;
import termTree.TermExpression;
import termTree.TermVariable;

import java.util.ArrayList;
import java.util.List;

public final class TermParser extends Parser {
    private static TermParser localInstance = new TermParser();

    private Token currentToken;

    //No instances for you
    private TermParser() {
    }

    public static TermParser getInstance() {
        return localInstance;
    }

    public TermExpression parse(String string) throws ParserException {
        expression = string;
        position = 0;
        currentToken = nextToken();
        return readTerm();
    }

    private TermExpression readTerm() throws ParserException {
        //(Терм) = (Функция) '(' (Терм) {',' (Терм) }* | (Переменная)
        if (currentToken != Token.LOWER_LETTER) {
            throw new ParserException("Expected identifier but found " + currentString);
        }
        char firstChar = currentString.charAt(0);
        if (firstChar < 'a' || firstChar > 'z') {
            throw new ParserException("Expected lower letter but found" + currentString);
        }
        if ('a' <= firstChar && firstChar <= 'h') {
            String functionName = currentString;
            currentToken = nextToken();
            List<TermExpression> args = new ArrayList<>();
            if (currentToken == Token.LBRACE) {
                currentToken = nextToken();
                TermExpression nextArg = readTerm();
                args.add(nextArg);
                while (currentToken == Token.COMMA) {
                    currentToken = nextToken();
                    nextArg = readTerm();
                    args.add(nextArg);
                }
                if (currentToken != Token.RBRACE) {
                    throw new ParserException("Expected ')'");
                }
                currentToken = nextToken();
            }
            return new Function(args, functionName);
        }
        String variableName = currentString;
        currentToken = nextToken();
        return new TermVariable(variableName);
    }
}
