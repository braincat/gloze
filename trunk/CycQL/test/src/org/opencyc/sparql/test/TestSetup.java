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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencyc.api.CycAccess;
import org.opencyc.cycobject.CycFort;
import org.opencyc.cycobject.CycList;
import org.opencyc.sparql.CycDatasetGraph;
import org.opencyc.sparql.CycDatasetGraph.CycGraph;
import org.opencyc.sparql.demo.Demo;
import org.opencyc.util.Log;

public class TestSetup {
	public static final String SPECIFIC_MICROTHEORY = "UniverseDataMt" ;
	
	static CycAccess cyc ;
		
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
	}
	
	@AfterClass
	public static void shutdown() {
        cyc.close();
	}
	
	@Test
	public void setupTest() throws Exception {
		// sample one of the asserted facts
		String cycl = "(#$orbits #$PlanetEarth ?SUN)";
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
        CycGraph graph = (CycGraph) dsg.getDefaultGraph();
        CycList<Object> results = graph.query(cycl, SPECIFIC_MICROTHEORY) ;
        if (results==null || results.isEmpty()) {
        	// the fact doesn't exist so run the setup
        	CycFort mt = cyc.getKnownConstantByName(SPECIFIC_MICROTHEORY);
    		Demo.setup(cyc,mt);
            results = graph.query(cycl, SPECIFIC_MICROTHEORY) ;
        }
        Object[] a = results.toArray();
        System.out.println(a.length+" results");
        assertTrue(a.length==1);
	}
	
}
