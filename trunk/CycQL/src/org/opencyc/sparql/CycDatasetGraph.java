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

package org.opencyc.sparql;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.opencyc.api.CycAccess;
import org.opencyc.api.CycObjectFactory;
import org.opencyc.cycobject.CycConstant;
import org.opencyc.cycobject.CycFort;
import org.opencyc.cycobject.CycList;
import org.opencyc.cycobject.CycSymbol;
import org.opencyc.cycobject.CycVariable;
import org.opencyc.cycobject.ELMt;
import org.opencyc.sparql.iterator.CycTripleIterator;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphBase;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.util.iterator.NullIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A custom dataset implementation (CycDatasetGraph) wraps Cyc as an RDF Graph. 
 * DemoQuery creates this customized data-set with:
 *
 *	CycDatasetGraph dsg = new CycDatasetGraph(query, cyc, "");
 *	QueryExecution engine = QueryExecutionFactory.create(query, new DatasetImpl(dsg)) ;
 *
 * @author stevebattle.me
 */

@SuppressWarnings("unchecked")

public class CycDatasetGraph extends DatasetGraphBase implements DatasetGraph {

	/* Feature properties */
	public static final CycSymbol MAX_TIME =  new CycSymbol(":MAX-TIME") ;
	public static final CycSymbol DIRECTION =  new CycSymbol(":DIRECTION") ; // CycSymbol
	public static final CycSymbol MAX_PROOF_DEPTH =  new CycSymbol(":MAX-PROOF-DEPTH") ; // Integer
	public static final CycSymbol ALLOW_INDETERMINATE_RESULTS =  new CycSymbol(":ALLOW-INDETERMINATE-RESULTS?") ; // Boolean
	public static final CycSymbol MAX_TRANSFORMATION_DEPTH =  new CycSymbol(":MAX-TRANSFORMATION-DEPTH") ; // Integer
	public static final CycSymbol MAX_NUMBER =  new CycSymbol(":MAX-NUMBER") ; // Integer
	
	/* Feature Values */
	/* Value for DIRECTION */
	public static final CycSymbol CYC_SYMBOL = new CycSymbol(":BACKWARD");
	
	protected static HashMap<String, CycConstant> symbolTable = new HashMap<String,CycConstant>();
	
	protected String defaultNS ;
	protected CycAccess cyc;
	protected List<String> graphs, namedGraphs ;
	protected HashMap<Object,Object> inferenceParams = new HashMap<Object,Object>();
	
	public CycDatasetGraph(CycAccess cyc) {
		super();
		this.cyc = cyc ;
	}

	public CycDatasetGraph(Query query, CycAccess cyc, String defaultNSPrefix) {
		super();
		graphs = query.getGraphURIs();
		namedGraphs = query.getNamedGraphURIs();
		defaultNS = query.getPrefix(defaultNSPrefix) ;
		this.cyc = cyc ;
	}

	public void setFeature(Object feature, Object value) {
		inferenceParams.put(feature, value);
	}
	
	public String getNamespace() {
		return defaultNS ;
	}
	
	/* Get localname reverses the getURI function.
	 * If we have previously seen a constant associated with a URI then return the constant
	 * otherwise strip the base namespace and build a new constant
	 */
	
	public static String localName(String uri) {
		CycConstant c = symbolTable.get(uri);
		if (c==null) return uri.substring(uri.lastIndexOf('/')+1);
		else return c.name;
	}
	
	static String encode(String uri) throws Exception {
		int n = uri.lastIndexOf('/') ;
		return uri.substring(0,n+1) + URLEncoder.encode(uri.substring(n+1), "UTF-8") ;
	}
	
	public boolean variableMatch(CycVariable v, String name) {
		// we have to sacrifice case sensitivity
		return v.name.toLowerCase().equals(name.toLowerCase().replaceAll("_","")) ;
	}
	
	/* If the constant has an associated rdfURI then return it
	 * otherwise append the constant to the current base namespace
	 */
	
	String getURI(CycConstant constant) {
		try {
			// Does this symbol have an associated URI?
			CycConstant predicate = cyc.find("rdfURI") ;
			Object obj = cyc.getArg2(predicate, constant) ;
			String uri = URLDecoder.decode(((String) obj), "UTF-8") ;
			symbolTable.put(uri, constant);
			return uri;
		}
		catch (Exception e) {}
		return getNamespace() + constant.name ;
	}
	
	public Node createNode(Object o) {
		if (o instanceof CycConstant)
			return Node.createURI(getURI((CycConstant) o)) ;
		else if (o instanceof Integer) 
			return Node.createLiteral(((Integer) o).toString(), null, XSDDatatype.XSDinteger) ;
		else if (o instanceof Double) 
			return Node.createLiteral(((Double) o).toString(), null, XSDDatatype.XSDdouble) ;
		else if (o instanceof Boolean)
			return Node.createLiteral(((Boolean) o).toString(), null, XSDDatatype.XSDboolean) ;		
		else if (o instanceof CycList) {
			CycList<?> l = (CycList<?>) o ;
			if (l.isProperList() && l.size()==3) { // assume (var units value)
				CycConstant units = (CycConstant) l.second();
				String value = l.third().toString() ;
				return  Node.createLiteral(value, null, new BaseDatatype(getURI(units))) ;
			}
			// (var . value) dotted pair
			else if (!l.isProperList()) return createNode(l.last()) ;
		}
		return Node.createLiteral(o.toString());
	}

