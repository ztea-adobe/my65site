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
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Implementation of DraftEnrichmentService that adds custom properties to draft metadata.
 */
@Component(
    service = DraftEnrichmentService.class,
    immediate = true
)
public class DraftEnrichmentServiceImpl implements DraftEnrichmentService {

    private static final Logger LOG = LoggerFactory.getLogger(DraftEnrichmentServiceImpl.class);
    
    // Constants for draft identification
    private static final String DRAFT_NODE_TYPE = "fp:Draft";
    private static final String DRAFT_RESOURCE_TYPE = "fd/fp/components/guidereload";
    private static final String DRAFT_PATH_PREFIX = "/content/forms/fp/admin/drafts/metadata";
    
    // Custom property constants
    private static final String CUSTOM_PROPERTY_NAME = "myPotato";
    private static final String CUSTOM_PROPERTY_VALUE = "baked";
    
    // Form field extraction constants
    private static final String FORM_FIELD_NAME = "myCustomDraftName";
    private static final String METADATA_PROPERTY_NAME = "myCustomDraftNameGathered";
    private static final String USER_DATA_ID_PROPERTY = "userdataID";
    private static final String DATA_PROPERTY = "data";

    @Override
    public boolean enrichDraft(Resource draftResource) {
        if (draftResource == null) {
            LOG.warn("Draft resource is null, cannot enrich");
            return false;
        }

        try {
            ResourceResolver resolver = draftResource.getResourceResolver();
            
            // Check if we have write access
            if (resolver == null) {
                LOG.error("ResourceResolver is null for resource: {}", draftResource.getPath());
                return false;
            }

            ModifiableValueMap properties = draftResource.adaptTo(ModifiableValueMap.class);
            
            if (properties == null) {
                LOG.error("Cannot adapt resource to ModifiableValueMap: {}", draftResource.getPath());
                return false;
            }

            boolean modified = false;

            // Add the static custom property if it doesn't exist
            if (!properties.containsKey(CUSTOM_PROPERTY_NAME)) {
                properties.put(CUSTOM_PROPERTY_NAME, CUSTOM_PROPERTY_VALUE);
                LOG.info("Added custom property {}={} to draft: {}", 
                    CUSTOM_PROPERTY_NAME, CUSTOM_PROPERTY_VALUE, draftResource.getPath());
                modified = true;
            }

            // Extract form field value and add as metadata property
            String formFieldValue = extractFormFieldValue(draftResource, resolver);
            if (formFieldValue != null && !formFieldValue.isEmpty()) {
                if (!properties.containsKey(METADATA_PROPERTY_NAME) || 
                    !formFieldValue.equals(properties.get(METADATA_PROPERTY_NAME, String.class))) {
                    properties.put(METADATA_PROPERTY_NAME, formFieldValue);
                    LOG.info("Added form field value {}={} to draft: {}", 
                        METADATA_PROPERTY_NAME, formFieldValue, draftResource.getPath());
                    modified = true;
                }
            } else {
                LOG.debug("Form field '{}' not found or empty in draft data for: {}", 
                    FORM_FIELD_NAME, draftResource.getPath());
            }
            
            // Persist changes if modifications were made
            if (modified) {
                resolver.commit();
                LOG.info("Successfully enriched draft: {}", draftResource.getPath());
            }
            
            return true;

        } catch (PersistenceException e) {
            LOG.error("Failed to persist custom property to draft: {}", draftResource.getPath(), e);
            return false;
        } catch (Exception e) {
            LOG.error("Unexpected error while enriching draft: {}", draftResource.getPath(), e);
            return false;
        }
    }

    @Override
    public boolean isDraftNode(Resource resource) {
        if (resource == null) {
            return false;
        }

        // Check if path is under drafts metadata
        String path = resource.getPath();
        if (!path.startsWith(DRAFT_PATH_PREFIX)) {
            return false;
        }

        // Get resource properties
        ValueMap properties = resource.getValueMap();
        
        // Check for draft-specific properties
        // Note: AEM Forms stores draft type in 'nodeType' property, not 'jcr:primaryType'
        String nodeType = properties.get("nodeType", String.class);
        String resourceType = properties.get("sling:resourceType", String.class);

        // Verify this is a draft node
        boolean isDraft = DRAFT_NODE_TYPE.equals(nodeType) && 
                         DRAFT_RESOURCE_TYPE.equals(resourceType);

        if (isDraft) {
            LOG.debug("Identified draft node at: {}", path);
        }

        return isDraft;
    }

