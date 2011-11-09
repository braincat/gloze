/*
 *  (c) Copyright Hewlett-Packard Company 2001 - 2009
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
  * @author steven.a.battle@googlemail.com
  */

package com.hp.gloze.www_w3_org_2001_XMLSchema;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import com.hp.gloze.Gloze;
import com.hp.gloze.XMLBean;
import com.hp.gloze.XMLBeanInfo;

public class complexTypeBeanInfo extends XMLBeanInfo implements BeanInfo {

	private static final Class BEAN = complexType.class;

	/**
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(BEAN);
		bd.setName("complexType");
		bd.setDisplayName(XMLBean.concatName(schema.XSD_URI, "complexType"));
		return bd;
	}

	/**
	 * @see java.beans.BeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			return new PropertyDescriptor[] {
				describeAttributeReserved("abstract",BEAN),
				describeAttributeReserved("final",BEAN),
				describeAttribute("id",BEAN),
				describeAttribute("mixed",BEAN),
				describeAttribute("name",BEAN),
				describeElement("annotation",BEAN), 
				describeElement("simpleContent",BEAN), 
				describeElement("complexContent",BEAN),
				describeElement("group",BEAN),
				describeElement("all",BEAN),
				describeElement("choice",BEAN),
				describeElement("sequence",BEAN),
				describeElement("attribute",BEAN), 
				describeElement("attributeGroup",BEAN), 
				describeElement("anyAttribute",BEAN) };
		} catch (IntrospectionException e) {
			Gloze.logger.error(e.getMessage());
			return null;
		}
	}


}
