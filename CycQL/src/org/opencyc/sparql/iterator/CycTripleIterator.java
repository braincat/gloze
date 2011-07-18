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

package org.opencyc.sparql.iterator;

import java.util.ListIterator;

import org.opencyc.cycobject.CycList;
import org.opencyc.cycobject.CycSymbol;
import org.opencyc.cycobject.CycVariable;
import org.opencyc.sparql.CycDatasetGraph;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
 * Iterate over solutions to a single triple.
 * @see org.opencyc.sparql.CycDatasetGraph
 * 
 * @author stevebattle.me
 */

@SuppressWarnings("rawtypes")
public class CycTripleIterator extends NiceIterator implements ExtendedIterator {
	CycList<?> ans;
	Node s, p, o;
	ListIterator<?> li = null;
	Triple first = null;
	CycDatasetGraph dataset ;
	
	public CycTripleIterator(Node s, Node p, Node o, CycList<?> ans, CycDatasetGraph dataset) {
		super();
		this.ans = ans;
		this.s = s;
		this.p = p;
		this.o = o;
		this.dataset = dataset ;
		li = ans.listIterator();
		first();
	}

	public CycTripleIterator() {
		super() ;
	}

	private void first() {
		if (li!=null && li.hasNext()) {
			Object row = li.next();
			// may be CycSymbol NIL - this means a solution with no bindings
			if (row instanceof CycSymbol) first = triple(null);
			else first = triple((CycList<?>) row);
		}
		else first = null;
	}

	private Triple triple(CycList<?> row) {
		return new Triple(answer(s,"S",row),answer(p,"P",row),answer(o,"O",row));
	}
	
	/* At this point we append a namespace to CYC terms */

	private Node answer(Node n, String defaultVar, CycList<?> row) {
		// A Node has five sub-types: Node_Blank, Node_Anon, Node_URI, Node_Variable, and Node_ANY
		if (n.isVariable()) {
			// get variable binding
			return lookup(row,n.getName());
		}
		else if (n instanceof Node_ANY) {
			// use the default variable, S, P, or O
			return lookup(row, defaultVar);
		}
		else return n;
	}

	private Node lookup(CycList<?> row, String var) {
		ListIterator<?> i=row.listIterator() ;
		while (i.hasNext()) {
			CycList<?> l = (CycList<?>) i.next();
			if (dataset.variableMatch((CycVariable) l.first(), var)) {
				return dataset.createNode(l) ;
			}
		}
		return null;
	}

	public boolean hasNext() {
		return first!=null;
	}

	public Object next() {
		Triple next = first;
		first();
		return next;
	}

}
