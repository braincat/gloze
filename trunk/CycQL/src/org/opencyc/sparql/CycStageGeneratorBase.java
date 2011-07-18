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

import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;

/**
 * A base implementation for different versions of the CycStageGenerator.
 * A simpler CycStageGeneratorByQuad implements only BGPs and NGPs (as QuadPatterns) but none of the SPARQL algebra operators.
 * This base class means that these simpler stage-generators don't need to implement these additional methods.
 * 
 * @author stevebattle.me
 */

public abstract class CycStageGeneratorBase implements CycStageGeneratorFace {
	
    public QueryIterator execute(QuadPattern pattern, QueryIterator input, ExecutionContext context) {
    	throw new RuntimeException("Unimplemented method");
    }
    
    public QueryIterator execute(OpFilter opFilter, QueryIterator input, ExecutionContext context) {
    	throw new RuntimeException("Unimplemented method");
    }
    
    public QueryIterator execute(OpSequence opSequence, QueryIterator input, ExecutionContext context) {
    	throw new RuntimeException("Unimplemented method");
    }
    
    public QueryIterator execute(OpJoin opJoin, QueryIterator input, ExecutionContext context) {
    	throw new RuntimeException("Unimplemented method");
    }
}
