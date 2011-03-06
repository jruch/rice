/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kns.uif.widget;

import org.kuali.rice.kns.uif.Component;
import org.kuali.rice.kns.uif.container.View;

/**
 * Components that provide a user interface function (besides the basic form
 * handing) should implement the widget interface
 * 
 * <p>
 * Widgets generally provide a special function such as a calendar or navigation
 * element. The can render one or more <code>Field</code> instances and
 * generally have associated client side code
 * </p>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface Widget extends Component {

	/**
	 * Special finalize method for <code>Widget</code> instances that takes in
	 * the parent component. This allows widgets to setup state based on the
	 * component they apply to (or are associated with)
	 * 
	 * <p>
	 * Widget implementations can implemented the normal performFinalize method,
	 * the overloaded (taking the parent), or both
	 * </p>
	 * 
	 * @param parent
	 *            - Component that contains the widget
	 * @see org.kuali.rice.kns.uif.Component.performFinalize(View, Object)
	 */
	public void performFinalize(View view, Object model, Component parent);

}
