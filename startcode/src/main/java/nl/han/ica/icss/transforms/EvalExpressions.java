package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.ElseClause;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

import java.util.HashMap;

public class EvalExpressions implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        for (ASTNode parent: ast.root.getChildren()) { // Loop through children of stylesheet
            if (parent instanceof VariableAssignment) { // Set variables in LinkedList
                setVariablesInList(parent);
            } else if (parent instanceof Stylerule) { // Loop through children of stylerule
                for (ASTNode styleRule: parent.getChildren()) {
                    if (styleRule instanceof Declaration) { //loop through children of declaration
                        checkDeclarations(styleRule);
                    } else if (styleRule instanceof IfClause) { // Loop through if clause
                        checkIfClauses(styleRule);
                    }
                }
            }
        }
    }

    // Loop through children of a if clause and set the real values for a variable
    private void checkIfClauses(ASTNode node) {
        for (ASTNode child: node.getChildren()) {
            if (child instanceof VariableReference) {
                try {
                    Literal literal = getVariableFromList((VariableReference) child);
                    child.removeChild(child);
                    node.addChild(literal);
                } catch (UnknownVariableException e) {
                    child.setError("Hier zou die niet moeten komen, er is een variabele onbekend...");
                }
            } else if (child instanceof Declaration) {
                checkDeclarations(child);
            } else if (child instanceof ElseClause) {
                checkDeclarations(child);
            } else if (child instanceof IfClause) {
                checkIfClauses(child);
            }
        }
    }

    // Loop through the children of a declaration and set the real values for a variable
    private void checkDeclarations(ASTNode node) {
        for (ASTNode child: node.getChildren()) {
            if (child instanceof VariableReference) {
                try {
                    Literal literal = getVariableFromList((VariableReference) child);
                    child.removeChild(child);
                    node.addChild(literal);
                } catch (UnknownVariableException e) {
                    child.setError("Dit kan niet gebeuren.");
                }
            } else if (child instanceof Operation) {
                Literal literal = handleOperation(child);
                node.removeChild(child);
                node.addChild(literal);
            }
        }
    }

    // Get the variable from a the list. Throws an exception when a variable is undefined. But it shouldn't come to this
    private Literal getVariableFromList(VariableReference reference) throws UnknownVariableException {
        for (int i = 0; i < variableValues.getSize(); ++i) {
            if (variableValues.get(i).containsKey(reference.name)) {
                return variableValues.get(i).get(reference.name);
            }
        }
        throw new UnknownVariableException();
    }

    // Calculate the operation
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
