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
package org.kuali.rice.kns.uif;

import java.io.Serializable;
import java.util.List;

import org.kuali.rice.kns.uif.container.View;
import org.kuali.rice.kns.uif.decorator.ComponentDecorator;
import org.kuali.rice.kns.uif.decorator.DecoratorChain;
import org.kuali.rice.kns.uif.initializer.ComponentInitializer;
import org.kuali.rice.kns.uif.service.ViewHelperService;

/**
 * All classes of the UIF that are used as a rendering element implement the
 * component interface. This interface defines basic properties and methods that
 * all such classes much implement. All components within the framework have the
 * following structure:
 * <ul>
 * <li>Dictionary Configuration/Composition</li>
 * <li>Java Class (the Component implementation</li>
 * <li>>JSP Template Renderer</li>
 * </ul>
 * There are three basic types of components:
 * <ul>
 * <li>Simple Components: <code>Control</code>, widgets</li>
 * <li>Container Components: <code>View</code>, <code>Group</code></li>
 * <li>Complex Components: <code>Field</code></li>
 * </ul>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 * 
 * @see org.kuali.rice.kns.uif.container.Container
 * @see org.kuali.rice.kns.uif.field.Field
 */
public interface Component extends Serializable {

	/**
	 * The unique id (within a given tree) for the component
	 * <p>
	 * The id will be used by renderers to set the HTML element id. This gives a
	 * way to find various elements for scripting. If the id is not given, a
	 * default will be generated by the framework
	 * </p>
	 * 
	 * @return String id
	 */
	public String getId();

	/**
	 * Sets the unique id (within a given tree) for the component
	 * 
	 * @param id
	 *            - string to set as the component id
	 */
	public void setId(String id);

	/**
	 * The name for the component type
	 * <p>
	 * This is used within the rendering layer to pass the component instance
	 * into the template. The component instance is exported under the name
	 * given by this method.
	 * </p>
	 * 
	 * @return String type name
	 */
	public String getComponentTypeName();

	/**
	 * The path to the JSP file that should be called to render the component
	 * <p>
	 * The path should be relative to the web root. An attribute will be
	 * available to the component to use under the name given by the method
	 * <code>getComponentTypeName</code>. Based on the component type,
	 * additional attributes could be available for use. See the component
	 * documentation for more information on such attributes.
	 * </p>
	 * <p>
	 * e.g. '/krad/WEB-INF/jsp/tiles/component.jsp'
	 * </p>
	 * 
	 * @return String representing the template path
	 */
	public String getTemplate();

	/**
	 * Should be called to initialize the component
	 * <p>
	 * Where components can set defaults and setup other necessary state. The
	 * initialize method should only be called once per component lifecycle and
	 * is invoked within the initialize phase of the view lifecylce.
	 * </p>
	 * 
	 * @param view
	 *            - view instance in which the component belongs
	 * @see ViewHelperService#initializeComponent
	 */
	public void performInitialization(View view);

	/**
	 * Called after the initialize phase to perform conditional logic based on
	 * the model data
	 * <p>
	 * Where components can perform conditional logic such as dynamically
	 * generating new fields or setting field state based on the given data
	 * </p>
	 * 
	 * @param view
	 *            - view instance to which the component belongs
	 * @param model
	 *            - Top level object containing the data (could be the form or a
	 *            top level business object, dto)
	 */
	public void performUpdate(View view, Object model);

	/**
	 * The last phase before the view is rendered. Here final preparations can
	 * be made based on the updated view state
	 * 
	 * 
	 * @param view
	 *            - view instance that should be finalized for rendering
	 * @param model
	 *            - top level object containing the data
	 */
	public void performFinalize(View view, Object model);

	/**
	 * List of components that are contained within the component
	 * <p>
	 * Used by <code>ViewHelperService</code> for the various lifecycle
	 * callbacks
	 * </p>
	 * 
	 * @return List<Component> child components
	 */
	public List<Component> getNestedComponents();

	/**
	 * <code>ComponentInitializer</code> instances that should be invoked to
	 * initialize the component
	 * <p>
	 * These provide dynamic initialization behavior for the component and are
	 * configured through the components definition. Each initializer will get
	 * invoked by the initialize method.
	 * </p>
	 * 
	 * @return List of component initializers
	 * @see ViewHelperService#initializeComponent
	 */
	public List<ComponentInitializer> getComponentInitializers();

	/**
	 * The name for the component
	 * <p>
	 * The name is an alternative way to uniquely identify a component. It can
	 * be used to give a more informative identifier for the component.
	 * </p>
	 * 
	 * @return String name
	 */
	public String getName();

	/**
	 * Indicates whether the component should be rendered in the UI
	 * <p>
	 * If set to false, the corresponding component template will not be invoked
	 * (therefore nothing will be rendered to the UI).
	 * </p>
	 * 
	 * @return boolean true if the component should be rendered, false if it
	 *         should not be
	 */
	public boolean isRender();

	/**
	 * Indicates whether the component should be hidden in the UI
	 * <p>
	 * How the hidden data is maintained depends on the views persistence mode.
	 * If the mode is request, the corresponding data will be rendered to the UI
	 * but not visible. If the mode is session, the data will not be rendered to
	 * the UI but maintained server side.
	 * </p>
	 * <p>
	 * For a <code>Container</code> component, the hidden setting will apply to
	 * all contained components (making a section hidden makes all fields within
	 * the section hidden)
	 * </p>
	 * 
	 * @return boolean true if the component should be hidden, false if it
	 *         should be visible
	 */
	public boolean isHidden();

