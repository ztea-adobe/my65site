# Drafts & Submissions property getter

The script `draftsAndSubmissionsPropertygetter.ts` is bundled with the site (my65site.site) and runs only when the Drafts & Submissions component is on the page.

## Behavior

1. On DOM ready, checks for `.draftsAndSubmissions` (the Drafts & Submissions component container).
2. If not found, does nothing.
3. If found:
   - Scrapes all elements with `data-draft-id` inside the container and collects unique draft IDs.
   - Makes **one** GET request: `/bin/my65site/draft-property?draftIDs=id1,id2,id3`.
   - Servlet returns JSON: `{"id1": "value1", "id2": "value2", ...}` (draftID → myCustomDraftName).
   - For each element with `data-draft-id`, finds the child `span[data-draft-custom-prop]` and sets its text to the value from the response.

## Template

The draft card template must have:

- An element with `data-draft-id="${draftID}"` (e.g. the card root).
- A child span with `data-draft-custom-prop` where the value will be injected: `<span data-draft-custom-prop></span>`.

## No separate clientlib

This logic lives in the ui.frontend module and is included in the main site bundle. Any page that loads `my65site.site` has the script; it only runs when `.draftsAndSubmissions` is present.
