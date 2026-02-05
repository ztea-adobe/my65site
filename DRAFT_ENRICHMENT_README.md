# Draft Enrichment Service - Custom Property Addition

## Overview

This solution automatically adds a custom property `myPotato` with value `baked` to draft metadata nodes whenever a user clicks the Save button on an adaptive form in AEM Forms.

## Architecture

The solution consists of three main components:

### 1. **DraftEnrichmentService** (Interface)
- Location: `core/src/main/java/com/mycompany/aem/core/services/DraftEnrichmentService.java`
- Defines the contract for enriching draft nodes with custom properties

### 2. **DraftEnrichmentServiceImpl** (Service Implementation)
- Location: `core/src/main/java/com/mycompany/aem/core/services/impl/DraftEnrichmentServiceImpl.java`
- Implements the business logic for adding custom properties to draft nodes
- Validates that resources are draft nodes before enrichment
- Handles errors gracefully with comprehensive logging

### 3. **DraftSaveListener** (Resource Change Listener)
- Location: `core/src/main/java/com/mycompany/aem/core/listeners/DraftSaveListener.java`
- Monitors changes to `/content/forms/fp/admin/drafts/metadata`
- Triggers enrichment when draft nodes are ADDED or CHANGED
- Uses a service user with appropriate permissions

## Configuration Files

### OSGi Configurations

1. **Service User Mapping**
   - File: `ui.config/src/main/content/jcr_root/apps/my65site/osgiconfig/config/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended~my65site-drafts.cfg.json`
   - Maps the bundle to the service user `draftEnrichmentService`

2. **Repository Initialization**
   - File: `ui.config/src/main/content/jcr_root/apps/my65site/osgiconfig/config/org.apache.sling.jcr.repoinit.RepositoryInitializer~my65site.cfg.json`
   - Creates the `draftEnrichmentService` system user
   - Grants read/write permissions on draft metadata paths

## Deployment Steps

### 1. Build the Project

```bash
cd /Users/ztea/Documents/AdobeProjects/forms/my65site
mvn clean install -PautoInstallPackage
```

### 2. Deploy to AEM Instance

The build command above will automatically deploy to your local AEM instance if configured properly.

Alternatively, deploy individual packages:

```bash
# Deploy core bundle
mvn clean install -PautoInstallBundle -f core/pom.xml

# Deploy ui.config
mvn clean install -PautoInstallPackage -f ui.config/pom.xml
```

### 3. Verify Service Registration

After deployment, verify the services are active:

1. Navigate to the OSGi Console: `http://localhost:4502/system/console/components`
2. Search for:
   - `DraftEnrichmentServiceImpl`
   - `DraftSaveListener`
3. Ensure both components show status: **Active**

### 4. Verify Service User

Check that the service user was created:

1. Navigate to: `http://localhost:4502/system/console/jmx/org.apache.sling.jcr.repoinit%3Atype%3DRepositoryInitializer`
2. Verify the repoinit scripts executed successfully
3. Check user exists at: `http://localhost:4502/useradmin` → Search for `draftEnrichmentService`

## Testing

### Manual Testing

1. **Open an Adaptive Form**
   - Navigate to your adaptive form in AEM Forms
   - Example: `http://localhost:4502/editor.html/content/forms/af/[your-form]`

2. **Fill and Save the Form**
   - Fill in some form fields
   - Click the **Save** button
   - This creates a draft node

3. **Verify the Custom Property**
   - Navigate to CRXDE Lite: `http://localhost:4502/crx/de/index.jsp`
   - Browse to: `/content/forms/fp/admin/drafts/metadata`
   - Find your draft node (pattern: `[DRAFT_ID]_af`)
   - Verify the property `myPotato` exists with value `baked`

### Automated Testing

Run unit tests:

```bash
cd /Users/ztea/Documents/AdobeProjects/forms/my65site/core
mvn clean test
```

The test suite includes:
- `DraftEnrichmentServiceImplTest`: Tests service logic for enriching drafts
- `DraftSaveListenerTest`: Tests listener configuration (structure only)

