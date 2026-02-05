# Quick Reference - Draft Enrichment (Updated)

## Properties Set on Draft Nodes

When a user clicks "Save" on an adaptive form, these properties are automatically added:

### 1. Static Property
- **Name:** `myCustomPropertyName`
- **Value:** `"my cust property value"`
- **Always set** on every draft save

### 2. Dynamic Property
- **Name:** `myCustomDraftNameGathered`
- **Value:** Extracted from form field named `myCustomDraftName`
- **Only set if** the field exists and has a value

### Location
```
/content/forms/fp/admin/drafts/metadata/{DRAFT_ID}_af
```

## Quick Test

### 1. Save a Draft
1. Open your adaptive form
2. Fill in data
3. Click **Save**

### 2. Verify Properties

```bash
# Check all drafts
curl -s -u admin:admin "http://localhost:4502/content/forms/fp/admin/drafts/metadata.1.json" \
  | python3 -m json.tool \
  | grep -E "(myCustomPropertyName|myCustomDraftNameGathered)"
```

**Expected:**
```json
"myCustomPropertyName": "my cust property value",
"myCustomDraftNameGathered": "[your value]",
```

### 3. Check Logs

```bash
tail -50 ~/Documents/AdobeProjects/forms/my65site/crx-quickstart/logs/error.log | grep -i "draft"
```

**Expected:**
```
INFO DraftSaveListener: Change detected
INFO DraftEnrichmentServiceImpl: Added custom property myCustomPropertyName=my cust property value
INFO DraftEnrichmentServiceImpl: Successfully enriched draft
```

## Build & Deploy

```bash
# Deploy core bundle
cd /Users/ztea/Documents/AdobeProjects/forms/my65site/core
mvn clean install -PautoInstallBundle

# Deploy everything
cd /Users/ztea/Documents/AdobeProjects/forms/my65site
mvn clean install -PautoInstallPackage

# Run tests
cd core && mvn test
```

## Verify Configuration

```bash
# Check listener is active
curl -s -u admin:admin "http://localhost:4502/system/console/components" | grep -A 3 "DraftSaveListener"
```

**Expected:** State = **active** ✅

## Components

| Component | Status | Purpose |
|-----------|--------|---------|
| DraftSaveListener | ✅ Active | Detects draft save events |
| DraftEnrichmentService | ✅ Active | Adds properties to drafts |
| DraftEnrichmentFilter | ❌ Removed | Incompatible with AEM Forms |

## Timing

Properties appear **50-200ms** after save (imperceptible to users).

## Configuration Files

- `DraftSaveListener.java` - Event listener
- `DraftEnrichmentServiceImpl.java` - Property enrichment logic
- `ServiceUserMapperImpl.amended~my65site-drafts.cfg.json` - Service user mapping
- `RepositoryInitializer~my65site.cfg.json` - Permissions

## Troubleshooting

### Properties not appearing?

**Check service user mapping:**
```bash
curl -s -u admin:admin "http://localhost:4502/system/console/configMgr" | grep -i "serviceusermapper"
```

**Check logs for errors:**
```bash
tail -100 ~/Documents/AdobeProjects/forms/my65site/crx-quickstart/logs/error.log | grep -i "error\|exception"
```

**Verify listener is active:**
```bash
open http://localhost:4502/system/console/components
# Search for: DraftSaveListener
```

## Summary

✅ Listener approach active  
✅ Properties: `myCustomPropertyName` = `"my cust property value"`  
✅ Dynamic extraction from `myCustomDraftName` field  
✅ Enrichment within 200ms of save  
❌ Filter approach removed (incompatible)

**All changes deployed and ready to test!**
