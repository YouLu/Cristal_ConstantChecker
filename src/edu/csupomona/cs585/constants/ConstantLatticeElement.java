package edu.csupomona.cs585.constants;

public enum ConstantLatticeElement {
	BOTTOM, FALSE, TRUE, MAYBE_CONSTANT;
	
	static int integer;
	
	boolean isInteger;
	
	public int getInt(){
		return this.integer;
	}

	public ConstantLatticeElement getOpposite() {
		if(isInteger){
			this.integer = 0 - this.integer;
			return this;
		}else{
			if (this == FALSE)
				return TRUE;
			else if (this == TRUE)
				return FALSE;
			else
				return this;
		}
		
	}
}
