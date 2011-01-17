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
package org.kuali.rice.kns.service.impl;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.bo.BusinessObjectRelationship;
import org.kuali.rice.kns.bo.ExternalizableBusinessObject;
import org.kuali.rice.kns.bo.ModuleConfiguration;
import org.kuali.rice.kns.datadictionary.BusinessObjectEntry;
import org.kuali.rice.kns.datadictionary.PrimitiveAttributeDefinition;
import org.kuali.rice.kns.datadictionary.RelationshipDefinition;
import org.kuali.rice.kns.service.*;
import org.kuali.rice.kns.service.KNSServiceLocatorInternal;
import org.kuali.rice.kns.util.ExternalizableBusinessObjectUtils;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.util.UrlFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * This class implements ModuleService interface.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class ModuleServiceBase implements ModuleService {

	protected static final Logger LOG = Logger.getLogger(ModuleServiceBase.class);

	protected ModuleConfiguration moduleConfiguration;
	protected BusinessObjectService businessObjectService;
	protected LookupService lookupService;
	protected BusinessObjectDictionaryService businessObjectDictionaryService;
	protected KualiModuleService kualiModuleService;
	protected ApplicationContext applicationContext;

	/***
	 * @see org.kuali.rice.kns.service.ModuleService#isResponsibleFor(java.lang.Class)
	 */
	public boolean isResponsibleFor(Class businessObjectClass) {
		if(getModuleConfiguration() == null)
			throw new IllegalStateException("Module configuration has not been initialized for the module service.");

		if (getModuleConfiguration().getPackagePrefixes() == null || businessObjectClass == null) {
			return false;
		}
		for (String prefix : getModuleConfiguration().getPackagePrefixes()) {
			if (businessObjectClass.getPackage().getName().startsWith(prefix)) {
				return true;
			}
		}
		if (ExternalizableBusinessObject.class.isAssignableFrom(businessObjectClass)) {
			Class externalizableBusinessObjectInterface = ExternalizableBusinessObjectUtils.determineExternalizableBusinessObjectSubInterface(businessObjectClass);
			if (externalizableBusinessObjectInterface != null) {
				for (String prefix : getModuleConfiguration().getPackagePrefixes()) {
					if (externalizableBusinessObjectInterface.getPackage().getName().startsWith(prefix)) {
						return true;
					}
				}
			}
		}
		return false;
	}



	/***
	 * @see org.kuali.rice.kns.service.ModuleService#isResponsibleFor(java.lang.Class)
	 */
	public boolean isResponsibleForJob(String jobName) {
		if(getModuleConfiguration() == null)
			throw new IllegalStateException("Module configuration has not been initialized for the module service.");

		if (getModuleConfiguration().getJobNames() == null || StringUtils.isEmpty(jobName))
			return false;

		return getModuleConfiguration().getJobNames().contains(jobName);
	}

    /***
     * @see org.kuali.rice.kns.service.ModuleService#getExternalizableBusinessObject(java.lang.Class, java.util.Map)
     */
    public <T extends ExternalizableBusinessObject> T getExternalizableBusinessObject(Class<T> businessObjectClass, Map<String, Object> fieldValues) {
    	Class<? extends ExternalizableBusinessObject> implementationClass = getExternalizableBusinessObjectImplementation(businessObjectClass);
		ExternalizableBusinessObject businessObject = (ExternalizableBusinessObject)
			getBusinessObjectService().findByPrimaryKey(implementationClass, fieldValues);
        return (T) businessObject;
	}

    /***
     * @see org.kuali.rice.kns.service.ModuleService#getExternalizableBusinessObject(java.lang.Class, java.util.Map)
     */
	public <T extends ExternalizableBusinessObject> List<T> getExternalizableBusinessObjectsList(
			Class<T> externalizableBusinessObjectClass, Map<String, Object> fieldValues) {
		Class<? extends ExternalizableBusinessObject> implementationClass = getExternalizableBusinessObjectImplementation(externalizableBusinessObjectClass);
		return (List<T>) getBusinessObjectService().findMatching(implementationClass, fieldValues);
	}

	/***
	 * @see org.kuali.rice.kns.service.ModuleService#getExternalizableBusinessObjectsListForLookup(java.lang.Class, java.util.Map, boolean)
	 */
	public <T extends ExternalizableBusinessObject> List<T> getExternalizableBusinessObjectsListForLookup(
			Class<T> externalizableBusinessObjectClass, Map<String, Object> fieldValues, boolean unbounded) {
		Class<? extends ExternalizableBusinessObject> implementationClass = getExternalizableBusinessObjectImplementation(externalizableBusinessObjectClass);
		if (isExternalizableBusinessObjectLookupable(implementationClass)) {
		    return (List<T>) getLookupService().findCollectionBySearchHelper(implementationClass, fieldValues, unbounded);
		} else {
		   throw new BusinessObjectNotLookupableException("External business object is not a Lookupable:  " + implementationClass);
		}
	}

	public List listPrimaryKeyFieldNames(Class businessObjectInterfaceClass){
		Class clazz = getExternalizableBusinessObjectImplementation(businessObjectInterfaceClass);
		return KNSServiceLocator.getPersistenceStructureService().listPrimaryKeyFieldNames(clazz);
	}

	/***
	 * @see org.kuali.rice.kns.service.ModuleService#getExternalizableBusinessObjectDictionaryEntry(java.lang.Class)
	 */
	public BusinessObjectEntry getExternalizableBusinessObjectDictionaryEntry(
			Class businessObjectInterfaceClass) {
		Class boClass = businessObjectInterfaceClass;
		if(businessObjectInterfaceClass.isInterface())
			boClass = getExternalizableBusinessObjectImplementation(businessObjectInterfaceClass);
		return boClass==null?null:
			KNSServiceLocatorInternal.getDataDictionaryService().getDataDictionary().getBusinessObjectEntryForConcreteClass(boClass.getName());
	}

	public String getExternalizableBusinessObjectInquiryUrl(Class inquiryBusinessObjectClass, Map<String, String[]> parameters) {
		if(!ExternalizableBusinessObject.class.isAssignableFrom(inquiryBusinessObjectClass)) {
	        return KNSConstants.EMPTY_STRING;
		}
		String businessObjectClassAttribute;
		if(inquiryBusinessObjectClass.isInterface()){
			Class implementationClass = getExternalizableBusinessObjectImplementation(inquiryBusinessObjectClass);
			if (implementationClass == null) {
				LOG.error("Can't find ExternalizableBusinessObject implementation class for interface " + inquiryBusinessObjectClass.getName());
				throw new RuntimeException("Can't find ExternalizableBusinessObject implementation class for interface " + inquiryBusinessObjectClass.getName());
			}
			businessObjectClassAttribute = implementationClass.getName();
		}else{
			LOG.warn("Inquiry was invoked with a non-interface class object " + inquiryBusinessObjectClass.getName());
			businessObjectClassAttribute = inquiryBusinessObjectClass.getName();
		}
        return UrlFactory.parameterizeUrl(
        		getInquiryUrl(inquiryBusinessObjectClass),
        		getUrlParameters(businessObjectClassAttribute, parameters));
	}

	protected Properties getUrlParameters(String businessObjectClassAttribute, Map<String, String[]> parameters){
		Properties urlParameters = new Properties();
		for (String paramName : parameters.keySet()) {
			String[] parameterValues = parameters.get(paramName);
			if (parameterValues.length > 0) {
				urlParameters.put(paramName, parameterValues[0]);
			}
		}
		urlParameters.put(KNSConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, businessObjectClassAttribute);
		urlParameters.put(KNSConstants.DISPATCH_REQUEST_PARAMETER, KNSConstants.CONTINUE_WITH_INQUIRY_METHOD_TO_CALL);
		return urlParameters;
	}

	protected String getInquiryUrl(Class inquiryBusinessObjectClass){
		String riceBaseUrl = KNSServiceLocatorInternal.getKualiConfigurationService().getPropertyString(KNSConstants.APPLICATION_URL_KEY);
		String inquiryUrl = riceBaseUrl;
		if (!inquiryUrl.endsWith("/")) {
			inquiryUrl = inquiryUrl + "/";
		}
		return inquiryUrl + "kr/" + KNSConstants.INQUIRY_ACTION;
	}

	/**
	 * This overridden method ...
	 *
	 * @see org.kuali.rice.kns.service.ModuleService#getExternalizableBusinessObjectLookupUrl(java.lang.Class, java.util.Map)
	 */
	public String getExternalizableBusinessObjectLookupUrl(Class inquiryBusinessObjectClass, Map<String, String> parameters) {
		Properties urlParameters = new Properties();

		String riceBaseUrl = KNSServiceLocatorInternal.getKualiConfigurationService().getPropertyString(KNSConstants.APPLICATION_URL_KEY);
		String lookupUrl = riceBaseUrl;
		if (!lookupUrl.endsWith("/")) {
			lookupUrl = lookupUrl + "/";
		}
		if (parameters.containsKey(KNSConstants.MULTIPLE_VALUE)) {
			lookupUrl = lookupUrl + "kr/" + KNSConstants.MULTIPLE_VALUE_LOOKUP_ACTION;
		}
		else {
			lookupUrl = lookupUrl + "kr/" + KNSConstants.LOOKUP_ACTION;
		}
		for (String paramName : parameters.keySet()) {
			urlParameters.put(paramName, parameters.get(paramName));
		}

		Class clazz = getExternalizableBusinessObjectImplementation(inquiryBusinessObjectClass);
		urlParameters.put(KNSConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, clazz==null?"":clazz.getName());

		return UrlFactory.parameterizeUrl(lookupUrl, urlParameters);
	}

	/***
	 *
	 * This method assumes that the property type for externalizable relationship in the business object is an interface
	 * and gets the concrete implementation for it
	 *
	 * @see org.kuali.rice.kns.service.ModuleService#retrieveExternalizableBusinessObjectIfNecessary(org.kuali.rice.kns.bo.BusinessObject, org.kuali.rice.kns.bo.BusinessObject, java.lang.String)
	 */
	public <T extends ExternalizableBusinessObject> T retrieveExternalizableBusinessObjectIfNecessary(
			BusinessObject businessObject, T currentInstanceExternalizableBO, String externalizableRelationshipName) {

		if(businessObject==null) return null;
		Class clazz;
		try{
			clazz = getExternalizableBusinessObjectImplementation(
					PropertyUtils.getPropertyType(businessObject, externalizableRelationshipName));
		} catch(Exception iex){
			LOG.warn("Exception:"+iex+" thrown while trying to get property type for property:"+externalizableRelationshipName+
					" from business object:"+businessObject);
			return null;
		}

		//Get the business object entry for this business object from data dictionary
		//using the class name (without the package) as key
		BusinessObjectEntry entry =
			KNSServiceLocatorInternal.getDataDictionaryService().getDataDictionary().getBusinessObjectEntries().get(
					businessObject.getClass().getSimpleName());
		RelationshipDefinition relationshipDefinition = entry.getRelationshipDefinition(externalizableRelationshipName);
		List<PrimitiveAttributeDefinition> primitiveAttributeDefinitions = relationshipDefinition.getPrimitiveAttributes();

		Map<String, Object> fieldValuesInEBO = new HashMap<String, Object>();
		Object sourcePropertyValue;
		Object targetPropertyValue = null;
		boolean sourceTargetPropertyValuesSame = true;
		for(PrimitiveAttributeDefinition primitiveAttributeDefinition: primitiveAttributeDefinitions){
	    	sourcePropertyValue = ObjectUtils.getPropertyValue(
	    			businessObject, primitiveAttributeDefinition.getSourceName());
	    	if(currentInstanceExternalizableBO!=null)
	    		targetPropertyValue = ObjectUtils.getPropertyValue(currentInstanceExternalizableBO, primitiveAttributeDefinition.getTargetName());
		    if(sourcePropertyValue==null){
		        return null;
		    } else if(targetPropertyValue==null || (targetPropertyValue!=null && !targetPropertyValue.equals(sourcePropertyValue))){
		    	sourceTargetPropertyValuesSame = false;
		    }
		    fieldValuesInEBO.put(primitiveAttributeDefinition.getTargetName(), sourcePropertyValue);
		}

		if(!sourceTargetPropertyValuesSame)
			return (T) getExternalizableBusinessObject(clazz, fieldValuesInEBO);
		return currentInstanceExternalizableBO;
	}

	/***
	 *
	 * This method assumes that the externalizableClazz is an interface
	 * and gets the concrete implementation for it
	 *
	 * @see org.kuali.rice.kns.service.ModuleService#retrieveExternalizableBusinessObjectIfNecessary(org.kuali.rice.kns.bo.BusinessObject, org.kuali.rice.kns.bo.BusinessObject, java.lang.String)
	 */
	public List<? extends ExternalizableBusinessObject> retrieveExternalizableBusinessObjectsList(
			BusinessObject businessObject, String externalizableRelationshipName, Class externalizableClazz) {

		if(businessObject==null) return null;
		//Get the business object entry for this business object from data dictionary
		//using the class name (without the package) as key
		String className = businessObject.getClass().getName();
		String key = className.substring(className.lastIndexOf(".")+1);
		BusinessObjectEntry entry =
			KNSServiceLocatorInternal.getDataDictionaryService().getDataDictionary().getBusinessObjectEntries().get(key);
		RelationshipDefinition relationshipDefinition = entry.getRelationshipDefinition(externalizableRelationshipName);
		List<PrimitiveAttributeDefinition> primitiveAttributeDefinitions = relationshipDefinition.getPrimitiveAttributes();
		Map<String, Object> fieldValuesInEBO = new HashMap<String, Object>();
		Object sourcePropertyValue;
		for(PrimitiveAttributeDefinition primitiveAttributeDefinition: primitiveAttributeDefinitions){
	    	sourcePropertyValue = ObjectUtils.getPropertyValue(
	    			businessObject, primitiveAttributeDefinition.getSourceName());
		    if(sourcePropertyValue==null){
		        return null;
		    }
		    fieldValuesInEBO.put(primitiveAttributeDefinition.getTargetName(), sourcePropertyValue);
		}
		return getExternalizableBusinessObjectsList(
				getExternalizableBusinessObjectImplementation(externalizableClazz), fieldValuesInEBO);
	}

	/**
	 * @see org.kuali.rice.kns.service.ModuleService#getExternalizableBusinessObjectImplementation(java.lang.Class)
	 */
	public <E extends ExternalizableBusinessObject> Class<E> getExternalizableBusinessObjectImplementation(Class<E> externalizableBusinessObjectInterface) {
		if (getModuleConfiguration() == null) {
			throw new IllegalStateException("Module configuration has not been initialized for the module service.");
		}
		int classModifiers = externalizableBusinessObjectInterface.getModifiers();
		if (!Modifier.isInterface(classModifiers) && !Modifier.isAbstract(classModifiers)) {
			// the interface is really a non-abstract class
			return externalizableBusinessObjectInterface;
		}
		if (getModuleConfiguration().getExternalizableBusinessObjectImplementations() == null) {
			return null;
		}
		else {
			Class<E> implementationClass = getModuleConfiguration().getExternalizableBusinessObjectImplementations().get(externalizableBusinessObjectInterface);
			int implClassModifiers = implementationClass.getModifiers();
			if (Modifier.isInterface(implClassModifiers) || Modifier.isAbstract(implClassModifiers)) {
				throw new RuntimeException("Implementation class must be non-abstract class: ebo interface: " + externalizableBusinessObjectInterface.getName() + " impl class: "
						+ implementationClass.getName() + " module: " + getModuleConfiguration().getNamespaceCode());
			}
			return implementationClass;
		}

	}

	/***
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		KualiModuleService kualiModuleService = null;
		try {
			kualiModuleService = KNSServiceLocatorInternal.getKualiModuleService();
			if ( kualiModuleService == null ) {
				kualiModuleService = ((KualiModuleService)applicationContext.getBean( KNSServiceLocatorInternal.KUALI_MODULE_SERVICE ));
			}
		} catch ( NoSuchBeanDefinitionException ex ) {
			kualiModuleService = ((KualiModuleService)applicationContext.getBean( KNSServiceLocatorInternal.KUALI_MODULE_SERVICE ));
		}
		kualiModuleService.getInstalledModuleServices().add( this );
	}

	/**
	 * @return the moduleConfiguration
	 */
	public ModuleConfiguration getModuleConfiguration() {
		return this.moduleConfiguration;
	}

	/**
	 * @param moduleConfiguration the moduleConfiguration to set
	 */
	public void setModuleConfiguration(ModuleConfiguration moduleConfiguration) {
		this.moduleConfiguration = moduleConfiguration;
	}

    /***
     * @see org.kuali.rice.kns.service.ModuleService#isExternalizable(java.lang.Class)
     */
    public boolean isExternalizable(Class boClazz){
    	if(boClazz==null) return false;
    	return ExternalizableBusinessObject.class.isAssignableFrom(boClazz);
    }

	public boolean isExternalizableBusinessObjectLookupable(Class boClass) {
		return getBusinessObjectDictionaryService().isLookupable(boClass);
	}

	public boolean isExternalizableBusinessObjectInquirable(Class boClass) {
		return getBusinessObjectDictionaryService().isInquirable(boClass);
	}

	public <T extends ExternalizableBusinessObject> T createNewObjectFromExternalizableClass(Class<T> boClass) {
		try {
			return (T) getExternalizableBusinessObjectImplementation(boClass).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Unable to create externalizable business object class", e);
		}
	}

	public BusinessObjectRelationship getBusinessObjectRelationship(Class boClass, String attributeName, String attributePrefix){
		return null;
	}



	public BusinessObjectDictionaryService getBusinessObjectDictionaryService () {
		if ( businessObjectDictionaryService == null ) {
			businessObjectDictionaryService = KNSServiceLocatorInternal.getBusinessObjectDictionaryService();
		}
		return businessObjectDictionaryService;
	}

	/**
	 * @return the businessObjectService
	 */
	public BusinessObjectService getBusinessObjectService() {
		if ( businessObjectService == null ) {
			businessObjectService = KNSServiceLocator.getBusinessObjectService();
		}
		return businessObjectService;
	}

    /**
     * Gets the lookupService attribute.
     * @return Returns the lookupService.
     */
    protected LookupService getLookupService() {
        return lookupService != null ? lookupService : KNSServiceLocatorInternal.getLookupService();
    }

	/**
	 * @return the kualiModuleService
	 */
	public KualiModuleService getKualiModuleService() {
		return this.kualiModuleService;
	}

	/**
	 * @param kualiModuleService the kualiModuleService to set
	 */
	public void setKualiModuleService(KualiModuleService kualiModuleService) {
		this.kualiModuleService = kualiModuleService;
	}

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}



	/**
	 * This overridden method ...
	 *
	 * @see org.kuali.rice.kns.service.ModuleService#listAlternatePrimaryKeyFieldNames(java.lang.Class)
	 */
	public List<List<String>> listAlternatePrimaryKeyFieldNames(
			Class businessObjectInterfaceClass) {
		return null;
	}

}

