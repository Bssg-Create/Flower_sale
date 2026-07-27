# AGENTS\.md

Behavioral guidelines to reduce common LLM coding mistakes\. Merge with project\-specific instructions as needed\.

Tradeoff: These guidelines bias toward caution over speed\. For trivial tasks, use judgment\.

## 1\. Think Before Coding

Don't assume\. Don't hide confusion\. Surface tradeoffs\.

Before implementing:

- State your assumptions explicitly\. If uncertain, ask\.

- If multiple interpretations exist, present them \- don't pick silently\.

- If a simpler approach exists, say so\. Push back when warranted\.

- If something is unclear, stop\. Name what's confusing\. Ask\.

## 2\. Simplicity First

Minimum code that solves the problem\. Nothing speculative\.

- No features beyond what was asked\.

- No abstractions for single\-use code\.

- No "flexibility" or "configurability" that wasn't requested\.

- No error handling for impossible scenarios\.

- If you write 200 lines and it could be 50, rewrite it\.

- Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify\.

## 3\. Surgical Changes

Touch only what you must\. Clean up only your own mess\.

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting\.

- Don't refactor things that aren't broken\.

- Match existing style, even if you'd do it differently\.

- If you notice unrelated dead code, mention it \- don't delete it\.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused\.

- Don't remove pre\-existing dead code unless asked\.

The test: Every changed line should trace directly to the user's request\.

## 4\. Goal\-Driven Execution

Define success criteria\. Loop until verified\.

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"

- "Fix the bug" → "Write a test that reproduces it, then make it pass"

- "Refactor X" → "Ensure tests pass before and after"

For multi\-step tasks, state a brief plan:

- 1\. \[Step\] → verify: \[check\]

- 2\. \[Step\] → verify: \[check\]

- 3\. \[Step\] → verify: \[check\]

Strong success criteria let you loop independently\. Weak criteria \("make it work"\) require constant clarification\.

## 5\. Git Commit \& Push Discipline

After every completed code modification, feature addition, bug fix, or code adjustment, perform `git add` → `git commit` → `git push` to preserve rollback availability.

Before performing the first Git push for a project, ask the user to provide or confirm:

- The remote repository URL.
- The target branch name.
- Whether the current local project should be connected to that repository.

Do not assume, hardcode, reuse, or automatically select a repository URL from another project or previous conversation. Do not change the existing Git remote or push any changes until the user has explicitly confirmed the repository and target branch.

Once the repository and branch have been confirmed for the current project, continue using them for subsequent Git operations unless the user provides different instructions.

## 6\. Solution\-First Approval

Whenever you propose new ideas, technical approaches, or implementation plans, explicitly present the complete plan to the user first\. Do not execute any solution or make arbitrary technical choices without explicit user approval\.

## 7\. Pre\-Installation Notification \& Approval

Before installing any tools, libraries, or project dependencies, first report the **function** and **estimated size** of the target resource, then request user permission\. No installations are allowed without prior approval\.

## 8\. Pre\-Session\-End Context Update

Before closing each conversation session, update the root `CONTEXT.md` file\. Record current project progress, completed work items, and pending next steps\. This prevents context loss from window resets and token limit overruns\.

## 9\. Independent Self\-Testing

Upon completing any feature or project iteration, conduct full self\-testing independently\. Fix all errors and exceptions until the program runs stably and fully meets requirements before final delivery\.

### 10. Chinese Output for Read & Invocation Results

After reading data or obtaining output from function/API calls, commands, or program execution, **present all output content in Chinese**.

1. If the original output is in English (e.g., error messages, logs), provide both the **original text and accurate Chinese translation** with correct technical terminology.
2. If the original output is already in Chinese, display it directly while keeping the original format intact.
3. Clearly label the source of the output (e.g., *API call output*, *script runtime log*) for traceability.
4. Retain original formats for code, file paths, values and symbols; only translate descriptive text.

## 11. Strict Git Privacy Protection

Avoid committing or pushing any private and sensitive information during all Git operations. Never submit API keys, account passwords, tokens, private credentials, database secrets or any other confidential project and personal data to the Git repository. Always perform full data desensitization to ensure project security and eliminate the risk of information leakage.

## 12. Temporary Test File Cleanup

Any test files, sample data, generated artifacts, logs, temporary scripts, build outputs, or other files created solely for the current test and no longer needed afterward must be deleted immediately after testing is complete. Before deletion, ensure that the files are not required for normal project operation, future development, or user data retention.


## 13. New Conversation Handoff Command

When the user sends the exact instruction `开启新对话`, treat it as a command to prepare a complete handoff for a new conversation.

Before responding to that instruction:

1. Update the root `CONTEXT.md` file with the latest project status, completed work, important decisions, current configuration, relevant file paths, unresolved issues, and clear next steps.
2. Ensure that all information required to continue the project in a new conversation is preserved, and remove or correct any outdated context when necessary.
3. After updating `CONTEXT.md`, provide the user with a complete, ready-to-copy prompt for starting the new conversation. The prompt must summarize the project background, current progress, confirmed requirements, important constraints, and the exact next task to continue.
4. Do not provide only a conversational summary. The `CONTEXT.md` file must be updated first, and the new-conversation prompt must be provided afterward.
5. Unless the user explicitly requests another language, write the new-conversation prompt in the same language currently used by the user.


## 14. Local Availability Check Before Downloads

Before downloading any dependency, driver, tool, runtime, package, model, installer, or other external resource, first check whether a suitable version already exists on the local machine. Reuse an existing local resource whenever possible. If the required resource is not available locally, clearly explain what is missing, why it is needed, its expected source, and its estimated download size, then ask the user whether to proceed with the download. Do not download anything without the user's explicit approval.

These guidelines are working if: fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.