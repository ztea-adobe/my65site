# Draft Display Template – Custom Property

## Use `${myCustomProperty}` in This Template

**Do not** use `data-sly-use` with `DraftDisplayHelper` on the repeatable Drafts & Submissions list template. Doing so stops the list from rendering (the Use-class fails in the Forms Portal repeatable context).

Instead, use the variable the Forms Portal already provides from the draft metadata:

- **`${myCustomProperty}`** – value of `myCustomPropertyName` on the draft node (set by our listener).

The portal’s data service passes draft properties (name, description, **myCustomProperty**, etc.) into the template for each row. So `${myCustomProperty}` is the correct and safe way to show the custom value here.

## DraftDisplayHelper (Use-Class) – When to Use It

The Java class **`com.mycompany.aem.core.models.DraftDisplayHelper`** exists in the core bundle for HTL scripts where the **current resource is the draft metadata resource** (e.g. a draft detail page, not the list).

- In such scripts you can use:  
  `data-sly-use.draftDisplay="com.mycompany.aem.core.models.DraftDisplayHelper"`  
  and then `${draftDisplay.myPropertyGetter}` / `${draftDisplay.myCustomDraftNameGathered}`.
- **Do not** use it on the list template’s repeatable block; use `${myCustomProperty}` there instead.

## Summary

| Context                         | What to use                          |
|---------------------------------|-------------------------------------|
| Drafts list (this template)    | `${myCustomProperty}`               |
| Page where resource = draft node| `DraftDisplayHelper` + getters     |
