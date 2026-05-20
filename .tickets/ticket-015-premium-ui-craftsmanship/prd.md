# Ticket: Premium UI Craftsmanship — Visual Design Overhaul

## Problem Statement

The current ClawDroid UI, while functionally complete after ticket-008, lacks visual polish and professional craftsmanship. The interface feels generic — flat cards, stock Material colors, no visual hierarchy emphasis, no micro-interactions, no brand personality. It looks like a developer built it (which is true), not a designer. Users judge app quality by visual polish within seconds. ClawDroid needs to communicate "premium AI assistant" at first glance.

### Specific Pain Points

1. **Color palette is generic**: Default Material3 DayNight with no brand-appropriate color story. No dynamic theming.
2. **No depth or elevation hierarchy**: Cards are flat with uniform 2dp elevation. No visual distinction between primary actions and secondary info.
3. **Zero micro-interactions**: Buttons snap instantly with no feedback ripple personality; status transitions are instant with no animation.
4. **Typography not differentiated**: All text uses same weight/size scale; no emphasized headlines or visual hierarchy.
5. **No brand personality**: The app has no visual identity — no gradient accents, no signature color moments, no iconography system.
6. **Dark theme is an afterthought**: Colors were likely auto-generated for dark mode rather than deliberately crafted.
7. **Status indicators are utilitarian chips**: They work but don't communicate emotion or state delightfully.

## Design Quality Rubric

Each UI component will be evaluated against this rubric (score 1-5 per dimension):

| Dimension | 1 (Poor) | 3 (Adequate) | 5 (Excellent) |
|-----------|----------|--------------|---------------|
| **Color Harmony** | Clashing/arbitrary colors | Functional M3 defaults | Curated palette with brand story; dynamic color |
| **Visual Hierarchy** | Everything same emphasis | Sections distinguishable | Clear focal points; scanning is effortless |
| **Typography** | No type system | M3 type scale used | Custom hierarchy with emphasis weights; readability at all sizes |
| **Motion & Animation** | No animation | Basic transitions | Intent-driven micro-interactions; spring physics |
| **Depth & Elevation** | Flat/no shadows | M3 elevation defaults | Purposeful depth separating content layers |
| **Touch Feedback** | No feedback | Default ripple | Custom ripple + haptic + state transitions |
| **Dark Theme** | Broken/incomplete | Functional auto-dark | Deliberately crafted dark palette with reduced eye strain |
| **Iconography** | No icons | Android stock icons | Custom-styled icons consistent with brand |
| **Spacing & Rhythm** | Inconsistent | 8dp grid used | Perfect 8dp/4dp rhythm; breathing room |
| **Consistency** | Mixed patterns | Same component same way | Every screen feels like part of one app |
| **Accessibility** | Fails contrast | Meets WCAG AA | Exceeds AA; readable in sunlight; supports font scaling |
| **Brand Personality** | Generic/forgettable | Logo visible | Distinctive visual voice; memorable |

Target: All components score **4+** across all dimensions.

## Proposed Solution

### Design Direction: "ClawDroid Dark Elegance"

**Concept**: Dark-themed AI tool with neon-soft accent colors. Think: VS Code dark theme meets premium fintech app. The app is an AI companion — the UI should feel intelligent, trustworthy, and slightly futuristic.

**Design Principles**:
1. **Dark-first**: Design dark mode as primary; light mode as adaptation
2. **Depth through layers**: Use elevation, glow, and subtle gradients to create spatial hierarchy
3. **Motion with purpose**: Every animation should communicate state change or guide attention
4. **Monochromatic with accent**: Base on near-black/charcoal with carefully chosen accent colors
5. **Glassmorphism accents**: Frosted glass effects for overlays and cards
6. **Generous whitespace**: Content should breathe

### Color System

