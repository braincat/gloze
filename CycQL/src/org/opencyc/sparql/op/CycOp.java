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

package org.opencyc.sparql.op;

import static org.opencyc.sparql.CycDatasetGraph.cycVar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opencyc.sparql.CycDatasetGraph;
import org.opencyc.sparql.function.nat.NATtoXSD;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Base class for Cyc implementations of SPARQL operators provides common methods.
 * 
 * @author stevebattle.me
 */

public abstract class CycOp {
	public static final String CYC_FN_NAMESPACE = "http://www.opencyc.org#" ;
	Binding binding;
	Map<String,String> mapNATtoVar = new HashMap<String,String>();
	int varCounter;
	
	public static class Context {
		// a given CycL query can only address one microtheory (graph-node)
		// queries to multiple graphs must be split into separate CycL queries
		private Node graphNode;

		public Node getGraphNode() {
			return graphNode;
		}

		public void setGraphNode(Node graphNode) {
			this.graphNode = graphNode;
		}

	}
	
	public CycOp(Binding binding) {
		super();
		this.binding = binding;
	}

	protected static String term(Node node, Binding binding) {
		node = substitute(node, binding);
		// the undercore character is invalid in SPARQL variable names,  but not Cyc variable names
		if (node.isVariable()) return "?"+cycVar(node.getName());
		else if (node.isURI() && node.getURI().equals(RDF.type.getURI())) return "#$isa";
		else return "#$"+CycDatasetGraph.localName(node.getURI());
	}
		
    protected static Node substitute(Node node, Binding binding) {
        if (binding!=null && Var.isVar(node)) {
            Node x = binding.get(Var.alloc(node)) ;
            if (x != null) return x ;
        }
        return node ;
    }
    
    protected static String substitute(Var var, Binding binding) {
    	if (binding!=null) {
            Node x = binding.get(Var.alloc(var)) ;
            if (x != null) return x.getLiteralLexicalForm() ;   		
    	}
    	return "?"+var.getName();
    }
    
    protected static String substitute(String var, Binding binding) {
    	if (binding!=null) {
            Node x = binding.get(Var.alloc(var)) ;
            if (x != null) return x.getLiteralLexicalForm() ;   		
    	}
    	return "?"+var;
    }
    
	public static String expression(Expr exp, Map<String,String> natMap, Binding binding, Context context) {
    	if (exp.isFunction() && exp.getFunction().getFunctionIRI()!=null) {
        	ExprFunction f = exp.getFunction();
        	String fn = f.getFunctionIRI();
        	if (fn.startsWith(CYC_FN_NAMESPACE)) fn = "#$"+fn.substring(CYC_FN_NAMESPACE.length());
        	// otherwise assume this is a NAT to XSD datatype function
        	StringBuffer str = new StringBuffer();
        	str.append("("+fn) ;
        	List<Expr> args = f.getArgs();
        	for (Expr e : args) {
        		str.append(' ');
        		str.append(expression(e,natMap,binding,context));
        	}
        	str.append(")");
        	String x = str.toString();
        	if (natMap!=null && natMap.containsKey(x)) return natMap.get(x);
        	else return x;
    	}
    	else if (exp.isFunction()) {
    		// currently only support for functions that coerce NATs to an XSD datatype
    		try {
    		if (FunctionRegistry.get().isRegistered(exp.getFunction().getFunctionIRI())
	    		&& !Class.forName(exp.getFunction().getFunctionIRI().substring(5)).isAnnotationPresent(NATtoXSD.class))
	    		return null;
    		} catch (Exception e) {
    			return null;
    		}
    		
    		StringBuffer str = new StringBuffer();
        	str.append('(');
        	ExprFunction f = exp.getFunction();
        	
	    	String opName = f.getOpName();
	    	if (opName==null) opName = f.getFunctionSymbol().getSymbol();
	    	if ("<".equals(opName)) str.append("#$lessThan") ;
	    	else if (">".equals(opName)) str.append("#$greaterThan") ;
	    	else if ("<=".equals(opName)) str.append("#$lessThanOrEqualTo") ;
	    	else if (">=".equals(opName)) str.append("#$greaterThanOrEqualTo") ;
	    	else if ("=".equals(opName)) str.append("#$equalSymbols") ;
	    	else if ("||".equals(opName)) str.append("#$or") ;
	    	else if ("&&".equals(opName)) str.append("#$and") ;
	    	else if ("notexists".equals(opName)) {
	    		Op subOp = f.getGraphPattern();
	    		if (subOp instanceof OpQuadPattern) {
		    		str.append("#$not ");
	    			OpQuadPattern qop = (OpQuadPattern) subOp ;
	    			// using toString will enclose the patern with #$and if required
	    			str.append(new CycOpQuadPattern(qop.getPattern(), binding).toString());
			    	// the filtered expression must not contain a different graph
			    	// otherwise the negated BGP will be evaluated in the same microtheory
	    			if (context!=null && context.getGraphNode()==null) 
	    				context.setGraphNode(qop.getGraphNode()) ;
		    		if (context!=null && !context.getGraphNode().equals(qop.getGraphNode())) 
		    			return null;
	    		}
	    		else return null;
	    	}
	    	else return null;
	    	
	    	for (int i=0; i<f.numArgs(); i++) {
	    		str.append(' ');
	    		String x = expression(f.getArg(i+1),natMap,binding,context);
	    		if (x==null) return null;
	    		else if (natMap!=null && natMap.containsKey(x)) str.append(natMap.get(x)); 
	    		else str.append(x); 
	    	}
	    	str.append(')');
	    	return str.toString();
    	}
    	else if (exp.isConstant()) {
    		if (exp.getConstant().isString()) return "\""+exp.getConstant().asString()+"\"";
    		else return exp.getConstant().asString();
    	}
    	else if (exp.isVariable()) {
    		//return "?"+cycVar(exp.getVarName());
    		//return substitute(exp.asVar(),binding);
    		return substitute(cycVar(exp.asVar().getVarName()),binding);

    	}
    	return null;
	}
	
