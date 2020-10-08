package nl.han.ica.icss.transforms;

import com.sun.security.jgss.GSSUtil;
import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;
import nl.han.ica.icss.parser.ASTListener;

import java.util.HashMap;
import java.util.LinkedList;

public class EvalExpressions implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public EvalExpressions() {
        //variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        for (ASTNode parent: ast.root.getChildren()) {
            if (parent instanceof VariableAssignment) {
                setVariablesInList(parent);
            } else if (parent instanceof Stylerule) {
                for (ASTNode styleRule: parent.getChildren()) {
                    if (styleRule instanceof Declaration) {
                        checkDeclarations(styleRule);
                    } else if (styleRule instanceof IfClause) {
                        System.out.println("zitten de if clauses een stappie hogert");
                    }
                }
            }
        }
    }

    private void checkDeclarations(ASTNode node) {
        for (ASTNode declaration: node.getChildren()) {
            if (declaration instanceof VariableReference) {
                try {
                    Literal literal = getVariableFromList((VariableReference) declaration);
                    declaration.removeChild(declaration);
                    node.addChild(literal);
                } catch (UnknownVariableException e) {
                    declaration.setError("Dit kan niet gebeuren.");
                }
            } else if (declaration instanceof Operation) {
                Literal literal = handleOperation(declaration);
                node.removeChild(declaration);
                node.addChild(literal);
            } 
        }
    }

    private Literal getVariableFromList(VariableReference reference) throws UnknownVariableException {
        for (int i = 0; i < variableValues.getSize(); ++i) {
            if (variableValues.get(i).containsKey(reference.name)) {
                return variableValues.get(i).get(reference.name);
            }
        }
        throw new UnknownVariableException();
    }

    private Literal handleOperation(ASTNode node) {
        Operation operation = (Operation) node;
        Expression literal = null;
        Literal lhs = null;
        Literal rhs = null;
        for (ASTNode e : operation.getChildren()) {
            literal = (Expression) e;
            if (lhs == null) {
                lhs = getLiteral((Expression) e);
            } else if (rhs == null) {
                rhs = getLiteral((Expression) e);
            } else {
                node.setError("Now we have a very big problem mate");
            }
        }

        Literal calculatedValue = null;
        Calculator calc = new Calculator();
        try {
            if (literal instanceof ScalarLiteral) {
                calculatedValue = calc.scalarOperations(operation, (ScalarLiteral) lhs, (ScalarLiteral) rhs);
            } else if (literal instanceof PixelLiteral) {
                calculatedValue = calc.pixelOperations(operation, (PixelLiteral) lhs, (PixelLiteral) rhs);
            }
        } catch (UnknownOperationException e) {
            node.setError("Error during calculation: " + e.getMessage());
        }

        return calculatedValue;
    }

    private void setVariablesInList(ASTNode node) {
        VariableAssignment variable = (VariableAssignment) node;
        HashMap<String, Literal> map = new HashMap<>();
        map.put( variable.name.name, getLiteral(variable.expression));
        variableValues.addFirst(map);
    }

    private Literal getLiteral(Expression expression) {
        if (expression instanceof BoolLiteral) {
            return (BoolLiteral) expression;
        } else if (expression instanceof ColorLiteral) {
            return (ColorLiteral) expression;
        } else if (expression instanceof PercentageLiteral) {
            return (PercentageLiteral) expression;
        } else if (expression instanceof PixelLiteral) {
            return (PixelLiteral) expression;
        } else if (expression instanceof ScalarLiteral) {
            return (ScalarLiteral) expression;
        } else if (expression instanceof VariableReference) {
            VariableReference reference = (VariableReference) expression;
            for (int i = 0; i < variableValues.getSize(); ++i) {
                if (variableValues.get(i).containsKey(reference.name)) {
                    return variableValues.get(i).get(reference.name);
                }
            }
        }
        return null;
    }

}
