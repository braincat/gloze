package org.opencyc.sparql;

import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;


/**
 * This is the interface used by all Cyc Stage Generator implementations.
 * @see org.opencyc.sparql.CycStageGeneratorBase
 * 
 * @author stevebattle.me
 */

public interface CycStageGeneratorFace extends StageGenerator {
	
    public QueryIterator execute(QuadPattern pattern, QueryIterator input, ExecutionContext context) ;
    
    public QueryIterator execute(OpFilter opFilter, QueryIterator input, ExecutionContext context) ;
    
    public QueryIterator execute(OpSequence opSequence, QueryIterator input, ExecutionContext context) ;

    public QueryIterator execute(OpJoin opJoin, QueryIterator input, ExecutionContext context) ;
}
