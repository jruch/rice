/**
 * Copyright 2005-2013 The Kuali Foundation
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
package edu.sampleu.main;

import org.kuali.rice.testtools.common.Failable;
import org.kuali.rice.testtools.selenium.AutomatedFunctionalTestUtils;
import org.kuali.rice.testtools.selenium.WebDriverUtil;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class CategoryLookUpAftBase extends MainTmplMthdSTNavBase{

    /**
     * ITUtil.PORTAL + "?channelTitle=Category%20Lookup&channelUrl="
     *   + WebDriverUtil.getBaseUrlString() + ITUtil.KRAD_LOOKUP_METHOD
     *   + "org.kuali.rice.krms.impl.repository.CategoryBo"
     *   + ITUtil.SHOW_MAINTENANCE_LINKS
     *   + "&returnLocation=" + ITUtil.PORTAL_URL + ITUtil.HIDE_RETURN_LINK;
     */
    public static final String BOOKMARK_URL = AutomatedFunctionalTestUtils.PORTAL + "?channelTitle=Category%20Lookup&channelUrl="
            + WebDriverUtil.getBaseUrlString() + AutomatedFunctionalTestUtils.KRAD_LOOKUP_METHOD
            + "org.kuali.rice.krms.impl.repository.CategoryBo"
            + AutomatedFunctionalTestUtils.SHOW_MAINTENANCE_LINKS
            + "&returnLocation=" + AutomatedFunctionalTestUtils.PORTAL_URL + AutomatedFunctionalTestUtils.HIDE_RETURN_LINK;
    /**
     * {@inheritDoc}
     * Category Lookup
     * @return
     */
    @Override
    protected String getLinkLocator() {
        return "Category Lookup";
    }

    public void testCategoryLookUpBookmark(Failable failable) throws Exception {
        testCategoryLookUp();
        passed();
    }
    public void testCategoryLookUpNav(Failable failable) throws Exception {
        testCategoryLookUp();
        passed();
    }
}
