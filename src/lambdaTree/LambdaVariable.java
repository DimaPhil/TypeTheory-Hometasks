package lambdaTree;

public class LambdaVariable implements LambdaExpression {
    private final String name;

    public LambdaVariable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LambdaVariable lambdaVariable = (LambdaVariable) o;

        return !(getName() != null ? !getName().equals(lambdaVariable.getName()) : lambdaVariable.getName() != null);

    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }
}
