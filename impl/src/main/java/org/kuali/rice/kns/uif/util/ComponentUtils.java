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
package org.kuali.rice.kns.uif.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kns.uif.Component;
import org.kuali.rice.kns.uif.DataBinding;
import org.kuali.rice.kns.uif.field.Field;
import org.kuali.rice.kns.util.ObjectUtils;

/**
 * Utility class providing methods to help create and modify
 * <code>Component</code> instances
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ComponentUtils {

	public static <T extends Component> T copy(T component) {
		return (T) ObjectUtils.deepCopy(component);
	}

	public static <T extends Field> List<T> copyFieldListAndPrefix(List<T> fields, String addBindingPrefix) {
		List<T> copiedFieldList = copyFieldList(fields);

		for (Field field : copiedFieldList) {
			if (field instanceof DataBinding) {
				prefixBindingPath((DataBinding) field, addBindingPrefix);
			}
		}

		return copiedFieldList;
	}

	public static <T extends Field> List<T> copyFieldList(List<T> fields) {
		List<T> copiedFieldList = new ArrayList<T>();

		for (T field : fields) {
			T copiedField = copy(field);
			copiedFieldList.add(copiedField);
		}

		return copiedFieldList;
	}

	public static void prefixBindingPath(DataBinding field, String addBindingPrefix) {
		String bindingPrefix = addBindingPrefix;
		if (StringUtils.isNotBlank(field.getBindByNamePrefix())) {
			bindingPrefix += "." + field.getBindByNamePrefix();
		}
		field.setBindByNamePrefix(bindingPrefix);
	}

}
