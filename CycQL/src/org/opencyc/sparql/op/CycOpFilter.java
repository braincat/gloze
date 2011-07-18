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

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * A Cyc implementation of a filter operator.
 * { PATTERN } FILTER (EXP1...EXPn) becomes PATTERN' EXP1'...EXPn'
 * 
 * @author stevebattle.me
 */

public class CycOpFilter  extends CycOp {
	StringBuffer filter = new StringBuffer();
	CycOp exp;
	
	public CycOpFilter(OpFilter opFilter, Binding binding) {
		super(binding);
		exp = createCycOp(opFilter.getSubOp(), binding);
		// then add the filters
        ExprList exprs = opFilter.getExprs() ;
        for (int i=0; i<exprs.size(); i++) {
        	Expr e = exprs.get(i) ;
        	atomizeNATs(e,filter,binding) ;
        	filter.append(expression(e,mapNATtoVar,binding,null)) ;
        }
	}

	
	public static boolean isValid(Op op, Context context) {
		if (op!=null && op.getName().equals("filter")) {
			OpFilter opFilter = (OpFilter) op;
			Op subOp = opFilter.getSubOp();
			if (isValidSubOp(subOp, context)) {
		        ExprList exprs = opFilter.getExprs() ;
		        for (int i=0; i<exprs.size(); i++) {
		        	if (expression(exprs.get(i),null,null, context)==null) 
		        		return false;
		        }
		        return true;
			}
		}
		return false;
	}

	@Override
	public Node getGraphName() {
		return exp.getGraphName();
	}

	@Override
	public List<Node> getVars() {
		return exp.getVars();
	}
	
	public String stringValue() {
		return exp.stringValue() + filter.toString();
	}

}
