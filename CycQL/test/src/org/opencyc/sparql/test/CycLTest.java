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
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencyc.api.CycAccess;
import org.opencyc.cycobject.CycList;
import org.opencyc.sparql.CycDatasetGraph;
import org.opencyc.sparql.CycDatasetGraph.CycGraph;
import org.opencyc.util.Log;

public class CycLTest {
	public static final String GENERAL_MICROTHEORY = "CurrentWorldDataCollectorMt-NonHomocentric" ;
	public static final String SPECIFIC_MICROTHEORY = "UniverseDataMt" ;
	
	static CycAccess cyc ;
	
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
	}
	
	@AfterClass
	public static void shutdown() {
        cyc.close();
	}
	
//	@Test
//	public void optionalTest() throws Exception {
//		String cycl = read(new File("test/CycL/optional.cycl"));
//		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
//        CycGraph graph = (CycGraph) dsg.getDefaultGraph();
//        CycList<Object> results = graph.query(cycl, SPECIFIC_MICROTHEORY) ;
//        Object[] a = results.toArray();
//        System.out.println(a.length+" results");
//        assertTrue(a.length==12);
//	}
	
	@Test
	public void negationTest() throws Exception {
		String cycl = read(new File("test/CycL/negation.cycl"));
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
        CycGraph graph = (CycGraph) dsg.getDefaultGraph();
        CycList<Object> results = graph.query(cycl, GENERAL_MICROTHEORY) ;
        Object[] a = results.toArray();
        System.out.println(a.length+" results");
        assertTrue(a.length==8);
	}

	
	@Test
	public void functionTest() throws Exception {
		String cycl = read(new File("test/CycL/function.cycl"));
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
        CycGraph graph = (CycGraph) dsg.getDefaultGraph();
        CycList<Object> results = graph.query(cycl, SPECIFIC_MICROTHEORY) ;
        Object[] a = results.toArray();
        System.out.println(a.length+" results");
        assertTrue(a.length==2);
	}
	
	@Test
	public void ruleTest() throws Exception {
		String cycl = read(new File("test/CycL/rule.cycl"));
		CycDatasetGraph dsg = new CycDatasetGraph(cyc);
		dsg.setFeature(CycDatasetGraph.MAX_TRANSFORMATION_DEPTH, new Integer(2));
        CycGraph graph = (CycGraph) dsg.getDefaultGraph();
        CycList<Object> results = graph.query(cycl, SPECIFIC_MICROTHEORY) ;
        Object[] a = results.toArray();
        System.out.println(a.length+" results");
        assertTrue(a.length==2);
	}
}
