/*
 * Copyright 2005-2007 The Kuali Foundation
 *
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
package org.kuali.rice.kew.actionrequest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.kuali.rice.core.jpa.annotations.Sequence;
import org.kuali.rice.core.util.OrmUtils;
import org.kuali.rice.core.util.RiceConstants;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.bo.WorkflowPersistable;
import org.kuali.rice.kew.engine.CompatUtils;
import org.kuali.rice.kew.engine.node.RouteNode;
import org.kuali.rice.kew.engine.node.RouteNodeInstance;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.rule.RuleBaseValues;
import org.kuali.rice.kew.rule.service.RuleService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.user.RoleRecipient;
import org.kuali.rice.kew.util.CodeTranslator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.entity.KimPrincipal;
import org.kuali.rice.kim.service.KIMServiceLocator;


/**
 * Bean mapped to DB. Represents ActionRequest to a workgroup, user or role.  Contains
 * references to children/parent if a member of a graph
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Entity
@Table(name="KREW_ACTN_RQST_T")
@Sequence(name="KREW_ACTN_RQST_S", property="actionRequestId")
@NamedQueries({
  @NamedQuery(name="ActionRequestValue.FindByRouteHeaderId", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId"),
  @NamedQuery(name="ActionRequestValue.GetUserRequestCount", query="select count(arv) from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.recipientTypeCd = :recipientTypeCd and arv.principalId = :principalId and arv.currentIndicator = :currentIndicator"),
  @NamedQuery(name="ActionRequestValue.FindActivatedByGroup", query="select count(arv) from ActionRequestValue arv where arv.groupId = :groupId and arv.currentIndicator = :currentIndicator and arv.status = :status"),
  @NamedQuery(name="ActionRequestValue.FindAllByDocId", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator"),
  @NamedQuery(name="ActionRequestValue.FindAllPendingByDocId", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and (arv.status = '" + KEWConstants.ACTION_REQUEST_INITIALIZED + "' or arv.status = '" + KEWConstants.ACTION_REQUEST_ACTIVATED + "')"),
  @NamedQuery(name="ActionRequestValue.FindAllRootByDocId", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and arv.parentActionRequest is null"),
  @NamedQuery(name="ActionRequestValue.FindByStatusAndDocId", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and arv.status = :status"),
  @NamedQuery(name="ActionRequestValue.FindPendingByActionRequestedAndDocId", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and arv.actionRequested = :actionRequested and (arv.status = '" + KEWConstants.ACTION_REQUEST_INITIALIZED + "' or arv.status = '" + KEWConstants.ACTION_REQUEST_ACTIVATED + "')"),
  @NamedQuery(name="ActionRequestValue.FindPendingByDocIdAtOrBelowRouteLevel", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and arv.status <> :status and arv.routeLevel <= :routeLevel"),
  @NamedQuery(name="ActionRequestValue.FindPendingByResponsibilityIds", query="select arv from ActionRequestValue arv where arv.responsibilityId in (:responsibilityIds) and (arv.status = '" + KEWConstants.ACTION_REQUEST_INITIALIZED + "' or arv.status = '" + KEWConstants.ACTION_REQUEST_ACTIVATED + "')"),
  @NamedQuery(name="ActionRequestValue.FindPendingRootRequestsByDocIdAtOrBelowRouteLevel", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and arv.parentActionRequest is null and arv.status <> :status and routeLevel <= :routeLevel"),
  @NamedQuery(name="ActionRequestValue.FindPendingRootRequestsByDocIdAtRouteLevel", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and arv.parentActionRequest is null and arv.status <> :status and routeLevel = :routeLevel"),
  @NamedQuery(name="ActionRequestValue.FindPendingRootRequestsByDocIdAtRouteNode", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and arv.parentActionRequest is null and arv.nodeInstance.routeNodeInstanceId = :routeNodeInstanceId and (arv.status = '" + KEWConstants.ACTION_REQUEST_INITIALIZED + "' or arv.status = '" + KEWConstants.ACTION_REQUEST_ACTIVATED + "')"),
  @NamedQuery(name="ActionRequestValue.FindPendingRootRequestsByDocumentType", query="select arv from ActionRequestValue arv where arv.routeHeader.documentTypeId = :documentTypeId and arv.currentIndicator = :currentIndicator and arv.parentActionRequest is null and (arv.status = '" + KEWConstants.ACTION_REQUEST_INITIALIZED + "' or arv.status = '" + KEWConstants.ACTION_REQUEST_ACTIVATED + "')"),
  @NamedQuery(name="ActionRequestValue.FindRootRequestsByDocIdAtRouteNode", query="select arv from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and arv.parentActionRequest is null and arv.nodeInstance.routeNodeInstanceId = :routeNodeInstanceId"),
  @NamedQuery(name="ActionRequestValue.GetRequestGroupIds", query="select arv.groupId from ActionRequestValue arv where arv.routeHeaderId = :routeHeaderId and arv.currentIndicator = :currentIndicator and arv.recipientTypeCd = :recipientTypeCd"),
  @NamedQuery(name="ActionRequestValue.FindByStatusAndGroupId", query="select arv from ActionRequestValue arv where arv.groupId = :groupId and arv.currentIndicator = :currentIndicator and arv.status = :status")
})
public class ActionRequestValue implements WorkflowPersistable {

	private static final long serialVersionUID = 8781414791855848385L;

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ActionRequestValue.class);

    private static final String ACTION_CODE_RANK = "FKACB";//B is a hack for allowing blanket approves to count for approve and complete requests in findPreviousAction in ActionTakenService this is a hack and accounts for the -3 on compareActionCode
    private static final String RECIPIENT_TYPE_RANK = "RWU";
    private static final String DELEGATION_TYPE_RANK = "SPN";

    @Id
	@Column(name="ACTN_RQST_ID")
	private Long actionRequestId;
    @Column(name="ACTN_RQST_CD")
	private String actionRequested;
    @Column(name="DOC_HDR_ID", insertable=false, updatable=false)
	private Long routeHeaderId;
    @Column(name="STAT_CD")
	private String status;
    @Column(name="RSP_ID")
	private Long responsibilityId;
    @Column(name="GRP_ID")
	private String groupId;
    @Column(name="RECIP_TYP_CD")
	private String recipientTypeCd;
    @Column(name="PRIO_NBR")
	private Integer priority;
    @Column(name="RTE_LVL_NBR")
	private Integer routeLevel;
    @Column(name="ACTN_TKN_ID", insertable=false, updatable=false)
	private Long actionTakenId;
    @Column(name="DOC_VER_NBR")
    private Integer docVersion = 1;
	@Column(name="CRTE_DT")
	private java.sql.Timestamp createDate;
    @Column(name="RSP_DESC_TXT")
	private String responsibilityDesc;
    @Column(name="ACTN_RQST_ANNOTN_TXT")
	private String annotation;
    @Column(name="VER_NBR")
	private Integer jrfVerNbr;
    @Column(name="PRNCPL_ID")
	private String principalId;
    @Column(name="FRC_ACTN")
	private Boolean forceAction;
    @Column(name="PARNT_ID", insertable=false, updatable=false)
	private Long parentActionRequestId;
    @Column(name="QUAL_ROLE_NM")
	private String qualifiedRoleName;
    @Column(name="ROLE_NM")
	private String roleName;
    @Column(name="QUAL_ROLE_NM_LBL_TXT")
	private String qualifiedRoleNameLabel;
    @Transient
    private String displayStatus;
    @Column(name="RULE_ID")
	private Long ruleBaseValuesId;

    @Column(name="DLGN_TYP")
    private String delegationType = KEWConstants.DELEGATION_NONE;
    @Column(name="APPR_PLCY")
	private String approvePolicy;

    @ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="PARNT_ID")
	private ActionRequestValue parentActionRequest;
    @Fetch(value = FetchMode.SUBSELECT)
    @ManyToMany(mappedBy="parentActionRequest",cascade={CascadeType.PERSIST, CascadeType.MERGE},fetch=FetchType.EAGER)
    private List<ActionRequestValue> childrenRequests = new ArrayList<ActionRequestValue>();
    @ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="ACTN_TKN_ID")
	private ActionTakenValue actionTaken;
    @ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="DOC_HDR_ID")
	private DocumentRouteHeaderValue routeHeader;
    @Fetch(value = FetchMode.SUBSELECT)
    @OneToMany(fetch=FetchType.EAGER,mappedBy="actionRequestId")
    private List<ActionItem> actionItems = new ArrayList<ActionItem>();
    @Column(name="CUR_IND")
    private Boolean currentIndicator = true;
    @Transient
    private String createDateString;
    @Transient
    private String groupName;

    /* New Workflow 2.1 Field */
    // The node instance at which this request was generated
    @ManyToOne(fetch=FetchType.EAGER/*, cascade={CascadeType.PERSIST}*/)
	@JoinColumn(name="RTE_NODE_INSTN_ID")
	private RouteNodeInstance nodeInstance;

    @Column(name="RQST_LBL")
    private String requestLabel;
    
    @Transient
    private boolean resolveResponsibility = true;
    
    public ActionRequestValue() {
        createDate = new Timestamp(System.currentTimeMillis());
    }
    
    @PrePersist
    public void beforeInsert(){
    	OrmUtils.populateAutoIncValue(this, KEWServiceLocator.getEntityManagerFactory().createEntityManager());
    }
   
    public Group getGroup() {
        if (getGroupId() == null) {
            LOG.error("Attempting to get a group with a blank group id");
            return null;
        }
        return KIMServiceLocator.getIdentityManagementService().getGroup(getGroupId());
    }

    public String getRouteLevelName() {
        // this is for backward compatibility of requests which have not been converted
        if (CompatUtils.isRouteLevelRequest(this)) {
            int routeLevelInt = getRouteLevel();
            if (routeLevelInt == KEWConstants.EXCEPTION_ROUTE_LEVEL) {
                return "Exception";
            }

            List routeLevelNodes = CompatUtils.getRouteLevelCompatibleNodeList(routeHeader.getDocumentType());
            if (!(routeLevelInt < routeLevelNodes.size())) {
                return "Not Found";
            }
            return ((RouteNode)routeLevelNodes.get(routeLevelInt)).getRouteNodeName();
        } else {
            return (nodeInstance == null ? "Exception" : nodeInstance.getName());
        }
    }

    public boolean isUserRequest() {
        return principalId != null;
    }

    public KimPrincipal getPrincipal() {
    	if (getPrincipalId() == null) {
    		return null;
    	}
    	return KEWServiceLocator.getIdentityHelperService().getPrincipal(getPrincipalId());
    }
    
    public Person getPerson() {
    	if (getPrincipalId() == null) {
    		return null;
    	}
    	return KIMServiceLocator.getPersonService().getPerson(getPrincipalId());
    }

    public String getDisplayName() {
    	if (isUserRequest()) {
    	    return getPerson().getName();
    	} else if (isGroupRequest()) {
    		return getGroup().getGroupName();
    	} else if (isRoleRequest()) {
    		return getRoleName();
    	}
    	return "";
    }

    public Recipient getRecipient() {
        if (getPrincipalId() != null) {
            return new KimPrincipalRecipient(getPrincipal());
        } else if (getGroupId() != null){
            return new KimGroupRecipient(getGroup());
        } else {
        	return new RoleRecipient(this.getRoleName());
        }
    }

    public boolean isPending() {
        return KEWConstants.ACTION_REQUEST_INITIALIZED.equals(getStatus()) || KEWConstants.ACTION_REQUEST_ACTIVATED.equals(getStatus());
    }

    public DocumentRouteHeaderValue getRouteHeader() {
        return routeHeader;
    }

    public String getStatusLabel() {
        return CodeTranslator.getActionRequestStatusLabel(getStatus());
    }

    public String getActionRequestedLabel() {
    	if (StringUtils.isNotBlank(getRequestLabel())) {
    		return getRequestLabel();
    	}
    	return CodeTranslator.getActionRequestLabel(getActionRequested());
    }

    /**
     * @param routeHeader
     *            The routeHeader to set.
     */
    public void setRouteHeader(DocumentRouteHeaderValue routeHeader) {
        this.routeHeader = routeHeader;
    }

    /**
     * @return Returns the actionTaken.
     */
    public ActionTakenValue getActionTaken() {
        return actionTaken;
    }

    /**
     * @param actionTaken
     *            The actionTaken to set.
     */
    public void setActionTaken(ActionTakenValue actionTaken) {
        this.actionTaken = actionTaken;
    }

    /**
     * @return Returns the actionRequested.
     */
    public String getActionRequested() {
        return actionRequested;
    }

    /**
     * @param actionRequested
     *            The actionRequested to set.
     */
    public void setActionRequested(String actionRequested) {
        this.actionRequested = actionRequested;
    }

    /**
     * @return Returns the actionRequestId.
     */
    public Long getActionRequestId() {
        return actionRequestId;
    }

    /**
     * @param actionRequestId
     *            The actionRequestId to set.
     */
    public void setActionRequestId(Long actionRequestId) {
        this.actionRequestId = actionRequestId;
    }

    /**
     * @return Returns the actionTakenId.
     */
    public Long getActionTakenId() {
        return actionTakenId;
    }

    /**
     * @param actionTakenId
     *            The actionTakenId to set.
     */
    public void setActionTakenId(Long actionTakenId) {
        this.actionTakenId = actionTakenId;
    }

    /**
     * @return Returns the annotation.
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * @param annotation
     *            The annotation to set.
     */
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    /**
     * @return Returns the createDate.
     */
    public java.sql.Timestamp getCreateDate() {
        return createDate;
    }

    /**
     * @param createDate
     *            The createDate to set.
     */
    public void setCreateDate(java.sql.Timestamp createDate) {
        this.createDate = createDate;
    }

    /**
     * @return Returns the docVersion.
     */
    public Integer getDocVersion() {
        return docVersion;
    }

    /**
     * @param docVersion
     *            The docVersion to set.
     */
    public void setDocVersion(Integer docVersion) {
        this.docVersion = docVersion;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }
    
    /**
     * @return Returns the forceAction.
     */
    public Boolean getForceAction() {
        return forceAction;
    }

    /**
     * @param forceAction
     *            The forceAction to set.
     */
    public void setForceAction(Boolean forceAction) {
        this.forceAction = forceAction;
    }

    /**
     * @return Returns the jrfVerNbr.
     */
    public Integer getJrfVerNbr() {
        return jrfVerNbr;
    }

    /**
     * @param jrfVerNbr
     *            The jrfVerNbr to set.
     */
    public void setJrfVerNbr(Integer jrfVerNbr) {
        this.jrfVerNbr = jrfVerNbr;
    }

    /**
     * @return Returns the priority.
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * @param priority
     *            The priority to set.
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * @return Returns the recipientTypeCd.
     */
    public String getRecipientTypeCd() {
        return recipientTypeCd;
    }

    /**
     * @param recipientTypeCd
     *            The recipientTypeCd to set.
     */
    public void setRecipientTypeCd(String recipientTypeCd) {
        this.recipientTypeCd = recipientTypeCd;
    }

    /**
     * @return Returns the responsibilityDesc.
     */
    public String getResponsibilityDesc() {
        return responsibilityDesc;
    }

    /**
     * @param responsibilityDesc
     *            The responsibilityDesc to set.
     */
    public void setResponsibilityDesc(String responsibilityDesc) {
        this.responsibilityDesc = responsibilityDesc;
    }

    /**
     * @return Returns the responsibilityId.
     */
    public Long getResponsibilityId() {
        return responsibilityId;
    }

    /**
     * @param responsibilityId
     *            The responsibilityId to set.
     */
    public void setResponsibilityId(Long responsibilityId) {
        this.responsibilityId = responsibilityId;
    }

    /**
     * @return Returns the routeHeaderId.
     */
    public Long getRouteHeaderId() {
        return routeHeaderId;
    }

    public void setRouteHeaderId(Long routeHeaderId) {
        this.routeHeaderId = routeHeaderId;
    }

    public Integer getRouteLevel() {
        return routeLevel;
    }

    public void setRouteLevel(Integer routeLevel) {
        this.routeLevel = routeLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Object copy(boolean preserveKeys) {
        ActionRequestValue clone = new ActionRequestValue();
        try {
            BeanUtils.copyProperties(clone, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!preserveKeys) {
            clone.setActionRequestId(null);
        }
        ActionTakenValue actionTakenClone = (ActionTakenValue) getActionTaken().copy(preserveKeys);
        clone.setActionTaken(actionTakenClone);
        return clone;
    }

    public boolean isInitialized() {
        return KEWConstants.ACTION_REQUEST_INITIALIZED.equals(getStatus());
    }

    public boolean isActive() {
        return KEWConstants.ACTION_REQUEST_ACTIVATED.equals(getStatus());
    }

    public boolean isApproveOrCompleteRequest() {
        return KEWConstants.ACTION_REQUEST_APPROVE_REQ.equals(getActionRequested()) || KEWConstants.ACTION_REQUEST_COMPLETE_REQ.equals(getActionRequested());
    }

    public boolean isDone() {
        return KEWConstants.ACTION_REQUEST_DONE_STATE.equals(getStatus());
    }

    public boolean isReviewerUser() {
        return KEWConstants.ACTION_REQUEST_USER_RECIPIENT_CD.equals(getRecipientTypeCd());
    }

    public boolean isRecipientRoutedRequest(String principalId) {
    	//before altering this method it is used in checkRouteLogAuthentication
    	//don't break that method
    	if (principalId == null || "".equals(principalId)) {
    		return false;
    	}

    	boolean isRecipientInGraph = false;
    	if (isReviewerUser()) {
    			isRecipientInGraph = getPrincipalId().equals(principalId);
    	} else if (isGroupRequest()) {
    		Group group = getGroup();
			if (group == null){
				LOG.error("Was unable to retrieve workgroup " + getGroupId());
			}
    		isRecipientInGraph = KIMServiceLocator.getIdentityManagementService().isMemberOfGroup(principalId, group.getGroupId());
    	}


        for (ActionRequestValue childRequest : getChildrenRequests())
        {
            isRecipientInGraph = isRecipientInGraph || childRequest.isRecipientRoutedRequest(principalId);
        }

    	return isRecipientInGraph;
    }

    public boolean isRecipientRoutedRequest(Recipient recipient) {
    	//before altering this method it is used in checkRouteLogAuthentication
    	//don't break that method
    	if (recipient == null) {
    		return false;
    	}

    	boolean isRecipientInGraph = false;
    	if (isReviewerUser()) {
    		if (recipient instanceof KimPrincipalRecipient) {
    			isRecipientInGraph = getPrincipalId().equals(((KimPrincipalRecipient) recipient).getPrincipalId());
    		} else if (recipient instanceof KimGroupRecipient){
    			isRecipientInGraph = KIMServiceLocator.getIdentityManagementService().isMemberOfGroup(getPrincipalId(), ((KimGroupRecipient)recipient).getGroup().getGroupId());
    		}

    	} else if (isGroupRequest()) {
    		Group group = getGroup();
			if (group == null){
				LOG.error("Was unable to retrieve workgroup " + getGroupId());
			}
    		if (recipient instanceof KimPrincipalRecipient) {
    			KimPrincipalRecipient principalRecipient = (KimPrincipalRecipient)recipient;
    			isRecipientInGraph = KIMServiceLocator.getIdentityManagementService().isMemberOfGroup(principalRecipient.getPrincipalId(), group.getGroupId());
    		} else if (recipient instanceof KimGroupRecipient) {
    			isRecipientInGraph = ((KimGroupRecipient) recipient).getGroup().getGroupId().equals(group.getGroupId());
    		}
    	}


        for (ActionRequestValue childRequest : getChildrenRequests())
        {
            isRecipientInGraph = isRecipientInGraph || childRequest.isRecipientRoutedRequest(recipient);
        }

    	return isRecipientInGraph;
    }

    public boolean isGroupRequest(){
    	return KEWConstants.ACTION_REQUEST_GROUP_RECIPIENT_CD.equals(getRecipientTypeCd());
    }

    public boolean isRoleRequest() {
        return KEWConstants.ACTION_REQUEST_ROLE_RECIPIENT_CD.equals(getRecipientTypeCd());
    }

    public boolean isAcknowledgeRequest() {
        return KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ.equals(getActionRequested());
    }

    public boolean isApproveRequest() {
        return KEWConstants.ACTION_REQUEST_APPROVE_REQ.equals(getActionRequested());
    }

    public boolean isCompleteRequst() {
        return KEWConstants.ACTION_REQUEST_COMPLETE_REQ.equals(getActionRequested());
    }

    public boolean isFYIRequest() {
        return KEWConstants.ACTION_REQUEST_FYI_REQ.equals(getActionRequested());
    }

    /**
     * Allows comparison of action requests to see which is greater responsibility. -1 : indicates code 1 is lesser responsibility than code 2 0 : indicates the same responsibility 1 : indicates code1 is greater responsibility than code 2 The priority of action requests is as follows: fyi < acknowledge < (approve == complete)
     *
     * @param code1
     * @param code2
     * @param completeAndApproveTheSame
     * @return -1 if less than, 0 if equal, 1 if greater than
     */
    public static int compareActionCode(String code1, String code2, boolean completeAndApproveTheSame) {
    	int cutoff = Integer.MAX_VALUE;
    	if (completeAndApproveTheSame) {
    		// hacked so that APPROVE and COMPLETE are equal
    		cutoff = ACTION_CODE_RANK.length() - 3;
    	}
        Integer code1Index = Math.min(ACTION_CODE_RANK.indexOf(code1), cutoff);
        Integer code2Index = Math.min(ACTION_CODE_RANK.indexOf(code2), cutoff);
        return code1Index.compareTo(code2Index);
    }

    /**
     * Allows comparison of action requests to see which is greater responsibility. -1 : indicates type 1 is lesser responsibility than type 2 0 : indicates the same responsibility 1 : indicates type1 is greater responsibility than type 2
     *
     * @param type1
     * @param type2
     * @return -1 if less than, 0 if equal, 1 if greater than
     */
    public static int compareRecipientType(String type1, String type2) {
        Integer type1Index = RECIPIENT_TYPE_RANK.indexOf(type1);
        Integer type2Index = RECIPIENT_TYPE_RANK.indexOf(type2);
        return type1Index.compareTo(type2Index);
    }

    public static int compareDelegationType(String type1, String type2) {
    	if (StringUtils.isEmpty(type1)) {
    		type1 = "N";
    	}
    	if (StringUtils.isEmpty(type2)) {
    		type2 = "N";
    	}
    	Integer type1Index = DELEGATION_TYPE_RANK.indexOf(type1);
        Integer type2Index = DELEGATION_TYPE_RANK.indexOf(type2);
        return type1Index.compareTo(type2Index);
    }

    public List<ActionItem> getActionItems() {
        return actionItems;
    }

    public void setActionItems(List<ActionItem> actionItems) {
        this.actionItems = actionItems;
    }

    public Boolean getCurrentIndicator() {
        return currentIndicator;
    }

    public void setCurrentIndicator(Boolean currentIndicator) {
        this.currentIndicator = currentIndicator;
    }

    public Long getParentActionRequestId() {
        return parentActionRequestId;
    }

    public void setParentActionRequestId(Long parentActionRequestId) {
        this.parentActionRequestId = parentActionRequestId;
    }

    public ActionRequestValue getParentActionRequest() {
        return parentActionRequest;
    }

    public void setParentActionRequest(ActionRequestValue parentActionRequest) {
        this.parentActionRequest = parentActionRequest;
    }

    public List<ActionRequestValue> getChildrenRequests() {
        return childrenRequests;
    }

    public void setChildrenRequests(List<ActionRequestValue> childrenRequests) {
        this.childrenRequests = childrenRequests;
    }

    public String getQualifiedRoleName() {
        return qualifiedRoleName;
    }

    public void setQualifiedRoleName(String roleName) {
        this.qualifiedRoleName = roleName;
    }

    public String getDelegationType() {
        return delegationType;
    }

    public void setDelegationType(String delegatePolicy) {
        this.delegationType = delegatePolicy;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getApprovePolicy() {
        return approvePolicy;
    }

    public void setApprovePolicy(String requestType) {
        this.approvePolicy = requestType;
    }

    public boolean getHasApprovePolicy() {
        return getApprovePolicy() != null;
    }

    public boolean isDeactivated() {
        return KEWConstants.ACTION_REQUEST_DONE_STATE.equals(getStatus());
    }

    public boolean hasParent() {
        return getParentActionRequest() != null;
    }

    public boolean hasChild(ActionRequestValue actionRequest) {
        if (actionRequest == null)
            return false;
        Long actionRequestId = actionRequest.getActionRequestId();
        for (Iterator<ActionRequestValue> iter = getChildrenRequests().iterator(); iter.hasNext();) {
            ActionRequestValue childRequest = iter.next();
            if (childRequest.equals(actionRequest) || (actionRequestId != null && actionRequestId.equals(childRequest.getActionRequestId()))) {
                return true;
            }
        }
        return false;
    }

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public String getQualifiedRoleNameLabel() {
        return qualifiedRoleNameLabel;
    }

    public void setQualifiedRoleNameLabel(String qualifiedRoleNameLabel) {
        this.qualifiedRoleNameLabel = qualifiedRoleNameLabel;
    }

    public String getCreateDateString() {
        if (createDateString == null || createDateString.trim().equals("")) {
            return RiceConstants.getDefaultDateFormat().format(getCreateDate());
        } else {
            return createDateString;
        }
    }

    public void setCreateDateString(String createDateString) {
        this.createDateString = createDateString;
    }

    public RouteNodeInstance getNodeInstance() {
		return nodeInstance;
	}

    public String getPotentialNodeName() {
        return (getNodeInstance() == null ? "" : getNodeInstance().getName());
    }

	public void setNodeInstance(RouteNodeInstance nodeInstance) {
		this.nodeInstance = nodeInstance;
	}

	public String getRecipientTypeLabel() {
        return (String) KEWConstants.ACTION_REQUEST_RECIPIENT_TYPE.get(getRecipientTypeCd());
    }

    public RuleBaseValues getRuleBaseValues(){
        if(ruleBaseValuesId != null){
            return getRuleService().findRuleBaseValuesById(ruleBaseValuesId);
        }
        return null;
    }
    public Long getRuleBaseValuesId() {
        return ruleBaseValuesId;
    }

    public void setRuleBaseValuesId(Long ruleBaseValuesId) {
        this.ruleBaseValuesId = ruleBaseValuesId;
    }
    
	private RuleService getRuleService() {
        return (RuleService) KEWServiceLocator.getService(KEWServiceLocator.RULE_SERVICE);
    }

    public boolean isPrimaryDelegator() {
        boolean primaryDelegator = false;
        for (Iterator<ActionRequestValue> iter = childrenRequests.iterator(); iter.hasNext();) {
            ActionRequestValue childRequest = iter.next();
            primaryDelegator = KEWConstants.DELEGATION_PRIMARY.equals(childRequest.getDelegationType()) || primaryDelegator;
        }
        return primaryDelegator;
    }

    /**
     * Used to get primary delegate names on route log in the 'Requested Of' section so primary delegate requests
     * list the delegate and not the delegator as having the request 'IN ACTION LIST'.  This method doesn't recurse
     * and therefore assume an AR structure.
     *
     * @return primary delgate requests
     */
    public List<ActionRequestValue> getPrimaryDelegateRequests() {
        List<ActionRequestValue> primaryDelegateRequests = new ArrayList<ActionRequestValue>();
        for (ActionRequestValue childRequest : childrenRequests)
        {
            if (KEWConstants.DELEGATION_PRIMARY.equals(childRequest.getDelegationType()))
            {
                if (childRequest.isRoleRequest())
                {
                    for (ActionRequestValue actionRequestValue : childRequest.getChildrenRequests())
                    {
                        primaryDelegateRequests.add(actionRequestValue);
                    }
                } else
                {
                	primaryDelegateRequests.add(childRequest);
                }
            }
        }
        return primaryDelegateRequests;
    }

    public boolean isAdHocRequest() {                                          
    	return KEWConstants.ADHOC_REQUEST_RESPONSIBILITY_ID.equals(getResponsibilityId());
    }

    public boolean isGeneratedRequest() {
    	return KEWConstants.MACHINE_GENERATED_RESPONSIBILITY_ID.equals(getResponsibilityId());
    }

    public boolean isExceptionRequest() {
    	return KEWConstants.EXCEPTION_REQUEST_RESPONSIBILITY_ID.equals(getResponsibilityId());
    }

    public boolean isRouteModuleRequest() {
    	return getResponsibilityId() > 0;
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("actionRequestId", actionRequestId)
            .append("actionRequested", actionRequested)
            .append("routeHeaderId", routeHeaderId)
            .append("status", status)
            .append("responsibilityId", responsibilityId)
            .append("groupId", groupId)
            .append("recipientTypeCd", recipientTypeCd)
            .append("priority", priority)
            .append("routeLevel", routeLevel)
            .append("actionTakenId", actionTakenId)
            .append("docVersion", docVersion)
            .append("createDate", createDate)
            .append("responsibilityDesc", responsibilityDesc)
            .append("annotation", annotation)
            .append("jrfVerNbr", jrfVerNbr)
            .append("principalId", principalId)
            .append("forceAction", forceAction)
            .append("parentActionRequestId", parentActionRequestId)
            .append("qualifiedRoleName", qualifiedRoleName)
            .append("roleName", roleName)
            .append("qualifiedRoleNameLabel", qualifiedRoleNameLabel)
            .append("displayStatus", displayStatus)
            .append("ruleBaseValuesId", ruleBaseValuesId)
            .append("delegationType", delegationType)
            .append("approvePolicy", approvePolicy)
            .append("childrenRequests", childrenRequests == null ? null : childrenRequests.size())
            .append("actionTaken", actionTaken)
            .append("routeHeader", routeHeader)
            .append("actionItems", actionItems == null ? null : actionItems.size())
            .append("currentIndicator", currentIndicator)
            .append("createDateString", createDateString)
            .append("nodeInstance", nodeInstance).toString();
    }

	public String getRequestLabel() {
		return this.requestLabel;
	}

	public void setRequestLabel(String requestLabel) {
		this.requestLabel = requestLabel;
	}

    public String getGroupName() {
        return KIMServiceLocator.getIdentityManagementService().getGroup(this.groupId).getGroupName();
    }

	/**
	 * @return the resolveResponsibility
	 */
	public boolean getResolveResponsibility() {
		return this.resolveResponsibility;
	}

	/**
	 * @param resolveResponsibility the resolveResponsibility to set
	 */
	public void setResolveResponsibility(boolean resolveResponsibility) {
		this.resolveResponsibility = resolveResponsibility;
	}
    
}