	/**
	 * Setter for the hidden indicator
	 * 
	 * @param hidden
	 */
	public void setHidden(boolean hidden);

	/**
	 * Indicates whether the component can be edited
	 * <p>
	 * When readOnly the controls and widgets of <code>Field</code> components
	 * will not be rendered. If the Field has an underlying value it will be
	 * displayed readOnly to the user.
	 * </p>
	 * For a <code>Container</code> component, the readOnly setting will apply
	 * to all contained components (making a section readOnly makes all fields
	 * within the section readOnly) </p>
	 * 
	 * @return boolean true if the component should be readOnly, false if is
	 *         allows editing
	 */
	public boolean isReadOnly();

	/**
	 * Setter for the read only indicator
	 * 
	 * @param readOnly
	 */
	public void setReadOnly(boolean readOnly);

	/**
	 * Indicates whether the component is required
	 * <p>
	 * At the general component level required means there is some action the
	 * user needs to take within the component. For example, within a section it
	 * might mean the fields within the section should be completed. At a field
	 * level, it means the field should be completed. This provides the ability
	 * for the renderers to indicate the required action.
	 * </p>
	 * 
	 * @return boolean true if the component is required, false if it is not
	 *         required
	 */
	public Boolean getRequired();

	/**
	 * Setter for the required indicator
	 * 
	 * @param required
	 */
	public void setRequired(Boolean required);

	/**
	 * CSS style string to be applied to the component
	 * <p>
	 * Any style override or additions can be specified with this attribute.
	 * This is used by the renderer to set the style attribute on the
	 * corresponding element.
	 * </p>
	 * <p>
	 * e.g. 'color: #000000;text-decoration: underline;'
	 * </p>
	 * 
	 * @return String css style string
	 */
	public String getStyle();

	/**
	 * CSS style class(s) to be applied to the component
	 * <p>
	 * Declares additional style classes for the component. Multiple classes are
	 * specified with a space delimiter. This is used by the renderer to set the
	 * class attribute on the corresponding element. The class(s) declared must
	 * be available in the common style sheets or the style sheets specified for
	 * the view
	 * </p>
	 * <p>
	 * e.g. 'header left'
	 * </p>
	 * 
	 * @return String css style class
	 */
	public String getStyleClass();

	/**
	 * Width the component should take up in the container
	 * <p>
	 * All components belong to a <code>Container</code> and are placed using a
	 * <code>LayoutManager</code>. This property specifies a width the component
	 * should take up in the Container. This is not applicable for all layout
	 * managers. Any valid html width setting can be given (fixed width,
	 * percentage)
	 * </p>
	 * <p>
	 * e.g. '30%', '55px'
	 * </p>
	 * 
	 * @return String width string
	 */
	public String getWidth();

	/**
	 * Horizontal alignment of the component within its container
	 * <p>
	 * All components belong to a <code>Container</code> and are placed using a
	 * <code>LayoutManager</code>. This property specifies how the component
	 * should be aligned horizontally within the container. Any valid html align
	 * setting can be given (left, center, right)
	 * </p>
	 * 
	 * @return String horizontal align
	 */
	public String getAlign();

	/**
	 * Vertical alignment of the component within its container
	 * <p>
	 * All components belong to a <code>Container</code> and are placed using a
	 * <code>LayoutManager</code>. This property specifies how the component
	 * should be aligned vertically within the container. Any valid html valign
	 * setting can be given (top, middle, bottom). Note note all container
	 * support the valign setting
	 * </p>
	 * 
	 * @return String vertical align
	 */
	public String getValign();

	/**
	 * Number of places the component should take up horizontally in the
	 * container
	 * <p>
	 * All components belong to a <code>Container</code> and are placed using a
	 * <code>LayoutManager</code>. This property specifies how many places
	 * horizontally the component should take up within the container. This is
	 * only applicable for table based layout managers. Default is 1
	 * </p>
	 * 
	 * @return int number of columns to span
	 */
	public int getColSpan();

	/**
	 * Number of places the component should take up vertically in the container
	 * <p>
	 * All components belong to a <code>Container</code> and are placed using a
	 * <code>LayoutManager</code>. This property specifies how many places
	 * vertically the component should take up within the container. This is
	 * only applicable for table based layout managers. Default is 1
	 * </p>
	 * 
	 * @return int number of rows to span
	 */
	public int getRowSpan();

	/**
	 * Setter for the component row span
	 * 
	 * @param rowSpan
	 */
	public void setRowSpan(int rowSpan);

	/**
	 * <code>ComponentDecorator</code> instance for the component
	 * 
	 * <p>
	 * Decorators can be used to wrap the given component with content
	 * (providing content before and after the component output). Multiple
	 * decorators can be applied by continually setting the decorator property
	 * (decorator for decorator). A <code>DecoratorChain</code> will be built up
	 * to render the decorators in the correct order
	 * </p>
	 * 
	 * @return ComponentDecorator instance
	 * @see org.kuali.rice.kns.uif.decorator.ComponentDecorator
	 */
	public ComponentDecorator getDecorator();

	/**
	 * Returns the <code>DecoratorChain</code> instance that will return the
	 * <code>ComponentDecorator</code> instances for the component in the
	 * correct order for rendering
	 * 
	 * @return DecoratorChain instance
	 */
	public DecoratorChain getDecoratorChain();

}
