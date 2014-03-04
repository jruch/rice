/*
 * Copyright 2005-2008 The Kuali Foundation
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
package org.kuali.rice.kew.actions;

import org.junit.Test;
import org.kuali.rice.kew.dto.NetworkIdDTO;
import org.kuali.rice.kew.dto.UserIdDTO;
import org.kuali.rice.kew.service.WorkflowDocument;
import org.kuali.rice.kew.test.KEWTestCase;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.util.KimConstants;


/**
 *
 * @author delyea
 */
public class ClearFYIActionTest extends KEWTestCase {

    private String getSavedStatusDisplayValue() {
        return (String) KEWConstants.DOCUMENT_STATUSES.get(KEWConstants.ROUTE_HEADER_SAVED_CD);
    }

    @Test public void testSavedDocumentAdhocRequest() throws Exception {
        WorkflowDocument doc = new WorkflowDocument(new NetworkIdDTO("rkirkend"), "TestDocumentType");
        doc.saveDocument("");
        doc.adHocRouteDocumentToPrincipal(KEWConstants.ACTION_REQUEST_FYI_REQ, "annotation1", getPrincipalIdForName("dewey"), "respDesc1", false);
        UserIdDTO user = new NetworkIdDTO("dewey");
        doc = new WorkflowDocument(user, doc.getRouteHeaderId());
        assertTrue("FYI should be requested of user " + user, doc.isFYIRequested());
        try {
            doc.clearFYI();
        } catch (Exception e) {
            fail("A non-initator with an FYI request should be allowed to take the FYI action on a " + getSavedStatusDisplayValue() + " document");
        }
        assertTrue("Document should be " + getSavedStatusDisplayValue(), doc.stateIsSaved());

        UserIdDTO workgroupUser = new NetworkIdDTO("dewey");
        doc = new WorkflowDocument(new NetworkIdDTO("rkirkend"), "TestDocumentType");
        doc.saveDocument("");

        doc.adHocRouteDocumentToGroup(KEWConstants.ACTION_REQUEST_FYI_REQ, "annotation1", getGroupIdForName(KimConstants.KIM_GROUP_WORKFLOW_NAMESPACE_CODE, "NonSIT"), "respDesc1", false);
        doc = new WorkflowDocument(workgroupUser, doc.getRouteHeaderId());
        assertTrue("FYI should be requested of user " + workgroupUser, doc.isFYIRequested());
        try {
            doc.clearFYI();
        } catch (Exception e) {
            fail("A non-initator with an FYI request should be allowed to take the FYI action on a " + getSavedStatusDisplayValue() + " document");
        }
        assertTrue("Document should be " + getSavedStatusDisplayValue(), doc.stateIsSaved());
    }
}