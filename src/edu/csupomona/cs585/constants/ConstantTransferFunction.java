package edu.csupomona.cs585.constants;

import java.util.List;


import org.eclipse.jdt.core.dom.MethodDeclaration;
import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.flow.BooleanLabel;
import edu.cmu.cs.crystal.flow.ILabel;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.LabeledResult;
import edu.cmu.cs.crystal.flow.LabeledSingleResult;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.simple.TupleLatticeOperations;
import edu.cmu.cs.crystal.tac.AbstractTACBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.BinaryOperator;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariableReadInstruction;
import edu.cmu.cs.crystal.tac.model.UnaryOperation;
import edu.cmu.cs.crystal.tac.model.Variable;

public class ConstantTransferFunction extends AbstractTACBranchSensitiveTransferFunction<TupleLatticeElement<Variable, ConstantLatticeElement>> {

	private AnnotationDatabase annoDB;

	TupleLatticeOperations<Variable, ConstantLatticeElement> ops = new TupleLatticeOperations<Variable, ConstantLatticeElement>(
			new ConstantLatticeOperations(), ConstantLatticeElement.MAYBE_CONSTANT);

	public ConstantTransferFunction(AnnotationDatabase annoDB) {
		this.annoDB = annoDB;
	}

	@Override
	public TupleLatticeElement<Variable, ConstantLatticeElement> createEntryValue(
			MethodDeclaration method) {
		TupleLatticeElement<Variable, ConstantLatticeElement> def = ops.getDefault();
		def.put(getAnalysisContext().getThisVariable(), ConstantLatticeElement.BOTTOM);
		return def;
	}
	
