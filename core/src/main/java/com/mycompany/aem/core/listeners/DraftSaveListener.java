/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mycompany.aem.core.listeners;

import com.mycompany.aem.core.services.DraftEnrichmentService;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource change listener that monitors draft metadata nodes and enriches them
 * with custom properties when they are created or modified.
 */
@Component(
    service = ResourceChangeListener.class,
    immediate = true,
    property = {
        ResourceChangeListener.CHANGES + "=ADDED",
        ResourceChangeListener.CHANGES + "=CHANGED",
        ResourceChangeListener.PATHS + "=/content/forms/fp/admin/drafts/metadata"
    }
)
@ServiceDescription("Listener for draft save events to add custom properties")
public class DraftSaveListener implements ResourceChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(DraftSaveListener.class);

    @Reference
    private DraftEnrichmentService draftEnrichmentService;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public void onChange(List<ResourceChange> changes) {
        ResourceResolver resolver = null;
        
        try {
            // Get a service resource resolver
            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put(ResourceResolverFactory.SUBSERVICE, "draftEnrichmentService");
            resolver = resolverFactory.getServiceResourceResolver(authInfo);

            for (ResourceChange change : changes) {
                String path = change.getPath();
                LOG.debug("Draft resource change detected: {} at path: {}", 
                    change.getType(), path);

                // Get the resource
                Resource resource = resolver.getResource(path);
                
                if (resource == null) {
                    LOG.warn("Resource not found at path: {}", path);
                    continue;
                }

                // Check if this is a draft node
                if (draftEnrichmentService.isDraftNode(resource)) {
                    LOG.info("Processing draft save event for: {}", path);
                    
                    // Enrich the draft with custom properties
                    boolean success = draftEnrichmentService.enrichDraft(resource);
                    
                    if (success) {
                        LOG.info("Successfully enriched draft at: {}", path);
                    } else {
                        LOG.error("Failed to enrich draft at: {}", path);
                    }
                }
            }

        } catch (LoginException e) {
            LOG.error("Failed to get service resource resolver. Make sure the service user is configured.", e);
        } catch (Exception e) {
            LOG.error("Error processing draft change event", e);
        } finally {
            if (resolver != null && resolver.isLive()) {
                resolver.close();
            }
        }
    }
}
