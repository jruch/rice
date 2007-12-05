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
package edu.iu.uis.eden.docsearch;

import java.util.List;

/**
 * Class to test custom search criteria processor implementation
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class CustomDocumentSearchCriteriaProcessor extends StandardDocumentSearchCriteriaProcessor {

    @Override
    public List<String> getAdvancedSearchHiddenFieldKeys() {
        List<String> hiddenKeys = super.getAdvancedSearchHiddenFieldKeys();
        hiddenKeys.add(DocumentSearchCriteriaProcessor.CRITERIA_KEY_DOCUMENT_TITLE);
        hiddenKeys.add("givenname");
        return hiddenKeys;
    }

    @Override
    public List<String> getBasicSearchHiddenFieldKeys() {
        List<String> hiddenKeys = super.getBasicSearchHiddenFieldKeys();
        hiddenKeys.add(DocumentSearchCriteriaProcessor.CRITERIA_KEY_CREATE_DATE);
        return hiddenKeys;
    }

    @Override
    public List<String> getGlobalHiddenFieldKeys() {
        List<String> hiddenKeys = super.getGlobalHiddenFieldKeys();
        hiddenKeys.add(DocumentSearchCriteriaProcessor.CRITERIA_KEY_INITIATOR);
        return hiddenKeys;
    }

}
