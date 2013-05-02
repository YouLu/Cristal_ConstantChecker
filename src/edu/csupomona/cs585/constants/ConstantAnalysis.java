package edu.csupomona.cs585.constants;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.IAnalysisReporter.SEVERITY;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;

public class ConstantAnalysis extends AbstractCrystalMethodAnalysis {

	TACFlowAnalysis<TupleLatticeElement<Variable, ConstantLatticeElement>> flowAnalysis;

	@Override
	public void analyzeMethod(MethodDeclaration d) {
		ConstantTransferFunction tf = new ConstantTransferFunction(getInput()
				.getAnnoDB());
		flowAnalysis = new TACFlowAnalysis<TupleLatticeElement<Variable, ConstantLatticeElement>>(
				tf, getInput());

		d.accept(new ConstantASTWalker());
	}

	@Override
	public String getName() {
		return "CS585:Constant Propagation Analysis";
	}

	public class ConstantASTWalker extends ASTVisitor {

		private void checkVariable(TupleLatticeElement<Variable, ConstantLatticeElement> tuple,Expression nodeToCheck) {
			Variable varToCheck = flowAnalysis.getVariable(nodeToCheck);
			ConstantLatticeElement element = tuple.get(varToCheck);
			if(!element.isInteger){
				if ((element == ConstantLatticeElement.TRUE)) {
					getReporter().reportUserProblem("The expression " + nodeToCheck + " is always true.",nodeToCheck, getName(), SEVERITY.WARNING);
				} else if (element == ConstantLatticeElement.FALSE) {
					getReporter().reportUserProblem("The expression " + nodeToCheck + " is always false.",nodeToCheck, getName(), SEVERITY.WARNING);
				} else if (element == ConstantLatticeElement.MAYBE_CONSTANT){
					getReporter().reportUserProblem("The expression " + nodeToCheck + " is MaybeConstant.",nodeToCheck, getName(), SEVERITY.INFO);
				} else {
					getReporter().reportUserProblem("The expression " + nodeToCheck + " is bottom.",nodeToCheck, getName(), SEVERITY.INFO);
				}
			}else{
				getReporter().reportUserProblem("The expression " + nodeToCheck + " is always " + element.integer + "." ,nodeToCheck, getName(), SEVERITY.WARNING);
			}
			
		}
		
		@Override
		public void endVisit(ConditionalExpression node) {
			TupleLatticeElement<Variable, ConstantLatticeElement> tuple = flowAnalysis.getResultsAfterAST(node);
			checkVariable(tuple, node.getExpression());
		}
		
		@Override
		public void endVisit(DoStatement node) {
			TupleLatticeElement<Variable, ConstantLatticeElement> tuple = flowAnalysis.getResultsAfterAST(node);
			checkVariable(tuple, node.getExpression());
		}
		
		
		@Override
		public void endVisit(ReturnStatement node) {
			TupleLatticeElement<Variable, ConstantLatticeElement> tuple = flowAnalysis.getResultsBefore(node);
			checkVariable(tuple, node.getExpression());
		}

		@Override
		public void endVisit(IfStatement node) {
			TupleLatticeElement<Variable, ConstantLatticeElement> tuple = null;
			if(node.getThenStatement().toString().contains("while") || node.getThenStatement().toString().contains("for")){
				tuple = flowAnalysis.getResultsBeforeAST(node);
			}else{
				tuple = flowAnalysis.getResultsAfter(node);
			}
			checkVariable(tuple, node.getExpression());
			
		}
		
		@Override
		public void endVisit(WhileStatement node) {
			TupleLatticeElement<Variable, ConstantLatticeElement> tuple = flowAnalysis.getResultsBefore(node);
			checkVariable(tuple, node.getExpression());
		}
		
		@Override
		public void endVisit(ForStatement node) {
			TupleLatticeElement<Variable, ConstantLatticeElement> tuple = flowAnalysis.getResultsBefore(node);
			checkVariable(tuple, node.getExpression());
		}

		@Override
		public void endVisit(SwitchStatement node) {
			TupleLatticeElement<Variable, ConstantLatticeElement> tuple = flowAnalysis.getResultsBeforeCFG(node);
			checkVariable(tuple, node.getExpression());
		}
	}

}
