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
package org.kuali.rice.kns.uif.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.uif.UifConstants;
import org.kuali.rice.kns.uif.UifConstants.ViewStatus;
import org.kuali.rice.kns.uif.UifConstants.ViewType;
import org.kuali.rice.kns.uif.UifConstants.ViewTypeIndexParameterNames;
import org.kuali.rice.kns.uif.container.View;
import org.kuali.rice.kns.uif.service.ViewHelperService;
import org.kuali.rice.kns.uif.service.ViewService;

/**
 * Implementation of <code>ViewService</code>
 * 
 * <p>
 * Provides methods for retrieving View instances and carrying out the View
 * lifecycle methods. Interacts with the configured
 * <code>ViewHelperService</code> during the view lifecycle
 * </p>
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ViewServiceImpl implements ViewService {
	private static final Logger LOG = Logger.getLogger(ViewServiceImpl.class);

	protected DataDictionaryService dataDictionaryService;

	/**
	 * @see org.kuali.rice.kns.uif.service.ViewService#getViewById(java.lang.String)
	 */
	public View getViewById(String viewId) {
		return getView(viewId, new HashMap<String, String>());
	}

	/**
	 * Retrieves the view from the data dictionary and its corresponding
	 * <code>ViewHelperService</code>. The first phase of the view lifecycle
	 * Initialize is then performed
	 * 
	 * @see org.kuali.rice.kns.uif.service.ViewService#getView(java.lang.String,
	 *      java.util.Map)
	 */
	public View getView(String viewId, Map<String, String> parameters) {
		LOG.debug("retrieving view instance for id: " + viewId);

		View view = dataDictionaryService.getViewById(viewId);
		if (view == null) {
			LOG.error("View not found for id: " + viewId);
			throw new RuntimeException("View not found for id: " + viewId);
		}

		// Initialize Phase
		LOG.debug("performing initialize phase for view: " + viewId);
		performInitialization(view, parameters);

		return view;
	}

	/**
	 * Initializes a newly created <code>View</code> instance. Each component of
	 * the tree is invoked to perform setup based on its configuration. In
	 * addition helper service methods are invoked to perform custom
	 * initialization
	 * 
	 * @param view
	 *            - view instance to initialize
	 * @param parameters
	 *            - Map of key values pairs that provide configuration for the
	 *            <code>View</code>, this is generally comes from the request
	 *            and can be the request parameter Map itself. Any parameters
	 *            not valid for the View will be filtered out
	 */
	protected void performInitialization(View view, Map<String, String> parameters) {
		// get the configured helper service for the view
		ViewHelperService helperService = view.getViewHelperService();

		// get the initial context for the view using the helper service
		Map<String, String> context = helperService.createInitialViewContext(view, parameters);

		// set context on View instance for reference by its components
		view.setContext(context);

		// invoke initialize phase on the views helper service
		helperService.performInitialization(view);

		// update status on view
		LOG.debug("Updating view status to INITIALIZED for view: " + view.getId());
		view.setViewStatus(ViewStatus.INITIALIZED);
	}

	/**
	 * @see org.kuali.rice.kns.uif.service.ViewService#updateView(org.kuali.rice.kns.uif.container.View,
	 *      java.lang.Object)
	 */
	public void updateView(View view, Object model) {
		// get the configured helper service for the view
		ViewHelperService helperService = view.getViewHelperService();

		// Apply Model Phase
		LOG.debug("performing apply model phase for view: " + view.getId());
		helperService.performUpdate(view, model);

		// Update State Phase
		LOG.debug("performing update state phase for view: " + view.getId());
		helperService.performFinalize(view, model);

		// do indexing
		LOG.info("processing indexing for view: " + view.getId());
		view.getViewIndex().index();

		// update status on view
		LOG.debug("Updating view status to UPDATED for view: " + view.getId());
		view.setViewStatus(ViewStatus.FINAL);
	}

	/**
	 * @see org.kuali.rice.kns.uif.service.ViewService#reconstructView(java.lang.String,
	 *      java.util.Map, java.lang.Object)
	 */
	public View reconstructView(String viewId, Map<String, String> parameters, Object model) {
		View view = getView(viewId, parameters);
		updateView(view, model);

		return view;
	}

	/**
	 * @see org.kuali.rice.kns.uif.service.ViewService#getInquiryViewId(java.lang.String,
	 *      java.lang.String)
	 */
	public String getInquiryViewId(String name, String modelClassName) {
		String viewName = name;
		if (StringUtils.isBlank(name)) {
			viewName = UifConstants.DEFAULT_VIEW_NAME;
		}

		Map<String, String> indexKey = new HashMap<String, String>();
		indexKey.put(ViewTypeIndexParameterNames.NAME, viewName);
		indexKey.put(ViewTypeIndexParameterNames.MODEL_CLASS, modelClassName);

		View view = dataDictionaryService.getViewByTypeIndex(ViewType.INQUIRY, indexKey);

		if (view != null) {
			return view.getId();
		}

		return null;
	}

	public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
		this.dataDictionaryService = dataDictionaryService;
	}

}
