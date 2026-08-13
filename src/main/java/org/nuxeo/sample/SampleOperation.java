package org.nuxeo.sample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

@Operation(id=SampleOperation.ID, category=Constants.CAT_DOCUMENT, label="SampleOperation", description="sample operation")
public class SampleOperation {
    public static final String ID = "Document.SampleOperation";

    protected static final Log log = LogFactory.getLog(SampleOperation.class);

    @Context
    protected CoreSession session;

    @OperationMethod
    public DocumentModel run(DocumentModel input){
        log.info("HI from SampleOperation");
        return input;
    }

}
