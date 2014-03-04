/*
 * Copyright 2005-2007 The Kuali Foundation
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
package org.kuali.rice.kns.workflow.service;

import java.rmi.RemoteException;
import java.util.List;

import org.kuali.rice.kew.dto.ActionRequestDTO;
import org.kuali.rice.kew.dto.ActionTakenDTO;
import org.kuali.rice.kew.dto.DocumentSearchCriteriaDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultDTO;
import org.kuali.rice.kew.dto.DocumentTypeDTO;
import org.kuali.rice.kew.dto.ReportCriteriaDTO;
import org.kuali.rice.kew.dto.RouteHeaderDTO;
import org.kuali.rice.kew.exception.WorkflowException;


/**
 * 
 * This class...
 * 
 * 
 */
public interface KualiWorkflowInfo {
    public abstract RouteHeaderDTO getRouteHeader(String principalId, Long routeHeaderId) throws WorkflowException;

    public abstract RouteHeaderDTO getRouteHeader(Long routeHeaderId) throws WorkflowException;

    public abstract DocumentTypeDTO getDocType(Long documentTypeId) throws WorkflowException;

    public abstract DocumentTypeDTO getDocType(String documentTypeName) throws WorkflowException;

    public abstract Long getNewResponsibilityId() throws WorkflowException;

    public abstract ActionRequestDTO[] getActionRequests(Long routeHeaderId) throws WorkflowException;

    public abstract ActionRequestDTO[] getActionRequests(Long routeHeaderId, String nodeName, String principalId) throws WorkflowException;
    
    public abstract ActionTakenDTO[] getActionsTaken(Long routeHeaderId) throws WorkflowException;

    public abstract void reResolveRole(String documentTypeName, String roleName, String qualifiedRoleNameLabel) throws WorkflowException;

    public abstract void reResolveRole(Long routeHeaderId, String roleName, String qualifiedRoleNameLabel) throws WorkflowException;

    /**
     * 
     * Determines whether the given routeHeaderId (also known as a documentNumber, or a docHeaderId) exists and is
     * retrievable in workflow.
     * 
     * @param routeHeaderId The docHeaderId/finDocNumber you would like to test.
     * @return True if the document exists in workflow and is retrievable without errors, False otherwise.
     * 
     */
    public abstract boolean routeHeaderExists(Long routeHeaderId);

    /**
     * Determines if a document generated (or retrieved) using the given criteria has (or will have) an action request using
     * one of the given action request codes.  User may or may not pass in a target node name inside the ReportCriteriaDTO object.
     * 
     * @param reportCriteriaDTO  - Holds either a document type name or a document id as well as other data to help simulate routing
     * @param actionRequestedCodes - List of Action Request Codes from the Workflow system
     * @param ignoreCurrentlyActiveRequests determines if method should look only at simulation generated requests 
     *        or both simulation generated requests and requests that are currently active on the document
     * @return true if the document has or will have at least one request that matches the criteria and has a requested code that matches one of the given codes
     * @throws WorkflowException
     */
    public boolean documentWillHaveAtLeastOneActionRequest(ReportCriteriaDTO reportCriteriaDTO, String[] actionRequestedCodes, boolean ignoreCurrentlyActiveRequests) throws WorkflowException;
    
    /**
     * @deprecated use {@link #documentWillHaveAtLeastOneActionRequest(ReportCriteriaDTO, String[], boolean)} instead
     * 
     * Use of this method passes the value 'false' in for the <code>ignoreCurrentlyActiveRequests</code> parameter of {@link #documentWillHaveAtLeastOneActionRequest(ReportCriteriaDTO, String[], boolean)}
     */
    public boolean documentWillHaveAtLeastOneActionRequest(ReportCriteriaDTO reportCriteriaDTO, String[] actionRequestedCodes) throws WorkflowException;

    public boolean isCurrentActiveDocumentType( String documentTypeName ) throws WorkflowException;
    
    /**
     * This method returns a list of Universal User Ids that have approval or completion requested of them for the document represented by the routeHeaderId parameter
     * 
     * @param routeHeaderId - the id of the document to check
     * @return a list of Universal User Ids that have approval or completion requested of them for the document with the given route header id
     * @throws WorkflowException
     */
    public List<String> getApprovalRequestedUsers(Long routeHeaderId) throws WorkflowException;

    /**
     * This method allows a document search to be executed just as would occur from the User Interface
     * 
     * @param criteriaVO - criteria to use for the search
     * @return a {@link DocumentSearchResultDTO} object containing a list of search result columns and data rows
     * @throws RemoteException
     * @throws WorkflowException
     */
    public DocumentSearchResultDTO performDocumentSearch(DocumentSearchCriteriaDTO criteriaVO) throws WorkflowException;
    
    public DocumentSearchResultDTO performDocumentSearch(String principalId, DocumentSearchCriteriaDTO criteriaVO) throws RemoteException, WorkflowException;    
}