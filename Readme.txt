Author : You Lu
Email: youlu@csupomona.edu

1.Booleans Propagation:
      The transfer functions I did are:
      		(1) LoadLiteralInstruction
      		(2) SourceVariableReadInstruction
      		(3) UnaryOperation
      		(4) CopyInstruction
      		(5) MethodCallInstruction
      		(6) BinaryOperation (== and !=)
      I added branch sensitivity to all of them.(I implemented all the transfer functions with branch sensitivity in the 
      written solutions you gave.) 
      I did the bonus part for conditional expression and return statement.  
2.Integer Propagation:
      I changed the lattice by adding an integer type to it, and implemented atLeastAsPrecise and join methods.
      I did LoadLiteralInstruction and CopyInstruction transfer functions.
      
Thanks.