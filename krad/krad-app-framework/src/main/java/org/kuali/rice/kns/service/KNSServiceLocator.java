package org.kuali.rice.kns.service;

import org.kuali.rice.core.api.CoreConstants;
import org.kuali.rice.core.api.DateTimeService;
import org.kuali.rice.core.resourceloader.GlobalResourceLoader;

public class KNSServiceLocator {
    public static final String ATTACHMENT_SERVICE = "attachmentService";
    public static final String PERSISTENCE_SERVICE = "persistenceService";
    public static final String PERSISTENCE_STRUCTURE_SERVICE = "persistenceStructureService";

    public static <T extends Object> T getService(String serviceName) {
        return GlobalResourceLoader.<T>getService(serviceName);
    }

    public static AttachmentService getAttachmentService() {
        return (AttachmentService) getService(ATTACHMENT_SERVICE);
    }

    public static PersistenceService getPersistenceService() {
        return getService(PERSISTENCE_SERVICE);
    }

    public static PersistenceStructureService getPersistenceStructureService() {
        return getService(PERSISTENCE_STRUCTURE_SERVICE);
    }

    public static DateTimeService getDateTimeService() {
        return (DateTimeService) getService(CoreConstants.Services.DATETIME_SERVICE);
    }
}
