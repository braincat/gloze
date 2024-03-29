= CycQL Cyc SPARQL adapter =

== Requirements ==
Opencyc-2.0 <http://sourceforge.net/projects/opencyc/files/>
Jena-2.6.4 <http://sourceforge.net/projects/jena/files/Jena/Jena-2.6.4/jena-2.6.4.zip/download> (includes arq-2.8.7 )

== Opencyc server ==

run Cyc server:
 $ ./scripts-mac/run-cyc.sh
OR
 $ ./scripts/run-cyc.sh

Access the Cyc server at <http://localhost:3602/cgi-bin/cyccgi/cg?cb-start>
CycL queries from the CycL directory can be entered in the page on the Query tab.

== File organization ==

* 'patch' directory

Patches required to use the opencyc API with jena 2.6.3. FYI Patches were generated thus:
diff -u src/org/opencyc/xml/ImportOwl.java patch/org/opencyc/xml/ImportOwl.java > patch/ImportOwl.patch
diff -u src/org/opencyc/xml/RDFTriples.java patch/org/opencyc/xml/RDFTriples.java > patch/RDFTriples.patch

* 'scripts-mac' directory
An additional script (run-cyc.sh) required to run OpenCyc on Mac OS X.

* 'sparql' directory
Example SPARQL queries.

* 'CycL' directory
Example CycL queries

* 'src' directory
SPARQL adapter source.

* 'doc' directory
SPARQL adapter documentation

An additional 'tmp' directory is created during download, and installation creates the 'lib', 'server', 'scripts', and 'build' directories.

== Installation ==

The Cyc SPARQL adapter is provided with an ant build script (build.xml).

* ant download
Downloads the required Jena (23.3 MB) and OpenCyc (220.8 MB) distributions to tmp directory.

* ant install (ant re-install)
Unpacks Jena and OpenCyc and applies patchesto OpenCyc bringing it up to date with Jena
It then merges the sparql adapter source with openCyc source in opencyc-2.0/src.

* ant unpack
Unzip jena and untar OpenCyc.

* ant compile (ant re-compile)
Compile the Opencyc API and SPARQL adapter.

* ant uninstall
Removes the build (build) and installed source (opencyc-2.0/src) but doesn't delete downloaded files.

* ant clean
Deletes downloaded files (tmp), libraries (opencyc-2.0/lib), and merged source files (opencyc-2.0/src)

Cyc SPARQL adapter source code in directory sparql.
Demo query (planets.rq) in 'sparql' directory

== Demo ==

The demos are set-up by running demo-setup. This makes various additional assertions required below.

$ ant demo-setup


=== Demo: optional.rq ===

$ ant -Dquery="optional.rq" demo-run

Create a planetary system, then query it to discover the planets and their moons. The sparql query is as follows. The default-graph is UniverseDataMt. 
The optional clause selects any moons of the planet. SPARQL is able to match triples against Cyc binary predicates. 
The Cyc constants returned are represented as URIs. For notational convenience, Cyc terms are defined relative to the document base (the empty namespace preceding the colon). 
Where defined, an rdf URI is used in preference to the Cyc term. In the example below, many planets define an equivalent dbpedia URI.

