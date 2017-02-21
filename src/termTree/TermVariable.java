package termTree;

public class TermVariable implements TermExpression {

    private final String name;

    public TermVariable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermVariable termVariable = (TermVariable) o;

        return !(getName() != null ? !getName().equals(termVariable.getName()) : termVariable.getName() != null);

    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }
}
