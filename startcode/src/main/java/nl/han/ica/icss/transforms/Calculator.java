package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

public class Calculator {

    public ScalarLiteral scalarOperations(Operation operation, ScalarLiteral lhs, ScalarLiteral rhs) throws
                                                                                                     UnknownOperationException {
        switch (operation.getNodeLabel()) {
            case "Add":
                return new ScalarLiteral(lhs.value + rhs.value);
            case "Subtract":
                return new ScalarLiteral(lhs.value - rhs.value);
            case "Multiply":
                return new ScalarLiteral(lhs.value * rhs.value);
        }
        throw new UnknownOperationException("The operation is unknown!");
    }

    public PixelLiteral pixelOperations(Operation operation, PixelLiteral lhs, PixelLiteral rhs) throws
                                                                                                 UnknownOperationException {
        switch (operation.getNodeLabel()) {
            case "Add":
                return new PixelLiteral(lhs.value + rhs.value);
            case "Subtract":
                return new PixelLiteral(lhs.value - rhs.value);
            case "Multiply":
                return new PixelLiteral(lhs.value * rhs.value);
        }
        throw new UnknownOperationException("The operation is unknown!");
    }

}
