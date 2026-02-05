# ✅ Draft Enrichment Service - Complete Feature Summary

## What You Have Now

Your AEM Forms draft enrichment service now does **two things** automatically when users save forms:

### 1. Static Property ✅
- Adds: `myCustomPropertyName = "my cust property value"`
- Purpose: Custom marker/flag on all drafts

### 2. Form Field Extraction ✅ NEW!
- Extracts: Value from form field named `myCustomDraftName`
- Adds: `myCustomDraftNameGathered = [the value from the form]`
- Purpose: Capture specific form data on the metadata node

## Quick Example

### Your Form Has:
```
┌─────────────────────────────────────┐
│ Adaptive Form                       │
│                                     │
│ myCustomDraftName: [Important Form] │ ← User fills this
│ Email: [john@example.com]          │
│                                     │
│ [Submit] [Save] [Clear]             │
└─────────────────────────────────────┘
```

### After User Clicks Save:

**Draft Metadata Node Gets:**
```json
{
  "name": "Form Sample 1",
  "owner": "admin",
  "myCustomPropertyName": "my cust property value",                          ← Always added
  "myCustomDraftNameGathered": "Important Form" ← Extracted from form!
}
```

## How to Use

### Step 1: Add Field to Your Form

In your adaptive form, add a field named **exactly** `myCustomDraftName`:
- Component: Text Field
- Name/Bind Reference: `myCustomDraftName`
- Label: Whatever you want (e.g., "Draft Name")

### Step 2: User Fills and Saves

User types in the field and clicks **Save**.

### Step 3: Check Results

Open CRXDE: `http://localhost:4502/crx/de/index.jsp`

Navigate to: `/content/forms/fp/admin/drafts/metadata/[DRAFT_ID]_af`

You'll see:
- ✅ `myCustomPropertyName = "my cust property value"`
- ✅ `myCustomDraftNameGathered = [whatever user typed]`

## Customization

### Change the Field Name

Edit these constants in `DraftEnrichmentServiceImpl.java`:

```java
private static final String FORM_FIELD_NAME = "myCustomDraftName";  // ← Your form field
private static final String METADATA_PROPERTY_NAME = "myCustomDraftNameGathered";  // ← Property name
```

### Examples

**Extract email:**
```java
private static final String FORM_FIELD_NAME = "emailAddress";
private static final String METADATA_PROPERTY_NAME = "userEmail";
```

**Extract application ID:**
```java
private static final String FORM_FIELD_NAME = "applicationNumber";
private static final String METADATA_PROPERTY_NAME = "appNumber";
```

Then rebuild and deploy:
```bash
cd /Users/ztea/Documents/AdobeProjects/forms/my65site/core
mvn clean install -PautoInstallBundle
```

## File Structure

```
Draft in AEM
│
├─ Metadata Node: /content/forms/fp/admin/drafts/metadata/ABC123_af
│  ├─ myCustomPropertyName = "my cust property value"
│  ├─ myCustomDraftNameGathered = "Important Form"
│  └─ userdataID = "/content/forms/fp/admin/drafts/data/XYZ789"
│
└─ Data Node: /content/forms/fp/admin/drafts/data/XYZ789
   └─ data (XML) contains all form field values:
      <myCustomDraftName>Important Form</myCustomDraftName>
      <emailAddress>john@example.com</emailAddress>
      ...
```

## Technical Features

✅ **Automatic extraction** - No manual coding needed per form  
✅ **XML parsing** - Handles AEM Forms data structure  
✅ **Multiple XPath patterns** - Finds field in various XML formats  
✅ **Security** - Protected against XXE attacks  
✅ **Error handling** - Non-blocking, logs issues  
✅ **Performance** - Parsed only once on save  

## Testing Checklist

- [ ] Deploy updated bundle ✅ (Done at 3:59 PM)
- [ ] Add `myCustomDraftName` field to your form
- [ ] Fill the field with test value (e.g., "Test Draft 001")
- [ ] Click **Save** button
- [ ] Open CRXDE and navigate to draft metadata
- [ ] Verify `myCustomPropertyName = "my cust property value"` exists
- [ ] Verify `myCustomDraftNameGathered = "Test Draft 001"` exists
- [ ] Test with different values
- [ ] Test with empty field (property won't be added)

## Common Scenarios

### Scenario 1: Track Draft Purpose
**Form field:** `draftPurpose`  
**Use case:** User specifies why they're saving (e.g., "Review later", "Incomplete data")  
**Result:** `draftPurposeMetadata = "Review later"`

### Scenario 2: Custom Draft Name
**Form field:** `myCustomDraftName`  
**Use case:** User gives draft a friendly name  
**Result:** `myCustomDraftNameGathered = "John's Application v2"`

### Scenario 3: Application Reference
**Form field:** `referenceNumber`  
**Use case:** Auto-generated or user-entered reference  
**Result:** `refNumber = "APP-2026-001"`

## What If Field Is Empty?

If the form field is empty or not found:
- ✅ `myCustomPropertyName = "my cust property value"` will still be added
- ❌ `myCustomDraftNameGathered` won't be added (property skipped)
- 📝 Debug log: "Field 'myCustomDraftName' not found or empty"

This is by design - we only add the property if there's a value.

## Troubleshooting Quick Reference

| Issue | Solution |
|-------|----------|
| Property not appearing | Check field name is exactly `myCustomDraftName` |
| Empty/null value | Field was empty when saved |
| Wrong value | Check for duplicate field names in form |
| Service not running | Restart bundle in OSGi console |

## Documentation Files

- 📘 **FORM_FIELD_EXTRACTION.md** - Complete technical documentation (this feature)
- 📘 **DRAFT_ENRICHMENT_README.md** - Overall service documentation
- 📘 **FIX_APPLIED.md** - Recent bug fixes
- 📘 **QUICK_START_GUIDE.md** - Quick deployment guide
- 📘 **CONFIGURATION_DEPLOYED.md** - Deployment verification

## Summary

**Status:** ✅ DEPLOYED AND READY

**What happens when user saves form:**
1. AEM Forms creates draft
2. Listener detects it
3. Service adds `myCustomPropertyName = "my cust property value"`
4. Service extracts `myCustomDraftName` from form data
5. Service adds `myCustomDraftNameGathered = [extracted value]`
6. Done! Both properties visible in CRXDE

**Next action for you:**
Add a field named `myCustomDraftName` to your adaptive form, then test!

---

**Need help?** Check the documentation files above or review the logs:
```bash
tail -f crx-quickstart/logs/error.log | grep DraftEnrichment
```
