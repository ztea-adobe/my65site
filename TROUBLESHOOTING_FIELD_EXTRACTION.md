# Troubleshooting: myCustomDraftNameGathered Not Appearing

## Current Status

✅ `myCustomPropertyName = "my cust property value"` is working  
❌ `myCustomDraftNameGathered` is NOT appearing  

This means the service is running, but the form field extraction isn't finding the field value.

## Most Likely Causes

### 1. Field Not Named Correctly in Form ⭐ MOST COMMON

Your adaptive form needs a field with the **exact** name: `myCustomDraftName`

**How to Check:**
1. Open your form in edit mode
2. Select the text field component
3. Go to Properties
4. Check the **"Name"** or **"Bind Reference"** field
5. It MUST say exactly: `myCustomDraftName` (case-sensitive!)

**Common Mistakes:**
- ❌ `MyCustomDraftName` (wrong capitalization)
- ❌ `my_custom_draft_name` (underscores instead of camelCase)
- ❌ `customDraftName` (missing "my" prefix)
- ✅ `myCustomDraftName` (CORRECT!)

### 2. Field Has No Value

If the field is empty when you click Save, the property won't be added.

**Solution:** Make sure to TYPE something in the field before clicking Save.

### 3. Field Not in Form Data Model

The field might not be bound to the data model or might be in an unbound panel.

**How to Check:**
1. In CRXDE, go to: `/content/forms/fp/admin/drafts/data/[YOUR_DATA_ID]`
2. Look for a `data` property
3. If it contains XML, check if `myCustomDraftName` appears in it

### 4. Data Structure Is Different

AEM Forms might be storing data in a different format than expected.

## Step-by-Step Diagnostic

### Step 1: Verify Form Field Exists

1. Open your adaptive form: `http://localhost:4502/editor.html/content/forms/af/[your-form]`
2. Add or verify you have a **Text Field** component
3. Configure it:
   - **Name/Bind Reference:** `myCustomDraftName`
   - **Title/Label:** "Draft Name" (or whatever you want)
   - Save the form

### Step 2: Fill and Save

1. Open the form in preview: `http://localhost:4502/content/forms/af/[your-form].html`
2. Type something in the `myCustomDraftName` field: **"TEST VALUE 123"**
3. Click **Save** button
4. Note the draft ID that appears (or check CRXDE)

### Step 3: Check Draft Metadata

1. Open CRXDE: `http://localhost:4502/crx/de/index.jsp`
2. Navigate to: `/content/forms/fp/admin/drafts/metadata/`
3. Find your newest draft (ends with `_af`)
4. Check for properties:
   - ✅ `myCustomPropertyName` should = `"my cust property value"`
   - ❓ `myCustomDraftNameGathered` should = `"TEST VALUE 123"`

### Step 4: Check Draft Data

1. In your draft metadata node, find the `userdataID` property
2. Copy its value (e.g., `/content/forms/fp/admin/drafts/data/ABC123`)
3. Navigate to that path in CRXDE
4. Look for child nodes:
   - `jcr:content` ?
   - `data` property?
5. Try to see the XML content

### Step 5: Check Logs

Run this in terminal (adjust path to your AEM installation):

```bash
tail -f [AEM_DIR]/crx-quickstart/logs/error.log | grep -i DraftEnrichment
```

Then trigger enrichment by:
1. Editing the draft (add any property to the metadata node)
2. Or saving the form again

Look for log messages:
- ✅ `"Added custom property myCustomPropertyName=baked"`
- ❌ `"Field 'myCustomDraftName' not found in XML data"`
- ❌ `"No userdataID property found"`
- ❌ `"Data resource not found"`

## Quick Fix: Use Your Own Field Name

If you want to extract a field that already exists in your form, you can change the constants:

### Edit the Service

1. Open: `core/src/main/java/com/mycompany/aem/core/services/impl/DraftEnrichmentServiceImpl.java`

2. Find these lines (around line 52):
```java
private static final String FORM_FIELD_NAME = "myCustomDraftName";
private static final String METADATA_PROPERTY_NAME = "myCustomDraftNameGathered";
```

3. Change to match YOUR form field:
```java
// Example: If your form has a field named "emailAddress"
private static final String FORM_FIELD_NAME = "emailAddress";
private static final String METADATA_PROPERTY_NAME = "userEmailGathered";
```

4. Rebuild and deploy:
```bash
cd /Users/ztea/Documents/AdobeProjects/forms/my65site/core
mvn clean install -PautoInstallBundle
```

5. Test again

## Manual Test: Inspect Form Data XML

### Get the Raw XML

1. Use curl to get draft data:
```bash
curl -u admin:admin "http://localhost:4502/content/forms/fp/admin/drafts/data/[DATA_ID].json"
```

2. Look for the `data` property in the response

3. Check if your field name appears in the XML

### Example XML Structure

Your form data might look like:
```xml
<afData>
  <afUnboundData>
    <data/>
  </afUnboundData>
  <afBoundData>
    <data>
      <firstName>John</firstName>
      <email>john@example.com</email>
      <myCustomDraftName>TEST VALUE 123</myCustomDraftName>
    </data>
  </afBoundData>
  <afSubmissionInfo>...</afSubmissionInfo>
</afData>
```

If `myCustomDraftName` appears in this XML, the extraction should work.

## Common Scenarios

### Scenario A: Field Name Mismatch

**Problem:** You're looking for `myCustomDraftName` but form has `customName`

**Solution:** Change the constant in code to match your form field name

### Scenario B: Field in Wrong Panel

**Problem:** Field is in an unbound panel or fragment

**Solution:** 
- Ensure field is in a bound panel
- Or update XPath patterns in code to search in all locations

### Scenario C: Data Not in Expected Format

**Problem:** AEM storing data differently

**Solution:** Check the actual data structure and update the extraction logic

## Still Not Working?

### Collect Diagnostic Info

1. **Draft Metadata Node:**
   - Path: `/content/forms/fp/admin/drafts/metadata/[ID]_af`
   - Properties: List all properties
   - userdataID value

2. **Draft Data Node:**
   - Path: from userdataID property
   - Child nodes
   - data property content (if exists)

3. **Form Structure:**
   - Form path
   - Field names
   - Field configuration

4. **Logs:**
   - Any DraftEnrichment messages
   - Any errors or warnings

### Create Test Form

Create a simple test form to verify:

1. New form with just one field named `myCustomDraftName`
2. Fill it with "TEST123"
3. Save
4. Check if property appears

This isolates whether issue is with the service or your specific form structure.

## Expected Behavior When Working

When everything is configured correctly:

1. User fills form field `myCustomDraftName` with value "John's Draft"
2. User clicks Save
3. Draft created at `/content/forms/fp/admin/drafts/metadata/[ID]_af`
4. Properties on node:
   - `myCustomPropertyName = "my cust property value"` ✅
   - `myCustomDraftNameGathered = "John's Draft"` ✅
5. Logs show:
   ```
   INFO Added custom property myCustomPropertyName=baked to draft
   INFO Added form field value myCustomDraftNameGathered=John's Draft to draft
   INFO Successfully enriched draft
   ```

## Next Steps

1. ✅ Verify your form has a field named **exactly** `myCustomDraftName`
2. ✅ Fill that field with a test value before saving
3. ✅ Check CRXDE for the property after save
4. ✅ Check logs for any error messages
5. ❓ If still not working, provide:
   - Form field name you're using
   - Draft data structure
   - Any log messages

---

**Most likely: Your form doesn't have a field named `myCustomDraftName`. Add it or change the constant in the code to match an existing field!**