    /**
     * Extracts a form field value from the draft data.
     * 
     * @param draftResource The draft metadata resource
     * @param resolver The resource resolver
     * @return The form field value, or null if not found
     */
    private String extractFormFieldValue(Resource draftResource, ResourceResolver resolver) {
        try {
            ValueMap draftProps = draftResource.getValueMap();
            
            // Get the data node path from userdataID property
            String dataNodePath = draftProps.get(USER_DATA_ID_PROPERTY, String.class);
            if (dataNodePath == null || dataNodePath.isEmpty()) {
                LOG.debug("No userdataID property found on draft: {}", draftResource.getPath());
                return null;
            }

            // Get the data resource
            Resource dataResource = resolver.getResource(dataNodePath);
            if (dataResource == null) {
                LOG.warn("Data resource not found at path: {}", dataNodePath);
                return null;
            }

            // Get properties from data resource
            ValueMap dataProps = dataResource.getValueMap();
            
            // Try to get data from the 'data' property (XML string format)
            String xmlData = dataProps.get(DATA_PROPERTY, String.class);
            if (xmlData != null && !xmlData.isEmpty()) {
                LOG.debug("Found XML data in 'data' property for draft: {}", draftResource.getPath());
                return extractFieldFromXml(xmlData, FORM_FIELD_NAME);
            }

            // Try to get data from 'jcr:data' directly on the data resource (binary format)
            Object jcrDataDirect = dataProps.get("jcr:data");
            if (jcrDataDirect instanceof InputStream) {
                LOG.debug("Found binary data in 'jcr:data' property for draft: {}", draftResource.getPath());
                return extractFieldFromInputStream((InputStream) jcrDataDirect, FORM_FIELD_NAME);
            }

            // Try to get data from 'jcr:content/jcr:data' (child node with binary format)
            Resource jcrContent = dataResource.getChild("jcr:content");
            if (jcrContent != null) {
                ValueMap jcrProps = jcrContent.getValueMap();
                Object jcrData = jcrProps.get("jcr:data");
                
                if (jcrData instanceof InputStream) {
                    LOG.debug("Found binary data in 'jcr:content/jcr:data' for draft: {}", draftResource.getPath());
                    return extractFieldFromInputStream((InputStream) jcrData, FORM_FIELD_NAME);
                }
            }

            LOG.debug("Could not find form data in expected locations for draft: {}. Checked: data property, jcr:data property, jcr:content/jcr:data", 
                draftResource.getPath());
            return null;

        } catch (Exception e) {
            LOG.error("Error extracting form field value from draft: {}", draftResource.getPath(), e);
            return null;
        }
    }

    /**
     * Extracts a field value from XML data using XPath.
     * 
     * @param xmlData The XML data as string
     * @param fieldName The field name to extract
     * @return The field value, or null if not found
     */
    private String extractFieldFromXml(String xmlData, String fieldName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));

            XPath xpath = XPathFactory.newInstance().newXPath();
            
            // Try multiple XPath patterns to find the field
            String[] xpathPatterns = {
                "//" + fieldName,
                "//*[local-name()='" + fieldName + "']",
                "//afData/afBoundData/data//" + fieldName,
                "//data//" + fieldName
            };

            for (String pattern : xpathPatterns) {
                NodeList nodes = (NodeList) xpath.evaluate(pattern, doc, XPathConstants.NODESET);
                if (nodes != null && nodes.getLength() > 0) {
                    String value = nodes.item(0).getTextContent();
                    if (value != null && !value.trim().isEmpty()) {
                        LOG.debug("Found field '{}' with value '{}' using pattern: {}", 
                            fieldName, value, pattern);
                        return value.trim();
                    }
                }
            }

            LOG.debug("Field '{}' not found in XML data", fieldName);
            return null;

        } catch (Exception e) {
            LOG.error("Error parsing XML data to extract field '{}': {}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts a field value from an InputStream containing XML data.
     * 
     * @param inputStream The input stream
     * @param fieldName The field name to extract
     * @return The field value, or null if not found
     */
    private String extractFieldFromInputStream(InputStream inputStream, String fieldName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            XPath xpath = XPathFactory.newInstance().newXPath();
            
            // Try multiple XPath patterns
            String[] xpathPatterns = {
                "//" + fieldName,
                "//*[local-name()='" + fieldName + "']",
                "//afData/afBoundData/data//" + fieldName,
                "//data//" + fieldName
            };

            for (String pattern : xpathPatterns) {
                NodeList nodes = (NodeList) xpath.evaluate(pattern, doc, XPathConstants.NODESET);
                if (nodes != null && nodes.getLength() > 0) {
                    String value = nodes.item(0).getTextContent();
                    if (value != null && !value.trim().isEmpty()) {
                        return value.trim();
                    }
                }
            }

            return null;

        } catch (Exception e) {
            LOG.error("Error parsing InputStream to extract field '{}': {}", fieldName, e.getMessage());
            return null;
        }
    }
}