	@Override
	public IResult<TupleLatticeElement<Variable, ConstantLatticeElement>> transfer(
			LoadLiteralInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, ConstantLatticeElement> value) {
		if(instr.getTarget().resolveType().getName().equals("boolean")){
			if(labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false))){
				LabeledResult<TupleLatticeElement<Variable,ConstantLatticeElement>> result = 
						LabeledResult.createResult(value);
				
				TupleLatticeElement<Variable, ConstantLatticeElement> tVal = ops.copy(value);
				TupleLatticeElement<Variable, ConstantLatticeElement> fVal = ops.copy(value);
				
				if((Boolean)instr.getLiteral() == true){
					tVal.put(instr.getTarget(), ConstantLatticeElement.TRUE);
					fVal.put(instr.getTarget(), ConstantLatticeElement.BOTTOM);
				}else{
					tVal.put(instr.getTarget(), ConstantLatticeElement.BOTTOM);
					fVal.put(instr.getTarget(), ConstantLatticeElement.FALSE);
				}
				
				result.put(BooleanLabel.getBooleanLabel(true), tVal);
				result.put(BooleanLabel.getBooleanLabel(false), fVal);
				return result;
			}else{
				if(instr.getLiteral().equals(true)){
					value.put(instr.getTarget(), ConstantLatticeElement.TRUE);
				}else{
					value.put(instr.getTarget(), ConstantLatticeElement.FALSE);
				}
				return LabeledSingleResult.createResult(value, labels);
			}
		} else if(instr.getTarget().resolveType().getName().equals("int")){
			ConstantLatticeElement integerElement = ConstantLatticeElement.BOTTOM;
			integerElement.integer = Integer.parseInt(instr.getLiteral().toString());
			integerElement.isInteger = true;
			value.put(instr.getTarget(), integerElement);
			return LabeledSingleResult.createResult(value, labels);
		}
		return LabeledSingleResult.createResult(value, labels);
	}
	

	@Override
	public IResult<TupleLatticeElement<Variable, ConstantLatticeElement>> transfer(
			SourceVariableReadInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, ConstantLatticeElement> value) {
			if(labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false))){
				LabeledResult<TupleLatticeElement<Variable,ConstantLatticeElement>> result =
						LabeledResult.createResult(value);
				TupleLatticeElement<Variable, ConstantLatticeElement> tVal = ops.copy(value);
				TupleLatticeElement<Variable, ConstantLatticeElement> fVal = ops.copy(value);
				
				if(value.get(instr.getVariable()) == ConstantLatticeElement.TRUE){
					tVal.put(instr.getVariable(), ConstantLatticeElement.TRUE);
					fVal.put(instr.getVariable(), ConstantLatticeElement.BOTTOM);
				}else if(value.get(instr.getVariable()) == ConstantLatticeElement.MAYBE_CONSTANT){
					tVal.put(instr.getVariable(), ConstantLatticeElement.TRUE);
					fVal.put(instr.getVariable(), ConstantLatticeElement.FALSE);
				}else if(value.get(instr.getVariable()) == ConstantLatticeElement.FALSE){
					tVal.put(instr.getVariable(), ConstantLatticeElement.BOTTOM);
					fVal.put(instr.getVariable(), ConstantLatticeElement.FALSE);
				}
				
				result.put(BooleanLabel.getBooleanLabel(true), tVal);
				result.put(BooleanLabel.getBooleanLabel(false), fVal);
				return result;

			}else{
				return LabeledSingleResult.createResult(value, labels);
			}
	}
	

	@Override
	public IResult<TupleLatticeElement<Variable, ConstantLatticeElement>> transfer(
			UnaryOperation unop, List<ILabel> labels,
			TupleLatticeElement<Variable, ConstantLatticeElement> value) {
		if(labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false))){
			LabeledResult<TupleLatticeElement<Variable, ConstantLatticeElement>> result = 
					LabeledResult.createResult(value);
			TupleLatticeElement<Variable, ConstantLatticeElement> tVal = ops.copy(value);
			TupleLatticeElement<Variable, ConstantLatticeElement> fVal = ops.copy(value);
			
			if(value.get(unop.getOperand()) == ConstantLatticeElement.FALSE){
				tVal.put(unop.getTarget(), ConstantLatticeElement.TRUE);
				tVal.put(unop.getOperand(), ConstantLatticeElement.FALSE);
				fVal.put(unop.getTarget(), ConstantLatticeElement.BOTTOM);
			}else if(value.get(unop.getOperand()) == ConstantLatticeElement.MAYBE_CONSTANT){
				tVal.put(unop.getTarget(), ConstantLatticeElement.TRUE);
				tVal.put(unop.getOperand(), ConstantLatticeElement.FALSE);
				fVal.put(unop.getTarget(), ConstantLatticeElement.FALSE);
				fVal.put(unop.getOperand(), ConstantLatticeElement.TRUE);
			}else if(value.get(unop.getOperand()) == ConstantLatticeElement.TRUE){
				tVal.put(unop.getTarget(), ConstantLatticeElement.BOTTOM);
				fVal.put(unop.getTarget(), ConstantLatticeElement.FALSE);
				fVal.put(unop.getOperand(), ConstantLatticeElement.TRUE);
			}
			
			result.put(BooleanLabel.getBooleanLabel(true), tVal);
			result.put(BooleanLabel.getBooleanLabel(false), fVal);
			return result;
		}else{
			value.put(unop.getTarget(), value.get(unop.getOperand()).getOpposite());
			return LabeledSingleResult.createResult(value, labels);
		}
		
	}

	@Override
	public IResult<TupleLatticeElement<Variable, ConstantLatticeElement>> transfer(
			CopyInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, ConstantLatticeElement> value) {
		if(instr.getTarget().resolveType().getName().equals("boolean")){
			if(labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false))){
				LabeledResult<TupleLatticeElement<Variable, ConstantLatticeElement>> result = 
						LabeledResult.createResult(value);
				
				TupleLatticeElement<Variable, ConstantLatticeElement> tVal = ops.copy(value);
				TupleLatticeElement<Variable, ConstantLatticeElement> fVal = ops.copy(value);
				
				if(value.get(instr.getOperand()) == ConstantLatticeElement.TRUE){
					tVal.put(instr.getOperand(), ConstantLatticeElement.TRUE);
					tVal.put(instr.getTarget(), ConstantLatticeElement.TRUE);
					fVal.put(instr.getTarget(), ConstantLatticeElement.BOTTOM);
				}else if(value.get(instr.getOperand()) == ConstantLatticeElement.MAYBE_CONSTANT){
					tVal.put(instr.getOperand(),ConstantLatticeElement.TRUE);
					tVal.put(instr.getTarget(), ConstantLatticeElement.TRUE);
					fVal.put(instr.getOperand(), ConstantLatticeElement.FALSE);
					fVal.put(instr.getTarget(), ConstantLatticeElement.FALSE);
				}else if(value.get(instr.getOperand()) == ConstantLatticeElement.FALSE){
					tVal.put(instr.getTarget(), ConstantLatticeElement.BOTTOM);
					fVal.put(instr.getOperand(), ConstantLatticeElement.FALSE);
					fVal.put(instr.getTarget(), ConstantLatticeElement.FALSE);
				}
				
				result.put(BooleanLabel.getBooleanLabel(true), tVal);
				result.put(BooleanLabel.getBooleanLabel(false), fVal);
				return result;
			}else{
				value.put(instr.getTarget(), value.get(instr.getOperand()));
				return LabeledSingleResult.createResult(value, labels);
			}
		}else if(instr.getTarget().resolveType().getName().equals("int")){
			System.out.println("Target : " + instr.getTarget());
			System.out.println("Value : " + value.get(instr.getOperand()));
			value.put(instr.getTarget(),value.get(instr.getOperand()));
			return LabeledSingleResult.createResult(value, labels);
		}
		return LabeledSingleResult.createResult(value, labels);
		
		
	}
	
	@Override
	public IResult<TupleLatticeElement<Variable, ConstantLatticeElement>> transfer(
			MethodCallInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, ConstantLatticeElement> value) {
		if(labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false))){
			LabeledResult<TupleLatticeElement<Variable, ConstantLatticeElement>> result = 
					LabeledResult.createResult(value);
			
			TupleLatticeElement<Variable, ConstantLatticeElement> tVal = ops.copy(value);
			TupleLatticeElement<Variable, ConstantLatticeElement> fVal = ops.copy(value);
			
			tVal.put(instr.getTarget(), ConstantLatticeElement.TRUE);
			fVal.put(instr.getTarget(), ConstantLatticeElement.FALSE);
			
			result.put(BooleanLabel.getBooleanLabel(true), tVal);
			result.put(BooleanLabel.getBooleanLabel(false), fVal);
			return result;
		}else{
			value.put(instr.getTarget(), ConstantLatticeElement.MAYBE_CONSTANT);
			return LabeledSingleResult.createResult(value, labels);
		}
		
	}
	
	@Override
	public IResult<TupleLatticeElement<Variable, ConstantLatticeElement>> transfer(
			BinaryOperation binop, List<ILabel> labels,
			TupleLatticeElement<Variable, ConstantLatticeElement> value) {
			if(labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false))){
				LabeledResult<TupleLatticeElement<Variable, ConstantLatticeElement>> result = 
						LabeledResult.createResult(value);
				
				TupleLatticeElement<Variable, ConstantLatticeElement> tVal = ops.copy(value);
				TupleLatticeElement<Variable, ConstantLatticeElement> fVal = ops.copy(value);
				
				if(binop.getOperator() != BinaryOperator.REL_EQ && binop.getOperator() != BinaryOperator.REL_NEQ){
					return LabeledSingleResult.createResult(value, labels);
				}
				ConstantLatticeElement leftValue = value.get(binop.getOperand1());
				ConstantLatticeElement rightValue = value.get(binop.getOperand2());
				if(binop.getOperator() == BinaryOperator.REL_EQ){
					if(leftValue == ConstantLatticeElement.BOTTOM || rightValue == ConstantLatticeElement.BOTTOM){
						tVal.put(binop.getTarget(), ConstantLatticeElement.BOTTOM);
						fVal.put(binop.getTarget(), ConstantLatticeElement.BOTTOM);
					}else if(leftValue == ConstantLatticeElement.MAYBE_CONSTANT){
						tVal.put(binop.getTarget(), ConstantLatticeElement.TRUE);
						tVal.put(binop.getOperand1(), rightValue);
						fVal.put(binop.getTarget(), ConstantLatticeElement.FALSE);
						fVal.put(binop.getOperand1(), rightValue.getOpposite());
					}else if(rightValue == ConstantLatticeElement.MAYBE_CONSTANT){
						tVal.put(binop.getTarget(), ConstantLatticeElement.TRUE);
						tVal.put(binop.getOperand2(), leftValue);
						fVal.put(binop.getTarget(), ConstantLatticeElement.FALSE);
						fVal.put(binop.getOperand2(), leftValue.getOpposite());
					}else if(leftValue == rightValue){
						tVal.put(binop.getTarget(), ConstantLatticeElement.TRUE);
						fVal.put(binop.getTarget(), ConstantLatticeElement.BOTTOM);
					}else if(leftValue != rightValue){
						tVal.put(binop.getTarget(), ConstantLatticeElement.BOTTOM);
						fVal.put(binop.getTarget(), ConstantLatticeElement.FALSE);
					}
				}else if(binop.getOperator() == BinaryOperator.REL_NEQ){
					if(leftValue == ConstantLatticeElement.BOTTOM || rightValue == ConstantLatticeElement.BOTTOM){
						tVal.put(binop.getTarget(), ConstantLatticeElement.BOTTOM);
						fVal.put(binop.getTarget(), ConstantLatticeElement.BOTTOM);
					}else if(leftValue == ConstantLatticeElement.MAYBE_CONSTANT){
						tVal.put(binop.getTarget(), ConstantLatticeElement.TRUE);
						tVal.put(binop.getOperand1(), rightValue.getOpposite());
						fVal.put(binop.getTarget(), ConstantLatticeElement.FALSE);
						fVal.put(binop.getOperand1(), rightValue);
					}else if(rightValue == ConstantLatticeElement.MAYBE_CONSTANT){
						tVal.put(binop.getTarget(), ConstantLatticeElement.TRUE);
						tVal.put(binop.getOperand2(), leftValue.getOpposite());
						fVal.put(binop.getTarget(), ConstantLatticeElement.FALSE);
						fVal.put(binop.getOperand2(), leftValue);
					}else if(leftValue != rightValue){
						tVal.put(binop.getTarget(), ConstantLatticeElement.TRUE);
						fVal.put(binop.getTarget(), ConstantLatticeElement.BOTTOM);
					}else if(leftValue == rightValue){
						tVal.put(binop.getTarget(), ConstantLatticeElement.BOTTOM);
						fVal.put(binop.getTarget(), ConstantLatticeElement.FALSE);
					}
				}
				
				result.put(BooleanLabel.getBooleanLabel(true), tVal);
				result.put(BooleanLabel.getBooleanLabel(false), fVal);
				return result;
			}else{
				if(binop.getOperator() != BinaryOperator.REL_EQ && binop.getOperator() != BinaryOperator.REL_NEQ){
					return LabeledSingleResult.createResult(value, labels);
				}
				ConstantLatticeElement leftValue = value.get(binop.getOperand1());
				ConstantLatticeElement rightValue = value.get(binop.getOperand2());
				if(leftValue == ConstantLatticeElement.BOTTOM || rightValue == ConstantLatticeElement.BOTTOM){
					value.put(binop.getTarget(), ConstantLatticeElement.BOTTOM);
				}else if(leftValue == ConstantLatticeElement.MAYBE_CONSTANT || rightValue == ConstantLatticeElement.MAYBE_CONSTANT){
					value.put(binop.getTarget(), ConstantLatticeElement.MAYBE_CONSTANT);
				}else{
					if(binop.getOperator() == BinaryOperator.REL_EQ){
						value.put(binop.getTarget(), (leftValue == rightValue) ? ConstantLatticeElement.TRUE : ConstantLatticeElement.FALSE);
					}else if(binop.getOperator() == BinaryOperator.REL_NEQ){
						value.put(binop.getTarget(), (leftValue != rightValue) ? ConstantLatticeElement.TRUE : ConstantLatticeElement.FALSE);
					}
				}
				return LabeledSingleResult.createResult(value, labels);
			}
	}
	
	@Override
	public ILatticeOperations<TupleLatticeElement<Variable, ConstantLatticeElement>> getLatticeOperations() {
		return ops;
	}
}
