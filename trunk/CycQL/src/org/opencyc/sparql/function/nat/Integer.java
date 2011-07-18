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

package org.opencyc.sparql.function.nat;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

/**
 * A function available in SPARQL to map the lexical value of a NAT (Non-Atomic Term) to an integer.
 * e.g.
 * PREFIX nat: <java:org.opencyc.sparql.function.nat.> 
 * LET (?days := nat:Integer(?orbital_period) )
 * where ?orbital_period is the NAT :- (#$DaysDuration 365)
 * 
 * @author stevebattle.me
 */

// parse the lexical value as a Double

@NATtoXSD
public class Integer extends FunctionBase1 {

	@Override
	public NodeValue exec(NodeValue v) {
		String lex = v.asNode().getLiteralLexicalForm() ;
		return NodeValueInteger.makeInteger(lex) ;
	}

}
