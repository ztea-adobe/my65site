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
}
