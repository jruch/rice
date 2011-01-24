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
package org.kuali.rice.kns.uif.layout;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.kuali.rice.kns.uif.Component;
import org.kuali.rice.kns.uif.container.Container;
import org.kuali.rice.kns.uif.container.View;
import org.kuali.rice.kns.uif.service.ViewHelperService;

/**
 * Manages the rendering of <code>Component</code> instances within a
 * <code>Container</code>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface LayoutManager extends Serializable {

	/**
	 * Should be called to initialize the layout manager
	 * <p>
	 * This is where layout managers can set defaults and setup other necessary
	 * state. The initialize method should only be called once per layout
	 * manager lifecycle and is invoked within the initialize phase of the view
	 * lifecylce.
	 * </p>
	 * 
	 * @param view
	 *            - View instance the layout manager is a part of
	 * @param container
	 *            - Container the layout manager applies to
	 * @see ViewHelperService#performInitialization
	 */
	public void performInitialization(View view, Container container);

	/**
	 * Called after the initialize phase to perform conditional logic based on
	 * the model data
	 * 
	 * @param view
	 *            - view instance to which the layout manager belongs
	 * @param container
	 *            - Container the layout manager applies to
	 * @param models
	 *            - Map of model instances, where the key is the model name as
	 *            given by the view modelClasses map, and the value is the model
	 *            instance
	 */
	public void performConditionalLogic(View view, Map<String, Object> models, Container container);

	/**
	 * Called to refresh any manager state when state has changed in the
	 * container
	 * 
	 * @param view
	 *            - view instance to which the layout manager belongs
	 * @param container
	 *            - Container the layout manager applies to
	 */
	public void refresh(View view, Container container);

	/**
	 * Determines what <code>Container</code> classes are supported by the
	 * <code>LayoutManager</code>
	 * 
	 * @return Class<? extends Container> container class supported
	 */
	public Class<? extends Container> getSupportedContainer();

	/**
	 * List of components that are contained within the layout manager
	 * <p>
	 * Used by <code>ViewHelperService</code> for the various lifecycle
	 * callbacks
	 * 
	 * @return List<Component> child components
	 */
	public List<Component> getNestedComponents();

}
