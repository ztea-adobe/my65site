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
package com.mycompany.aem.core.services.impl;

import com.mycompany.aem.core.services.DraftEnrichmentService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
class DraftEnrichmentServiceImplTest {

    private final AemContext context = new AemContext();
    private DraftEnrichmentService draftEnrichmentService;

    @BeforeEach
    void setUp() {
        // Register the service
        draftEnrichmentService = context.registerInjectActivateService(new DraftEnrichmentServiceImpl());
    }

    @Test
    void testIsDraftNode_ValidDraft() {
        // Create a mock draft resource
        // Note: AEM Forms uses 'nodeType' property, not 'jcr:primaryType'
        Resource draftResource = context.create().resource(
            "/content/forms/fp/admin/drafts/metadata/TEST123_af",
            "jcr:primaryType", "nt:unstructured",
            "nodeType", "fp:Draft",
            "sling:resourceType", "fd/fp/components/guidereload",
            "owner", "testuser"
        );

        // Test
        boolean result = draftEnrichmentService.isDraftNode(draftResource);
        
        // Verify
        assertTrue(result, "Should identify valid draft node");
    }

    @Test
    void testIsDraftNode_InvalidPath() {
        // Create a resource not under drafts path
        Resource resource = context.create().resource(
            "/content/test",
            "nodeType", "fp:Draft"
        );

        // Test
        boolean result = draftEnrichmentService.isDraftNode(resource);
        
        // Verify
        assertFalse(result, "Should not identify resource outside drafts path as draft");
    }

    @Test
    void testIsDraftNode_WrongNodeType() {
        // Create a resource with wrong node type
        Resource resource = context.create().resource(
            "/content/forms/fp/admin/drafts/metadata/TEST123_af",
            "jcr:primaryType", "nt:unstructured",
            "nodeType", "nt:unstructured",
            "sling:resourceType", "fd/fp/components/guidereload"
        );

        // Test
        boolean result = draftEnrichmentService.isDraftNode(resource);
        
        // Verify
        assertFalse(result, "Should not identify resource with wrong node type as draft");
    }

    @Test
    void testIsDraftNode_NullResource() {
        // Test with null
        boolean result = draftEnrichmentService.isDraftNode(null);
        
        // Verify
        assertFalse(result, "Should handle null resource gracefully");
    }

    @Test
    void testEnrichDraft_Success() {
        // Create a mock draft resource
        Resource draftResource = context.create().resource(
            "/content/forms/fp/admin/drafts/metadata/TEST123_af",
            "jcr:primaryType", "nt:unstructured",
            "nodeType", "fp:Draft",
            "sling:resourceType", "fd/fp/components/guidereload",
            "owner", "testuser"
        );

        // Test
        boolean result = draftEnrichmentService.enrichDraft(draftResource);
        
        // Verify
        assertTrue(result, "Should successfully enrich draft");
        
        // Check if property was added
        ValueMap properties = draftResource.getValueMap();
        assertEquals("my cust property value", properties.get("myCustomPropertyName", String.class), 
            "Should add myCustomPropertyName property with value 'my cust property value'");
    }

    @Test
    void testEnrichDraft_AlreadyEnriched() {
        // Create a mock draft resource with property already set
        Resource draftResource = context.create().resource(
            "/content/forms/fp/admin/drafts/metadata/TEST123_af",
            "jcr:primaryType", "nt:unstructured",
            "nodeType", "fp:Draft",
            "sling:resourceType", "fd/fp/components/guidereload",
            "owner", "testuser",
            "myCustomPropertyName", "my cust property value"
        );

        // Test
        boolean result = draftEnrichmentService.enrichDraft(draftResource);
        
        // Verify
        assertTrue(result, "Should handle already enriched draft");
        
        // Property should still exist with same value
        ValueMap properties = draftResource.getValueMap();
        assertEquals("my cust property value", properties.get("myCustomPropertyName", String.class));
    }

    @Test
    void testEnrichDraft_NullResource() {
        // Test with null
        boolean result = draftEnrichmentService.enrichDraft(null);
        
        // Verify
        assertFalse(result, "Should handle null resource gracefully");
    }
}
