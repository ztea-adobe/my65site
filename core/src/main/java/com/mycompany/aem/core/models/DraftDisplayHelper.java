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
 *  See the License for the specific language governing permissions and the
 *  limitations under the License.
 */
package com.mycompany.aem.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

/**
 * HTL Use-class for draft display templates. Exposes Java-backed getters for use in
 * {@code template.html} as {@code ${draftDisplay.myPropertyGetter}}.
 * <p>
 * When the current resource is a draft metadata node, properties are read from it;
 * otherwise defaults or empty values are returned.
 */
@Model(adaptables = Resource.class)
public class DraftDisplayHelper {

    private static final String CUSTOM_PROPERTY_NAME = "myCustomPropertyName";
    private static final String CUSTOM_DRAFT_NAME_GATHERED = "myCustomDraftNameGathered";

    @SlingObject
    private Resource resource;

    /**
     * Value exposed in HTL as ${draftDisplay.myPropertyGetter}.
     * Reads from draft node property "myCustomPropertyName", or returns a default.
     */
    public String getMyPropertyGetter() {
        if (resource == null) {
            return "";
        }
        ValueMap props = resource.getValueMap();
        String value = props.get(CUSTOM_PROPERTY_NAME, String.class);
        return value != null ? value : "";
    }

    /**
     * Value exposed in HTL as ${draftDisplay.myCustomDraftNameGathered}.
     * Reads from draft node property "myCustomDraftNameGathered" (form field value).
     */
    public String getMyCustomDraftNameGathered() {
        if (resource == null) {
            return "";
        }
        ValueMap props = resource.getValueMap();
        String value = props.get(CUSTOM_DRAFT_NAME_GATHERED, String.class);
        return value != null ? value : "";
    }
}
