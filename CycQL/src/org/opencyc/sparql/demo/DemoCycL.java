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
/**
 */

import java.io.File;

import org.opencyc.cycobject.CycList;
import org.opencyc.sparql.CycDatasetGraph;
import org.opencyc.sparql.CycDatasetGraph.CycGraph;

/**
 * This class demonstrates the basic functionality of the CycDatasetGraph to evaluate CycL queries.
 * @see org.opencyc.sparql.CycDatasetGraph
 *
 * @author stevebattle.me
 */

public class DemoCycL extends Demo {

	public static void main(String[] args) throws Exception {
		initializeCyc() ;
		
		File queryFile = new File(args[0]) ;
		String query = read(queryFile);
		
		String microtheory = args[1];
		
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		
		// Allow transformation of expressions by Cyc
		dsg.setFeature(CycDatasetGraph.MAX_TRANSFORMATION_DEPTH, new Integer(2));
		
        long time = System.currentTimeMillis();
        CycGraph graph = (CycGraph) dsg.getDefaultGraph();
        CycList<Object> results = graph.find(query, microtheory) ;
        time = System.currentTimeMillis() - time;
        
        System.out.println("\nRESULTS\n");
        System.out.println(results.toPrettyEscapedCyclifiedString("\t"));
        System.out.println("\nEvaluation time: " + time + " milliseconds");

        System.exit(-1);
	}

}
