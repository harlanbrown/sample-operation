package org.nuxeo.sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
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
    public Blob run() throws IOException {

        AuditReader reader = Framework.getService(AuditReader.class);
        AuditQueryBuilder builder = new AuditQueryBuilder();        
        builder.predicate(Predicates.eq("repositoryId", "default"))
            .and(Predicates.eq("eventId", "download"));
//            .and(Predicates.eq("extended.blobXPath", "file:content"))
//            .and(Predicates.noteq("extended.downloadReason", "preview"));

        List<LogEntry> logEntriesFiltered = reader.queryLogs(builder);

        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("id", "type", "date", "comment").withRecordSeparator('\n');

        File outputFile = Framework.createTempFile("report",".csv");

        if (outputFile != null) {
            Writer writer = new BufferedWriter(new FileWriter(outputFile));
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            logEntriesFiltered.forEach(entry -> { 
                try {
                    csvPrinter.printRecord(entry.getDocUUID(), entry.getDocType(), entry.getEventDate().toString(), entry.getComment()); 
                } catch (IOException e) {
                }
            });

            csvPrinter.flush();
            csvPrinter.close();
        } 
        Blob result = Blobs.createBlob(new FileInputStream(outputFile));        
        result.setFilename("report.csv");        
        Files.delete(outputFile.toPath());

        return result;
    }
}