**Dark Theme (Primary)**:
- Background: `#0D1117` (GitHub dark) → deep charcoal
- Surface: `#161B22` (elevated surface)
- Surface Container: `#1C2333` (cards)
- Primary: `#58A6FF` (electric blue — trust, intelligence)
- Secondary: `#3FB950` (emerald green — success, running)
- Tertiary: `#D2A8FF` (soft purple — AI/creative)
- Accent Gradient: Primary → Tertiary (hero moments)
- Error: `#F85149` (soft red)
- Warning: `#D29922` (amber)
- On colors: White `#F0F6FC` with appropriate opacity

**Light Theme Adaptation**:
- Background: `#F6F8FA`
- Surface: `#FFFFFF`
- Primary: `#0969DA`
- Secondary: `#1A7F37`
- Tertiary: `#8250DF`

## Acceptance Criteria

- [ ] Color palette is deliberately crafted with brand story (not default M3)
- [ ] Dark theme is the primary design target; light theme is equally polished
- [ ] Status indicators use animated gradient/glow effects (not static chips)
- [ ] Cards have layered depth with subtle border glow for active states
- [ ] FAB has animated pulse/lift effect
- [ ] App bar is translucent with backdrop blur (glassmorphism)
- [ ] All transitions have spring-based animations (start/stop, status changes)
- [ ] Welcome/onboarding screen has hero illustration or animated gradient
- [ ] Typography uses custom scale with variable font emphasis weights
- [ ] Iconography is consistent — all icons same style
- [ ] Spacing follows strict 8dp grid with deliberate whitespace
- [ ] Dark mode color contrast exceeds WCAG AA (target AAA for primary text)
- [ ] Config screen matches main screen design language
- [ ] All screens feel like part of one cohesive app
- [ ] Layout renders without errors; all existing tests pass

## Technical Considerations

- **ViewBinding** already in use — continue with XML layouts (no Compose migration)
- **Material Components** already in dependency — use M3 attributes extensively
- **No new dependencies** — achieve effects with existing Android APIs
- **Drawable-based effects**: Use `<gradient>`, `<shape>`, `<layer-list>` for depth
- **Animation**: Use `ViewPropertyAnimator`, `AnimatorSet`, `Transition` APIs
- **Backdrop blur**: Use `WindowInsetsControllerCompat` + `RenderEffect` on API 31+
- **Dark theme**: Already supported via `Theme.Material3.DayNight.NoActionBar`
- **Must maintain**: All existing functionality, test IDs, and accessibility

## Dependencies

- Depends on ticket-008 (basic UI overhaul)
- No new external dependencies

## Tasks

- [ ] Task 1: Research & Design Specification
  - **Problem**: Need informed design direction
  - **Test**: Design spec documented with references
  - **Depends on**: None
  - **Subtasks**:
    - [ ] Subtask 1.1: Research 2026 mobile UI trends and collect Dribbble references
    - [ ] Subtask 1.2: Create UI design quality rubric
    - [ ] Subtask 1.3: Document color system, typography, and spacing specs

- [ ] Task 2: Mockup Proposals (3 Parallel)
  - **Problem**: Need visual direction options
  - **Test**: 3 distinct mockup proposals evaluated against rubric
  - **Depends on**: Task 1
  - **Subtasks**:
    - [ ] Subtask 2.1: Mockup A — "Dark Elegance" (dark-first with glassmorphism)
    - [ ] Subtask 2.2: Mockup B — "Clean Minimal" (light-first with subtle color pops)
    - [ ] Subtask 2.3: Mockup C — "Bold Future" (gradient-heavy with expressive shapes)

- [ ] Task 3: Select & Implement Best Mockup
  - **Problem**: Need to execute the winning design
  - **Test**: All acceptance criteria met; build passes
  - **Depends on**: Task 2
  - **Subtasks**:
    - [ ] Subtask 3.1: Update colors.xml with curated palette
    - [ ] Subtask 3.2: Update themes.xml with refined dark/light themes
    - [ ] Subtask 3.3: Redesign activity_main.xml layout
    - [ ] Subtask 3.4: Add micro-interactions and animations
    - [ ] Subtask 3.5: Update config/agent/mission-control screens for consistency
    - [ ] Subtask 3.6: Run quality check (lint + test + build)
