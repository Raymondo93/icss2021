package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.ElseClause;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.ArrayList;

public class RemoveIf implements Transform {

    @Override
    public void apply(AST ast) {
        for (ASTNode parent: ast.root.getChildren()) { // Loop through the children of Stylesheet
            if (parent instanceof Stylerule) {
                for (ASTNode ifClause: parent.getChildren()) { // Loop through the children StyleRule
                    if (ifClause instanceof IfClause) {
                        ArrayList<ASTNode> nodes = checkIfClause(ifClause);
                        parent.removeChild(ifClause);
                        for (ASTNode n: nodes) {        // Loop through the children of IfClause
                            parent.addChild(n);
                        }
                    }
                }
            }
        }
    }


    private ArrayList<ASTNode> checkIfClause(ASTNode node) {
        IfClause ifClause = (IfClause) node;
        BoolLiteral condition = (BoolLiteral) ifClause.conditionalExpression;
        ArrayList<ASTNode> nodes = new ArrayList<>();
        if (condition.value) {
            for (ASTNode declaration : ifClause.body) {   // Loop through Declarations
                if (declaration instanceof Declaration) {
                    nodes.add(declaration);
                } else if (declaration instanceof IfClause) {
                    ArrayList<ASTNode> recursiveIfs;
                    recursiveIfs = checkIfClause(declaration);
                    // Loop through recursive if clauses and add to arraylist
                    for (ASTNode recursiveDeclaration : recursiveIfs) {
                        if (recursiveDeclaration instanceof Declaration) {
                            nodes.add(recursiveDeclaration);
                        }
                    }
                }
            }
        } else {
            for (ASTNode elseClause: node.getChildren()) { // Loop through else clause
                if (elseClause instanceof ElseClause) {
                    for (ASTNode declaration : ((ElseClause) elseClause).body) {
                        nodes.add(declaration);
                    }
                }
            }
        }
        for (ASTNode recursiveIfClauses: node.getChildren()) {
            if (recursiveIfClauses instanceof IfClause) { // Loop through recursive if clauses and call this function
                ArrayList<ASTNode> n;
                n = checkIfClause(recursiveIfClauses);
                nodes.addAll(n);
            }
        }
        return nodes;
    }
}
