package org.nuxeo.sample;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.runtime.api.Framework;

@Operation(id=SampleOperation.ID, category=Constants.CAT_DOCUMENT, label="SampleOperation", description="sample operation")
public class SampleOperation {

    public static final String ID = "Document.SampleOperation";

    private static int MAX_ITERATION = 5;

    @Context
    protected CoreSession session;

    @OperationMethod
    public DocumentModel run(DocumentModel input){

        DocumentModel doc = session.getSourceDocument(input.getRef()); 
        for (int i = 0; i < MAX_ITERATION && !isLive(doc); i++) {
            doc = session.getSourceDocument(doc.getRef());
        }

        if (!session.isCheckedOut(doc.getRef())) {
            session.checkOut(doc.getRef());
        }
        session.removeDocument(input.getRef());
        return null;
    }

    @OperationMethod
    public DocumentModel run(DocumentRef input){

        DocumentModel doc = session.getSourceDocument(input);
        for (int i = 0; i < MAX_ITERATION && !isLive(doc); i++) {
            doc = session.getSourceDocument(doc.getRef());
        }

        if (!session.isCheckedOut(doc.getRef())) {
            session.checkOut(doc.getRef());
        }
        session.removeDocument(input);
        return null;
    }

    private boolean isLive(DocumentModel doc) {
        return !doc.isVersion() && !doc.isProxy();
    }

}
