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

package org.opencyc.sparql.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencyc.api.CycAccess;
import org.opencyc.sparql.CycDatasetGraph;
import org.opencyc.sparql.CycFunctionFactory;
import org.opencyc.sparql.CycOpExecutorFactory;
import org.opencyc.sparql.CycQueryEngine;
import org.opencyc.sparql.CycStageGenerator;
import org.opencyc.util.Log;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.function.FunctionFactory;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.util.Context;

public class QueryByPatternTest {
	static final String PLUS_FN = "http://www.opencyc.org#PlusFn" ;
	static final String QUOTIENT_FN = "http://www.opencyc.org#QuotientFn" ;
	static final String EXPONENT_FN = "http://www.opencyc.org#ExponentFn" ;
	
	public static final String GENERAL_MICROTHEORY = "CurrentWorldDataCollectorMt-NonHomocentric" ;
	public static final String SPECIFIC_MICROTHEORY = "UniverseDataMt" ;
	
	static CycAccess cyc ;
	static FunctionFactory funFactory ;
	
	static String read(File file) throws Exception {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int n = 0;
		while ((n = br.read(buf)) > 0) sb.append(buf, 0, n);
		br.close();
		return sb.toString();
	}
	
	@BeforeClass
	public static void initializeCyc() throws Exception {
	    Log.makeLog();
	    Log.current.println("Initializing Cyc server connection, and caching frequently used terms.");
	    try {
	      cyc = new CycAccess();
	    }
	    catch (Exception e) {
	      Log.current.errorPrintln(e.getMessage());
	      Log.current.printStackTrace(e);
	    }
	    cyc.traceOn();
	    Log.current.println("Now tracing Cyc server messages");
		funFactory = new CycFunctionFactory(cyc) ;
	}
	
	@AfterClass
	public static void shutdown() {
        cyc.close();
	}
	
	@Before
	public void setup() {
		// add a custom stage generator
		Context context = ARQ.getContext();
		context.set(ARQ.stageGenerator, new CycStageGenerator()) ;
		
		// a custom opExecutor directs graph operations to the appropriate stage generator
		QC.setFactory(context, new CycOpExecutorFactory(CycOpExecutorFactory.Mode.PATTERN)) ;
		
		// a custom query engine overrides modifyOp transforming operations to quad form
		QueryEngineRegistry.addFactory(new CycQueryEngine.Factory()) ; 
		
		// Register functions
		FunctionRegistry.get().put(PLUS_FN, funFactory) ;
		FunctionRegistry.get().put(QUOTIENT_FN, funFactory) ;
		FunctionRegistry.get().put(EXPONENT_FN, funFactory) ;
	}

	@Test
	public void optionalTest() throws Exception {
		String sparql = read(new File("sparql/optional.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==16);
	}
	
	@Test
	public void negationTest() throws Exception {
		String sparql = read(new File("sparql/negation.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==8);
	}

	@Test
	public void negation1Test() throws Exception {
		String sparql = read(new File("sparql/negation1.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==8);
	}

	@Test
	public void negation2Test() throws Exception {
		String sparql = read(new File("sparql/negation2.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==8);
	}
	
	@Test
	public void functionTest() throws Exception {
		String sparql = read(new File("sparql/function.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==2);
	}
	
	@Test
	public void ruleTest() throws Exception {
		String sparql = read(new File("sparql/function.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
		dsg.setFeature(CycDatasetGraph.MAX_TRANSFORMATION_DEPTH, new Integer(2));
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        //System.out.println(results.getRowNumber());
        assertTrue(results.getRowNumber()==2);
	}
	
	@Test
	public void dogsTest() throws Exception {
		String sparql = read(new File("sparql/dogs.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        System.out.println(results.getRowNumber());
        assertTrue(results.getRowNumber()==2);
	}
	
	@Test
	public void dogs1Test() throws Exception {
		String sparql = read(new File("sparql/dogs1.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==167);
	}
}
