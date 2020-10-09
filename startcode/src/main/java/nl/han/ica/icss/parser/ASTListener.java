package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.ElseClause;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.PropertyName;
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
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {
        return ast;
    }


	@Override
	public void enterStyleBlock(ICSSParser.StyleBlockContext context) {
		currentContainer.push(new Declaration());
	}

	@Override
	public void enterStyleRule(ICSSParser.StyleRuleContext context) {
		currentContainer.push(new Stylerule());
	}

	@Override
	public void exitStyleRule(ICSSParser.StyleRuleContext context) {
		Stylerule stylerule = (Stylerule) currentContainer.pop();
		ast.root.addChild(stylerule);
	}

	@Override
	public void exitStyleBlock(ICSSParser.StyleBlockContext context) {
		Declaration declaration = (Declaration) currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}

	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext context) {
		currentContainer.push(new VariableAssignment());
	}

	@Override
	public void enterVariableReference(ICSSParser.VariableReferenceContext context) {
		currentContainer.peek().addChild(new VariableReference(context.getText()));
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext context) {
		VariableAssignment assignment = (VariableAssignment) currentContainer.pop();
		ast.root.addChild(assignment);
	}

	@Override
	public void enterSelector(ICSSParser.SelectorContext context) {
		if (context.classSelector() != null) {
			currentContainer.push(new ClassSelector(context.getText()));
		} else if (context.idSelector() != null) {
			currentContainer.push(new IdSelector(context.getText()));
		} else if (context.tagSelector() != null) {
			currentContainer.push(new TagSelector(context.getText()));
		}
	}

	@Override
	public void exitSelector(ICSSParser.SelectorContext context) {
		if (context.classSelector() != null) {
			Selector selector = (ClassSelector) currentContainer.pop();
			currentContainer.peek().addChild(selector);
		} else if (context.idSelector() != null) {
			Selector selector = (IdSelector) currentContainer.pop();
			currentContainer.peek().addChild(selector);
		} else if (context.tagSelector() != null) {
			Selector selector = (TagSelector) currentContainer.pop();
			currentContainer.peek().addChild(selector);
		}
	}

	@Override
	public void enterLiteral(ICSSParser.LiteralContext context) {
		if (context.bool() != null) {
			currentContainer.peek().addChild(new BoolLiteral(context.getText()));
		} else if (context.percentage() != null) {
			currentContainer.peek().addChild(new PercentageLiteral(context.getText()));
		} else if (context.color() != null) {
			currentContainer.peek().addChild(new ColorLiteral(context.getText()));
		} else if (context.pixel() != null) {
			currentContainer.peek().addChild(new PixelLiteral(context.getText()));
		} else if (context.scalar() != null) {
			currentContainer.peek().addChild(new ScalarLiteral(context.getText()));
		}
	}

	@Override
	public void enterOperation(ICSSParser.OperationContext context) {
		if (context.sum() != null) {
			currentContainer.push(new AddOperation());
		} else if (context.sub() != null) {
			currentContainer.push(new SubtractOperation());
		} else if (context.multiply() != null) {
			currentContainer.push(new MultiplyOperation());
		}
	}

	@Override
	public void exitOperation(ICSSParser.OperationContext context) {
		if (context.sum() != null) {
			AddOperation operation = (AddOperation) currentContainer.pop();
			currentContainer.peek().addChild(operation);
		} else if (context.sub() != null) {
			SubtractOperation operation = (SubtractOperation) currentContainer.pop();
			currentContainer.peek().addChild(operation);
		} else if (context.multiply() != null) {
			MultiplyOperation operation = (MultiplyOperation) currentContainer.pop();
			currentContainer.peek().addChild(operation);
		}
	}

	@Override
	public void exitPropertyName(ICSSParser.PropertyNameContext context) {
		currentContainer.peek().addChild(new PropertyName(context.getText()));
	}

	@Override
	public void enterIfClause(ICSSParser.IfClauseContext context) {
		currentContainer.push(new IfClause());
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext context) {
		IfClause clause = (IfClause) currentContainer.pop();
		currentContainer.peek().addChild(clause);
	}

	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext context){
		currentContainer.push(new ElseClause());
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext context) {
		ElseClause clause = (ElseClause) currentContainer.pop();
		currentContainer.peek().addChild(clause);
	}

}