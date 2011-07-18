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

import java.util.HashMap;
import java.util.ListIterator;

import org.opencyc.api.CycAccess;
import org.opencyc.cycobject.CycConstant;
import org.opencyc.cycobject.CycList;
import org.opencyc.cycobject.CycVariable;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDouble;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionBase2;
import com.hp.hpl.jena.sparql.function.FunctionFactory;

/**
 * Access to Cyc functions is provided by registering them with a function registry. 
 * Given the function URI, the function factory automatically queries Cyc for the arity and return type. 
 * Function registration in DemoQuery:
 *
 *	FunctionRegistry.get().put(PLUS_FN, funFactory) ;
 *	FunctionRegistry.get().put(QUOTIENT_FN, funFactory) ;
 *	FunctionRegistry.get().put(EXPONENT_FN, funFactory) ;
 *
 * @author stevebattle.me
 */

public class CycFunctionFactory implements FunctionFactory {
	CycAccess cyc ;
	
	public CycFunctionFactory(CycAccess cyc) throws Exception {
		this.cyc = cyc ;
	}
	
	private static String lookup(CycList<?> row, String var) {
		ListIterator<?> i=row.listIterator() ;
		while (i.hasNext()) {
			CycList<?> l = (CycList<?>) i.next();
			CycVariable v = (CycVariable) l.first();
			if (var.equals(v.name)) return l.second().toString() ;
		}
		return null;
	}
	
	String lexicalValue(NodeValue v) {
		return v.asNode().getLiteralLexicalForm() ;
	}

	class CycFunction2 extends FunctionBase2 {
		String uri ;
		public CycFunction2(String uri) {
			this.uri = uri ;
		}
		@Override
		public NodeValue exec(NodeValue arg0, NodeValue arg1) {
			try {
				String name = uri.substring(uri.lastIndexOf('#') +1) ;
				String eval = "(#$evaluate ?ANSWERS (#$" + name + " " + lexicalValue(arg0) + " " + lexicalValue(arg1) + "))" ;
				HashMap<Object,Object> inferenceParams = new HashMap<Object,Object>();
				CycList<?> query = cyc.makeCycList(eval);
				CycList<?> answer = cyc.askNewCycQuery(query, CycAccess.baseKB, inferenceParams);
				String value = lookup((CycList<?>) answer.listIterator().next(), "ANSWERS") ;
				
				// function resultIsa
				CycList<?> isas = cyc.getResultIsas(cyc.getKnownConstantByName(name)) ;
				for (int i=0 ; i<isas.size() ; i++) {
					CycConstant t = (CycConstant) isas.get(i) ;
					if (t.name.equals("ScalarInterval") || t.name.equals("ComplexNumber"))
						return NodeValueDouble.makeDouble(Double.parseDouble(value));
				}
			}
			catch (Exception e) {
				e.printStackTrace() ;
			}
			return null ;
		}		
	}

	@Override
	public Function create(String uri) {
		try {
			String name = uri.substring(uri.lastIndexOf('#') +1) ;
		    int arity = cyc.getArity(cyc.getKnownConstantByName(name));
			switch (arity) {
			case 2: return new CycFunction2(uri) ;
			}
		}
		catch (Exception e) {
			e.printStackTrace() ;
		}
		throw new RuntimeException("Unknown Cyc function: "+uri);
	}

}
