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
package com.mycompany.aem.core.services;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Service interface for enriching draft metadata with custom properties.
 */
public interface DraftEnrichmentService {

    /**
     * Enriches a draft node with custom properties.
     * 
     * @param draftResource The draft resource to enrich
     * @return true if enrichment was successful, false otherwise
     */
    boolean enrichDraft(Resource draftResource);

    /**
     * Checks if a resource is a draft metadata node.
     * 
     * @param resource The resource to check
     * @return true if the resource is a draft, false otherwise
     */
    boolean isDraftNode(Resource resource);

    /**
     * Returns the form field value (myCustomDraftName) for a draft by its draftID.
     * Finds the metadata node via draftID, then reads userdataID and extracts the field from draft data.
     *
     * @param resolver Resource resolver (e.g. service resolver) with read access to /content/forms
     * @param draftId  The draftID (e.g. "XDQIEU4KLIRQUAB4DLM2ZIO2YQ_af")
     * @return The field value, or null if not found
     */
    String getFormFieldValueForDraftId(ResourceResolver resolver, String draftId);
}
