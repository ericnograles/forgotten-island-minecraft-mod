# CurseForge publishing

The listing's player guide lives in `description.html`; project fields live in `project.json`, and the original square icon lives in `../branding/curseforge-icon.png`. The player guide includes installation, all companions, controls, travel, bosses, crafting, growth, multiplayer, credits, and the unofficial fan-project notice. Keep its version-specific walkthrough/reference links current when preparing a release.

## One-time setup

1. Create the Minecraft **Mods** project in the [author dashboard](https://authors.curseforge.com/), using the prepared name, summary, categories, icon, guide, source/issues/wiki links and the complete repository license. Do not mark the project experimental.
2. Set repository Actions variable `CURSEFORGE_PROJECT_ID` to its numeric ID.
3. Create a CurseForge **author upload API token**, then store it directly as repository Actions secret `CURSEFORGE_TOKEN`. Do not put it in source, chat, release notes, or workflow inputs. A CurseForge Core API key is a different credential.
4. Run **Publish release to CurseForge** manually with the verified release tag and **dry_run enabled**. Then run with dry_run disabled to submit the existing, verified JAR. Complete any project submission step shown by the author dashboard. CurseForge moderation remains a separate process.
5. Run **Sync CurseForge player guide** manually and inspect the saved guide in the dashboard. Only if the endpoint works and the content matches, set repository variable `CURSEFORGE_DESCRIPTION_SYNC_ENABLED` to `true`.

The description workflow uses an **undocumented** `update-project` endpoint. The [official upload API](https://support.curseforge.com/support/solutions/articles/9000197321-curseforge-upload-api) documents file upload/update, not project-description updates. The initial listing must contain the full guide regardless of this probe's result. If unsupported, leave automatic description sync disabled and paste the rendered HTML into the dashboard editor whenever the guide changes. The Wiki link still points to the current GitHub reference.

## Future releases

1. Update the mod version, changelog, player guide, and compatibility settings if needed. Merge the reviewed changes to `main`.
2. Create a **draft GitHub release**. Attach the tested `forgotten-island-VERSION.jar` and `SHA256SUMS.txt` before publishing it. The checksum file must contain one ordinary `sha256sum` line for that JAR filename. Attach the walkthrough separately if desired.
3. Publish the GitHub release. The `release.published` event submits the exact attached JAR to CurseForge, with GitHub release notes as its changelog. Prerelease → Beta; regular release → Release. A tag push by itself does not publish a CurseForge file.
4. Check the Actions run and CurseForge moderation status. Successful upload is not the same as public approval. Betas require users to enable Beta files in the CurseForge app.

The publishing workflow checks the SHA-256, packaged mod ID/version, Minecraft/Forge ranges, and client/server compatibility. It never rebuilds the release. A successful upload adds `curseforge-publication.json` to the GitHub release, binding the CurseForge file ID to the exact JAR checksum and project. Reruns skip an already-recorded upload. Releases are serialized per tag; different tags may publish independently.

If upload succeeds but recording the receipt fails, **do not blindly rerun the upload**. Find the file ID in the action output/dashboard, verify its filename and checksum, then run `release.py prepare` followed by `release.py record` with that file ID. The script reads `GH_TOKEN`, `GITHUB_REPOSITORY`, `RELEASE_TAG`, `CURSEFORGE_PROJECT_ID`, and (for record only) `CURSEFORGE_FILE_ID` from the environment. This also records a file uploaded manually through the dashboard. A request timeout can leave upload status uncertain; inspect the dashboard before retrying.

If another workflow creates a release with `GITHUB_TOKEN`, GitHub normally suppresses downstream event-triggered workflow runs. Explicitly dispatch this publishing workflow from that release workflow, or publish the draft in GitHub's UI/with an authorized user token. See [GitHub release events](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#release).

## Verification

Run `python3 -m unittest discover -s tools/publishing -p 'test_*.py' -v` for the checksum, version, compatibility, and publication-receipt safeguards. `workflow_dispatch` with dry_run verifies the real GitHub release and makes no CurseForge upload. Publishing actions are pinned to immutable commits; update those pins deliberately.

## First submission

On November 22, 2026, GitHub Actions uploaded `forgotten-island-0.5.0.jar` as a Beta for Minecraft 1.20.1 on Forge. The receipt identifies project `1825044`, file `9763079`, and the SHA-256 of the GitHub release JAR. [Publishing run](https://github.com/ericnograles/forgotten-island-minecraft-mod/actions/runs/45083293596) · [Publication receipt](https://github.com/ericnograles/forgotten-island-minecraft-mod/releases/download/v0.5.0/curseforge-publication.json)

At submission, the author dashboard showed the file undergoing its malware scan and the new project awaiting moderator approval. The project uses CurseForge-only distribution. [Author file status](https://authors.curseforge.com/#/projects/1825044/files/9763079) · [Public listing after approval](https://www.curseforge.com/minecraft/mc-mods/forgotten-island-wild-companions)

## Moon Pursuit submission

On November 24, 2026, CurseForge accepted the 0.6.0 Beta as file `9800084` for project `1825044`. Its JAR SHA-256 is `4e3e68bcd1b05436d19a941c866069affa45eaf2214fd09db0732930a5f98d25`. The description workflow also submitted the updated player guide. [File receipt](https://github.com/ericnograles/forgotten-island-minecraft-mod/releases/download/v0.6.0/curseforge-publication.json) · [Player-guide sync](https://github.com/ericnograles/forgotten-island-minecraft-mod/actions/runs/45493780393) · [Author file status](https://authors.curseforge.com/#/projects/1825044/files/9800084)