	public CycList<Object> getArg1s(CycFort predicate, Object arg2) throws Exception {
		CycList<Object> query = new CycList<Object>();
		query.add(predicate);
		query.add(CycObjectFactory.makeCycVariable("?arg1"));
		query.add(arg2);
	    ELMt inf = cyc.makeELMt(cyc.getKnownConstantByGuid("bd58915a-9c29-11b1-9dad-c379636f7270"));
		return (CycList<Object>) cyc.askWithVariable(query, CycObjectFactory.makeCycVariable("?arg1"), inf);
	}
	
	public Object getArg1(CycFort predicate, Object arg1) throws Exception {
		CycList<Object> arg1s = getArg1s(predicate, arg1);
		if (arg1s.isEmpty()) return null;
		else return arg1s.first();
	}

	String cycTerm(Node node, String defaultVar, CycFort mt) {
		if (node==null || node instanceof Node_ANY) return "?"+defaultVar;
		// Cyc names cannot contain underscore
		if (node.isVariable()) return "?"+node.getName().replaceAll("_", "");
		else if (node.isURI() && node.getURI().equals(RDF.type.getURI())) return "#$isa";
		else if (node.isURI() && !node.getURI().startsWith(getNamespace())) {
			try {
				Object arg1 = getArg1(cyc.find("rdfURI"), encode(node.getURI())) ;
				if (arg1!=null) return "#$"+arg1 ;
				else return node.getURI() ;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "#$"+localName(node.getURI());
	}

	public class CycGraph extends GraphBase implements Graph {
		String graphName;
		
		public String getGraphName() {
			return graphName;
		}

		PrefixMapping prefixes = PrefixMapping.Factory.create();
		
		public CycGraph(String graphName) {
			super();
			this.graphName = graphName;
		}

		@Override
		public ExtendedIterator graphBaseFind(Node s, Node p, Node o) {			
			try {
				CycFort mt = cyc.getKnownConstantByName(graphName);
				// move predicate to the head
				String q = "(" + cycTerm(p,"P",mt) +" " +cycTerm(s,"S",mt) +" " +cycTerm(o,"O",mt) +")";
				CycList<Object> query = cyc.makeCycList(q);
				CycList<Object> answer = cyc.askNewCycQuery(query, mt, inferenceParams);
				return new CycTripleIterator(s, p, o, answer, CycDatasetGraph.this) ;
			} catch (Exception e) {
				e.printStackTrace();
			}
			// return an empty iterator
			return new CycTripleIterator();
		}
		
		public CycList<Object> find(String q, String graphName) throws Exception {
			CycFort mt = cyc.getKnownConstantByName(graphName);
			CycList<Object> query = cyc.makeCycList(q);
			return cyc.askNewCycQuery(query, mt, inferenceParams);
		}

		public int graphBaseSize() {
			return this.graphBaseFind(null, null, null).toList().size();
		}

		@Override
		protected ExtendedIterator<Triple> graphBaseFind(TripleMatch tm) {
			return graphBaseFind(tm.getMatchSubject(), tm.getMatchPredicate(), tm.getMatchObject()) ;
		}		
	}
	
	// The default graph should be the union of these graphs, but just take the first
	@Override
	public Graph getDefaultGraph() {
		if (graphs!=null && graphs.size()>0) {
			Node n = Node.createURI(graphs.get(0));
			return new CycGraph(localName(n.getURI()));
		} 
		else return new CycGraph(CycAccess.baseKB.toString()) ;
	}

	@Override
	public Graph getGraph(Node graphNode) {
		if (graphNode!=null && graphNode.isURI()) return new CycGraph(localName(graphNode.getURI()));
		return null;
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		return new Iterator<Node>() {
			ListIterator<String> i = namedGraphs.listIterator();
			public boolean hasNext() {
				return i.hasNext();
			}
			public Node next() {
				// return a literal representing the graph name
				return Node.createURI(((String) i.next()));
			}
			public void remove() {}	
		};
	}
	
	// graph may not be a variable
	// have substitutions been done?

	@Override
	public Iterator<Quad> find(Node graph, Node subject, Node predicate, Node object) {
		Iterator<Quad> it = new NullIterator<Quad>() ;
		
		if (graph.isVariable()) {
			// iterate over all named graphs in the model
			for (Iterator<Node> gi = listGraphNodes(); gi.hasNext() ;) {
				final Node g = gi.next() ;
				// create a triple iterator for this graph
				ExtendedIterator<Triple> ti = getGraph(g).find(subject, predicate, object) ;
				// map triples to quads
				ExtendedIterator qi = ti.mapWith(new Map1() {
					public Object map1(Object t) { return new Quad(g, (Triple) t) ; }
				}) ;
				// concatenate results over multiple graph bindings
				it = qi.andThen(it) ;
			}
		} else { // the graph is bound
			final Node g = graph ;
			ExtendedIterator<Triple> ti = getGraph(g).find(subject, predicate, object) ;
			// map triples to quads
			it = ti.mapWith(new Map1() {
				public Object map1(Object t) { return new Quad(g, (Triple) t) ; }
			}) ;		
		}
		return it ;
	}
	
	@Override
	public Iterator<Quad> findNG(Node graph, Node subject, Node predicate, Node object) {
		return find(graph,subject,predicate,object);
	}

	public static String cycVar(String var) {
		return var.replaceAll("_", "-");
	}
}
