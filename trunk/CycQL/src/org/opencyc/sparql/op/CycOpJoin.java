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

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * A Cyc implementation of a join operator.
 * OP1 . OP2 becomes OP1' OP2'
 * 
 * @author stevebattle.me
 */

public class CycOpJoin extends CycOp {
	CycOp left, right;
	
	public static boolean isValid(Op op, Context context) {
		if (op!=null && op.getName().equals("join")) {
			OpJoin opJoin = (OpJoin) op;
			if (!isValidSubOp(opJoin.getLeft(), context)
			|| !isValidSubOp(opJoin.getRight(), context)) return false;
			return true;
		}
		return false;
	}
	
	public CycOpJoin(OpJoin opJoin, Binding binding) {
		super(binding);
		left = createCycOp(opJoin.getLeft(), binding);
		right = createCycOp(opJoin.getRight(), binding);
	}

	@Override
	public Node getGraphName() {
		Node g;
		if ((g=left.getGraphName())!=null) return g;
		if ((g=right.getGraphName())!=null) return g;
		return null;
	}

	@Override
	public List<Node> getVars() {
		List<Node> vars = new LinkedList<Node>();
		vars.addAll(left.getVars());
		vars.addAll(right.getVars());
		return vars;
	}

	@Override
	public String stringValue() {
		StringBuffer str = new StringBuffer();
		str.append(left.stringValue());
		str.append(right.stringValue());
		return str.toString();
	}


}
