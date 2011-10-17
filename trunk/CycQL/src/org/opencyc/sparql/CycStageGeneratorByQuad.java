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

package org.opencyc.sparql;

import java.util.Iterator;
import java.util.List;

import org.opencyc.sparql.iterator.QueryIterByQuad;
import org.opencyc.sparql.iterator.QueryIterByTriple;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;
import com.hp.hpl.jena.util.iterator.NullIterator;

/**
 * This class demonstrates fine grained query evaluation, by triple (for BGP) or quad (for NGP).
 * This is functionally correct, but requires many interactions with the Cyc server.
 * As such, this code is for demonstration/test purposes only.
 * 
 * @author stevebattle.me
 */

public class CycStageGeneratorByQuad extends CycStageGeneratorBase {
		
	/**
	 * Iterate over a basic graph pattern containing a number of triples
	 * The individual triple iterators, QueryIterByTriple, are chained together combinatorially.
	 * @see com.hp.hpl.jena.sparql.engine.iterator.QueryIterBlockTriples
	 */
	
	public class QueryIterBasicPattern extends QueryIter1 {
		BasicPattern pattern ;
		QueryIterator iterator ;
		
		class QueryIterTriple extends QueryIterRepeatApply {
			private Triple triple;
			public QueryIterTriple(QueryIterator input, Triple triple, ExecutionContext context) {
				super(input, context);
				this.triple = triple ;
			}
			@Override
			protected QueryIterator nextStage(Binding binding) {
				return new QueryIterByTriple(triple, binding, getExecContext());
			}
		}
		
		public QueryIterBasicPattern(QueryIterator input, BasicPattern pattern, ExecutionContext context) {
			super(input, context) ;
			this.pattern = pattern ;
			// iterate over the basic pattern and combine the results
			iterator = getInput() ;
			for (Triple t : pattern.getList()) {
				iterator = new QueryIterTriple(iterator, t, context) ;
			}
		}
		@Override
		protected boolean hasNextBinding() {
			return iterator.hasNext();
		}
		@Override
		protected Binding moveToNextBinding() {
			return iterator.next();
		}
		@Override
		protected void closeSubIterator() {
			if (iterator!=null) iterator.close() ;
			iterator = null ;
		}		
	}
	
	/** 
	 * For a given quad pattern, repeatedly execute nextStage() for each input binding.
	 * If the graph node is unbound then iterate over available named graphs.
	 */
	
	public class QueryIterQuadPattern extends QueryIter1 {
		QuadPattern pattern ;
		QueryIterator iterator ;
		
		class QueryIterQuad extends QueryIterRepeatApply {
			private Quad quad;
			public QueryIterQuad(QueryIterator input, Quad quad, ExecutionContext context) {
				super(input, context);
				this.quad = quad ;
			}
			@Override
			protected QueryIterator nextStage(Binding binding) {
				Node graphNode = this.quad.getGraph() ;
				if (substitute(graphNode,binding).isVariable()) {
					QueryIterConcat it = new QueryIterConcat(getExecContext()) ;
					for (Iterator<Node> named = listGraphNodes(getExecContext()); named.hasNext() ;) {
						Binding b = bind(graphNode, named.next(), binding) ;
						// the iterator below returns the graph binding to subsequent stages
						it.add(new QueryIterByQuad(quad, b, getExecContext())) ;
					}
					return it ;
				}
				else return new QueryIterByQuad(quad, binding, getExecContext());
			}
		}
		
		public QueryIterQuadPattern(QueryIterator input, QuadPattern pattern, ExecutionContext context) {
			super(input, context) ;
			this.pattern = pattern ;

			// iterate over the quad pattern and combine the results
			iterator = getInput() ;
			for (Quad q : pattern.getList()) {
				iterator = new QueryIterQuad(iterator, q, context) ;
			}
		}
		@Override
		protected boolean hasNextBinding() {
			return iterator.hasNext() ;
		}
		@Override
		protected Binding moveToNextBinding() {
			return iterator.next();
		}
		@Override
		protected void closeSubIterator() {
			if (iterator!=null) iterator.close() ;
			iterator = null ;			
		}
	}
	
	static Iterator<Node> listGraphNodes(ExecutionContext context) {
		final CycDatasetGraph dsg = (CycDatasetGraph) context.getDataset() ;
		// are the named graphs specified in the query
		Query query = (Query) context.getContext().get(CycQueryEngine.QUERY) ;

		final List<String> namedGraphs = query.getNamedGraphURIs() ;
		if (namedGraphs.size()>0) {
			return new Iterator<Node>() {
				int i=0 ;
				public boolean hasNext() {
					return i < namedGraphs.size();
				}
				public Node next() {
					return Node.createURI(dsg.getURI(namedGraphs.get(i++))) ;
				}
				public void remove() {}		
			};
		}
		// otherwise enumerate over named graphs defined in the dataset graph
		//return dsg.listGraphNodes() ;
		return new NullIterator<Node>();
	}

	
    protected static Node substitute(Node node, Binding binding) {
        if (binding!=null && Var.isVar(node)) {
            Node x = binding.get(Var.alloc(node)) ;
            if (x != null) return x ;
        }
        return node ;
    }
    
	protected Binding bind(Node varNode, Node valNode, Binding binding) {
		Binding b = new BindingMap(binding) ;
		Var var = Var.alloc(varNode.getName()) ;
		b.add(var, valNode) ;
		return b;
	}

    
    public CycStageGeneratorByQuad(StageGenerator chainedStageGenerator) {
		super();
	}
	
	@Override
	public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext context) {
		return new QueryIterBasicPattern(input, pattern, context) ;
	}
	
	/* NGPs are evaluated by iterating over the possible graphs */

	public QueryIterator execute(QuadPattern pattern, QueryIterator input, ExecutionContext context) {
		return new QueryIterQuadPattern(input, pattern, context) ;
	}

}
