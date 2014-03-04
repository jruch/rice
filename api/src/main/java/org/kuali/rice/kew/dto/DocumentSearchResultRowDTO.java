/*
 * Copyright 2007-2009 The Kuali Foundation
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
package org.kuali.rice.kew.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a virtual object used for the DocumentSearchResult class.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DocumentSearchResultRowDTO implements Serializable {

    private static final long serialVersionUID = -4512313267985796233L;

    private List<KeyValueDTO> fieldValues = new ArrayList<KeyValueDTO>();

    public List<KeyValueDTO> getFieldValues() {
        return this.fieldValues;
    }

    public void setFieldValues(List<KeyValueDTO> fieldValues) {
        this.fieldValues = fieldValues;
    }

}