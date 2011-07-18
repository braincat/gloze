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

package org.opencyc.sparql.op;/**

 */

/* This class represents a BGP compiled into CycL */

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * A Cyc implementation of a quad pattern.
 * { QUAD1 ... QUADn } becomes QUAD1' ... QUADn'
 * 
 * @author stevebattle.me
 */

public class CycOpQuadPattern extends CycOp {
	QuadPattern pattern;
	StringBuffer exp = new StringBuffer();
	
	// graph node extracted from quad pattern
	Node graphName;
	
	// keep a list of vars that need to be bound
	List<Node> vars = new LinkedList<Node>();
	
	static public boolean isValid(Op op, Context context) {
		// if there is another named graph in this context then they can't be evaluated together
		if (op!=null && OpQuadPattern.isQuadPattern(op)) {
	    	OpQuadPattern qop = (OpQuadPattern) op ;
	    	// if a filter includes a negation then the pattern must be in the default graph
	    	// otherwise the negated BGP will be evaluated in the microtheory
			if (context!=null && context.getGraphNode()==null) 
				context.setGraphNode(qop.getGraphNode()) ;
    		if (context!=null && !context.getGraphNode().equals(qop.getGraphNode())) 
    			return false;
    		return true;
		}
		return false;
	}
	
	public CycOpQuadPattern(QuadPattern pattern, Binding binding) {
		super(binding);
		this.pattern = pattern;
		graphName = graphName(pattern, binding);

		for (ListIterator<?> i = pattern.iterator(); i.hasNext();) {
			Quad quad = (Quad) i.next();
			Node s = quad.getSubject();
			Node p = quad.getPredicate();
			Node o = quad.getObject();
			String q = "("+term(p,binding)+" "+term(s,binding)+" " +term(o,binding)+")";
			exp.append(q);
			
			if (s.isVariable()) vars.add(s);
			if (p.isVariable()) vars.add(p);
			if (o.isVariable()) vars.add(o);
		}
	}
	
	public String stringValue() {
		return exp.toString();
	}
	
	public String toString() {
		if (pattern.size()==0) return "#$True" ;
		if (pattern.size()==1) return stringValue();
		else return "(#$and "+stringValue()+")";
	}
	
	public List<Node> getVars() {
		return vars;
	}
	
	public Node getGraphName() {
		return graphName;
	}

	public static Node graphName(QuadPattern pattern, Binding binding) {
		// assume all graph entries are the same within a BGP
		Node g = pattern.get(0).getGraph();
		return substitute(g, binding);
	}
}
