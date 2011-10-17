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

package org.opencyc.sparql.demo;/**

 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.opencyc.api.CycAccess;
import org.opencyc.cycobject.CycConstant;
import org.opencyc.cycobject.CycFort;
import org.opencyc.cycobject.CycList;
import org.opencyc.util.Log;

/**
 * This class is used to setup the demo and is extended by the other demo classes.
 * 
 * @author stevebattle.me
 */

public class Demo {
	static CycAccess cyc;
	public static final String SPECIFIC_MICROTHEORY = "UniverseDataMt" ;
	CycFort microtheory ;
	
	public static void initializeCyc() {
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
	
	static String read(File file) throws Exception {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int n = 0;
		while ((n = br.read(buf)) > 0) sb.append(buf, 0, n);
		br.close();
		return sb.toString();
	}

	public Demo() {
		try {
			microtheory = cyc.getKnownConstantByName(SPECIFIC_MICROTHEORY);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	static void assertion(String assertion, CycAccess cyc, CycFort mt) throws Exception {
		CycList<?> gaf = cyc.makeCycList(assertion);
		cyc.assertGaf(gaf, mt);
	}

	static public void setup(CycAccess cyc, CycFort mt) throws Exception {

		// Mercury
		assertion("(#$orbits #$PlanetMercury #$TheSun)",cyc,mt);
		assertion("(#$isa #$PlanetMercury #$ThreeDimensionalThing)",cyc,mt);		
		assertion("(#$orbitalPeriod #$PlanetMercury (#$DaysDuration 88))",cyc,mt);

		// Venus
		assertion("(#$orbits #$PlanetVenus #$TheSun)",cyc,mt);
		assertion("(#$isa #$PlanetVenus #$ThreeDimensionalThing)",cyc,mt);
		assertion("(#$orbitalPeriod #$PlanetVenus (#$DaysDuration 225))",cyc,mt);

		// Earth
		assertion("(#$orbits #$PlanetEarth #$TheSun)",cyc,mt);
		assertion("(#$isa #$PlanetEarth #$ThreeDimensionalThing)",cyc,mt);
		assertion("(#$orbitalPeriod #$PlanetEarth (#$DaysDuration 365))",cyc,mt);
		assertion("(#$orbits #$MoonOfEarth #$PlanetEarth)",cyc,mt);

		// Mars
		assertion("(#$orbits #$PlanetMars #$TheSun)",cyc,mt);
		assertion("(#$isa #$PlanetMars #$ThreeDimensionalThing)",cyc,mt);
		assertion("(#$orbitalPeriod #$PlanetMars (#$DaysDuration 687))",cyc,mt);
		assertion("(#$orbits #$Deimos-MoonOfMars #$PlanetMars)",cyc,mt);
		assertion("(#$orbits #$Phobos-MoonOfMars #$PlanetMars)",cyc,mt);

		// Jupiter
		assertion("(#$orbits #$PlanetJupiter #$TheSun)",cyc,mt);
		assertion("(#$isa #$PlanetJupiter #$ThreeDimensionalThing)",cyc,mt);
		assertion("(#$orbitalPeriod #$PlanetJupiter (#$DaysDuration 4329))",cyc,mt);
		assertion("(#$orbits #$Io-MoonOfJupiter #$PlanetJupiter)",cyc,mt);
		assertion("(#$orbits #$Europa-MoonOfJupiter #$PlanetJupiter)",cyc,mt);
		assertion("(#$orbits #$Ganymede-MoonOfJupiter #$PlanetJupiter)",cyc,mt);
		assertion("(#$orbits #$Callisto-MoonOfJupiter #$PlanetJupiter)",cyc,mt);

		// Saturn
		assertion("(#$orbits #$PlanetSaturn #$TheSun)",cyc,mt);
		assertion("(#$isa #$PlanetSaturn #$ThreeDimensionalThing)",cyc,mt);
		assertion("(#$orbitalPeriod #$PlanetSaturn (#$DaysDuration 10753))",cyc,mt);
		assertion("(#$orbits #$Titan-MoonOfSaturn #$PlanetSaturn)",cyc,mt);
		assertion("(#$orbits #$Enceladus-MoonOfSaturn #$PlanetSaturn)",cyc,mt);

		// Uranus
		assertion("(#$orbits #$PlanetUranus #$TheSun)",cyc,mt);
		assertion("(#$isa #$PlanetUranus #$ThreeDimensionalThing)",cyc,mt);
		assertion("(#$orbitalPeriod #$PlanetUranus (#$DaysDuration 30660))",cyc,mt);

		// Neptune
		assertion("(#$orbits #$PlanetNeptune #$TheSun)",cyc,mt);
		assertion("(#$isa #$PlanetNeptune #$ThreeDimensionalThing)",cyc,mt);
		assertion("(#$orbitalPeriod #$PlanetNeptune (#$DaysDuration 60152))",cyc,mt);

		// Pluto
		assertion("(#$orbits #$PlanetPluto #$TheSun)",cyc,mt);
		assertion("(#$isa #$PlanetPluto #$ThreeDimensionalThing)",cyc,mt);
		assertion("(#$orbitalPeriod #$PlanetPluto (#$DaysDuration 90739))",cyc,mt);
		assertion("(#$orbits #$Charon-MoonOfPluto #$PlanetPluto)",cyc,mt);
		assertion("(#$orbits #$Nix-MoonOfPluto #$PlanetPluto)",cyc,mt);
		assertion("(#$orbits #$Hydra-MoonOfPluto #$PlanetPluto)",cyc,mt);
		
		// create constant 'orbitalRadius'
	    CycConstant orbitalRadius = cyc.findOrCreate("orbitalRadius");
	    cyc.assertIsa(orbitalRadius, cyc.find("BinaryPredicate"), mt);
		
		// rules
		assertion(
			"(#$implies "+
			"  (#$and "+
			"    (#$orbitalPeriod ?BODY (#$DaysDuration ?PERIOD))"+
			"    (#$evaluate ?RADIUS (#$ExponentFn (#$QuotientFn ?PERIOD 365) (#$QuotientFn 2 3) )) "+
			"  )"+
			"  (#$orbitalRadius ?BODY (#$AstronomicalUnit ?RADIUS)))"
			,cyc,mt);
		
	}
	
	void setup() throws Exception {
		setup(cyc, microtheory);
	}

	public void close() {
		cyc.close();
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		initializeCyc();
		Demo demo = new Demo();
		demo.setup();
		demo.close();
	}

}
