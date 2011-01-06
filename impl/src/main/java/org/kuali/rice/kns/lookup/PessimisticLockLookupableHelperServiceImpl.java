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
package org.kuali.rice.kns.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.KIMServiceLocatorInternal;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.entity.KimPrincipal;
import org.kuali.rice.kim.util.KIMPropertyConstants;
import org.kuali.rice.kns.document.authorization.PessimisticLock;
import org.kuali.rice.kns.service.KNSServiceLocatorInternal;
import org.kuali.rice.kns.service.PessimisticLockService;
import org.kuali.rice.kns.util.BeanPropertyComparator;
import org.kuali.rice.kns.util.FieldUtils;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.KNSPropertyConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.util.RiceKeyConstants;
import org.kuali.rice.kns.web.ui.Field;
import org.kuali.rice.kns.web.ui.Row;

/**
 * This class is the lookup helper for {@link org.kuali.rice.kns.document.authorization.PessimisticLock} objects
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class PessimisticLockLookupableHelperServiceImpl extends AbstractLookupableHelperServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PessimisticLockLookupableHelperServiceImpl.class);

    private static final long serialVersionUID = -5839142187907211804L;
    private static final String OWNER_PRINCIPAL_ID_PROPERTY_NAME = "ownedByPrincipalIdentifier";
    private static final String OWNER_PRINCIPAL_NAME_PROPERTY_NAME = "ownedByUser.principalName";

    private List<Row> localRows;

    /**
     * Hides the applicable links when the PessimisticLock is not owned by the current user
     *
     * @see org.kuali.rice.kns.lookup.LookupableHelperService#getCustomActionUrls(org.kuali.rice.kns.bo.BusinessObject, java.util.List, java.util.List pkNames)
     */
    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        PessimisticLock lock = (PessimisticLock)businessObject;
        if ( (lock.isOwnedByUser(GlobalVariables.getUserSession().getPerson())) || (KNSServiceLocatorInternal.getPessimisticLockService().isPessimisticLockAdminUser(GlobalVariables.getUserSession().getPerson())) ) {
            List<HtmlData> anchorHtmlDataList = new ArrayList<HtmlData>();
            anchorHtmlDataList.add(getUrlData(businessObject, KNSConstants.DELETE_METHOD, pkNames));
            return anchorHtmlDataList;
        } else {
            return super.getEmptyActionUrls();
        }
    }

    /**
     * This overridden method checks whether the user is an admin user according to {@link PessimisticLockService#isPessimisticLockAdminUser(Person)} and if the user is not an admin user the user field is set to Read Only and the lookup field
     *
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#getRows()
     */
    @Override
    public List<Row> getRows() {
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        if (KNSServiceLocatorInternal.getPessimisticLockService().isPessimisticLockAdminUser(currentUser)) {
            return super.getRows();
        } else {
            if ( (ObjectUtils.isNull(localRows)) || localRows.isEmpty() ) {
                List<Field> fieldList = new ArrayList<Field>();
                int numColumns = -1;
                // hide a field and forcibly set a field
                for (Iterator<Row> iterator = super.getRows().iterator(); iterator.hasNext();) {
                    Row row = (Row) iterator.next();
                    if (numColumns == -1) {
                    	numColumns = row.getFields().size();
                    }
                    for (Iterator<Field> iterator2 = row.getFields().iterator(); iterator2.hasNext();) {
                        Field field = (Field) iterator2.next();
                        if (!(KNSPropertyConstants.OWNED_BY_USER + "." + KIMPropertyConstants.Person.PRINCIPAL_NAME).equals(field.getPropertyName()) &&
                        		!Field.BLANK_SPACE.equals(field.getFieldType())) {
                            fieldList.add(field);
                        }
                    }
                }
                // Since the removed field is the first one in the list, use FieldUtils to re-wrap the remaining fields accordingly.
                localRows = FieldUtils.wrapFields(fieldList, numColumns);
            }
            return localRows;
        }
    }

    /**
     * This method implementation is used to search for objects
     *
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        // remove hidden fields
        LookupUtils.removeHiddenCriteriaFields( getBusinessObjectClass(), fieldValues );
        // force criteria if not admin user
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        if (!KNSServiceLocatorInternal.getPessimisticLockService().isPessimisticLockAdminUser(currentUser)) {
            fieldValues.put(KNSPropertyConstants.OWNED_BY_PRINCIPAL_ID,GlobalVariables.getUserSession().getPerson().getPrincipalId());
        }

        //set owner's principal id and remove owner principal name field 
        String principalName = fieldValues.get(OWNER_PRINCIPAL_NAME_PROPERTY_NAME);
        if (!StringUtils.isEmpty(principalName)) {
            KimPrincipal principal = KIMServiceLocatorInternal.getIdentityManagementService().getPrincipalByPrincipalName(principalName);
            if (principal != null) { 
                fieldValues.put(OWNER_PRINCIPAL_ID_PROPERTY_NAME, principal.getPrincipalId());
            }
            fieldValues.remove(OWNER_PRINCIPAL_NAME_PROPERTY_NAME);
        }
        
        setBackLocation(fieldValues.get(KNSConstants.BACK_LOCATION));
        setDocFormKey(fieldValues.get(KNSConstants.DOC_FORM_KEY));
        setReferencesToRefresh(fieldValues.get(KNSConstants.REFERENCES_TO_REFRESH));
        if (LOG.isInfoEnabled()) {
        	LOG.info("Search Criteria: " + fieldValues);
        }
        
        //replace principal name with principal id in fieldValues
        List searchResults;
        searchResults = (List) getLookupService().findCollectionBySearchHelper(getBusinessObjectClass(), fieldValues, true);
        // sort list if default sort column given
        List defaultSortColumns = getDefaultSortColumns();
        if (defaultSortColumns.size() > 0) {
            Collections.sort(searchResults, new BeanPropertyComparator(getDefaultSortColumns(), true));
        }
        return searchResults;
    }

    @Override
    public void validateSearchParameters(Map fieldValues) {
        super.validateSearchParameters(fieldValues);
        if (StringUtils.isNotEmpty((String)fieldValues.get(OWNER_PRINCIPAL_NAME_PROPERTY_NAME))) {
            Person person = KIMServiceLocator.getPersonService().getPersonByPrincipalName((String)fieldValues.get(OWNER_PRINCIPAL_NAME_PROPERTY_NAME));
            if (person == null) {
                String attributeLabel = getDataDictionaryService().getAttributeLabel(getBusinessObjectClass(), OWNER_PRINCIPAL_NAME_PROPERTY_NAME);
                GlobalVariables.getMessageMap().putError(OWNER_PRINCIPAL_NAME_PROPERTY_NAME, RiceKeyConstants.ERROR_EXISTENCE, attributeLabel);
            } 
        }
    }

}

