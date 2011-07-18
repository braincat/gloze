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
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


/**
 * A Cyc implementation of a sequence operator.
 * OP1 ... OPn becomes OP1' ... OPn'
 * 
 * @author stevebattle.me
 */

public class CycOpSequence extends CycOp {
	List<CycOp> exps = new LinkedList<CycOp>();
	
	public static boolean isValid(Op op, Context context) {
		if (op!=null && op.getName().equals("sequence")) {
			OpSequence opSequence = (OpSequence) op;
			for (int i=0; i<opSequence.size() ; i++) {
				if (!isValidSubOp(opSequence.get(i), context)) return false;
			}
			return true;
		}
		return false;
	}
	
	public CycOpSequence(OpSequence opSequence, Binding binding) {
		super(binding);
		for (int i=0; i<opSequence.size() ; i++) {
			exps.add(createCycOp(opSequence.get(i), binding));
		}
	}

	@Override
	public Node getGraphName() {
		for (int i=0; i<exps.size(); i++) {
			Node g = exps.get(i).getGraphName();
			if (g!=null) return g;
		}
		return null;
	}

	@Override
	public List<Node> getVars() {
		List<Node> vars = new LinkedList<Node>();
		for (int i=0; i<exps.size(); i++) {
			vars.addAll(exps.get(i).getVars());
		}
		return vars;
	}

	@Override
	public String stringValue() {
		StringBuffer str = new StringBuffer();
		for (int i=0; i<exps.size(); i++) {
			str.append(exps.get(i).stringValue());
		}
		return str.toString();
	}

}
