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

public class QueryTest {
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
		QC.setFactory(context, new CycOpExecutorFactory()) ;
		
		// a custom query engine overrides modifyOp transforming operations to quad form
		QueryEngineRegistry.addFactory(new CycQueryEngine.Factory()) ; 
		
		// Register functions
		FunctionRegistry.get().put(PLUS_FN, funFactory) ;
		FunctionRegistry.get().put(QUOTIENT_FN, funFactory) ;
		FunctionRegistry.get().put(EXPONENT_FN, funFactory) ;
	}

	/** Simple select query in the default namespace */
	
	@Test
	public void selectTest() throws Exception {
		String sparql = read(new File("test/sparql/select.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==1);
	}
	
	/** Select query in a specified namespace */
	
	@Test
	public void select1Test() throws Exception {
		String sparql = read(new File("test/sparql/select1.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==0);
	}
	
	@Test
	public void optionalTest() throws Exception {
		String sparql = read(new File("test/sparql/optional.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==16);
	}
	
	@Test
	public void negationTest() throws Exception {
		String sparql = read(new File("test/sparql/negation.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==8);
	}

	@Test
	public void negation1Test() throws Exception {
		String sparql = read(new File("test/sparql/negation1.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==8);
	}

	@Test
	public void negation2Test() throws Exception {
		String sparql = read(new File("test/sparql/negation2.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==8);
	}
	
	@Test
	public void functionTest() throws Exception {
		String sparql = read(new File("test/sparql/function.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==2);
	}
	
	@Test
	public void ruleTest() throws Exception {
		String sparql = read(new File("test/sparql/rule.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		dsg.setFeature(CycDatasetGraph.MAX_TRANSFORMATION_DEPTH, new Integer(2));
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        //System.out.println(results.getRowNumber());
        assertTrue(results.getRowNumber()==2);
	}
	
	/** test that variables in expressions are substituted */
	
	@Test
	public void rule1Test() throws Exception {
		String sparql = read(new File("test/sparql/rule1.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		dsg.setFeature(CycDatasetGraph.MAX_TRANSFORMATION_DEPTH, new Integer(2));
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        //System.out.println(results.getRowNumber());
        assertTrue(results.getRowNumber()==2);
	}
	
	/** test that graph clauses are evaluated */
	
	@Test
	public void graphTest() throws Exception {
		String sparql = read(new File("test/sparql/graph.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==8);
	}

	/** test that when FROM NAMED clauses are specified without FROM, then the default namespace is empty */
	
	@Test
	public void graph1Test() throws Exception {
		String sparql = read(new File("test/sparql/graph1.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==0);
	}
	
	/** test that variable graph enumerates over FROM NAMED */

	@Test
	public void graph2Test() throws Exception {
		String sparql = read(new File("test/sparql/graph2.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==2);
	}
	
	/** test that variable graph with no FROM NAMED enumerates over dataset named graphs */
	
	@Test
	public void graph3Test() throws Exception {
		String sparql = read(new File("test/sparql/graph3.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==0);
	}
	
	/** check that missing FROM NAMED doesn't result in empty named graph set */
	
	@Test
	public void graph4Test() throws Exception {
		String sparql = read(new File("test/sparql/graph4.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==1);
	}
	
	@Test
	public void dogsTest() throws Exception {
		String sparql = read(new File("test/sparql/dogs.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        System.out.println(results.getRowNumber());
        assertTrue(results.getRowNumber()==2);
	}
	
	@Test
	public void dogs1Test() throws Exception {
		String sparql = read(new File("test/sparql/dogs1.rq"));
		Query query = QueryFactory.create(sparql,Syntax.syntaxARQ);
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
        ResultSet results = engine.execSelect();
        while (results.hasNext()) results.nextBinding();
        assertTrue(results.getRowNumber()==167);
	}
}
