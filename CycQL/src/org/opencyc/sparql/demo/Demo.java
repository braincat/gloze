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
	
	void assertion(String assertion) throws Exception {
		CycList<?> gaf = cyc.makeCycList(assertion);
		cyc.assertGaf(gaf, microtheory);
	}

	void setup() throws Exception {

		// Mercury
		assertion("(#$orbits #$PlanetMercury #$TheSun)");
		assertion("(#$isa #$PlanetMercury #$ThreeDimensionalThing)");		
		assertion("(#$orbitalPeriod #$PlanetMercury (#$DaysDuration 88))");

		// Venus
		assertion("(#$orbits #$PlanetVenus #$TheSun)");
		assertion("(#$isa #$PlanetVenus #$ThreeDimensionalThing)");
		assertion("(#$orbitalPeriod #$PlanetVenus (#$DaysDuration 225))");

		// Earth
		assertion("(#$orbits #$PlanetEarth #$TheSun)");
		assertion("(#$isa #$PlanetEarth #$ThreeDimensionalThing)");
		assertion("(#$orbitalPeriod #$PlanetEarth (#$DaysDuration 365))");
		assertion("(#$orbits #$MoonOfEarth #$PlanetEarth)");

		// Mars
		assertion("(#$orbits #$PlanetMars #$TheSun)");
		assertion("(#$isa #$PlanetMars #$ThreeDimensionalThing)");
		assertion("(#$orbitalPeriod #$PlanetMars (#$DaysDuration 687))");
		assertion("(#$orbits #$Deimos-MoonOfMars #$PlanetMars)");
		assertion("(#$orbits #$Phobos-MoonOfMars #$PlanetMars)");

		// Jupiter
		assertion("(#$orbits #$PlanetJupiter #$TheSun)");
		assertion("(#$isa #$PlanetJupiter #$ThreeDimensionalThing)");
		assertion("(#$orbitalPeriod #$PlanetJupiter (#$DaysDuration 4329))");
		assertion("(#$orbits #$Io-MoonOfJupiter #$PlanetJupiter)");
		assertion("(#$orbits #$Europa-MoonOfJupiter #$PlanetJupiter)");
		assertion("(#$orbits #$Ganymede-MoonOfJupiter #$PlanetJupiter)");
		assertion("(#$orbits #$Callisto-MoonOfJupiter #$PlanetJupiter)");

		// Saturn
		assertion("(#$orbits #$PlanetSaturn #$TheSun)");
		assertion("(#$isa #$PlanetSaturn #$ThreeDimensionalThing)");
		assertion("(#$orbitalPeriod #$PlanetSaturn (#$DaysDuration 10753))");
		assertion("(#$orbits #$Titan-MoonOfSaturn #$PlanetSaturn)");
		assertion("(#$orbits #$Enceladus-MoonOfSaturn #$PlanetSaturn)");

		// Uranus
		assertion("(#$orbits #$PlanetUranus #$TheSun)");
		assertion("(#$isa #$PlanetUranus #$ThreeDimensionalThing)");
		assertion("(#$orbitalPeriod #$PlanetUranus (#$DaysDuration 30660))");

		// Neptune
		assertion("(#$orbits #$PlanetNeptune #$TheSun)");
		assertion("(#$isa #$PlanetNeptune #$ThreeDimensionalThing)");
		assertion("(#$orbitalPeriod #$PlanetNeptune (#$DaysDuration 60152))");

		// Pluto
		assertion("(#$orbits #$PlanetPluto #$TheSun)");
		assertion("(#$isa #$PlanetPluto #$ThreeDimensionalThing)");
		assertion("(#$orbitalPeriod #$PlanetPluto (#$DaysDuration 90739))");
		assertion("(#$orbits #$Charon-MoonOfPluto #$PlanetPluto)");
		assertion("(#$orbits #$Nix-MoonOfPluto #$PlanetPluto)");
		assertion("(#$orbits #$Hydra-MoonOfPluto #$PlanetPluto)");
		
		// create constant 'orbitalRadius'
	    CycConstant orbitalRadius = cyc.findOrCreate("orbitalRadius");
	    cyc.assertIsa(orbitalRadius, cyc.find("BinaryPredicate"), microtheory);
		
		// rules
		assertion(
			"(#$implies "+
			"  (#$and "+
			"    (#$orbitalPeriod ?BODY (#$DaysDuration ?PERIOD))"+
			"    (#$evaluate ?RADIUS (#$ExponentFn (#$QuotientFn ?PERIOD 365) (#$QuotientFn 2 3) )) "+
			"  )"+
			"  (#$orbitalRadius ?BODY (#$AstronomicalUnit ?RADIUS)))"
		);
		
	}

	void close() {
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
