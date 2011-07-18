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

import org.opencyc.sparql.iterator.CycOpIter;
import org.opencyc.sparql.op.CycOpBGP;
import org.opencyc.sparql.op.CycOpFilter;
import org.opencyc.sparql.op.CycOpJoin;
import org.opencyc.sparql.op.CycOpQuadPattern;
import org.opencyc.sparql.op.CycOpSequence;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;

/** 
 * Operators to be compiled into CycL are routed by a CycOpExecutor to a CycStageGenerator. 
 * The CycStageGenerator executes triple/quad patterns and other algebraic operations and returns a BindingIterator. 
 * Patterns and operators are compiled into a CycL query and then evaluated to produce Bindings.
 * This is registered in DemoQuery with:
 *
 *	Context context = ARQ.getContext();
 *	context.set(ARQ.stageGenerator, new CycStageGenerator()) ;
 *
 *  @author stevebattle.me
 */

public class CycStageGenerator extends CycStageGeneratorBase {
	
	/* For a given quad pattern, repeatedly execute nextStage() for each input binding */

	protected class NGPIter extends QueryIterRepeatApply {
		QuadPattern pattern;
		public NGPIter(QuadPattern pattern, QueryIterator input, ExecutionContext context) {
			super(input, context);
			this.pattern = pattern;
		}
		protected QueryIterator nextStage(Binding binding) {
			// sum bindings over matching graphs
			QueryIterConcat it = new QueryIterConcat(getExecContext()) ;
			// iterate over all named graphs in the model
			DatasetGraph dsg = getExecContext().getDataset() ;
			// in case the graph is unbound, iterate over named graphs
			Node name = CycOpBGP.graphName(pattern, binding); 
			if (name.isVariable()) {
				for (Iterator<Node> named = dsg.listGraphNodes(); named.hasNext() ;) {
					// bind the graph name so this is available in the BGP
					Binding b = bind(name, named.next(), binding);
					it.add(new CycOpIter(dsg,new CycOpQuadPattern(pattern,b),getExecContext(), b));
				}
			}
			// add an iterator in case the graph name is constant or bound
			else it.add(new CycOpIter(dsg,new CycOpQuadPattern(pattern,binding),getExecContext(), binding));
			return it ;
		}		
	}

	protected Binding bind(Node varNode, Node valNode, Binding binding) {
		Binding b = new BindingMap(binding) ;
		Var var = Var.alloc(varNode.getName()) ;
		b.add(var, valNode) ;
		return b;
	}

	protected class BGPIter extends QueryIterRepeatApply {
		BasicPattern pattern;
		public BGPIter(BasicPattern pattern, QueryIterator input, ExecutionContext context) {
			super(input, context);
			this.pattern = pattern;
		}
		protected QueryIterator nextStage(Binding binding) {
			DatasetGraph dsg = getExecContext().getDataset() ;
			return new CycOpIter(dsg, new CycOpBGP(pattern, binding), getExecContext(), binding);
		}		
	}

	protected class FilterIter extends QueryIterRepeatApply {
		OpFilter opFilter;
		public FilterIter(OpFilter opFilter, QueryIterator input, ExecutionContext context) {
			super(input, context);
			this.opFilter = opFilter;
		}
		@Override
		protected QueryIterator nextStage(Binding binding) {
			DatasetGraph dsg = getExecContext().getDataset() ;
			return new CycOpIter(dsg, new CycOpFilter(opFilter, binding), getExecContext(), binding);
		}
	}
	
	protected class SequenceIter extends QueryIterRepeatApply {
		OpSequence opSequence;
		public SequenceIter(OpSequence opSequence, QueryIterator input, ExecutionContext context) {
			super(input, context);
			this.opSequence = opSequence;
		}
		@Override
		protected QueryIterator nextStage(Binding binding) {
			DatasetGraph dsg = getExecContext().getDataset() ;
			return new CycOpIter(dsg, new CycOpSequence(opSequence, binding), getExecContext(), binding);
		}
	}

	protected class JoinIter extends QueryIterRepeatApply {
		OpJoin opJoin;
		public JoinIter(OpJoin opJoin, QueryIterator input, ExecutionContext context) {
			super(input, context);
			this.opJoin = opJoin;
		}
		@Override
		protected QueryIterator nextStage(Binding binding) {
			DatasetGraph dsg = getExecContext().getDataset() ;
			return new CycOpIter(dsg, new CycOpJoin(opJoin, binding), getExecContext(), binding);
		}
	}

	/* execute BGPs */
	
	@Override
	public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext context) {
		return new BGPIter(pattern, input, context) ;
	}
	
	/* NGPs are evaluated by iterating over possible graphs */
	
	@Override
	public QueryIterator execute(QuadPattern pattern, QueryIterator input, ExecutionContext context) {
		return new NGPIter(pattern, input, context) ;
	}
	
	@Override
	public QueryIterator execute(OpFilter opFilter, QueryIterator input, ExecutionContext context) {
		return new FilterIter(opFilter, input, context) ;
	}

	@Override
	public QueryIterator execute(OpSequence opSequence, QueryIterator input, ExecutionContext context) {
		return new SequenceIter(opSequence, input, context) ;
	}

	@Override
	public QueryIterator execute(OpJoin opJoin, QueryIterator input, ExecutionContext context) {
		return new JoinIter(opJoin, input, context) ;
	}

}
