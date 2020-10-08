package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Operation;

public class GenericCalculator<T> {

    private Object lhs;
    private Object rhs;

    public GenericCalculator(T lhs, T rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public T calculateOperation(Operation operation) {
        if (operation.getNodeLabel().equals("Add")) {
//            T calculation = (T) lhs.value + (T) rhs.value;
//            return calculation;
        }
        return null;
    }
}
