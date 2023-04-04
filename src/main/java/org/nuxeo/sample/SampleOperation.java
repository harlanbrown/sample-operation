package org.nuxeo.sample;

import java.util.List;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.query.sql.model.Predicates;
import org.nuxeo.ecm.platform.audit.api.AuditQueryBuilder;
import org.nuxeo.ecm.platform.audit.api.AuditReader;
import org.nuxeo.ecm.platform.audit.api.LogEntry;
import org.nuxeo.runtime.api.Framework;

@Operation(id=SampleOperation.ID, category=Constants.CAT_DOCUMENT, label="SampleOperation", description="sample operation")
public class SampleOperation {
    public static final String ID = "Document.SampleOperation";

    @Context
    protected CoreSession session;

    @OperationMethod
    public DocumentModel run(DocumentModel input){

        AuditReader reader = Framework.getService(AuditReader.class);
        AuditQueryBuilder builder = new AuditQueryBuilder();        
        builder.predicate(Predicates.eq("repositoryId", "default"))
            .and(Predicates.eq("eventId", "download"))
            .and(Predicates.eq("extended.blobXPath", "file:content"))
            .and(Predicates.noteq("extended.downloadReason", "preview"));

//        List<LogEntry> logEntriesFiltered = reader.queryLogs(builder);

        return input;
    }
}
