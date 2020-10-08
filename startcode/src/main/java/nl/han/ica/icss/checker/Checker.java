package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.Selector;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        for (ASTNode parent : ast.root.getChildren()) {
            if (parent instanceof VariableAssignment) {
                setVariables(parent);
            } else if (parent instanceof Stylerule) {
                for (ASTNode styleRule : parent.getChildren()) {
                    if (styleRule instanceof Declaration) {
                        walkDeclarations(styleRule);
                    } else if (styleRule instanceof IfClause) {
                        if (!checkIfClauseExpression((IfClause) styleRule)) {
                            // TODO Ik denk dat ik hier iets recursives voor moet maken.
                            IfClause clause = (IfClause) styleRule;
                            styleRule.setError(
                                    "The expression in an if-clause should be an boolean. The given expression is" +
                                            " " +
                                            getExpressionType(clause.conditionalExpression));
                        }
                    }
                }
            }
        }
    }

    private void walkDeclarations(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof VariableReference) {
                if (!checkIfVariableIsInstantiated((VariableReference) child)) {
                    VariableReference reference = (VariableReference) child;
                    node.setError("This variable is not instantiated => " + reference.name);
                }
            } else if (child instanceof Operation) {
                checkOperands((Operation) child);
            }
        }
    }

    private boolean checkIfClauseExpression(IfClause child) {
        return getExpressionType(child.conditionalExpression) == ExpressionType.BOOL;
    }

    private void checkOperands(Operation node) {
        Expression lhs = null;
        Expression rhs = null;
        ArrayList<ASTNode> expressions = node.getChildren();
        for (ASTNode e : expressions) {
            if (lhs == null) {
                lhs = (Expression) e;
            } else if (rhs == null) {
                rhs = (Expression) e;
            } else {
                node.setError("There should be two operands in a operation.");
            }
        }

        if (rhs instanceof Operation) {
            checkOperands((Operation) rhs);
        }

        if (checkExpressionTypes(lhs, ExpressionType.BOOL)) {
            node.setError("You cannot use booleans in an operation!");
        } else if (checkExpressionTypes(rhs, ExpressionType.BOOL)) {
            node.setError("You cannot use booleans in an operation");
        }

        if (checkExpressionTypes(lhs, ExpressionType.COLOR)) {
            node.setError("You cannot use colors in an operation");
        } else if (checkExpressionTypes(rhs, ExpressionType.COLOR)) {
            node.setError("You cannot use colors in an operation");
        }

        if (checkExpressionTypes(lhs, ExpressionType.PERCENTAGE)) {
            node.setError("You cannot use percentages in an operation");
        } else if (checkExpressionTypes(rhs, ExpressionType.PERCENTAGE)) {
            node.setError("You cannot use percentages in an operation");
        }

        if (getExpressionType(lhs) != getExpressionType(rhs)) {
            node.setError(
                    "You cannot do an operation with a " + getExpressionType(lhs) + " and a " + getExpressionType(rhs) +
                            "!");
        }
    }

    private boolean checkExpressionTypes(Expression expression, ExpressionType type) {
        return getExpressionType(expression) == type;
    }



    private boolean checkIfVariableIsInstantiated(VariableReference reference) {
        for (int i = 0; i < variableTypes.getSize(); ++i) {
            if (variableTypes.get(i).containsKey(reference.name)) {
                return true;
            }
        }
        return false;
    }

    private void setVariables(ASTNode node) {
        HashMap<String, ExpressionType> map = new HashMap<>();
        map.put(((VariableAssignment) node).name.name, getExpressionType(((VariableAssignment) node).expression));
        variableTypes.addFirst(map);
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof VariableReference) {
            VariableReference reference = (VariableReference) expression;
            for (int i = 0; i < variableTypes.getSize(); ++i) {
                if (variableTypes.get(i).containsKey(reference.name)) {
                    return variableTypes.get(i).get(reference.name);
                }
            }
        }
        return ExpressionType.UNDEFINED;
    }
}
