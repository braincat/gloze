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

import org.opencyc.sparql.CycDatasetGraph;
import org.opencyc.sparql.CycQueryEngine;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

/**
 * A simple query iterator that iterates solutions to single triples.
 * 
 * @author stevebattle.me
 */

public class QueryIterByTriple extends QueryIterBase {
	Triple triple;
	Binding binding ;
	
	public QueryIterByTriple(Triple triple, Binding binding, ExecutionContext context) {
		super(context) ;
		this.binding = binding ;
		this.triple = substitute(triple, binding) ;
		CycDatasetGraph dsg = (CycDatasetGraph) context.getDataset() ;
		
		Node s = this.triple.getSubject() ;
		Node p = this.triple.getPredicate() ;
		Node o = this.triple.getObject() ;
		
		Query query = (Query) getExecContext().getContext().get(CycQueryEngine.QUERY) ;
		List<String> graphs = query.getGraphURIs() ;
		final List<String> namedGraphs = query.getNamedGraphURIs() ;

		// the default graph is defined to be empty if FROM is undefined and FROM NAMED are defined 
		if (namedGraphs.size()>0 && graphs.size()==0) {
			this.iterator = null ;
		}
		// use the specified graph if defined in the query
		else if (graphs.size()==1) {
			Node graphNode = Node.createURI(dsg.getURI(graphs.get(0))) ;
			Graph g = dsg.getGraph(graphNode) ;
			this.iterator = g.find(s, p, o) ;
		}
		// otherwise use the default graph defined by the dataset
		else {
			Graph g = dsg.getDefaultGraph() ;
			this.iterator = g.find(s, p, o) ;
		}
	}
	
	@Override
	protected Binding getBinding(Object triple) {
		Binding b = new BindingMap(binding) ;
		bind(b, this.triple.getSubject(), ((Triple) triple).getSubject()) ;
		bind(b, this.triple.getPredicate(), ((Triple) triple).getPredicate()) ;
		bind(b, this.triple.getObject(), ((Triple) triple).getObject()) ;
		return b ;
	}

}
