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
import java.util.Iterator;

import org.opencyc.sparql.CycDatasetGraph;
import org.opencyc.sparql.CycFunctionFactory;
import org.opencyc.sparql.CycOpExecutorFactory;
import org.opencyc.sparql.CycQueryEngine;
import org.opencyc.sparql.CycStageGenerator;
import org.opencyc.sparql.CycStageGeneratorByQuad;

import com.hp.hpl.jena.graph.Node;
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

public class DemoDatasetGraph extends Demo {
	static final String PLUS_FN = "http://www.opencyc.org#PlusFn" ;
	static final String QUOTIENT_FN = "http://www.opencyc.org#QuotientFn" ;
	static final String EXPONENT_FN = "http://www.opencyc.org#ExponentFn" ;
	
	static FunctionFactory funFactory ;
	static PropertyFunctionFactory propFactory ;

	public static void main(String[] args) throws Exception {
		initializeCyc() ;
		
		File queryFile = new File(args[0]) ;
		String queryString = read(queryFile);
		Query query = QueryFactory.create(queryString,Syntax.syntaxARQ);
		
		// extract default namespaces and graphs from the query
		CycDatasetGraph dsg = new CycDatasetGraph(cyc, "", "");
		
		Iterator<String> names = dsg.toDataset().listNames();
		while (names.hasNext()) System.out.println(names.next());

		Iterator<Node> graphNodes = dsg.listGraphNodes();
		while (graphNodes.hasNext()) System.out.println(graphNodes.next());

        System.exit(-1);
	}

}
