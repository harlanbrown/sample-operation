package org.nuxeo.sample;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.query.sql.model.Predicates;
import org.nuxeo.ecm.platform.audit.api.AuditQueryBuilder;
import org.nuxeo.ecm.platform.audit.api.AuditReader;
import org.nuxeo.ecm.platform.audit.api.LogEntry;
import org.nuxeo.runtime.api.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

@Operation(id = RetrieveAuditLog.ID, category = "PVH", label = "RetrieveAuditLog", description = "RetrieveAuditLog")
public class RetrieveAuditLog {
    private static Logger LOG = LoggerFactory.getLogger(RetrieveAuditLog.class);

    public static final String ID = "PVH.RetrieveAuditLog";

    private static final String[] HEADERS = {"UID", "Doc Path", "Doc Type", "Date", "Name", "Type", "User"};

    @Context
    protected CoreSession session;

    @Param(name = "startTime")
    private String startTime;

    @Param(name = "endTime")
    private String endTime;

    @Param(name = "principalName", required = false)
    private String principalName;

    @OperationMethod
    public Blob run() throws IOException {
        AuditReader reader = Framework.getService(AuditReader.class);

        AuditQueryBuilder builder = new AuditQueryBuilder();
        builder.predicate(Predicates.eq("repositoryId", "default"))
                .and(Predicates.eq("eventId", "download"))
                .and(Predicates.gte("eventDate", startTime))
                .and(Predicates.lte("eventDate", endTime))
                .and(Predicates.noteq("extended.downloadReason", "preview"));

        if (!Strings.isNullOrEmpty(principalName)) {
            builder.and(Predicates.eq("principalName", principalName));
        }

        List<LogEntry> logEntriesFiltered = reader.queryLogs(builder);

        String exportedFileName = String.format("audit-log-%d.csv", new Date().getTime());
        File exportedFile = new File(exportedFileName);
        FileWriter out = new FileWriter(exportedFile);
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS))) {
            logEntriesFiltered.forEach(entry -> {
                String blobXPath = "";
                try {
                    blobXPath = entry.getExtendedInfos().get("blobXPath").getValue(String.class);
                } catch (Exception ignore) {}
                String fileType = "file:content".equals(blobXPath) ? "main blob" : "rendition";
                try {
                    if(!(String.valueOf(entry.getComment())).contains("no_image") &&
                            !(String.valueOf(entry.getComment())).contains("empty_picture")) {
                        printer.printRecord(
                                entry.getDocUUID(),
                                entry.getDocPath(),
                                entry.getDocType(),
                                entry.getEventDate().toString(),
                                entry.getComment(),
                                fileType,
                                entry.getPrincipalName());
                    }
                } catch (IOException e) {
                    LOG.error("Error when exporting download log", e);
                }
            });
            printer.flush();
            out.close();
        }

        Blob result = Blobs.createBlob(new FileInputStream(exportedFile));
        result.setFilename(exportedFileName);
        Files.delete(exportedFile.toPath());

        return result;
    }
}
