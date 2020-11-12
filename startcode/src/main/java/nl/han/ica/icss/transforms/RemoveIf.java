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
        for (ASTNode parent : ast.root.getChildren()) { // Loop through the children of Stylesheet
            if (parent instanceof Stylerule) {
                for (ASTNode ifClause : parent.getChildren()) { // Loop through the children StyleRule
                    if (ifClause instanceof IfClause) {
                        ArrayList<ASTNode> nodes = checkIfClause(ifClause);
                        parent.removeChild(ifClause);
                        for (ASTNode n : nodes) {        // Loop through the children of IfClause
                            parent.addChild(n);
                        }
                    }
                }
            }
        }
    }


    private ArrayList<ASTNode> checkIfClause(ASTNode node) {
        ArrayList<ASTNode> ifClauseNodes = new ArrayList<>();
        IfClause ifClause = (IfClause) node;
        BoolLiteral condition = (BoolLiteral) ifClause.conditionalExpression;
        // Check if clause is true
        if (condition.value) {
            // Check if there are recursive if clauses in the body
            ArrayList<ASTNode> selectedIfClause = new ArrayList<>();
            for (ASTNode recursiveIfClause : ifClause.body) {
                if (recursiveIfClause instanceof IfClause) {
                    selectedIfClause.add(recursiveIfClause);
                    ArrayList<ASTNode> n = checkIfClause(recursiveIfClause);
                    ifClauseNodes.addAll(n);
                }
            }
            // remove recursive ifClauses
            for (int i = 0; i < selectedIfClause.size(); ++i) {
                ifClause.body.remove(selectedIfClause.get(i));
            }

            // second loop, to be sure that recursive ifclauses are done
            for (ASTNode declaration : ifClause.body) {
                if (declaration instanceof Declaration) {
                    ifClauseNodes.add(declaration);
                }
            }
        } else if (ifClause.elseClause != null) {
            ElseClause elseClause = ifClause.elseClause;
            ArrayList<ASTNode> selectedIfClause = new ArrayList<>();
            for (ASTNode recursiveIfClause : elseClause.body) {
                if (recursiveIfClause instanceof IfClause) {
                    selectedIfClause.add(recursiveIfClause);
                    elseClause.body.remove(recursiveIfClause);
                    ArrayList<ASTNode> n = checkIfClause(recursiveIfClause);
                    ifClauseNodes.addAll(n);
                }
            }
            for (int i = 0; i < selectedIfClause.size(); ++i) {
                ifClause.body.remove(selectedIfClause.get(i));
            }
            // second loop to be sure that ifclauses in else body are done
            for (ASTNode declaration : elseClause.body) {
                if (declaration instanceof Declaration) {
                    ifClauseNodes.add(declaration);
                }
            }
        } else {
            return new ArrayList<>();
        }
        return ifClauseNodes;
    }
}
