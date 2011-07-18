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

import org.opencyc.sparql.iterator.QueryIterByQuad;

import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;

/**
 * This class demonstrates fine grained query evaluation, by triple (for BGP) or quad (for NGP).
 * This is functionally correct, but requires many interactions with the Cyc server.
 * As such, this code is for demonstration/test purposes only.
 * 
 * @author stevebattle.me
 */

public class CycStageGeneratorByQuad extends CycStageGeneratorBase {
	StageGenerator chainedStageGenerator ;
	
	/* For a given quad pattern, repeatedly execute nextStage() for each input binding */
	
	public class QueryIterQuadPattern extends QueryIterRepeatApply {
		QuadPattern pattern ;
		public QueryIterQuadPattern(QueryIterator input, QuadPattern pattern, ExecutionContext context) {
			super(input, context) ;
			this.pattern = pattern ;
		}
		protected QueryIterator nextStage(Binding binding) {
			// iterate over the quad pattern and combine the results
			QueryIterator it = new QueryIterNullIterator(getExecContext()) ;
			for (Quad q : pattern.getList()) {
				it = new QueryIterByQuad(it, q, binding, getExecContext()) ;
			}
			return it ;
		}		
	}

	public CycStageGeneratorByQuad(StageGenerator chainedStageGenerator) {
		super();
		this.chainedStageGenerator = chainedStageGenerator;
	}

	/* BGPs are executed by the standard stage generator (chained) */
	
	@Override
	public QueryIterator execute(BasicPattern pattern, QueryIterator input,
			ExecutionContext context) {
        return chainedStageGenerator.execute(pattern, input, context) ;
	}
	
	/* NGPs are evaluated by iterating over the possible graphs */

	public QueryIterator execute(QuadPattern pattern, QueryIterator input,
			ExecutionContext context) {
		return new QueryIterQuadPattern(input, pattern, context) ;
	}

}
