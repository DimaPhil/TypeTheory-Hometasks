import exceptions.ParserException;
import parsers.LambdaParser;
import lambdaTree.Abstraction;
import lambdaTree.Application;
import lambdaTree.LambdaExpression;
import lambdaTree.LambdaVariable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class HW3 {

    private static LambdaExpression substitute(LambdaExpression expression,
                                               LambdaVariable oldLambdaVariable,
                                               LambdaExpression replacement,
                                               Map<LambdaExpression, Integer> counter,
                                               Set<LambdaVariable> replacementFreeVars) throws SubstituteException {
        if (expression instanceof LambdaVariable) {
            if (!counter.containsKey(oldLambdaVariable) && expression.equals(oldLambdaVariable)) {
                for (LambdaVariable freeVar : replacementFreeVars) {
                    if (counter.containsKey(freeVar)) {
                        throw new SubstituteException("Нет свободы для подстановки для переменной " + freeVar);
                    }
                }
                return replacement;
            }
            return expression;
        } else if (expression instanceof Abstraction) {
            Abstraction it = (Abstraction) expression;
            LambdaExpression itVariable = it.getVariable();
            LambdaExpression itStatement = it.getStatement();
            counter.putIfAbsent(itVariable, 0);
            Integer oldValue = counter.get(itVariable);
            counter.put(itVariable, oldValue + 1);
            LambdaExpression statementSub = substitute(itStatement, oldLambdaVariable, replacement, counter, replacementFreeVars);
            LambdaExpression result = new Abstraction(itVariable, statementSub);
            counter.put(itVariable, oldValue);
            if (oldValue == 0) {
                counter.remove(itVariable);
            }
            return result;
        } else if (expression instanceof Application) {
            Application it = (Application) expression;
            LambdaExpression left = it.getLeft();
            LambdaExpression right = it.getRight();
            LambdaExpression leftSub = substitute(left, oldLambdaVariable, replacement, counter, replacementFreeVars);
            LambdaExpression rightSub = substitute(right, oldLambdaVariable, replacement, counter, replacementFreeVars);
            return new Application(leftSub, rightSub);
        }
        throw new SubstituteException("Unknown type");
    }

    public static void main(String[] args) {
        try (
                Scanner in = new Scanner(new File("input.txt"));
                PrintWriter out = new PrintWriter(new File("output.txt"))
        ) {
            String input = getInput(in);
            int indexOf = input.indexOf('[');
            String exprStr = input.substring(0, indexOf);
            String subExpr = input.substring(indexOf);

            int index = subExpr.indexOf(":=");
            String varStr = subExpr.substring(1, index);
            String subStr = subExpr.substring(index + 2, subExpr.length() - 1);

            LambdaVariable var = new LambdaVariable(varStr);

            LambdaParser parser = LambdaParser.getInstance();
            LambdaExpression substitution = parser.parse(subStr);

            LambdaExpression expression = parser.parse(exprStr);
            try {
                Set<LambdaVariable> freeVars = new HashSet<>();
                Utils.getFreeVariables(substitution, new HashMap<>(), freeVars);
                LambdaExpression result = substitute(expression, var, substitution, new HashMap<>(), freeVars);
                out.print(result);
            } catch (SubstituteException e) {
                out.print(e.getMessage());
            }
        } catch (FileNotFoundException | ParserException e) {
            e.printStackTrace();
        }
    }

    private static String getInput(Scanner in) {
        StringBuilder input = new StringBuilder();
        while (in.hasNextLine()) {
            input.append(in.nextLine());
        }
        return input.toString().trim();
    }

    private static class SubstituteException extends RuntimeException {
        SubstituteException(String message) {
            super(message);
        }
    }
}
