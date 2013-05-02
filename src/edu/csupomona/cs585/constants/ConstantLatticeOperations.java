package edu.csupomona.cs585.constants;

import edu.cmu.cs.crystal.simple.SimpleLatticeOperations;

public class ConstantLatticeOperations extends
		SimpleLatticeOperations<ConstantLatticeElement> {

	@Override
	public boolean atLeastAsPrecise(ConstantLatticeElement left,
			ConstantLatticeElement right) {
		if(left.isInteger && right.isInteger){
			return true;
		}else{
			if (left == right) {
				return true;
			} else if (left == ConstantLatticeElement.BOTTOM) {
				return true;
			} else if (right == ConstantLatticeElement.MAYBE_CONSTANT) {
				return true;
			} else {
				return false;
			}
		}
		
	}

	@Override
	public ConstantLatticeElement join(ConstantLatticeElement left,
			ConstantLatticeElement right) {
		if(left.isInteger && right.isInteger){
			if(left.integer == right.integer){
				return left;
			}else{
				return ConstantLatticeElement.MAYBE_CONSTANT;
			}
		}else {
			if (left == right) {
				return left;
			} else if (left == ConstantLatticeElement.BOTTOM) {
				return right;
			} else if (right == ConstantLatticeElement.BOTTOM) {
				return left;
			} else {
				return ConstantLatticeElement.MAYBE_CONSTANT;
			}
		}
	}

	@Override
	public ConstantLatticeElement bottom() {
		return ConstantLatticeElement.BOTTOM;
	}

	@Override
	public ConstantLatticeElement copy(ConstantLatticeElement original) {
		return original;
	}

}
