import lambdaTree.Abstraction;
import lambdaTree.Application;
import lambdaTree.LambdaExpression;
import lambdaTree.LambdaVariable;
import termTree.Function;
import termTree.TermExpression;
import termTree.TermVariable;

import java.util.*;

class Utils {
    static void getFreeVariables(LambdaExpression expression, Map<LambdaExpression, Integer> counter, Set<LambdaVariable> answer) {
        if (expression instanceof LambdaVariable) {
            if (!counter.containsKey(expression)) {
                answer.add((LambdaVariable) expression);
            }
        } else if (expression instanceof Abstraction) {
            Abstraction it = (Abstraction) expression;
            LambdaExpression variable = it.getVariable();
            counter.putIfAbsent(variable, 0);
            Integer oldValue = counter.get(variable);
            counter.put(variable, oldValue + 1);
            getFreeVariables(it.getStatement(), counter, answer);
            counter.put(variable, oldValue);
            if (oldValue == 0) {
                counter.remove(variable);
            }
        } else if (expression instanceof Application) {
            Application it = (Application) expression;
            getFreeVariables(it.getLeft(), counter, answer);
            getFreeVariables(it.getRight(), counter, answer);
        }
    }

    static Set<LambdaVariable> getFreeVariables(LambdaExpression expression) {
        Map<LambdaExpression, Integer> counter = new HashMap<>();
        Set<LambdaVariable> result = new HashSet<>();
        getFreeVariables(expression, counter, result);
        return result;
    }

    static Set<TermVariable> getFreeVariables(TermExpression expression) {
        Set<TermVariable> set = new HashSet<>();
        if (expression instanceof TermVariable) {
            set.add((TermVariable) expression);
        } else {
            Function function = (Function) expression;
            for (TermExpression var : function.getArgs()) {
                set.addAll(getFreeVariables(var));
            }
        }
        return set;
    }

    static TermExpression substitute(TermExpression expression, TermVariable termVariable, TermExpression replacement) {
        if (expression instanceof TermVariable) {
            if (expression.equals(termVariable)) {
                return replacement;
            }
            return expression;
        } else {
            Function function = (Function) expression;
            List<TermExpression> newArgs = new ArrayList<>();
            for (TermExpression arg : function.getArgs()) {
                newArgs.add(substitute(arg, termVariable, replacement));
            }
            return new Function(newArgs, function.getName());
        }
    }
}