## How It Works

### Flow Diagram

```
User clicks Save on Adaptive Form
    ↓
AEM Forms creates/updates draft node at:
/content/forms/fp/admin/drafts/metadata/[DRAFT_ID]_af
    ↓
DraftSaveListener detects resource change
    ↓
Listener calls DraftEnrichmentService.isDraftNode()
    ↓
If valid draft → DraftEnrichmentService.enrichDraft()
    ↓
Service adds property: myPotato = "baked"
    ↓
Changes committed to repository
```

### Technical Details

**Draft Node Identification:**
- Path must start with: `/content/forms/fp/admin/drafts/metadata`
- Node type: `fp:Draft`
- Resource type: `fd/fp/components/guidereload`

**Property Addition:**
- Property name: `myPotato`
- Property value: `baked`
- Property type: String

## Monitoring and Logging

### Log Locations

Monitor the service activity in AEM logs:

```bash
tail -f crx-quickstart/logs/error.log | grep -i "draft"
```

### Log Messages

**Success:**
```
INFO [DraftEnrichmentServiceImpl] Successfully enriched draft /content/forms/fp/admin/drafts/metadata/ABC123_af with custom property myPotato=baked
```

**Errors:**
```
ERROR [DraftEnrichmentServiceImpl] Failed to persist custom property to draft: /content/forms/fp/admin/drafts/metadata/ABC123_af
ERROR [DraftSaveListener] Failed to get service resource resolver. Make sure the service user is configured.
```

## Customization

### Changing the Custom Property

To modify the property name or value:

1. Edit `DraftEnrichmentServiceImpl.java`
2. Update constants:
   ```java
   private static final String CUSTOM_PROPERTY_NAME = "myCustomProperty";
   private static final String CUSTOM_PROPERTY_VALUE = "myCustomValue";
   ```
3. Rebuild and redeploy the core bundle

### Adding Multiple Properties

Modify the `enrichDraft()` method in `DraftEnrichmentServiceImpl.java`:

```java
properties.put("myPotato", "baked");
properties.put("myTomato", "fresh");
properties.put("myCarrot", "raw");
```

### Dynamic Property Values

To set dynamic values based on form data or user context:

```java
// Get user info
String userId = resolver.getUserID();
properties.put("lastModifiedBy", userId);

// Get timestamp
properties.put("customTimestamp", Calendar.getInstance());

// Access form data from draft
Resource dataNode = draftResource.getChild("data");
if (dataNode != null) {
    // Process form data
}
```

## Troubleshooting

### Issue: Property Not Being Added

**Check:**
1. Service is active in OSGi console
2. Service user exists and has correct permissions
3. Draft path matches expected pattern
4. Check logs for error messages

**Solution:**
```bash
# Restart the bundle
http://localhost:4502/system/console/bundles
# Find my65site.core and click Restart
```

### Issue: Permission Denied Errors

**Check:**
1. Service user mapping is correct
2. ACL permissions are properly set

**Solution:**
Re-run repoinit scripts or manually grant permissions in User Admin.

### Issue: Listener Not Triggering

**Check:**
1. Listener is registered and active
2. Path configuration matches draft location
3. Change types (ADDED/CHANGED) are correct

**Solution:**
Verify listener properties in OSGi component configuration.

## Security Considerations

- The service user has write access only to `/content/forms/fp/admin/drafts`
- Access is restricted to draft metadata operations
- All operations are logged for audit purposes
- Consider adding additional validation if exposing to external systems

## Performance Notes

- The listener processes changes asynchronously
- Minimal overhead as it only monitors specific paths
- Property addition is a lightweight operation
- Consider batch processing if dealing with high volume

## Support and Maintenance

For issues or enhancements:
1. Check AEM logs for detailed error messages
2. Verify service configuration in OSGi console
3. Review unit tests for expected behavior
4. Monitor JCR for proper node structure

## Version History

- **v1.0.0** - Initial implementation
  - Basic draft enrichment functionality
  - Custom property: myPotato = "baked"
  - Service user configuration
  - Unit tests
