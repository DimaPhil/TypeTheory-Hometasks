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
import java.util.stream.Collectors;

public class HW2 {

    public static void main(String[] args) {
        try (
                Scanner in = new Scanner(new File("input.txt"));
                PrintWriter out = new PrintWriter(new File("output.txt"))
        ) {
            String input = getInput(in);
            LambdaParser parser = LambdaParser.getInstance();
            LambdaExpression expression = parser.parse(input);

            Set<LambdaVariable> result = Utils.getFreeVariables(expression);

            List<String> answer = result.stream().map(LambdaVariable::getName).collect(Collectors.toList());
            Collections.sort(answer);
            answer.forEach(out::println);
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
