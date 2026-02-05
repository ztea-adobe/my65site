/**
 * Drafts & Submissions property getter.
 * Scrapes draft IDs from the DOM (data-draft-id or .__FP_eachDraftLink cards),
 * one batch request to the servlet, then fills each span[data-draft-custom-prop].
 */

const DRAFTS_COMPONENT_SELECTOR = ".draftsAndSubmissions";
const CARD_SELECTOR = ".__FP_eachDraftLink";
const SERVLET_PATH = "/bin/my65site/draft-property";
const ATTR_DRAFT_ID = "data-draft-id";
const ATTR_DRAFT_CUSTOM_PROP = "data-draft-custom-prop";

function getDraftIdFromCard(card: Element): string | null {
  const id = card.getAttribute(ATTR_DRAFT_ID);
  if (id) {
    return id;
  }
  const link = card.querySelector("a.__FP_draftlink");
  const href = link && link.getAttribute("href");
  if (href) {
    const m = href.match(/\/draft\/([^/?]+)/);
    if (m) {
      return m[1];
    }
  }
  return null;
}

function runPropertygetter(container: Element): void {
  const draftIds: string[] = [];
  const elementByDraftId: Record<string, Element> = {};

  // 1) Elements that have data-draft-id
  const withAttr = container.querySelectorAll(`[${ATTR_DRAFT_ID}]`);
  withAttr.forEach((el) => {
    const id = el.getAttribute(ATTR_DRAFT_ID);
    if (id && !elementByDraftId[id]) {
      draftIds.push(id);
      elementByDraftId[id] = el;
    }
  });

  // 2) Fallback: .__FP_eachDraftLink cards (get id from link if no data-draft-id)
  const cards = container.querySelectorAll(CARD_SELECTOR);
  cards.forEach((card) => {
    const id = getDraftIdFromCard(card);
    if (id && !elementByDraftId[id]) {
      draftIds.push(id);
      elementByDraftId[id] = card;
    }
  });

  if (draftIds.length === 0) {
    return;
  }

  const url = `${SERVLET_PATH}?draftIDs=${draftIds.map((id) => encodeURIComponent(id)).join(",")}`;

  const xhr = new XMLHttpRequest();
  xhr.open("GET", url, true);
  xhr.setRequestHeader("Accept", "application/json");
  xhr.onreadystatechange = (): void => {
    if (xhr.readyState !== 4) {
      return;
    }
    if (xhr.status !== 200) {
      return;
    }
    let map: Record<string, string>;
    try {
      map = JSON.parse(xhr.responseText) as Record<string, string>;
    } catch {
      return;
    }

    draftIds.forEach((draftId) => {
      const el = elementByDraftId[draftId];
      if (!el) {
        return;
      }
      const span = el.querySelector(`[${ATTR_DRAFT_CUSTOM_PROP}]`);
      if (!span) {
        return;
      }
      const value = map[draftId];
      span.textContent = value != null ? value : "";
    });
  };
  xhr.onerror = (): void => { /* ignore network error */ };
  xhr.send();
}

function init(): void {
  // Prefer scoping to Drafts & Submissions component if present
  const component = document.querySelector(DRAFTS_COMPONENT_SELECTOR);
  const container = component || document.body;
  runPropertygetter(container);
}

function schedule(): void {
  init();
  // Run again after a short delay in case draft list is rendered asynchronously
  setTimeout(init, 500);
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", schedule);
} else {
  schedule();
}
