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

import java.util.Date;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import org.kuali.rice.kns.uif.UifConstants;
import org.kuali.rice.kns.uif.container.CollectionGroup;
import org.kuali.rice.kns.uif.container.View;
import org.kuali.rice.kns.uif.core.Component;
import org.kuali.rice.kns.uif.field.AttributeField;
import org.kuali.rice.kns.uif.field.GroupField;
import org.kuali.rice.kns.uif.layout.LayoutManager;
import org.kuali.rice.kns.uif.layout.TableLayoutManager;
import org.kuali.rice.kns.uif.util.ObjectPropertyUtils;

/**
 * Decorates a HTML Table client side with various tools
 * 
 * <p>
 * Decorations implemented depend on widget implementation. Examples are
 * sorting, paging and skinning.
 * </p>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class TableTools extends WidgetBase {
	private static final long serialVersionUID = 4671589690877390070L;

	/**
	 * A text to be displayed when the table is empty
	 */
	private String emptyTableMessage;
	private boolean disableTableSort;
	
	/**
	 * By default, show the search and export options
	 */
	private boolean showSearchAndExportOptions = true;
	
	public TableTools() {
		super();
	}

	/**
	 * The following initialization is performed:
	 * 
	 * <ul>
	 * <li>Initializes component options for empty table message</li>
	 * </ul>
	 *
	 */
	@Override
	public void performFinalize(View view, Object model, Component component) {
		super.performFinalize(view,model,component);
		
		if (isRender()){
			if (StringUtils.isNotBlank(getEmptyTableMessage())){
				getComponentOptions().put(UifConstants.TableToolsKeys.LANGUAGE, "{\"" + UifConstants.TableToolsKeys.EMPTY_TABLE + "\" : \"" + getEmptyTableMessage() + "\"}");
			}
			
			if (isDisableTableSort()){
				getComponentOptions().put(UifConstants.TableToolsKeys.TABLE_SORT,"false");
			}
			
			if (!isShowSearchAndExportOptions()){
				String sDomOption = getComponentOptions().get(UifConstants.TableToolsKeys.SDOM);
				sDomOption = StringUtils.remove(sDomOption, "T"); //Removes Export option 	
				sDomOption = StringUtils.remove(sDomOption, "f"); //Removes search option
				getComponentOptions().put(UifConstants.TableToolsKeys.SDOM,sDomOption);
			}
			
			if (component instanceof CollectionGroup){
				buildTableSortOptions((CollectionGroup)component);
			}
		}
	}
	
	/**
	 * Builds column options for sorting
	 * 
	 * @param collectionGroup
	 */
	protected void buildTableSortOptions(CollectionGroup collectionGroup) {

		LayoutManager layoutManager = collectionGroup.getLayoutManager();
		 
		/**
		 * If subcollection exists, dont allow the table sortable
		 */
		if (!collectionGroup.getSubCollections().isEmpty()){
			setDisableTableSort(true);
		}

		if (!isDisableTableSort()){
			/**
			 * If rendering add line, skip that row from col sorting
			 */
			if (collectionGroup.isRenderAddLine() && !collectionGroup.isReadOnly()) {
				getComponentOptions().put(UifConstants.TableToolsKeys.SORT_SKIP_ROWS, "[" + UifConstants.TableToolsValues.ADD_ROW_DEFAULT_INDEX + "]");
			}
			
			if (!collectionGroup.isReadOnly()) {
				
				StringBuffer tableToolsColumnOptions = new StringBuffer("[");

				if (layoutManager instanceof TableLayoutManager && ((TableLayoutManager)layoutManager).isRenderSequenceField()) {
					tableToolsColumnOptions.append(" null ,");
				}

				for (Component component : collectionGroup.getItems()) {
					/**
					 * For GroupField, get the first field from that group
					 */
					if (component instanceof GroupField){
						component = ((GroupField)component).getItems().get(0);
					}
					Class dataTypeClass = ObjectPropertyUtils.getPropertyType(collectionGroup.getCollectionObjectClass(), ((AttributeField)component).getPropertyName());
					String colOptions = constructTableColumnOptions(true, dataTypeClass);
					tableToolsColumnOptions.append(colOptions + " , ");
					
				}

				if (collectionGroup.isRenderLineActions()) {
					String colOptions = constructTableColumnOptions(false, null);
					tableToolsColumnOptions.append(colOptions);
				}else{
					tableToolsColumnOptions = new StringBuffer(StringUtils.removeEnd(tableToolsColumnOptions.toString(), ", "));
				}

				tableToolsColumnOptions.append("]");

				getComponentOptions().put(UifConstants.TableToolsKeys.AO_COLUMNS, tableToolsColumnOptions.toString());
				
			}
		}
		
	}
	
	/**
	 * 
	 * This method constructs the sort data type for each datatable columns. 
	 * 
	 */
	protected String constructTableColumnOptions(boolean isSortable, Class dataTypeClass){
		
		String colOptions = "null";
		
		if (!isSortable || dataTypeClass == null){
			colOptions = "{ \"" + UifConstants.TableToolsKeys.SORTABLE + "\" : false } ";
		}else{
			if (ClassUtils.isAssignable(dataTypeClass, String.class)){
				colOptions = "{ \"" + UifConstants.TableToolsKeys.SORT_DATA_TYPE + "\" : \"" + UifConstants.TableToolsValues.DOM_TEXT + "\" } ";
			}else if (ClassUtils.isAssignable(dataTypeClass, Date.class)){
				colOptions = "{ \"" + UifConstants.TableToolsKeys.SORT_DATA_TYPE + "\" : \"" + UifConstants.TableToolsValues.DOM_TEXT + "\" , \"" + UifConstants.TableToolsKeys.SORT_TYPE + "\" : \"" + UifConstants.TableToolsValues.DATE + "\" } ";				
			}else if (ClassUtils.isAssignable(dataTypeClass, Number.class)){
				colOptions = "{ \"" + UifConstants.TableToolsKeys.SORT_DATA_TYPE + "\" : \"" + UifConstants.TableToolsValues.DOM_TEXT + "\" , \"" + UifConstants.TableToolsKeys.SORT_TYPE + "\" : \"" + UifConstants.TableToolsValues.NUMERIC + "\" } ";
			}
		}
		
		return colOptions;
	}
	
	/**
	 * Returns the text which is used to display text when the table is empty
	 * 
	 *  @return empty table message
	 */
	public String getEmptyTableMessage() {
		return emptyTableMessage;
	}

	/**
	 * Setter for a text to be displayed when the table is empty
	 * 
	 * @param emptyTableMessage
	 */
	public void setEmptyTableMessage(String emptyTableMessage) {
		this.emptyTableMessage = emptyTableMessage;
	}
	
	/**
	 * Returns true if sorting is disabled
	 *  
	 * @return the disableTableSort
	 */
	public boolean isDisableTableSort() {
		return this.disableTableSort;
	}

	/**
	 * Enables/disables the table sorting
	 *  
	 * @param disableTableSort the disableTableSort to set
	 */
	public void setDisableTableSort(boolean disableTableSort) {
		this.disableTableSort = disableTableSort;
	}
	
	/**
	 * Returns true if search and export options are enabled
	 * 
	 * @return the showSearchAndExportOptions
	 */
	public boolean isShowSearchAndExportOptions() {
		return this.showSearchAndExportOptions;
	}

	/**
	 * Show/Hide the search and export options in tabletools
	 * 
	 * @param showSearchAndExportOptions the showSearchAndExportOptions to set
	 */
	public void setShowSearchAndExportOptions(boolean showSearchAndExportOptions) {
		this.showSearchAndExportOptions = showSearchAndExportOptions;
	}
}
