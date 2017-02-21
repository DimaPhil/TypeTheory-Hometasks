package parsers;

import exceptions.IllegalCharacterException;

class Parser {
    String expression;
    String currentString;
    int position;

    enum Token {

        LBRACE("("),
        RBRACE(")"),
        LAMBDA("\\"),
        DOT("."),
        LOWER_LETTER("low"),
        UPPER_LETTER("high"),
        COMMA(","),
        EQUALS("::="),
        SINGLE_QUOTE("\'"),
        END("eof");

        private final String name;

        Token(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    private boolean isLowerLetter(char c) {
        return Character.isLowerCase(c);
    }

    private boolean isUpperLetter(char c) {
        return Character.isUpperCase(c);
    }

    Token nextToken() throws IllegalCharacterException {
        while (Character.isWhitespace(currentChar())) {
            position++;
        }
        if (position >= expression.length()) {
            return Token.END;
        }
        char oldChar = currentChar();
        position++;
        for (Token token : Token.values()) {
            if (token.getName().equals(Character.toString(oldChar))) {
                return token;
            }
        }
        if (isLowerLetter(oldChar)) {
            currentString = String.valueOf(oldChar);
            while (isLowerLetter(currentChar()) || isDigit(currentChar())) {
                currentString += currentChar();
                position++;
            }
            return Token.LOWER_LETTER;
        }
        if (isUpperLetter(oldChar)) {
            currentString = String.valueOf(oldChar);
            while (isUpperLetter(currentChar()) || isDigit(currentChar())) {
                currentString += currentChar();
                position++;
            }
            return Token.UPPER_LETTER;
        }
        throw new IllegalCharacterException(position);
    }

    private char currentChar() {
        //$ - impossible symbol in parsing
        return position >= expression.length() ? '$' : expression.charAt(position);
    }
}
