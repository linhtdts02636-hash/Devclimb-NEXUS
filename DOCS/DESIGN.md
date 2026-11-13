# DevClimb Nexus: Design System

The visual language is borrowed from firecrawl.dev (warm off-white, hairline grid, mono labels, one blue accent) and adapted to DevClimb. Keep it recognisable, but do not copy Firecrawl's content or branding.

---

## 1. Principles

1. **One accent.** Blue (`--ac`) marks the primary action, the current selection, links and key numbers. Nothing else is blue.
2. **Borders, not shadows.** A card is a 1px border on a surface. Shadows are only for things that float: popovers, toasts, the browser mockup.
3. **Mono for data, sans for people.** Geist Mono carries labels, numbers, code, times and counts. Inter Tight carries headings and sentences.
4. **Calm on arrival.** A page never shows more than the visitor can act on. New users get one focused card, not a dashboard (see page 11).
5. **Honest data.** Anything invented is labelled "sample data for illustration". No fake claims, no fake history for new users.
6. **Operate screens are quiet.** No entrance animations, no decoration, motion only to show a state change.

## 2. Tokens

Defined on `:root`, with the colour set swapped by `data-theme="light|dark"` on `<html>`. The toggle and its persistence live in `design-lab/_shared/theme.js` (button attribute `data-theme-toggle`).

### Colour

| Token | Light | Dark | Use |
|---|---|---|---|
| `--bg` | `#f9f9f8` | `#0d0d0c` | Page background |
| `--s1` | `#ffffff` | `#151514` | Cards, inputs, popovers |
| `--s2` | `#f1f1ef` | `#1c1c1a` | Secondary buttons, chips, inset blocks |
| `--s3` | `#e7e7e4` | `#262624` | Hover on `--s2`, tracks, skeletons |
| `--line` | `rgba(20,20,20,.08)` | `rgba(255,255,255,.07)` | Dividers inside a surface |
| `--line2` | `rgba(20,20,20,.14)` | `rgba(255,255,255,.13)` | Borders around a surface |
| `--tx` | `#1f1f1e` | `#ecebe6` | Primary text |
| `--tx2` | `#66665f` | `#9c9b93` | Secondary text, labels, icons |
| `--tx3` | `#a3a39b` | `#5f5e58` | Decoration only (grid dots, `+` marks). Fails contrast, never use for words |
| `--ac` | `#2554e8` | `#6f93ff` | Accent |
| `--ac-ink` | `#ffffff` | `#08122e` | Text on an accent fill |
| `--ac-soft` | accent at 10% | accent at 14% | Selected / highlighted background |
| `--ok` | `#14804a` | `#4ad08a` | Accepted, success, easy |
| `--bad` | `#b4442c` | `#f08a6c` | Wrong answer, errors |
| `--warn` | `#9a6a00` | `#e7b44c` | Time limit, hard, achievements |

Semantic colours mean one thing each: green is "passed", red is "failed", amber is "slow or hard". Do not use them for decoration.

### Type

- **Fonts:** Inter Tight (400, 500, 600) and Geist Mono (400, 500), both from Google Fonts. Fallbacks: `system-ui` and `ui-monospace`.
- **Headings:** weight 500 (600 for app page titles), tracking `-0.025em` (`-0.035em` for the landing h1). Never tighter than `-0.04em`.
- **Mono labels:** 11–12px, uppercase, `letter-spacing: .06em`, colour `--tx2`.
- **Numbers in tables, counters, clocks:** `font-variant-numeric: tabular-nums`.
- **Body measure:** about 64–68ch for prose.

| Step | Landing (09, fluid) | App pages (10, 11, fixed) |
|---|---|---|
| xs | 12px | 12px |
| sm | 14px | 13px |
| md | 16px | 14px |
| lg | 18px | 16px |
| xl | `clamp(30px, 3.6vw, 48px)` | 22px (10), 20px (11) |
| xxl | `clamp(38px, 5.4vw, 68px)` | 26px (11 only) |

Fluid headings suit a marketing page. App pages use fixed sizes so headings do not shrink inside narrow panels.

### Spacing, radius, elevation

- **Spacing scale (px):** 4, 8, 12, 16, 24, 32, 48 as `--s-1 … --s-7`. The landing page adds 72 and 112 for section padding.
- **Radius:** 8 (`--r-sm`: buttons, inputs, inset blocks), 12 (`--r`: cards), 16 (`--r-lg`: large panels, popovers), 999 for pills and chips.
- **Elevation:** `--sh-card` (landing mockups) and `--sh-pop` (popovers, toasts). Everything else is flat with a `--line2` border.

### Motion

- Easing `cubic-bezier(.2, .7, .2, 1)`; most transitions 150–250ms.
- Buttons press down 1px on `:active`.
- `prefers-reduced-motion: reduce` removes all animation and transitions on every page.
- Landing only: GSAP entrance, scramble-decode text, drifting ASCII, scroll-linked effects (see section 5).

## 3. Layout

- **Landing:** centred frame, `--rail: 1240px`, hairline vertical borders, `+` marks at section corners, 112px section padding, split section headers (title left, lead right).
- **Workspace (10):** full viewport, two resizable panes (problem left, editor over console right). Pane sizes are remembered. Below 900px the panes stack and the page scrolls.
- **Home (11):** top nav plus a 3-column grid (240px / flexible / 300px), `--rail: 1280px`. Below 1160px the right column drops under the feed; below 820px everything is one column. New users get a single 720px column.

## 4. Components

