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

package org.opencyc.sparql.demo;

import java.io.File;

import org.opencyc.sparql.CycDatasetGraph;
import org.opencyc.sparql.CycFunctionFactory;
import org.opencyc.sparql.CycOpExecutorFactory;
import org.opencyc.sparql.CycQueryEngine;
import org.opencyc.sparql.CycStageGenerator;
import org.opencyc.sparql.CycStageGeneratorByQuad;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;
import com.hp.hpl.jena.sparql.function.FunctionFactory;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.QueryExecUtils;


/**
 * This class demonstrates the functionality of the CycQL SPARQL adapter.
 * The default is to compile the largest sections of the SPARQL algebra that it can (minimizing number of Cyc invocations).
 * By setting -Dmode=pattern it can be restricted to process only BGPs and NGPs (medium number of Cyc invocations).
 * By setting -Dmode=quad it can be restricted to process only triples (maximizing number of Cyc invocations).
 * 
 * @author stevebattle.me
 */

public class DemoQuery extends Demo {
	static final String PLUS_FN = "http://www.opencyc.org#PlusFn" ;
	static final String QUOTIENT_FN = "http://www.opencyc.org#QuotientFn" ;
	static final String EXPONENT_FN = "http://www.opencyc.org#ExponentFn" ;
	
	static FunctionFactory funFactory ;
	static PropertyFunctionFactory propFactory ;

	public static void main(String[] args) throws Exception {
		initializeCyc() ;
		funFactory = new CycFunctionFactory(cyc) ;
		
		CycOpExecutorFactory.Mode opMode = CycOpExecutorFactory.getDefaultMode();
		String mode = System.getProperty("mode");
		if ("quad".equals(mode)) opMode = CycOpExecutorFactory.Mode.QUAD;
		else if ("pattern".equals(mode)) opMode = CycOpExecutorFactory.Mode.PATTERN;
		
		File queryFile = new File(args[0]) ;
		String queryString = read(queryFile);
		Query query = QueryFactory.create(queryString,Syntax.syntaxARQ);
		
		System.out.println(query.serialize(Syntax.syntaxARQ));
		
		// show the query plan
		Op op = Algebra.compile(query) ;
		System.out.println(op);
		
		// add a custom stage generator
		Context context = ARQ.getContext();
		StageGenerator stageGenerator = (StageGenerator)context.get(ARQ.stageGenerator) ;
		if (opMode==CycOpExecutorFactory.Mode.QUAD)
			context.set(ARQ.stageGenerator, new CycStageGeneratorByQuad(stageGenerator)) ;
		else
			context.set(ARQ.stageGenerator, new CycStageGenerator()) ;
		
		// a custom opExecutor directs graph operations to the appropriate stage generator
		QC.setFactory(context, new CycOpExecutorFactory(opMode)) ;
		
		// a custom query engine overrides modifyOp transforming operations to quad form
		QueryEngineRegistry.addFactory(new CycQueryEngine.Factory()) ; 
		
		// Register functions
		FunctionRegistry.get().put(PLUS_FN, funFactory) ;
		FunctionRegistry.get().put(QUOTIENT_FN, funFactory) ;
		FunctionRegistry.get().put(EXPONENT_FN, funFactory) ;
		
		// extract default namespaces and graphs from the query
		CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
		// Allow transformation of expressions by Cyc
		dsg.setFeature(CycDatasetGraph.MAX_TRANSFORMATION_DEPTH, new Integer(2));
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
		
        long time = System.currentTimeMillis();
        QueryExecUtils.executeQuery(query, engine) ;
        time = System.currentTimeMillis() - time;
        System.out.println("Evaluation time: " + time + " milliseconds");
        
        System.exit(-1);
	}

}
