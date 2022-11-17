package org.nuxeo.sample;

import java.util.Map;
import java.util.Map.Entry;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.blob.BlobInfo;
import org.nuxeo.ecm.core.blob.BlobManager;
import org.nuxeo.ecm.core.blob.BlobProvider;
import org.nuxeo.ecm.core.blob.SimpleManagedBlob;
import org.nuxeo.ecm.platform.mimetype.MimetypeNotFoundException;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;

@Operation(id=SampleOperation.ID, category=Constants.CAT_DOCUMENT, label="SampleOperation", description="sample operation")
public class SampleOperation {
    public static final String ID = "Document.SampleOperation";

    @Context
    protected CoreSession session;

    @OperationMethod
    public DocumentModel run(DocumentModel input){
        input.setPropertyValue("dc:title", "title");

        Long length = Long.parseLong("10485760");
   			Blob b = createBlobFromDigest("edd29f1b7b353e913dfc73f049008a48","file",length);
        input.setPropertyValue("file:content", (SimpleManagedBlob) b);

        input = session.saveDocument(input);
        return input;
    }




    protected Blob createBlobFromDigest(String digest, String filename, Long length) {
        // Get the BlobProvider id
        BlobManager blobManager = Framework.getLocalService(BlobManager.class);
        Map<String, BlobProvider> mapBlob = blobManager.getBlobProviders();
        String blobProviderId = null;
        for (Entry<String, BlobProvider> entry : mapBlob.entrySet()) {
            blobProviderId = entry.getKey();
            break;
        }

        // Create BlobInfo
        BlobInfo info = new BlobInfo();
        info.key = blobProviderId + ":" + digest;
        info.digest = digest;
        info.filename = filename;
        info.length = length;
        try {
            info.mimeType = Framework.getService(MimetypeRegistry.class).getMimetypeFromFilename(filename);
        } catch (MimetypeNotFoundException e) {
//            log.warn("Mimetype not found for file " + filename);
        }

        return new SimpleManagedBlob(info);
    }


}
