# Changelog

All notable changes to this project will be documented in this file. See [commit-and-tag-version](https://github.com/absolute-version/commit-and-tag-version) for commit guidelines.

## 0.1.4 (2026-05-20)


### Features

* **ticket-001:** project scaffold with Gradle build, Docker container, and build scripts ([39b63d7](https://github.com/asieduernest12/ClawDroid/commit/39b63d7e76d5c49696f668c68e9a89c079a8082f))
* **ticket-002:** configuration screen with ViewModel, Repository, and data binding ([95306ea](https://github.com/asieduernest12/ClawDroid/commit/95306ea538ddc0c0873faa4316412f7c15879955))
* **ticket-003:** embedded Mission Control HTTP server with status API ([19e65ea](https://github.com/asieduernest12/ClawDroid/commit/19e65ea2ea48d52e96384a0b199d53b9c080b94c))
* **ticket-004:** terminal emulator integration with Termux bootstrap and PicoClaw session management ([8d7b5cd](https://github.com/asieduernest12/ClawDroid/commit/8d7b5cd56c13f3997a37ddc3cf04a95c022c740d))
* **ticket-005:** main activity with bootstrap status display and E2E validation ([db91490](https://github.com/asieduernest12/ClawDroid/commit/db9149045ccd88eaf43686e2b601079f0d77d94d))
* **ticket-006:** bundle Termux bootstrap and PicoClaw binary with auto-download fallback ([49fe455](https://github.com/asieduernest12/ClawDroid/commit/49fe455f4b675528cc3c203de0f40c9f207cc2b1))
* **ticket-007:** BDD acceptance test suite with 15 scenarios across 4 classes ([40e98b9](https://github.com/asieduernest12/ClawDroid/commit/40e98b98505daf28b142eb3d7a8c4e504b66bd17))
* **ticket-008:** UI/UX overhaul with Dashboard Card design ([15a304f](https://github.com/asieduernest12/ClawDroid/commit/15a304f3cbe0cd4514941d1845a10bb404dfa6b8))
* **ticket-009:** provider management UI — add/edit/delete AI providers ([d804614](https://github.com/asieduernest12/ClawDroid/commit/d8046147ba75b11e62397b4f143d6cc821b215d3))
* **ticket-010,ticket-012:** model picker, emulator control skill, and ticket docs ([107e3e9](https://github.com/asieduernest12/ClawDroid/commit/107e3e9c3365ee2a0fbd72d29cc44a97eddfcb04))
* **ticket-010:** agent chat + CLI terminal + provider/model switching ([84a5806](https://github.com/asieduernest12/ClawDroid/commit/84a580636f47d51f1a26e8e2b651e7337023a5a8))
* **ticket-013:** full-history chat sessions + thinking stream + tool calls + deploy workflow ([ac2a189](https://github.com/asieduernest12/ClawDroid/commit/ac2a189974688230a7f6955c0161af2b0a018d23))
* **ticket-014:** conversation context, slash commands, state management, and telemetry ([d153a2a](https://github.com/asieduernest12/ClawDroid/commit/d153a2ae5337c9d2c056a68088a75c57bf42cb52))


### Bug Fixes

* **ci:** remove duplicate git tag creation and bump Node.js to 22 ([64ad474](https://github.com/asieduernest12/ClawDroid/commit/64ad4745ee1ca6b0bfb0298c867fa5eeb2af0c3d))
* **p0:** Mission Control showing raw HTML — MIME sent as text/plain ([99f7f39](https://github.com/asieduernest12/ClawDroid/commit/99f7f39f97a2b67bc6f7c626a9313bd064bf2276))
* **p0:** picoclaw binary architecture mismatch — multi-arch build + gateway launch ([344f7f3](https://github.com/asieduernest12/ClawDroid/commit/344f7f3e63bfae23e732849397df112d8cff672c))
* **ticket-010:** AgentChatActivity crash — BottomSheet must be direct child of CoordinatorLayout ([ed0e131](https://github.com/asieduernest12/ClawDroid/commit/ed0e13121dc890dc1c61326108d8d19bea021bf5))
* **ticket-012:** mark all tasks and acceptance criteria complete ([4d036be](https://github.com/asieduernest12/ClawDroid/commit/4d036be45636a722349349d308cebb05837b5970))
* **ticket-015:** CLI command execution and agent chat fixes ([bb9ae88](https://github.com/asieduernest12/ClawDroid/commit/bb9ae889391728c12c99e56b4d1beec8c7c27e71))
* **ticket-016:** group models by provider, seed defaults, inject OpenRouter dev key ([2ff0d68](https://github.com/asieduernest12/ClawDroid/commit/2ff0d686ff26903e70b67172a87a732fd434d047))
* **ticket-016:** update OpenRouter default model to working free model ([082bf23](https://github.com/asieduernest12/ClawDroid/commit/082bf23c94eddaec23e9bfd72db54a1a27464d62))
* write request body to HttpURLConnection and simplify default providers ([f75de09](https://github.com/asieduernest12/ClawDroid/commit/f75de09f35f0351f21748337ca306c82112de233))

## 0.1.3 (2026-05-20)


### Features

* **ticket-001:** project scaffold with Gradle build, Docker container, and build scripts ([39b63d7](https://github.com/asieduernest12/ClawDroid/commit/39b63d7e76d5c49696f668c68e9a89c079a8082f))
* **ticket-002:** configuration screen with ViewModel, Repository, and data binding ([95306ea](https://github.com/asieduernest12/ClawDroid/commit/95306ea538ddc0c0873faa4316412f7c15879955))
* **ticket-003:** embedded Mission Control HTTP server with status API ([19e65ea](https://github.com/asieduernest12/ClawDroid/commit/19e65ea2ea48d52e96384a0b199d53b9c080b94c))
* **ticket-004:** terminal emulator integration with Termux bootstrap and PicoClaw session management ([8d7b5cd](https://github.com/asieduernest12/ClawDroid/commit/8d7b5cd56c13f3997a37ddc3cf04a95c022c740d))
* **ticket-005:** main activity with bootstrap status display and E2E validation ([db91490](https://github.com/asieduernest12/ClawDroid/commit/db9149045ccd88eaf43686e2b601079f0d77d94d))
* **ticket-006:** bundle Termux bootstrap and PicoClaw binary with auto-download fallback ([49fe455](https://github.com/asieduernest12/ClawDroid/commit/49fe455f4b675528cc3c203de0f40c9f207cc2b1))
* **ticket-007:** BDD acceptance test suite with 15 scenarios across 4 classes ([40e98b9](https://github.com/asieduernest12/ClawDroid/commit/40e98b98505daf28b142eb3d7a8c4e504b66bd17))
* **ticket-008:** UI/UX overhaul with Dashboard Card design ([15a304f](https://github.com/asieduernest12/ClawDroid/commit/15a304f3cbe0cd4514941d1845a10bb404dfa6b8))
* **ticket-009:** provider management UI — add/edit/delete AI providers ([d804614](https://github.com/asieduernest12/ClawDroid/commit/d8046147ba75b11e62397b4f143d6cc821b215d3))
* **ticket-010,ticket-012:** model picker, emulator control skill, and ticket docs ([107e3e9](https://github.com/asieduernest12/ClawDroid/commit/107e3e9c3365ee2a0fbd72d29cc44a97eddfcb04))
* **ticket-010:** agent chat + CLI terminal + provider/model switching ([84a5806](https://github.com/asieduernest12/ClawDroid/commit/84a580636f47d51f1a26e8e2b651e7337023a5a8))
* **ticket-013:** full-history chat sessions + thinking stream + tool calls + deploy workflow ([ac2a189](https://github.com/asieduernest12/ClawDroid/commit/ac2a189974688230a7f6955c0161af2b0a018d23))
* **ticket-014:** conversation context, slash commands, state management, and telemetry ([d153a2a](https://github.com/asieduernest12/ClawDroid/commit/d153a2ae5337c9d2c056a68088a75c57bf42cb52))


### Bug Fixes

* **p0:** Mission Control showing raw HTML — MIME sent as text/plain ([99f7f39](https://github.com/asieduernest12/ClawDroid/commit/99f7f39f97a2b67bc6f7c626a9313bd064bf2276))
* **p0:** picoclaw binary architecture mismatch — multi-arch build + gateway launch ([344f7f3](https://github.com/asieduernest12/ClawDroid/commit/344f7f3e63bfae23e732849397df112d8cff672c))
* **ticket-010:** AgentChatActivity crash — BottomSheet must be direct child of CoordinatorLayout ([ed0e131](https://github.com/asieduernest12/ClawDroid/commit/ed0e13121dc890dc1c61326108d8d19bea021bf5))
* **ticket-012:** mark all tasks and acceptance criteria complete ([4d036be](https://github.com/asieduernest12/ClawDroid/commit/4d036be45636a722349349d308cebb05837b5970))
* **ticket-015:** CLI command execution and agent chat fixes ([bb9ae88](https://github.com/asieduernest12/ClawDroid/commit/bb9ae889391728c12c99e56b4d1beec8c7c27e71))
* **ticket-016:** group models by provider, seed defaults, inject OpenRouter dev key ([2ff0d68](https://github.com/asieduernest12/ClawDroid/commit/2ff0d686ff26903e70b67172a87a732fd434d047))
* **ticket-016:** update OpenRouter default model to working free model ([082bf23](https://github.com/asieduernest12/ClawDroid/commit/082bf23c94eddaec23e9bfd72db54a1a27464d62))
* write request body to HttpURLConnection and simplify default providers ([f75de09](https://github.com/asieduernest12/ClawDroid/commit/f75de09f35f0351f21748337ca306c82112de233))

## 0.1.2 (2026-05-20)


### Features

* **ticket-001:** project scaffold with Gradle build, Docker container, and build scripts ([39b63d7](https://github.com/asieduernest12/ClawDroid/commit/39b63d7e76d5c49696f668c68e9a89c079a8082f))
* **ticket-002:** configuration screen with ViewModel, Repository, and data binding ([95306ea](https://github.com/asieduernest12/ClawDroid/commit/95306ea538ddc0c0873faa4316412f7c15879955))
* **ticket-003:** embedded Mission Control HTTP server with status API ([19e65ea](https://github.com/asieduernest12/ClawDroid/commit/19e65ea2ea48d52e96384a0b199d53b9c080b94c))
* **ticket-004:** terminal emulator integration with Termux bootstrap and PicoClaw session management ([8d7b5cd](https://github.com/asieduernest12/ClawDroid/commit/8d7b5cd56c13f3997a37ddc3cf04a95c022c740d))
* **ticket-005:** main activity with bootstrap status display and E2E validation ([db91490](https://github.com/asieduernest12/ClawDroid/commit/db9149045ccd88eaf43686e2b601079f0d77d94d))
* **ticket-006:** bundle Termux bootstrap and PicoClaw binary with auto-download fallback ([49fe455](https://github.com/asieduernest12/ClawDroid/commit/49fe455f4b675528cc3c203de0f40c9f207cc2b1))
* **ticket-007:** BDD acceptance test suite with 15 scenarios across 4 classes ([40e98b9](https://github.com/asieduernest12/ClawDroid/commit/40e98b98505daf28b142eb3d7a8c4e504b66bd17))
* **ticket-008:** UI/UX overhaul with Dashboard Card design ([15a304f](https://github.com/asieduernest12/ClawDroid/commit/15a304f3cbe0cd4514941d1845a10bb404dfa6b8))
* **ticket-009:** provider management UI — add/edit/delete AI providers ([d804614](https://github.com/asieduernest12/ClawDroid/commit/d8046147ba75b11e62397b4f143d6cc821b215d3))
* **ticket-010,ticket-012:** model picker, emulator control skill, and ticket docs ([107e3e9](https://github.com/asieduernest12/ClawDroid/commit/107e3e9c3365ee2a0fbd72d29cc44a97eddfcb04))
* **ticket-010:** agent chat + CLI terminal + provider/model switching ([84a5806](https://github.com/asieduernest12/ClawDroid/commit/84a580636f47d51f1a26e8e2b651e7337023a5a8))
* **ticket-013:** full-history chat sessions + thinking stream + tool calls + deploy workflow ([ac2a189](https://github.com/asieduernest12/ClawDroid/commit/ac2a189974688230a7f6955c0161af2b0a018d23))
* **ticket-014:** conversation context, slash commands, state management, and telemetry ([d153a2a](https://github.com/asieduernest12/ClawDroid/commit/d153a2ae5337c9d2c056a68088a75c57bf42cb52))


### Bug Fixes

* **p0:** Mission Control showing raw HTML — MIME sent as text/plain ([99f7f39](https://github.com/asieduernest12/ClawDroid/commit/99f7f39f97a2b67bc6f7c626a9313bd064bf2276))
* **p0:** picoclaw binary architecture mismatch — multi-arch build + gateway launch ([344f7f3](https://github.com/asieduernest12/ClawDroid/commit/344f7f3e63bfae23e732849397df112d8cff672c))
* **ticket-010:** AgentChatActivity crash — BottomSheet must be direct child of CoordinatorLayout ([ed0e131](https://github.com/asieduernest12/ClawDroid/commit/ed0e13121dc890dc1c61326108d8d19bea021bf5))
* **ticket-012:** mark all tasks and acceptance criteria complete ([4d036be](https://github.com/asieduernest12/ClawDroid/commit/4d036be45636a722349349d308cebb05837b5970))
* **ticket-015:** CLI command execution and agent chat fixes ([bb9ae88](https://github.com/asieduernest12/ClawDroid/commit/bb9ae889391728c12c99e56b4d1beec8c7c27e71))
* **ticket-016:** group models by provider, seed defaults, inject OpenRouter dev key ([2ff0d68](https://github.com/asieduernest12/ClawDroid/commit/2ff0d686ff26903e70b67172a87a732fd434d047))
* **ticket-016:** update OpenRouter default model to working free model ([082bf23](https://github.com/asieduernest12/ClawDroid/commit/082bf23c94eddaec23e9bfd72db54a1a27464d62))
* write request body to HttpURLConnection and simplify default providers ([f75de09](https://github.com/asieduernest12/ClawDroid/commit/f75de09f35f0351f21748337ca306c82112de233))

## 0.1.1 (2026-05-17)


### Features

* **ticket-001:** project scaffold with Gradle build, Docker container, and build scripts ([39b63d7](https://github.com/asieduernest12/ClawDroid/commit/39b63d7e76d5c49696f668c68e9a89c079a8082f))
* **ticket-002:** configuration screen with ViewModel, Repository, and data binding ([95306ea](https://github.com/asieduernest12/ClawDroid/commit/95306ea538ddc0c0873faa4316412f7c15879955))
* **ticket-003:** embedded Mission Control HTTP server with status API ([19e65ea](https://github.com/asieduernest12/ClawDroid/commit/19e65ea2ea48d52e96384a0b199d53b9c080b94c))
* **ticket-004:** terminal emulator integration with Termux bootstrap and PicoClaw session management ([8d7b5cd](https://github.com/asieduernest12/ClawDroid/commit/8d7b5cd56c13f3997a37ddc3cf04a95c022c740d))
* **ticket-005:** main activity with bootstrap status display and E2E validation ([db91490](https://github.com/asieduernest12/ClawDroid/commit/db9149045ccd88eaf43686e2b601079f0d77d94d))
* **ticket-006:** bundle Termux bootstrap and PicoClaw binary with auto-download fallback ([49fe455](https://github.com/asieduernest12/ClawDroid/commit/49fe455f4b675528cc3c203de0f40c9f207cc2b1))
* **ticket-007:** BDD acceptance test suite with 15 scenarios across 4 classes ([40e98b9](https://github.com/asieduernest12/ClawDroid/commit/40e98b98505daf28b142eb3d7a8c4e504b66bd17))
* **ticket-008:** UI/UX overhaul with Dashboard Card design ([15a304f](https://github.com/asieduernest12/ClawDroid/commit/15a304f3cbe0cd4514941d1845a10bb404dfa6b8))
* **ticket-009:** provider management UI — add/edit/delete AI providers ([d804614](https://github.com/asieduernest12/ClawDroid/commit/d8046147ba75b11e62397b4f143d6cc821b215d3))
* **ticket-010,ticket-012:** model picker, emulator control skill, and ticket docs ([107e3e9](https://github.com/asieduernest12/ClawDroid/commit/107e3e9c3365ee2a0fbd72d29cc44a97eddfcb04))
* **ticket-010:** agent chat + CLI terminal + provider/model switching ([84a5806](https://github.com/asieduernest12/ClawDroid/commit/84a580636f47d51f1a26e8e2b651e7337023a5a8))
* **ticket-013:** full-history chat sessions + thinking stream + tool calls + deploy workflow ([ac2a189](https://github.com/asieduernest12/ClawDroid/commit/ac2a189974688230a7f6955c0161af2b0a018d23))


### Bug Fixes

* **p0:** Mission Control showing raw HTML — MIME sent as text/plain ([99f7f39](https://github.com/asieduernest12/ClawDroid/commit/99f7f39f97a2b67bc6f7c626a9313bd064bf2276))
* **p0:** picoclaw binary architecture mismatch — multi-arch build + gateway launch ([344f7f3](https://github.com/asieduernest12/ClawDroid/commit/344f7f3e63bfae23e732849397df112d8cff672c))
* **ticket-010:** AgentChatActivity crash — BottomSheet must be direct child of CoordinatorLayout ([ed0e131](https://github.com/asieduernest12/ClawDroid/commit/ed0e13121dc890dc1c61326108d8d19bea021bf5))
* **ticket-012:** mark all tasks and acceptance criteria complete ([4d036be](https://github.com/asieduernest12/ClawDroid/commit/4d036be45636a722349349d308cebb05837b5970))
