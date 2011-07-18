/*  Copyright 2010-11 Gloze Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 *  
 */

package org.opencyc.sparql.op;

import static org.opencyc.sparql.CycDatasetGraph.cycVar;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;

/** 
 * A Cyc implementation of assignment operator
 * LET (X1 := Y1 ... Xn := Yn) becomes ((#$evaluate X1' Y1') ... (#$evaluate Xn' Yn'))
 * 
 * @author stevebattle.me
 */

public class CycOpAssign extends CycOp {
	StringBuffer assign = new StringBuffer();
	List<Var> eval = new LinkedList<Var>();
	CycOp exp;
	
	public static boolean isValid(Op op, Context context) {
		if (op!=null && op.getName().equals("assign")) {
			OpAssign opAssign = (OpAssign) op;
			for (Var var : opAssign.getVarExprList().getVars()) {
				if (expression(opAssign.getVarExprList().getExpr(var),null,null, context)==null) 
	        		return false;
			}
			return isValidSubOp(opAssign.getSubOp(), context);
		}
		return false;
	}
	
	public CycOpAssign(OpAssign opAssign, Binding binding) {
		super(binding);
		exp = createCycOp(opAssign.getSubOp(), binding);
        // then add the assignments
		VarExprList vars = opAssign.getVarExprList();
		for (Var var : vars.getVars()) {
			eval.add(var);
			Expr e = vars.getExpr(var);
        	atomizeNATs(e,assign,binding) ;
        	assign.append('(');
        	assign.append("#$evaluate ?"+cycVar(var.getName())+" ");
        	assign.append(expression(e,mapNATtoVar,binding,null)) ;
        	assign.append(')');
        }
	}

	@Override
	public Node getGraphName() {
		return exp.getGraphName();
	}

	@Override
	public List<Node> getVars() {
		List<Node> vars = new LinkedList<Node>();
		vars.addAll(eval);
		vars.addAll(exp.getVars()) ;
		return vars;
	}

	@Override
	public String stringValue() {
		return exp.stringValue() + assign.toString();
	}

}
