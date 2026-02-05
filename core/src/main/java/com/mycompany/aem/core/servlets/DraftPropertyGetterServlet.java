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
package com.mycompany.aem.core.servlets;

import com.mycompany.aem.core.services.DraftEnrichmentService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet that returns myCustomDraftName form field values for multiple draftIDs in one call.
 * Request: GET /bin/my65site/draft-property?draftIDs=id1,id2,id3
 * Response: application/json with {"id1": "value1", "id2": "value2", ...}
 */
@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/my65site/draft-property",
        "sling.servlet.methods=GET"
    }
)
@ServiceDescription("Draft property getter – returns form field values for multiple draftIDs (batch)")
public class DraftPropertyGetterServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final String PARAM_DRAFT_IDS = "draftIDs";

    @Reference
    private DraftEnrichmentService draftEnrichmentService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String draftIdsParam = request.getParameter(PARAM_DRAFT_IDS);
        List<String> draftIds = new ArrayList<>();
        if (draftIdsParam != null && !draftIdsParam.isEmpty()) {
            for (String id : draftIdsParam.split(",")) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) {
                    draftIds.add(trimmed);
                }
            }
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (String draftId : draftIds) {
            String value = draftEnrichmentService.getFormFieldValueForDraftId(request.getResourceResolver(), draftId);
            result.put(draftId, value != null ? value : "");
        }

        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : result.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            json.append("\"").append(escapeJson(e.getKey())).append("\":");
            json.append("\"").append(escapeJson(e.getValue())).append("\"");
        }
        json.append("}");
        response.getWriter().write(json.toString());
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