	public void atomizeNATs(Expr exp, StringBuffer str, Binding binding) {
		try {
			if (exp.isFunction()) {
	        	ExprFunction f = exp.getFunction();
	        	String iri = f.getFunctionIRI();

				if (iri!=null && !iri.startsWith(CYC_FN_NAMESPACE)
				// remove 'java:' for the class name
				&& Class.forName(iri.substring(5)).isAnnotationPresent(NATtoXSD.class)) {
					// this is a unary NAT
	    			Expr arg = f.getArg(1);
	    			// the key is the raw expression before substitution (by a variable var2)
					String key = expression(exp, null,binding,null);
	    			if (!mapNATtoVar.containsKey(key)) {
		    			String var1 = "??var"+(varCounter++);
		    			String var2 = "?var"+(varCounter++);
			    		str.append("(#$equals ?"+cycVar(arg.getVarName())+" ("+var1+" "+var2+"))") ;
			    		mapNATtoVar.put(key, var2);
	    			}
				}
		    	for (int i=0; i<f.numArgs(); i++)
		    		atomizeNATs(f.getArg(i+1),str,binding);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected List<Node> getNATVars() {
		List<Node> vars = new LinkedList<Node>() ;
		for (String v : mapNATtoVar.values()) {
			vars.add(Node.createVariable(v));
		}
		return vars;
	}
	
	protected static boolean isValidSubOp(Op subOp, Context context) {
		return CycOpQuadPattern.isValid(subOp, context) 
    	|| CycOpAssign.isValid(subOp, context) 
    	|| CycOpFilter.isValid(subOp, context)
    	|| CycOpSequence.isValid(subOp, context)
    	|| CycOpJoin.isValid(subOp, context) ;
	}
	
	protected CycOp createCycOp(Op op, Binding binding) {
		if (OpQuadPattern.isQuadPattern(op)) {
        	OpQuadPattern quadPattern = (OpQuadPattern) op ;
        	if (quadPattern.isDefaultGraph())
        		return new CycOpBGP(quadPattern.getBasicPattern(), binding);
        	else 
        		return new CycOpQuadPattern(quadPattern.getPattern(), binding);
        }
		else if (op instanceof OpAssign) {
			return new CycOpAssign((OpAssign)op,binding);
		}
		else if (op instanceof OpFilter) {
			return new CycOpFilter((OpFilter)op,binding);
		}
		else if (op instanceof OpSequence) {
			return new CycOpSequence((OpSequence)op,binding);
		}
		else if (op instanceof OpJoin) {
			return new CycOpJoin((OpJoin)op,binding);
		}
		return null;
	}
	
    public abstract Node getGraphName() ;
    
    public abstract List<Node> getVars() ;
    
	public abstract String stringValue() ;
    
	public String toString() {
		return "(#$and "+stringValue()+")";
	}
}
