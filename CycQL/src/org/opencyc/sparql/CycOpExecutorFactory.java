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

import org.opencyc.sparql.op.CycOp;
import org.opencyc.sparql.op.CycOpFilter;
import org.opencyc.sparql.op.CycOpJoin;
import org.opencyc.sparql.op.CycOpSequence;

import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder;

/**
 * Each step of query evaluation is routed through an OpExecutor. 
 * This class is a switch that will route graphs (specifically the default graph) to basic, or named graph execution.
 * Different CycOpExecutor implementations evaluate graph patterns, filters, sequences and joins. 
 * This factory is registered in DemoQuery with:
 * QC.setFactory(context, new CycOpExecutorFactory()) ;
 * 
 * @author stevebattle.me
 */

public class CycOpExecutorFactory implements OpExecutorFactory {
	
	public static enum Mode{ QUAD, PATTERN, OP };
	Mode mode ;
	
	public static Mode getDefaultMode() {
		return Mode.OP;
	}
	
	public CycOpExecutorFactory() {
		mode =  getDefaultMode();
	}
	
	public CycOpExecutorFactory(Mode mode) {
		this.mode = mode;
	}
	
	@Override
	public OpExecutor create(ExecutionContext context) {
		switch (mode) {
		// Quad and Pattern execution differ in stage generation
		case QUAD:
		case PATTERN:
			return new CycOpExecutor1(context);
		default:
			return new CycOpExecutor(context);
		}
	}
	
	class CycOpExecutor1 extends OpExecutor {
		protected CycOpExecutor1(ExecutionContext context) {
			super(context);
		}
		@Override
		protected QueryIterator execute(OpQuadPattern quadPattern, QueryIterator input) {
			// all graph patterns are routed this way - even those for the default graph
			CycStageGeneratorFace gen = (CycStageGeneratorFace) StageBuilder.getGenerator();
			if (quadPattern.isDefaultGraph()) {
				//OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern());
				//return execute(opBGP, input);
				return gen.execute(quadPattern.getBasicPattern(), input, this.execCxt);
			} else
				return gen.execute(quadPattern.getPattern(), input, this.execCxt);
		}
	}

	class CycOpExecutor extends OpExecutor {
		protected CycOpExecutor(ExecutionContext context) {
			super(context);
		}
		@Override
		protected QueryIterator execute(OpQuadPattern quadPattern, QueryIterator input) {
			// all graph patterns are routed this way - even those for the default graph
			CycStageGeneratorFace gen = (CycStageGeneratorFace) StageBuilder.getGenerator();
			if (quadPattern.isDefaultGraph())
				return gen.execute(quadPattern.getBasicPattern(), input, this.execCxt);
			else
				return gen.execute(quadPattern.getPattern(), input, this.execCxt);
		}
	    protected QueryIterator execute(OpFilter opFilter, QueryIterator input) {
	    	// if a cyc Filter/BGP can be created then execute it, otherwise follow default behaviour
	    	if (CycOpFilter.isValid(opFilter, new CycOp.Context())) {
	    		CycStageGeneratorFace gen = (CycStageGeneratorFace) StageBuilder.getGenerator();
	    		return gen.execute(opFilter, input, this.execCxt);
	    	}
	    	return super.execute(opFilter, input) ;
	    }
	    protected QueryIterator execute(OpSequence opSequence, QueryIterator input) {
	    	// if a cyc Filter/BGP can be created then execute it, otherwise follow default behaviour
	    	if (CycOpSequence.isValid(opSequence, new CycOp.Context())) {
	    		CycStageGeneratorFace gen = (CycStageGeneratorFace) StageBuilder.getGenerator();
	    		return gen.execute(opSequence, input, this.execCxt);
	    	}
	    	return super.execute(opSequence, input) ;
	    }
	    protected QueryIterator execute(OpJoin opJoin, QueryIterator input) {
	    	// if a cyc Filter/BGP can be created then execute it, otherwise follow default behaviour
	    	if (CycOpJoin.isValid(opJoin, new CycOp.Context())) {
	    		CycStageGeneratorFace gen = (CycStageGeneratorFace) StageBuilder.getGenerator();
	    		return gen.execute(opJoin, input, this.execCxt);
	    	}
	    	return super.execute(opJoin, input) ;
	    }
	}

}
