#!/bin/bash

# Diagnostic script to check what fields are in your form draft data

AEM_URL="http://localhost:4502"
AEM_USER="admin:admin"

echo "===========================================" 
echo "AEM Forms Draft Field Diagnostic Tool"
echo "==========================================="
echo ""

# Get latest draft
DRAFT_ID=$(curl -s -u $AEM_USER "$AEM_URL/content/forms/fp/admin/drafts/metadata.1.json" | \
  python3 -c "import sys, json; data=json.load(sys.stdin); drafts=[k for k in data.keys() if k.endswith('_af')]; print(drafts[0] if drafts else '')" 2>/dev/null)

if [ -z "$DRAFT_ID" ]; then
  echo "❌ No drafts found. Please save a form first."
  exit 1
fi

echo "✅ Found draft: $DRAFT_ID"
echo ""

# Get draft metadata
echo "📋 Draft Metadata Properties:"
curl -s -u $AEM_USER "$AEM_URL/content/forms/fp/admin/drafts/metadata/$DRAFT_ID.json" | \
  python3 -c "
import sys, json
data = json.load(sys.stdin)
print(f'  name: {data.get(\"name\", \"N/A\")}')
print(f'  owner: {data.get(\"owner\", \"N/A\")}')
print(f'  myPotato: {data.get(\"myPotato\", \"NOT FOUND\")}')
print(f'  myCustomDraftNameGathered: {data.get(\"myCustomDraftNameGathered\", \"NOT FOUND\")}')
print(f'  userdataID: {data.get(\"userdataID\", \"N/A\")}')
" 2>/dev/null
echo ""

# Get data path
DATA_PATH=$(curl -s -u $AEM_USER "$AEM_URL/content/forms/fp/admin/drafts/metadata/$DRAFT_ID.json" | \
  python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('userdataID', ''))" 2>/dev/null)

if [ -z "$DATA_PATH" ]; then
  echo "❌ No userdataID found in draft"
  exit 1
fi

echo "📁 Draft Data Location: $DATA_PATH"
echo ""

# Check if data has jcr:data
echo "🔍 Checking data node structure..."
HAS_JCR_DATA=$(curl -s -u $AEM_USER "$AEM_URL$DATA_PATH.json" | \
  python3 -c "import sys, json; data=json.load(sys.stdin); print('YES' if ':jcr:data' in data else 'NO')" 2>/dev/null)

echo "  Has jcr:data binary: $HAS_JCR_DATA"
echo ""

if [ "$HAS_JCR_DATA" = "YES" ]; then
  echo "📄 Extracting XML from binary data..."
  echo ""
  
  # Try to get XML content (this might not work via curl, but worth trying)
  curl -s -u $AEM_USER "$AEM_URL$DATA_PATH" | head -100 | grep -o '<[^>]*>[^<]*</[^>]*>' | head -20 || echo "  (Unable to extract via HTTP - data is binary)"
  
  echo ""
  echo "💡 To see the actual form fields:"
  echo "   1. Go to CRXDE: $AEM_URL/crx/de/index.jsp"
  echo "   2. Navigate to: $DATA_PATH"
  echo "   3. Double-click on 'jcr:data' property"
  echo "   4. Look for XML field names like:"
  echo "      <fieldName>value</fieldName>"
  echo ""
fi

# Trigger enrichment
echo "🔄 Triggering draft enrichment..."
curl -s -u $AEM_USER -X POST "$AEM_URL/content/forms/fp/admin/drafts/metadata/$DRAFT_ID" \
  -d "diagnostic=true" > /dev/null 2>&1

sleep 2

# Check if field was extracted
echo ""
echo "✅ Checking if field was extracted..."
FIELD_VALUE=$(curl -s -u $AEM_USER "$AEM_URL/content/forms/fp/admin/drafts/metadata/$DRAFT_ID.json" | \
  python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('myCustomDraftNameGathered', 'NOT FOUND'))" 2>/dev/null)

if [ "$FIELD_VALUE" != "NOT FOUND" ]; then
  echo "  ✅ SUCCESS! myCustomDraftNameGathered = \"$FIELD_VALUE\""
else
  echo "  ❌ Field NOT extracted"
  echo ""
  echo "Possible reasons:"
  echo "  1. Your form doesn't have a field named 'myCustomDraftName'"
  echo "  2. The field was empty when you saved"
  echo "  3. The field is in an unbound panel"
  echo ""
  echo "Next steps:"
  echo "  1. Check CRXDE at $DATA_PATH for the actual field names"
  echo "  2. Add a field named 'myCustomDraftName' to your form"
  echo "  3. OR update the code to use one of your existing field names"
fi

echo ""
echo "==========================================="
echo "Diagnostic complete!"
echo "==========================================="
