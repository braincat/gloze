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

package org.opencyc.sparql.iterator;

import org.opencyc.sparql.CycDatasetGraph;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

/**
 * A simple query iterator that iterates solutions to single quads.
 * Quads appear in named graph patterns (NGPs) defined by GRAPH clauses.
 * @see org.opencyc.sparql.CycStageGeneratorByQuad.QueryIterQuadPattern
 * 
 * @author stevebattle.me
 */

public class QueryIterByQuad extends QueryIterBase {
	Quad quad;
	Binding binding ;
	
	public QueryIterByQuad(Quad quad, Binding binding, ExecutionContext context) {
		super(context) ;
		this.binding = binding ;
		this.quad = substitute(quad, binding) ;
		CycDatasetGraph dsg = (CycDatasetGraph) context.getDataset() ;
		this.iterator = dsg.find(this.quad) ;
	}
	
	@Override
	protected Binding getBinding(Object q) {
		Binding b = new BindingMap(binding) ;
		bind(b, quad.getGraph(), ((Quad) q).getGraph()) ;
		bind(b, quad.getSubject(), ((Quad) q).getSubject()) ;
		bind(b, quad.getPredicate(), ((Quad) q).getPredicate()) ;
		bind(b, quad.getObject(), ((Quad) q).getObject()) ;
		return b ;
	}

}