The following CycL assertions have been made in 'UniverseDataMt' (in demo-setup).

		// Mercury
		(#$orbits #$PlanetMercury #$TheSun)

		// Venus
		(#$orbits #$PlanetVenus #$TheSun)

		// Earth
		(#$orbits #$PlanetEarth #$TheSun)
		(#$orbits #$MoonOfEarth #$PlanetEarth)

		// Mars
		(#$orbits #$PlanetMars #$TheSun)
		(#$orbits #$Deimos-MoonOfMars #$PlanetMars)
		(#$orbits #$Phobos-MoonOfMars #$PlanetMars)

		// Jupiter
		(#$orbits #$PlanetJupiter #$TheSun)
		(#$orbits #$Io-MoonOfJupiter #$PlanetJupiter)
		(#$orbits #$Europa-MoonOfJupiter #$PlanetJupiter)
		(#$orbits #$Ganymede-MoonOfJupiter #$PlanetJupiter)
		(#$orbits #$Callisto-MoonOfJupiter #$PlanetJupiter)

		// Saturn
		(#$orbits #$PlanetSaturn #$TheSun)
		(#$orbits #$Titan-MoonOfSaturn #$PlanetSaturn)
		(#$orbits #$Enceladus-MoonOfSaturn #$PlanetSaturn)

		// Uranus
		(#$orbits #$PlanetUranus #$TheSun)

		// Neptune
		(#$orbits #$PlanetNeptune #$TheSun)

		// Pluto
		(#$orbits #$PlanetPluto #$TheSun)
		(#$orbits #$Charon-MoonOfPluto #$PlanetPluto)
		(#$orbits #$Nix-MoonOfPluto #$PlanetPluto)
		(#$orbits #$Hydra-MoonOfPluto #$PlanetPluto)

This information is accessed via the Cyc SPARQL adapter with the following query:

PREFIX : <>

SELECT ?planet ?moon
FROM :UniverseDataMt
WHERE {
	?planet a :Planet ; :orbits :TheSun ;
	OPTIONAL { ?moon :orbits ?planet; a :MoonOfAPlanet }
}


The results are as follows. Note Cyc constants defined relative to the base, e.g. :PlanetEarth, and the use of RDF URIs, e.g. <http://dbpedia.org/resource/Pluto>.

----------------------------------------------------------------------------
| planet                                         | moon                    |
============================================================================
| <http://dbpedia.org/resource/Mercury_(planet)> |                         |
| <http://dbpedia.org/resource/Venus>            |                         |
| :PlanetEarth                                   |                         |
| <http://dbpedia.org/resource/Mars>             | :Deimos-MoonOfMars      |
| <http://dbpedia.org/resource/Mars>             | :Phobos-MoonOfMars      |
| <http://dbpedia.org/resource/Jupiter>          | :Io-MoonOfJupiter       |
| <http://dbpedia.org/resource/Jupiter>          | :Europa-MoonOfJupiter   |
| <http://dbpedia.org/resource/Jupiter>          | :Ganymede-MoonOfJupiter |
| <http://dbpedia.org/resource/Jupiter>          | :Callisto-MoonOfJupiter |
| <http://dbpedia.org/resource/Saturn>           | :Titan-MoonOfSaturn     |
| <http://dbpedia.org/resource/Saturn>           | :Enceladus-MoonOfSaturn |
| <http://dbpedia.org/resource/Uranus>           |                         |
| <http://dbpedia.org/resource/Neptune>          |                         |
| <http://dbpedia.org/resource/Pluto>            | :Charon-MoonOfPluto     |
| <http://dbpedia.org/resource/Pluto>            | :Nix-MoonOfPluto        |
| <http://dbpedia.org/resource/Pluto>            | :Hydra-MoonOfPluto      |
----------------------------------------------------------------------------

=== Demo: negation.rq ===

$ ant -Dquery="negation.rq" demo-run

This example filters the planets resulting from the previous example by eliminating Dwarf planets (i.e. Pluto). This demonstrates the use of negation using 'FILTER NOT EXISTS'.

This makes use of the additional CycL assertion: (#$isa #$PlanetPluto #$DwarfPlanet).
It also makes use of additional assertions that define the orbital periods of the planets:

		(#$orbitalPeriod #$PlanetMercury (#$DaysDuration 88))
		(#$orbitalPeriod #$PlanetVenus (#$DaysDuration 225))
		(#$orbitalPeriod #$PlanetEarth (#$DaysDuration 365))
		(#$orbitalPeriod #$PlanetMars (#$DaysDuration 687))
		(#$orbitalPeriod #$PlanetJupiter (#$DaysDuration 4329))
		(#$orbitalPeriod #$PlanetSaturn (#$DaysDuration 10753))
		(#$orbitalPeriod #$PlanetUranus (#$DaysDuration 30660))
		(#$orbitalPeriod #$PlanetNeptune (#$DaysDuration 60152))
		(#$orbitalPeriod #$PlanetPluto (#$DaysDuration 90739))

The SPARQL is as follows.

PREFIX : <>

SELECT ?planet ?orbital_period
WHERE {
	?planet a :Planet ; 
		:orbits :TheSun ;
		:orbitalPeriod ?orbital_period
	FILTER (NOT EXISTS { ?planet a :DwarfPlanet })
}

This is compiled into the following CycL query for evaluation. The SPARQL adapter searches for the largest units of the SPARQL algebra that make sense as a single CycL query. 

(#$and 
	(#$isa ?planet #$Planet)
	(#$not (#$isa ?planet #$DwarfPlanet))
	(#$orbits ?planet #$TheSun)
	(#$orbitalPeriod ?planet ?orbital-period))

The results are as follows:

---------------------------------------------------------------------------
| planet                                         | orbital_period         |
===========================================================================
| <http://dbpedia.org/resource/Mercury_(planet)> | "88"^^:DaysDuration    |
| <http://dbpedia.org/resource/Venus>            | "225"^^:DaysDuration   |
| :PlanetEarth                                   | "365"^^:DaysDuration   |
| <http://dbpedia.org/resource/Mars>             | "687"^^:DaysDuration   |
| <http://dbpedia.org/resource/Jupiter>          | "4329"^^:DaysDuration  |
| <http://dbpedia.org/resource/Saturn>           | "10753"^^:DaysDuration |
| <http://dbpedia.org/resource/Uranus>           | "30660"^^:DaysDuration |
| <http://dbpedia.org/resource/Neptune>          | "60152"^^:DaysDuration |
---------------------------------------------------------------------------

Observe that the type of the orbital period, represented in Cyc as a Non_Atomic Term (NAT), has been preserved in the output as a custom datatype.

You can try evaluating the CycL above, directly on the Cyc server at <http://localhost:3602/cgi-bin/cyccgi/cg?cb-start>. 
Select 'Query' (Enter the CycL query into the opencyc browser text box then click on 'Start as New').

=== Demo: graph.rq ===

$ ant -Dquery="graph.rq" demo-run

This example highlights the use of the SPARQL named graph mechanism to select a specific Cyc micro-theory. 
This is similar to the previous example (and has the same result-set), but the planetary information is obtained from the specific micro-theory, 'UniverseDataMt',  it was asserted in. 
However, the negation has to be performed in the 'MtSpace' (default) micro-theory, as this triple doesn't exist at all in the UniverseDataMt micro-theory.

PREFIX : <>

SELECT ?planet ?orbital_period
FROM :CurrentWorldDataCollectorMt-NonHomocentric
FROM NAMED :UniverseDataMt
WHERE {
	GRAPH :UniverseDataMt { 
		?planet a :Planet ; 
			:orbits :TheSun ;
			:orbitalPeriod ?orbital_period
	}
	FILTER (NOT EXISTS { ?planet a :DwarfPlanet })
}

In this case the FILTER is not combined with the GRAPH in a single CycL query because they are evaluated in different micro-theories.

=== Demo: function.rq ===

$ ant -Dquery="function.rq" demo-run

Next, we compute which planets are in the supposed "Circumstellar Habitable Zone" around the Sun. These planets should lie within 0.725 and 3 Astronomical Units from the Sun. The orbital radius is computed from the orbital period using Kepler's 3rd law. The functions in this example are evaluated directly by Cyc and the results assigned to the variable 'orbit'.

# Circumstellar Habitable Zone
PREFIX cyc: <http://www.opencyc.org#>
PREFIX nat: <java:org.opencyc.sparql.function.nat.> 
PREFIX : <>

SELECT ?planet ?AUs
FROM :UniverseDataMt
WHERE {
	?planet a :Planet ; 
		:orbits :TheSun ;
		:orbitalPeriod ?orbital_period
	
	# Calculate planetary distance with Kepler's 3rd law:
	# orbital_period(years)^2 = astronomical_units^3
	LET (?AUs := cyc:ExponentFn(cyc:QuotientFn(nat:Double(?orbital_period), 365), cyc:QuotientFn(2,3)) )
	
	# In the solar system the CHZ extends from 0.725 to 3.0 AUs
	FILTER (0.725 < ?AUs && ?AUs < 3.0)
}
ORDER BY ?AUs

This query is, for the most part, compiled into the CycL below, with only the remaining ORDER BY clause being evaluated by the SPARQL adapter. Note the introduction of the equality to extract the value (?var1) of the orbital period NAT.

(#$and 
	(#$isa ?planet #$Planet)
	(#$orbits ?planet #$TheSun)
	(#$orbitalPeriod ?planet ?orbital-period)
	(#$equals ?orbital-period (??var0 ?var1))
	(#$evaluate ?AUs (#$ExponentFn (#$QuotientFn ?var1 365) (#$QuotientFn 2 3)))
	(#$lessThan 0.725 ?AUs)(#$lessThan ?AUs 3.0))

In this case we use the nat:Double function to cast the orbital period into a double value that could be used in calculations.

-------------------------------------------------------------------------
| planet                             | AUs                              |
=========================================================================
| :PlanetEarth                       | "1.0"^^xsd:double                |
| <http://dbpedia.org/resource/Mars> | "1.5244361831950344"^^xsd:double |
-------------------------------------------------------------------------

=== Demo: rule.rq ===

$ ant -Dquery="rule.rq" demo-run

This example allows demonstrates the use of Cyc inference to dynamically compute the value of a predicate. We again use Kepler's 3rd law, but this time we use the following rule (asserted in demo-setup) to calculate the orbital radius.

 (#$implies 
  (#$and 
    (#$orbitalPeriod ?BODY (#$DaysDuration ?PERIOD))
    (#$evaluate ?RADIUS (#$ExponentFn (#$QuotientFn ?PERIOD 365) (#$QuotientFn 2 3) )) 
  )
  (#$orbitalRadius ?BODY (#$AstronomicalUnit ?RADIUS)))

Observe again, in the results below, that the units of the result are preserved. Before the orbital radius can be used in a comparison (or order-by clause) these units need to be converted into a Double. This uses ARQ's facility to dynamically load Java functions (see the 'fun' namespace).

PREFIX nat: <java:org.opencyc.sparql.function.nat.> 
PREFIX : <>

SELECT ?planet ?orbital_radius
FROM :UniverseDataMt
WHERE {
	?planet a :Planet ;
		:orbits :TheSun ;
		:orbitalRadius ?orbital_radius

	# In the solar system the CHZ extends from 0.725 to 3.0 AUs
	FILTER (0.725 < nat:Double(?orbital_radius) && nat:Double(?orbital_radius) < 3.0)
}
ORDER BY nat:Double(?orbital_radius)

This is compiled into the following CycL:

(#$and 
	(#$isa ?planet #$Planet)
	(#$orbits ?planet #$TheSun)
	(#$orbitalRadius ?planet ?orbital-radius)
	(#$equals ?orbital-radius (??var0 ?var1))
	(#$lessThan 0.725 ?var1)(#$lessThan ?var1 3.0))

--------------------------------------------------------------------------------
| planet                             | orbital_radius                          |
================================================================================
| :PlanetEarth                       | "1.0"^^:AstronomicalUnit                |
| <http://dbpedia.org/resource/Mars> | "1.5244361831950344"^^:AstronomicalUnit |
--------------------------------------------------------------------------------

=== Guide to the Code ===

To work with named graphs we define a CycQueryEngine that overrides QueryEngineMain.modifyOp(). It's factory is registered in DemoQuery with:

	// a custom query engine overrides modifyOp transforming operations to quad form
	QueryEngineRegistry.addFactory(new CycQueryEngine.Factory()) ; 

Each step of query evaluation is routed through an OpExecutor. We provide CycOpExecutors to evaluate graph patterns, filters, sequences and joins. The factory for generating these OpExecutors is registered in DemoQuery with:

	// a custom opExecutor directs graph operations to the appropriate stage generator
	QC.setFactory(context, new CycOpExecutorFactory()) ;

A custom dataset implementation (CycDatasetGraph) wraps Cyc as an RDF Graph. DemoQuery creates this customised dataset with:

	CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "
	QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;

Operators to be compiled into CycL are routed by a CycOpExecutor to a CycStageGenerator. This is registered in DemoQuery with:

	// add a custom stage generator
	Context context = ARQ.getContext();
	context.set(ARQ.stageGenerator, new CycStageGenerator()) ;

The CycStageGenerator executes triple/quad patterns and other algebraic operations and returns a BindingIterator. Patterns and operators are compiled into a CycL query and then evaluated to produce Bindings.

Access to Cyc functions is provided by registering them with a function registry. Given the function URI, the function factory automatically queries Cyc for the arity and return type. Function registration in DemoQuery:

	// Register functions
	FunctionRegistry.get().put(PLUS_FN, funFactory) ;
	FunctionRegistry.get().put(QUOTIENT_FN, funFactory) ;
	FunctionRegistry.get().put(EXPONENT_FN, funFactory) ;

Furthermore, a number of Java functions are registered (nat:Double, nat:Integer) to map the lexical value of NATs (Non-Atomic Terms) to XSD datatypes.

=== Testing ===

To run the test-suite:

1) cd to the test directory

If setup has not been run for the demo or test, run the following:
2) $ ant test-setup

To run all tests:
3) $ ant test-run
The test-results are written to test/results


