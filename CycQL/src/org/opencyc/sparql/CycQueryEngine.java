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

import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;

/**
 * To work with named graphs we define a CycQueryEngine that overrides QueryEngineMain.modifyOp(). It's factory is registered in DemoQuery with:
 *	QueryEngineRegistry.addFactory(new CycQueryEngine.Factory()) ;
 *	
 * @author stevebattle.me
 */

public class CycQueryEngine extends QueryEngineMain {
	private Binding startBinding;
	Query query ;
	public static final Symbol QUERY = Symbol.create("QUERY") ;
	
	static public class Factory implements QueryEngineFactory {

		@Override
		public boolean accept(Query query, DatasetGraph dataset, Context context) {
			return true;
		}

		@Override
		public boolean accept(Op op, DatasetGraph dataset, Context context) {
			return false;
		}

		@Override
		public Plan create(Query query, DatasetGraph dataset, Binding initial, Context context) {
	        try {
				return new CycQueryEngine(query, dataset, initial, context).getPlan() ;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public Plan create(Op op, DatasetGraph dataset, Binding initial, Context context) {
			return null;
		}

	}

	class NullTransform extends TransformCopy {
		@Override
		public Op transform(OpBGP opBGP) {
			return opBGP;
		}
	}

	public CycQueryEngine(Query query, DatasetGraph dataset, Binding initial, Context context) throws Exception {
		super(query, dataset, initial, context);
		// duplicating QueryEngineBase but not visible
		startBinding = initial;
		// save query for eval()
		this.query = query ;
		
		// The CycQL adapter cannot handle multiple FROM clauses.
		List<String> graphs = query.getGraphURIs() ;
		if (graphs.size()>1)
			throw new Exception("unable to merge multiple FROM clauses") ;
	}

	public CycQueryEngine(Query query, DatasetGraph dataset) throws Exception {
		this(query, dataset, null, null);
	}

	@Override
	public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context) {
		op = Transformer.transform(new NullTransform(), op);
		// set the full query in the current context for graph name resolution
		context.set(QUERY, query) ;
		return super.eval(op, dsg, input, context);
	}

	@Override
	protected Op modifyOp(Op op) {
		// Cope with initial bindings.
		op = Substitute.substitute(op, startBinding);
		// Use standard optimizations.
		op = super.modifyOp(op);
		// return quads
		return Algebra.toQuadForm(op);
	}

}
