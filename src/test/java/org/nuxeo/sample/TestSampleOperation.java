package org.nuxeo.sample;

import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import java.util.HashMap;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.sample.sample-operation")
@Deploy("org.nuxeo.sample.sample-operation:test-user-directories-contrib.xml")
public class TestSampleOperation {
    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Before
    public void setUp() {
        DocumentModel ws1 = session.createDocumentModel("/default-domain/workspaces", "ws1", "Workspace");
        ws1 = session.createDocument(ws1);
        
        // give 'jdoe' Everything access to the workspace and its contents
        ACE ace = ACE.builder("jdoe", SecurityConstants.EVERYTHING).build();
        ACP acp = ws1.getACP();
        acp.addACE(ACL.LOCAL_ACL, ace);
        ws1.setACP(acp, true);

        DocumentModel fil1 = session.createDocumentModel("/default-domain/workspaces/ws1", "fil1", "File");
        fil1 = session.createDocument(fil1);

        session.save();
    }

    @Test
    public void shouldCallOperation() throws OperationException {
        // open session as 'jdoe' so the op runs as that user
        CoreSession session = CoreInstance.getCoreSession(this.session.getRepositoryName(), "jdoe");
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        DocumentModel input = session.getDocument(new PathRef("/default-domain/workspaces/ws1/fil1"));
        ctx.setInput(input);
        DocumentModel doc = (DocumentModel) automationService.run(ctx, SampleOperation.ID, params);
    }
}
