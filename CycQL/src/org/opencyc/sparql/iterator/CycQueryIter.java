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

import static org.opencyc.sparql.CycDatasetGraph.cycVar;

import java.util.ListIterator;

import org.opencyc.cycobject.CycList;
import org.opencyc.cycobject.CycVariable;
import org.opencyc.sparql.CycDatasetGraph;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A base class for Cyc specific QueryIterators providing common methods.
 * @see org.opencyc.sparql.iterator.CycOpIter
 * 
 * @author stevebattle.me
 */

public abstract class CycQueryIter extends QueryIterBase {

	Binding binding;
	CycDatasetGraph dataset ;
	
	public CycQueryIter(DatasetGraph dsg, ExecutionContext context, Binding binding) {
		super(context);
		dataset = (CycDatasetGraph) dsg ;
		this.binding = binding;
	}

	protected String term(Node node) {
		node = substitute(node, binding);
		// the underscore character is invalid in SPARQL variable names,  but not Cyc variable names
		if (node.isVariable()) return "?"+cycVar(node.getName());
		else if (node.isURI() && node.getURI().equals(RDF.type.getURI())) return "#$isa";
		else return "#$"+CycDatasetGraph.localName(node.getURI());
	}
	
	protected void addBinding(Binding b, CycList<?> row, Node n) {
		if (!n.isVariable()) return;
		ListIterator<?> i=row.listIterator() ;
		while (i.hasNext()) {
			CycList<?> l = (CycList<?>) i.next();
			String name = cycVar(n.getName());
			if (dataset.variableMatch((CycVariable) l.first(), name)) {
				Var var = Var.alloc(n.getName()) ;
				if (!b.contains(var)) b.add(var, dataset.createNode(l)) ;
			}
		}
	}
	
    protected static Node substitute(Node node, Binding binding) {
        if (Var.isVar(node)) {
            Node x = binding.get(Var.alloc(node)) ;
            if (x != null) return x ;
        }
        return node ;
    }

}
