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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** 
 * Annotation class used to indicate a user-defined function that extracts an atomic value from a Cyc NAT, 
 * converting it to an XSD data-type.
 * 
 * @author stevebattle.me
 */

// retain the annotation for runtime introspection
@Retention(RetentionPolicy.RUNTIME)  
public @interface NATtoXSD {}
