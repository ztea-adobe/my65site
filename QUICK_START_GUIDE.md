# Quick Start Guide - Draft Custom Property

## What This Does

Automatically adds `myPotato="baked"` to draft nodes when users save adaptive forms.

## Quick Deploy

```bash
# From project root
cd /Users/ztea/Documents/AdobeProjects/forms/my65site

# Build and deploy everything
mvn clean install -PautoInstallPackage
```

## Quick Test

1. Open adaptive form: `http://localhost:4502/content/forms/af/[your-form].html`
2. Fill some fields and click **Save**
3. Check CRXDE: `http://localhost:4502/crx/de/index.jsp`
4. Navigate to: `/content/forms/fp/admin/drafts/metadata/[DRAFT_ID]_af`
5. Verify property: `myPotato = "baked"`

## Quick Verify

```bash
# Check OSGi components are active
open http://localhost:4502/system/console/components

# Search for:
# - DraftEnrichmentServiceImpl (should be Active)
# - DraftSaveListener (should be Active)
```

## Quick Logs

```bash
# Watch for draft enrichment logs
tail -f crx-quickstart/logs/error.log | grep -i "enriched draft"
```

## Files Created

### Java Services
- `core/src/main/java/com/mycompany/aem/core/services/DraftEnrichmentService.java`
- `core/src/main/java/com/mycompany/aem/core/services/impl/DraftEnrichmentServiceImpl.java`
- `core/src/main/java/com/mycompany/aem/core/listeners/DraftSaveListener.java`

### OSGi Configuration
- `ui.config/.../org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended~my65site-drafts.cfg.json`
- Updated: `ui.config/.../org.apache.sling.jcr.repoinit.RepositoryInitializer~my65site.cfg.json`

### Tests
- `core/src/test/java/com/mycompany/aem/core/services/impl/DraftEnrichmentServiceImplTest.java`
- `core/src/test/java/com/mycompany/aem/core/listeners/DraftSaveListenerTest.java`

### Documentation
- `DRAFT_ENRICHMENT_README.md` (detailed documentation)
- `QUICK_START_GUIDE.md` (this file)

## Troubleshooting

**Property not added?**
```bash
# Restart the bundle
# Go to: http://localhost:4502/system/console/bundles
# Find "My AEM 6.5.8 Site - Core" and click Restart
```

**Permission errors?**
```bash
# Check service user
# Go to: http://localhost:4502/useradmin
# Search: draftEnrichmentService
# Should exist with permissions on /content/forms/fp/admin/drafts
```

## Next Steps

- See `DRAFT_ENRICHMENT_README.md` for detailed documentation
- Customize property name/value in `DraftEnrichmentServiceImpl.java`
- Add additional properties as needed
- Monitor logs for successful enrichment messages
