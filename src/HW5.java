import exceptions.ParserException;
import parsers.TermParser;
import termTree.Function;
import termTree.TermExpression;
import termTree.TermVariable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class HW5 {

    private static class Equals {

        final TermExpression left;
        final TermExpression right;

        private Equals(TermExpression left, TermExpression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Equals equals = (Equals) o;

            if (left != null ? !left.equals(equals.left) : equals.left != null) return false;
            return !(right != null ? !right.equals(equals.right) : equals.right != null);

        }

        @Override
        public int hashCode() {
            int result = left != null ? left.hashCode() : 0;
            result = 31 * result + (right != null ? right.hashCode() : 0);
            return result;
        }
    }

    private static List<Equals> getWithout(List<Equals> initial, Equals withoutWhat) {
        return initial.stream().filter(eq -> !eq.equals(withoutWhat)).collect(Collectors.toList());
    }

    private static List<Equals> substitute(List<Equals> system, TermVariable termVariable, TermExpression replacement) {
        List<Equals> result = new ArrayList<>();
        for (Equals eq : system) {
            TermExpression sLeft = Utils.substitute(eq.left, termVariable, replacement);
            TermExpression sRight = Utils.substitute(eq.right, termVariable, replacement);
            result.add(new Equals(sLeft, sRight));
        }
        return result;
    }

    public static void main(String[] args) throws ParserException {
        try (
                Scanner in = new Scanner(new File("input.txt"));
                PrintWriter out = new PrintWriter(new File("output.txt"))) {

            TermParser parser = TermParser.getInstance();

            List<Equals> system = new ArrayList<>();

            while (in.hasNextLine()) {
                String line = in.nextLine();
                int index = line.indexOf("=");
                String subTerm1 = line.substring(0, index);
                String subTerm2 = line.substring(index + 1);
                TermExpression term1 = parser.parse(subTerm1);
                TermExpression term2 = parser.parse(subTerm2);
                system.add(new Equals(term1, term2));
            }

            repeat:
            while (true) {
                if (system.isEmpty()) break;

                for (Equals eq : system) {
                    //1 rule
                    if (eq.left.equals(eq.right)) {
                        system = getWithout(system, eq);
                        continue repeat;
                    }

                    TermExpression left = eq.left;
                    TermExpression right = eq.right;

                    if (left instanceof Function && right instanceof Function) {
                        Function leftF = (Function) left;
                        Function rightF = (Function) right;

                        //3 rule - conflict
                        if (!leftF.getName().equals(rightF.getName()) || leftF.getArgs().size() != rightF.getArgs().size()) {
                            out.println("Система неразрешима: " + leftF + " != " + rightF);
                            return;
                        }

                        //2 rule - decompose
                        List<Equals> nextSystem = getWithout(system, eq);
                        List<TermExpression> lefts = leftF.getArgs();
                        List<TermExpression> rights = rightF.getArgs();

                        for (int i = 0; i < lefts.size(); i++) {
                            nextSystem.add(new Equals(lefts.get(i), rights.get(i)));
                        }

                        system = nextSystem;
                        continue repeat;
                    }

                    //4 rule (swap)
                    if (left instanceof Function && right instanceof TermVariable) {
                        List<Equals> nextSystem = getWithout(system, eq);
                        nextSystem.add(new Equals(right, left));
                        system = nextSystem;
                        continue repeat;
                    }

                    //6 rule (check)
                    if (left instanceof TermVariable) {
                        if (Utils.getFreeVariables(right).contains(left)) {
                            out.println("Система неразрешима: переменная " + left + " входит свободно в " + right);
                            return;
                        }

                        //5 rule (eliminate)
                        List<Equals> nextSystem = getWithout(system, eq);

                        boolean isInG = false;
                        for (Equals neq : nextSystem) {
                            if (Utils.getFreeVariables(neq.left).contains(left)) {
                                isInG = true;
                                break;
                            }
                            if (Utils.getFreeVariables(neq.right).contains(left)) {
                                isInG = true;
                                break;
                            }
                        }

                        if (isInG) {
                            nextSystem = substitute(nextSystem, (TermVariable) left, right);
                            nextSystem.add(new Equals(left, right));
                            system = nextSystem;
                            continue repeat;
                        }
                    }

                }
                break;
            }

            for (Equals eq : system) {
                out.println(eq.left + "=" + eq.right);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
