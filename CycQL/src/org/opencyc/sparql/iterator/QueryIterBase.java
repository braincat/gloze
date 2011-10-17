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

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
 * A generic base class for QueryIter
 * @see org.opencyc.sparql.iterator.QueryIterByQuad
 * @see org.opencyc.sparql.iterator.CycQueryIter
 * 
 * @author stevebattle.me 
 */

public abstract class QueryIterBase extends QueryIter {
	Binding slot;
	protected Iterator<?> iterator;
		
	public QueryIterBase(ExecutionContext context) {
		super(context);
	}
	
	/* utility method to substitute node for existing node binding */
		
    static Node substitute(Node node, Binding binding) {
        if (Var.isVar(node)) {
            Node x = binding.get(Var.alloc(node)) ;
            if (x != null) return x ;
        }
        return node ;
    }
    
    static Triple substitute(Triple triple, Binding binding) {
    	return new Triple(
    		substitute(triple.getSubject(), binding),
    		substitute(triple.getPredicate(), binding),
    		substitute(triple.getObject(), binding)) ;
    }
    
    static Quad substitute(Quad quad, Binding binding) {
    	return new Quad(
    		substitute(quad.getGraph(), binding),
    		substitute(quad.getSubject(), binding),
    		substitute(quad.getPredicate(), binding),
    		substitute(quad.getObject(), binding)) ;
    }
    
	void bind(Binding b, Node x, Node y) {
		if (x.isVariable() && !y.isVariable()) {
			Var var = Var.alloc(x.getName()) ;
			if (!b.contains(var)) b.add(var, y) ;
		}
	}	
		
	protected boolean hasNextBinding() {
        if (slot != null) return true ;
        // fill the binding slot
        try {
        	if (iterator!=null && iterator.hasNext()) slot = getBinding(iterator.next());
        }
        catch (Exception e) {
        	e.printStackTrace() ;
        }      
        return slot != null ;
	}
	
	protected abstract Binding getBinding(Object next) ;

	protected Binding moveToNextBinding() {
        hasNextBinding() ;
        Binding r = slot ;
        slot = null ;
        return r ;
	}

	protected void closeIterator() {
        if ( iterator != null ) {
        	NiceIterator.close(iterator) ;
        	iterator = null ;
        }
	}

}
