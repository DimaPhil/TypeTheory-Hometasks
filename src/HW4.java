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

public class HW4 {
    private static LambdaVariable getSomeVariable(Set<LambdaVariable> busy) {
        for (char c = 'a'; c <= 'z'; c++) {
            LambdaVariable it = new LambdaVariable(String.valueOf(c));
            if (!busy.contains(it)) {
                return it;
            }
            it = new LambdaVariable(String.valueOf(c) + "'");
            if (!busy.contains(it)) {
                return it;
            }
        }
        return new LambdaVariable("x''");
    }

    private static LambdaExpression substitute(LambdaExpression expression,
                                        LambdaVariable oldVariable,
                                        LambdaExpression replacement) {

        if (expression instanceof LambdaVariable) {
            LambdaVariable it = (LambdaVariable) expression;
            if (it.equals(oldVariable)) {
                return replacement;
            }
            return expression;
        }
        if (expression instanceof Application) {
            Application it = (Application) expression;
            LambdaExpression left = it.getLeft();
            LambdaExpression right = it.getRight();
            return new Application(substitute(left, oldVariable, replacement), substitute(right, oldVariable, replacement));
        }
        if (expression instanceof Abstraction) {
            Abstraction it = (Abstraction) expression;
            LambdaVariable itVariable = (LambdaVariable) it.getVariable();
            LambdaExpression itStatement = it.getStatement();
            if (itVariable.equals(oldVariable) || !Utils.getFreeVariables(itStatement).contains(oldVariable)) {
                return expression;
            }
            if (!Utils.getFreeVariables(replacement).contains(itVariable)) {
                return new Abstraction(itVariable, substitute(itStatement, oldVariable, replacement));
            }
            Set<LambdaVariable> allBusyVars = Utils.getFreeVariables(itStatement);
            allBusyVars.addAll(Utils.getFreeVariables(replacement));
            LambdaVariable someFreeVar = getSomeVariable(allBusyVars);
            LambdaExpression afterChange = substitute(itStatement, itVariable, someFreeVar);
            return new Abstraction(someFreeVar, substitute(afterChange, oldVariable, replacement));
        }
        throw new IllegalArgumentException("Unknown type");
    }

    private static Map<LambdaExpression, LambdaExpression> memory = new HashMap<>();
    private static Map<LambdaExpression, LambdaExpression> headMemory = new HashMap<>();

    private static void remember(LambdaExpression from, LambdaExpression to) {
        if (!memory.containsKey(from)) {
            memory.put(from, to);
        }
    }

    private static void rememberHead(LambdaExpression from, LambdaExpression to) {
        if (!headMemory.containsKey(from)) {
            headMemory.put(from, to);
        }
    }

    private static LambdaExpression headNormalForm(LambdaExpression expression) {
        if (headMemory.containsKey(expression)) {
            return headMemory.get(expression);
        }
        if (expression instanceof LambdaVariable || expression instanceof Abstraction) {
            return expression;
        } else if (expression instanceof Application) {
            Application it = (Application) expression;
            LambdaExpression left = it.getLeft();
            LambdaExpression right = it.getRight();
            LambdaExpression leftNormal = headNormalForm(left);
            if (leftNormal instanceof Abstraction) {
                Abstraction leftIt = (Abstraction) leftNormal;
                LambdaVariable leftItLambdaVariable = (LambdaVariable) leftIt.getVariable();
                LambdaExpression leftItStatement = leftIt.getStatement();
                LambdaExpression substitution = substitute(leftItStatement, leftItLambdaVariable, right);

                LambdaExpression headNormalForm = /*norm*/headNormalForm(substitution);
                rememberHead(expression, headNormalForm);
                return headNormalForm;
            } else {
                Application applicative = new Application(/*not simple*/leftNormal, right);
                rememberHead(expression, applicative);
                return applicative;
            }
        }
        throw new IllegalArgumentException("Unknown type");
    }

    //
    //(\x.A)y === A[x:=y]
    //

    private static LambdaExpression normalForm(LambdaExpression expression) {
        if (memory.containsKey(expression)) {
            return memory.get(expression);
        }
        if (expression instanceof LambdaVariable) {
            return expression;
        } else if (expression instanceof Abstraction) {
            Abstraction it = (Abstraction) expression;
            LambdaExpression itVariable = it.getVariable();
            LambdaExpression itStatement = it.getStatement();
            Abstraction abstraction = new Abstraction(itVariable, normalForm(itStatement));
            remember(expression, abstraction);
            return abstraction;
        } else if (expression instanceof Application) {
            Application it = (Application) expression;
            LambdaExpression left = it.getLeft();
            LambdaExpression right = it.getRight();
            LambdaExpression headLeftNormalForm = headNormalForm(left);
            if (headLeftNormalForm instanceof Abstraction) {
                Abstraction leftIt = (Abstraction) headLeftNormalForm;
                LambdaVariable leftItLambdaVariable = (LambdaVariable) leftIt.getVariable();
                LambdaExpression leftItStatement = leftIt.getStatement();
                LambdaExpression substitution = substitute(leftItStatement, leftItLambdaVariable, right);
                LambdaExpression normalForm = normalForm(substitution);
                remember(expression, normalForm);
                return normalForm;
            } else {
                Application applicative = new Application(normalForm(headLeftNormalForm), normalForm(right));
                remember(expression, applicative);
                return applicative;
            }
        }
        throw new IllegalArgumentException("Unknown type");
    }

    public static void main(String[] args) {
        try (
                Scanner in = new Scanner(new File("input.txt"));
                PrintWriter out = new PrintWriter(new File("output.txt"))
        ) {
            String input = getInput(in);
            LambdaParser parser = LambdaParser.getInstance();
            LambdaExpression expression = parser.parse(input);
            LambdaExpression normalExpression = normalForm(expression);
            out.print(normalExpression);

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

}
