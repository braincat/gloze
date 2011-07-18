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
/**
 */

/* This class represents a BGP compiled into CycL */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * A Cyc implementation of a BGP.
 * { TRIPLE1 ... TRIPLEn } becomes TRIPLE1' ... TRIPLEn
 * 
 * @author stevebattle.me
 */

public class CycOpBGP extends CycOp {
	BasicPattern pattern;
	StringBuffer exp = new StringBuffer();
	
	// graph node extracted from quad pattern
	Node graphName;
	
	// keep a list of vars that need to be bound
	List<Node> vars = new LinkedList<Node>();

	public CycOpBGP(BasicPattern pattern, Binding binding) {
		super(binding);
		this.pattern = pattern;
		// leave the graphNode nil to signify this is a triple pattern

		for (Iterator<Triple> i = pattern.iterator(); i.hasNext();) {
			Triple triple = (Triple) i.next();
			Node s = triple.getSubject();
			Node p = triple.getPredicate();
			Node o = triple.getObject();
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
