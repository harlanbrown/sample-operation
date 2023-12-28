package org.nuxeo.sample;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.blob.binary.Binary;
import org.nuxeo.ecm.core.blob.binary.BinaryBlob;
import org.nuxeo.runtime.api.Framework;

/*
Creates a document pointing to an existing blob in the configured binary manager
*/
@Operation(id = CreateDocumentWithExistingBlob.ID, category = Constants.CAT_DOCUMENT, label = "Create a document pointing to an existing blob in the configured binary manager", description = "")
public class CreateDocumentWithExistingBlob {
    public static final String ID = "CreateDocumentWithExistingBlob";

    @Context
    protected CoreSession session;

    @Param(name = "filename")
    protected String filename;

    @Param(name = "mimeType")
    protected String mimeType;

    @Param(name = "digest")
    protected String digest;

    @Param(name = "length")
    protected long length;

    @OperationMethod
    public DocumentModel run(DocumentModel doc) throws Exception {

        // Document's _name_ value cannot contain a slash
        DocumentModel newDoc = session.createDocumentModel(doc.getPathAsString(), filename.replace("/","_"), "File");
        newDoc = session.createDocument(newDoc);

        Binary b = new Binary(null, digest, "default");
        BinaryBlob sb = new BinaryBlob(b, digest, filename, mimeType, null, digest, length);
        newDoc.setPropertyValue("file:content", sb);

        // the dc:title _can_ contain a slash
        newDoc.setPropertyValue("dc:title", filename);

        return session.saveDocument(newDoc);
      }

}
