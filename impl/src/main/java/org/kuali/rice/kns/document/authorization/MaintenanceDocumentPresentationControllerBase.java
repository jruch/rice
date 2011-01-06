/*
 * Copyright 2007-2008 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kns.document.authorization;

import java.util.HashSet;
import java.util.Set;

import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.service.KNSServiceLocatorInternal;
import org.kuali.rice.kns.service.MaintenanceDocumentDictionaryService;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

/**
 * Base class for all MaintenanceDocumentPresentationControllers.
 */
public class MaintenanceDocumentPresentationControllerBase extends
		DocumentPresentationControllerBase implements
		MaintenanceDocumentPresentationController {
//	private static final Logger LOG = Logger
//			.getLogger(MaintenanceDocumentPresentationControllerBase.class);

	protected static MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService;

	public boolean canCreate(Class boClass) {
		return getMaintenanceDocumentDictionaryService().getAllowsNewOrCopy(
				getMaintenanceDocumentDictionaryService().getDocumentTypeName(
						boClass));
	}

	public Set<String> getConditionallyHiddenPropertyNames(
			BusinessObject businessObject) {
		return new HashSet<String>();
	}

	public Set<String> getConditionallyHiddenSectionIds(
			BusinessObject businessObject) {
		return new HashSet<String>();
	}

	public Set<String> getConditionallyReadOnlyPropertyNames(
			MaintenanceDocument document) {
		return new HashSet<String>();
	}

	public Set<String> getConditionallyReadOnlySectionIds(
			MaintenanceDocument document) {
		return new HashSet<String>();
	}
	
	public Set<String> getConditionallyRequiredPropertyNames(
			MaintenanceDocument document) {
		return new HashSet<String>();
	}	

	public static MaintenanceDocumentDictionaryService getMaintenanceDocumentDictionaryService() {
		if (maintenanceDocumentDictionaryService == null) {
			maintenanceDocumentDictionaryService = KNSServiceLocatorInternal
					.getMaintenanceDocumentDictionaryService();
		}
		return maintenanceDocumentDictionaryService;
	}
	
	/**
	 * @see org.kuali.rice.kns.document.authorization.DocumentPresentationControllerBase#canSave(org.kuali.rice.kns.document.Document)
	 */
	@Override
    protected boolean canSave(Document document){
    	KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
    	return (!workflowDocument.stateIsEnroute() && super.canSave(document));
    } 
	
	/**
	 * @see org.kuali.rice.kns.document.authorization.DocumentPresentationControllerBase#canBlanketApprove(org.kuali.rice.kns.document.Document)
	 */
	@Override
	protected boolean canBlanketApprove(Document document) {
    	KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
    	return (!workflowDocument.stateIsEnroute() && super.canBlanketApprove(document));
	}
}
