package nl.han.ica.icss.checker;

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
import nl.han.ica.icss.ast.PropertyName;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    // This check loops through the AST and performs multiple semantic checks.
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
                        handleIfClause(styleRule);
                    }
                }
            }
        }
    }

    // handle the children of a if clause
    private void handleIfClause(ASTNode node) {
        if (!checkIfClauseExpression((IfClause) node)) {
            node.setError(
                    "The expression in an if-clause should be an boolean. The given expression is" +
                            " " +
                            getExpressionType(((IfClause) node).conditionalExpression));
        }
        // loop through body of the if clause
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Declaration) {
                walkDeclarations(child);
            } else if (child instanceof ElseClause) {
                handleElseClause(child);
            } else if (child instanceof IfClause) {
                handleIfClause(child);
            }
        }
    }

    // Handle the body of the else clause
    private void handleElseClause(ASTNode node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Declaration) {
                walkDeclarations(child);
            } else if (child instanceof IfClause) {
                handleIfClause(child);
            }
        }
    }

    // Loop through Declarations
    private void walkDeclarations(ASTNode node) {
        PropertyName propertyName = null;
        Expression expression = null;
        for (ASTNode child : node.getChildren()) {
            if (child instanceof VariableReference) {
                if (!checkIfVariableIsInstantiated((VariableReference) child)) {
                    VariableReference reference = (VariableReference) child;
                    node.setError("This variable is not instantiated => " + reference.name);
                }
            } else if (child instanceof Operation) {
                checkOperands((Operation) child);
            } else if (child instanceof PropertyName) {
                propertyName = (PropertyName) child;
            } else if (child instanceof Expression) {
                expression = (Expression) child;
            }

            if (propertyName != null && expression != null) {
                checkPropertyExpressions(child, propertyName, expression);
                propertyName = null;
                expression = null;
            }


        }
    }

    // Check if the expression matches with the property
    private void checkPropertyExpressions(ASTNode node, PropertyName propertyName, Expression expression) {
        if (getExpressionType(expression) == ExpressionType.BOOL) {
            node.setError(setPropertyError(getExpressionType(expression), propertyName.name));
        }
        if (propertyName.name.equals("width") || propertyName.name.equals("height")) {
            if (getExpressionType(expression) == ExpressionType.COLOR) {
                node.setError(setPropertyError(getExpressionType(expression), propertyName.name));
            }
        } else if (propertyName.name.equals("background-color") || propertyName.name.equals("color")){
            if (getExpressionType(expression) == ExpressionType.PERCENTAGE) {
                node.setError(setPropertyError(getExpressionType(expression), propertyName.name));
            }
            if (getExpressionType(expression) == ExpressionType.PIXEL) {
                node.setError(setPropertyError(getExpressionType(expression), propertyName.name));
            }
            if (getExpressionType(expression) == ExpressionType.SCALAR) {
                node.setError(setPropertyError(getExpressionType(expression), propertyName.name));
            }
        }
    }

    private String setPropertyError(ExpressionType type, String propertyName) {
        return type + " are not allowed in the property " + propertyName;
    }

    // check if the conditional expression of an if-clause is of the ExpressionType Bool
    private boolean checkIfClauseExpression(IfClause child) {
        return getExpressionType(child.conditionalExpression) == ExpressionType.BOOL;
    }

    // Mathmatic rules
    private void checkOperands(Operation node) {
        Expression lhs = null;
        Expression rhs = null;
        ArrayList<ASTNode> expressions = node.getChildren();
        for (ASTNode e : expressions) {
            if (lhs == null) {
                lhs = (Expression) e;
            } else {
                rhs = (Expression) e;
            }
        }

        if (rhs instanceof Operation) {
            checkOperands((Operation) rhs);
        }


        // BoolLiterals are not allowed in operations
        if (checkExpressionTypes(lhs, ExpressionType.BOOL)) {
            node.setError("Booleans are not allowed in an operation!");
        } else if (checkExpressionTypes(rhs, ExpressionType.BOOL)) {
            node.setError("Booleans are not allowed in an operation!");
        }

        // ColorLiterals are not allowed in operations
        if (checkExpressionTypes(lhs, ExpressionType.COLOR)) {
            node.setError("Colors are not allowed in an operation!");
        } else if (checkExpressionTypes(rhs, ExpressionType.COLOR)) {
            node.setError("Colors are not allowed in an operation!");
        }

        if (node instanceof AddOperation || node instanceof SubtractOperation) {
            // PercentageLiterals are not allowed in an AddOperation or SubtractOperation
            if (checkExpressionTypes(lhs, ExpressionType.PERCENTAGE)) {
                node.setError("Percentages are not allowed in an Add or Subtract operation!");
            } else if (checkExpressionTypes(rhs, ExpressionType.PERCENTAGE)) {
                node.setError("Percentages are not allowed in an Add or Subtract operation!");
            }

            // Both operands need to have an equal expression type
            if (getExpressionType(lhs) != getExpressionType(rhs)) {
                node.setError(
                        "Operations with a " + getExpressionType(lhs) + " and a " + getExpressionType(rhs) +
                                " are not allowed!");
            }
        }

        if (node instanceof MultiplyOperation) {
            // Check if both operands are of type PercentageLiteral, then set error
            if (checkExpressionTypes(lhs, ExpressionType.PERCENTAGE) && checkExpressionTypes(rhs,
                                                                                             ExpressionType.PERCENTAGE)) {
                node.setError("Multiply operations are not allowed with two percentages. One of the operands needs to" +
                                      " have a type scalar.");
            }
            // Check if both operands are not of type Pixel
            if (checkExpressionTypes(lhs, ExpressionType.PIXEL) && checkExpressionTypes(rhs, ExpressionType.PIXEL)) {
                node.setError("Multiply operations are not allowed with two percentages. One of the operands needs to" +
                                      " have a type scalar.");
            }

            // Check if one operand is a percentage and the other a pixel. Then set error
            if ((checkExpressionTypes(lhs, ExpressionType.PIXEL) && checkExpressionTypes(rhs,
                                                                                         ExpressionType.PERCENTAGE)) ||
                    (checkExpressionTypes(lhs, ExpressionType.PERCENTAGE) &&
                            checkExpressionTypes(rhs, ExpressionType.PIXEL))) {
            node.setError("Multiply operations are not allowed with a percentage and a pixel. One of the operands " +
                                  "needs to have a type scalar.");
            }
        }

    }

    // Check if the expressions are of the same ExpressionType
    private boolean checkExpressionTypes(Expression expression, ExpressionType type) {
        return getExpressionType(expression) == type;
    }

    // Check if a variable is instantiated.
    private boolean checkIfVariableIsInstantiated(VariableReference reference) {
        for (int i = 0; i < variableTypes.getSize(); ++i) {
            if (variableTypes.get(i).containsKey(reference.name)) {
                return true;
            }
        }
        return false;
    }

    // Set variables in linkedlist
    private void setVariables(ASTNode node) {
        HashMap<String, ExpressionType> map = new HashMap<>();
        map.put(((VariableAssignment) node).name.name, getExpressionType(((VariableAssignment) node).expression));
        variableTypes.addFirst(map);
    }

    // Get the expressionType of the expression
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