**Buttons** (`.btn`): 32px high, radius 8, weight 500. Variants: default (`--s2`), primary `.p` (accent fill with a soft inset shadow), ghost `.ghost`, icon `.icon` (32px square), small `.sm` (28px). Disabled is 50% opacity with `not-allowed`.

**Badges** (`.badge`): mono 12px pill on `--s2`. Difficulty variants: easy (green tint), medium (accent tint), hard (amber tint).

**Tabs:** 38–40px bar, active tab has a 2px accent underline. They use `role="tablist"` with roving `tabindex` and Arrow-key support. Pill chips (`.chip`, `.cmt`) are used for filters and small switchers, selected state is an accent border.

**Cards and lists:** `--s1` surface, `--line2` border, radius 12–16, no shadow. Rows inside a list are separated by `--line`.

**Inputs:** 32–36px, `--bg` fill, `--line2` border; focus shows an accent border plus a 3px `--ac-soft` ring. Errors use `--bad` with a message under the field (`aria-invalid` and `aria-describedby`).

**Search combobox:** in the top nav on landing and home. Shows recents (or popular items for new users) when empty; while typing, scoped rows ("`java` in Problems / Communities / Contests / People") plus top matches with the matched letters in accent. `/` focuses it, arrows move, Enter opens, Esc closes. Collapses to an icon on phones.

**Progress bars:** thin (4–6px) rounded tracks. Hidden-test bars use one segment per test and colour each green, red or amber.

**Toast:** dark pill at the bottom centre, 5 seconds, optional single action ("Undo", "Read it").

**Code blocks:** always dark (`--code-bg`), mono 12–13px, with a small syntax palette (keywords in the accent family, strings green, numbers amber, comments grey-italic).

**Skeletons:** `--s2`/`--s3` shimmer, used instead of spinners for page content. A small spinner is only used inside buttons while an action runs.

**Empty and locked states:** always say what to do next ("Pass every test to join the discussion", "Solve a few problems or join a community and this tab fills up").

## 5. Landing-only signature details

These carry the Firecrawl feel and are deliberately **not** used on the app pages:

- hairline cell grid in the hero with pixel icons that morph, and a corner of `[ 200 OK ]`-style mono labels
- ASCII ridge and wave art, pixel-built wordmark in the footer that fills as you scroll
- scramble-decode text and a scanning "anatomy of a page" mockup
- `[ 01 / 05 ]` section labels, `+` corner marks, split section headers

## 6. How the pages differ

| | Landing 09 | Workspace 10 | Home 11 |
|---|---|---|---|
| Density | Spacious | Dense | Medium |
| Decoration | Grid, ASCII, pixel art | None | None |
| Entrance animation | Yes | No | No |
| Type scale | Fluid | Fixed | Fixed |
| Section padding | 112px | n/a | 24–32px |
| Rail width | 1240px | full width | 1280px |

### Page 11 states

The home page has two viewer states (a design-lab "preview as" switch at the bottom left toggles them; remove it in production):

- **New user:** one centred card with three steps (pick languages, join a community, solve a first problem). Only the current step is open. Every step can be skipped. No streak, rank, achievements, announcements or notifications. After setup or skipping, a calm starter feed (popular, beginner-friendly items) with a slim "Finish setting up" bar.
- **Returning user:** full layout with the personalised feed, "Continue" card, communities, streak, rank, upcoming contests and next achievement.

Every feed item has a **Why this?** toggle that expands inline to show its recommendation score breakdown. This is a core feature of the page: the ranking is explained, not hidden.

## 7. Accessibility rules (all pages)

- Text contrast 4.5:1 or better; `--tx3` is never used for words.
- Visible focus ring on everything interactive (2px accent, 2px offset).
- Icon-only buttons have an `aria-label`. Toggles use `aria-pressed`; disclosures use `aria-expanded`.
- Tabs, listbox/combobox, panel resizers and the setup steps work from the keyboard.
- After an action re-renders a list, focus returns to the control that was used.
- Colour is never the only signal: verdicts always include text, progress includes a number.
- Layouts work at 390px wide with no horizontal page scroll.

## 8. Copy rules

- Plain, short sentences. One idea per sentence.
- A control names its action ("Start problem", "Mark all read"), not "Submit" or "OK" where something more specific fits.
- Errors say what is wrong and how to fix it.
- No filler openers, no hype words, no em dashes in UI copy.
- Anything invented carries "sample data for illustration".

## 9. Known gaps and next steps

- **Tokens are copy-pasted into each file.** Move them to a shared `_shared/tokens.css` so a change lands everywhere. Do this before building more pages.
- **Section number labels** (`[ 01 / 05 ]`) are kept for the Firecrawl look, but impeccable's rules flag them. Remove them if the landing page is ever judged on cleanliness over style.
- **Judge, search, feed, and recommendations are all sample data.** Each has a `ponytail:` comment where the real endpoint plugs in (Judge0 for non-JS languages, `GET /feed` for the recommender in roadmap 0.6.0, a search endpoint).
- **Fonts load from Google Fonts.** Self-host them if the product must work offline or in restricted networks.
- **Missing pages:** community page, contest page, profile, admin. They should reuse this file's tokens and the Operate layout rules.

## 10. Checklist for a new page

1. Pick the surface type: Persuade (spacious, decorative) or Operate (dense, quiet).
2. Copy the token block; do not invent new colours or radii.
3. Use `--ac` only for the primary action, selection and links.
4. Border on cards, shadow only on floating layers.
5. Mono for numbers and labels, sans for sentences.
6. Design the empty, loading, error and first-visit states, not just the full one.
7. Test light and dark, 1440px and 390px, keyboard only.
