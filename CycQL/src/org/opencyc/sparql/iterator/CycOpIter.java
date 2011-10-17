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

import java.util.List;

import org.opencyc.cycobject.CycList;
import org.opencyc.cycobject.CycSymbol;
import org.opencyc.sparql.CycDatasetGraph;
import org.opencyc.sparql.CycDatasetGraph.CycGraph;
import org.opencyc.sparql.CycQueryEngine;
import org.opencyc.sparql.op.CycOp;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

/**
 * An iterator over a compiled (into CycL) operator; an iterator over bindings made by the operator.
 * 
 * @author stevebattle.me
 */

public class CycOpIter extends CycQueryIter {
	BasicPattern pattern;
	CycOp cycOp;
	
	public CycOpIter(DatasetGraph dsg, CycOp cycOp, ExecutionContext context, Binding binding) {
		super(dsg,context,binding);
		this.cycOp = cycOp;

		try {
			CycGraph graph = null;
			String mt = null;
			if (cycOp.getGraphName()==null) {
				graph = (CycGraph) dsg.getDefaultGraph() ;
				Query query = (Query) context.getContext().get(CycQueryEngine.QUERY) ;
				List<String> graphs = query.getGraphURIs() ;
				List<String> namedGraphs = query.getNamedGraphURIs() ;
				
				// if no FROM clause is specified use the dataset default
				if (graphs.size()==0) {
					// if FROM NAMED are specified then the default graph is empty (null)
					if (namedGraphs.size()==0)
						mt = graph.getGraphName() ;
				}
				else if (graphs.size()==1) {
					mt = CycDatasetGraph.localName(graphs.get(0)) ;
				}
				else throw new Exception("Unable to merge multiple FROM clauses. ") ;
				query.toString();
			}
			// a variable with a known binding (likely set by calling class)
			else if (cycOp.getGraphName().isVariable()) {
				Node g = substitute(cycOp.getGraphName(), binding) ;
				graph = (CycGraph) dsg.getGraph(g);
				mt = CycDatasetGraph.localName(g.getURI());
			}
			// otherwise a constant
			else {
				graph = (CycGraph) dsg.getGraph(cycOp.getGraphName());
				mt = CycDatasetGraph.localName(cycOp.getGraphName().getURI());
			}
			CycList<Object> l = graph.query(cycOp.toString(), mt);
			if (l!=null) iterator = l.iterator();
		} catch (Exception e) {
			//e.printStackTrace();
			System.err.println(e.getMessage()) ;
		}
		// otherwise the iterator remains null
	}

	@Override
	protected Binding getBinding(Object next) {
		Binding b = new BindingMap(binding) ;
		// may be CycSymbol NIL - this is a solution with no bindings

		if (!(next instanceof CycSymbol)) {
			CycList<?> l = (CycList<?>) next;
			for (Node n: cycOp.getVars()) {
				addBinding(b, l, n) ;
			}
		}
		return b ;
	}

}
