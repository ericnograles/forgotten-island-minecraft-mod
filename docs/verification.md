# Verification ledger

Status: **full identified roster implemented and tested: thirteen creatures, including mandatory Bakunawa**. Verification date: 2026-11-21. The Flaming Aswang Chef uses the original gameplay design explicitly authorized by the user; its film likeness is not claimed. Sarangay follows primary production concept art. See [the research inventory](creature-roster.md) for sources, confidence and interpretation choices.

## Build and automated evidence

Test environment: macOS arm64, Apple M1 Pro, OpenJDK 17.0.15, Minecraft Java 1.20.1, Forge 47.4.10, Gradle 8.8 and ForgeGradle 6.0.54. There is no additional runtime mod dependency.

- `./gradlew --gradle-user-home .gradle-home build runGameTestServer`: passed at 09:31:06 local time. **Three JUnit tests and all seven Forge GameTests passed.**
- `.tools-venv/bin/python tools/validate_assets.py`: passed for **13 species, 199 bones and 420 model boxes**. Validates editable/generated mesh agreement, parents, atlas bounds, textures, eggs, translations and mono 22,050 Hz OGG calls.
- A second `jar --rerun-tasks` build recompiled and reobfuscated the final mod. Its JAR was byte-for-byte identical to the first build. SHA-256 of `forgotten-island-0.1.0.jar`: `cb5ecdaccb1654e08757a5711f8669c02d2e7732f8ccbf989ea44bb1117eb0b9`.

Reproducibility applies to building the checked-in assets with the pinned toolchain. Regenerating OGG assets may change their container metadata; byte-identical asset regeneration is not claimed.

[Final build, server, unload and performance log excerpts](evidence/full-roster-checks.txt) record the checks below.

| Automated check | Coverage |
|---|---|
| JUnit companion rules | Stranger/range/invalid-number authorization, gifts from different players, malformed saved-duration bounds |
| `friendshipPersistenceAndRequests` | All 13 real entity factories; three gifts; ownership; stranger refusal; targetless abilities; cooldown refusal; name/owner/friendship/command/variant/timer NBT round trips; allied-owner protection; Sarangay's four absorption hearts |
| `chefWardRespectsAlliesAndExpires` | Chef fire immunity; allied creature extinguished and protected for 200 ticks; another owner's companion unaffected; cooldown refusal; animation/buff expiry; nearby wool remains intact with no placed fire |
| `partialGiftsAndFairyRemedy` | Partial friendship across reload; Zippy removes poison and grants haste |
| `mountsRejectStrangersAndProtectDismounts` | All 3 mounts; stranger refusal; collision-free high-altitude exit; altitude-scaled slow falling |
| `elevatedRidersCannotEnterCeilings` | All 3 mounts; obstructed saddle rejection and elevated rider ceiling clearance |
| `aquaticStayHoldsDepth` | Bakunawa and Mermahn hold position/depth in water for 40 ticks; air remains available |
| `abilitiesExpireAndDoNotChangeWorld` | Overlapping Bakunawa eclipses; one source removed; remaining event expires; world time/weather unchanged |

Runtime testing found and fixed Stay synchronization after NBT reload, server flight-mode transitions and water buoyancy fighting Stay. Regression checks cover those paths. An early dismount test placed its high-altitude fixture underground in the generated test world; the fixture was corrected, while a separate ceiling fixture retained the obstruction check.

## Dedicated server and multiplayer

The installable, reobfuscated JAR was copied into a **standard Forge 47.4.10 server installation**, launched through Forge's generated `unix_args.txt`. Startup completed successfully and both clients joined. This exercises production server class loading, not only ForgeGradle's development server.

The original two native clients used matching development classes/resources and separate local directories, with test identities Dev and Observer. Final verification used **two production clients, each with the packaged thirteen-creature JAR as its only mod**, as detailed below. The test server used loopback-only `127.0.0.1:25585`, offline test identities, an isolated flat world and explicit user EULA approval. Neither player had operator privileges. These QA settings are not public-server installation recommendations.

Each included creature was spawned with its actual Creative egg and befriended through client gift/wand interactions. Owner ability requests, transformations and effects were observed with the second client. A foreign client's wand/ability request was refused. Single-player showcase, Creative tab, Bakunawa eclipse, roar and flight were also inspected.

| Creature | Observed runtime behavior and evidence |
|---|---|
| Bakunawa | Segmented body motion, roar and bounded eclipse; second client sees eclipse and rider. Actual flight ascent, steering, descent, landing and dismount. Survival underwater swimming supplied air; surfacing switched to flight and descending into water switched back. Low ceiling stopped upward movement with full health. Underwater dismount preserved health and applied water breathing. [Flight](evidence/multiplayer-bakunawa-flight.png), [eclipse](evidence/multiplayer-eclipse.png), [swimming](evidence/bakunawa-underwater.png), [ceiling](evidence/bakunawa-ceiling-clearance.png), [dismount](evidence/bakunawa-underwater-dismount.png). |
| Raww | Upright and visibly quadruped forms; pack howl, speed and reveal pulse. [Transformation](evidence/raww-transformation.png). |
| Manang | Upper/lower separation, wing motion and warding ability. [Separation](evidence/manang-separation.png). |
| Sarimanok | Scarlet wings, folded idle pose and wingbeat pulse; actual ascent, forward flight, descent, landing and dismount. Revised saddle/rider alignment inspected by Observer. [Wingbeat](evidence/sarimanok-wingbeat.png), [saddle in flight](evidence/sarimanok-saddle-flight.png). |
| Datu | Root-guard pulse; actual ground steering and dismount; shoulder seat inspected by Observer. [Ability](evidence/datu-root-guard.png), [rider position](evidence/datu-shoulder-seat.png). |
| Duwende | Mushroom jubilee; King Truffle Wuffle and warrior appearance/label switching. [Jubilee](evidence/duwende-jubilee.png), [warrior](evidence/duwende-warrior.png). |
| Bungi | Three eyes, single tooth, arm/body stomp and knockback pulse. Corrected wrap surfaces inspected without the earlier flicker. [Stomp](evidence/bungi-stomp.png). |
| Batiba | Enlarged tarsier eyes, patterned dress, animated memory motes and night-vision effect. [Memory bloom](evidence/batiba-memory-bloom.png). |
| Mermahn | Tidal serenade and water buffs; underwater follow moved toward the owner, and Stay held depth. Wet appearance and face inspected from the second client. [Serenade](evidence/mermahn-serenade.png), [underwater](evidence/mermahn-underwater.png). |
| Zippy | Three sugar gifts, Stay/Passive controls, wing flutter and remedy pulse seen remotely. Follow took flight toward the moving owner. A vanilla name tag renamed her Lumi; the name, commands and working owner ability survived restart. [Remedy](evidence/zippy-remedy.png), [named follow](evidence/zippy-named-follow.png), [after restart](evidence/zippy-after-server-restart.png). |
| Tikbalang | Three apple gifts, command controls, moving gait/follow and misdirection dance seen remotely. [Dance](evidence/tikbalang-dance.png). |
| Sarangay | Three amethyst gifts, Follow/Stay and Passive controls, heavy body/arm motion, jewel aegis and purple particles seen remotely. Survival HUD visibly gained four gold absorption hearts. [Aegis](evidence/sarangay-aegis.png), [four hearts](evidence/sarangay-survival-shield.png). |
| Flaming Aswang Chef | Actual Creative egg, three glowstone gifts, swaying flame crown, pan/utensil poses, owner-follow movement and Stay/Passive controls. Vanilla name tag renamed it Ember. R triggered stirring/serving, flame/steam particles and the owner's fire-resistance icon; the other production client saw the same animation. Effects expired and ordinary R worked again after restart. Foreign wand control was refused. [Commands](evidence/chef-commands.png), [follow](evidence/chef-follow.png), [supper](evidence/chef-sizzling-supper.png), [remote ability](evidence/chef-multiplayer-supper.png), [stranger refusal](evidence/chef-stranger-refused.png), [after restart](evidence/chef-after-server-restart.png). |

The final [Creative tab](evidence/full-roster-creative-tab.png) contains thirteen eggs and three tools. Guide pages were inspected for layout, including [the chef's page 13/13 and original-design label](evidence/guide-chef.png), [Sarangay](evidence/guide-sarangay.png) and [Datu's ground controls](evidence/guide-datu-controls.png). Screenshots show observed states; continuous movement and timing observations also came from live client playback.

## Installed-JAR client verification

The official Forge installer successfully created an isolated 1.20.1/47.4.10 client installation. The client launched through Forge's production `forgeclient` target using the generated profile arguments, official libraries verified against their metadata hashes, and the packaged JAR as its **only mod**. No development classes or `MOD_CLASSES` were supplied. A small local native wrapper invoked Java's launcher for macOS window access; the Minecraft Launcher application's login/profile UI was not exercised.

At 09:21–09:24, before the chef was added, the production client displayed the [mod/version entry](evidence/installed-client-mod-list.png), [twelve eggs and three tools](evidence/installed-client-creative-tab.png), and the [field guide](evidence/installed-client-guide.png). In a fresh Creative superflat world, the documented `/island showcase` command created those twelve companions. Ordinary R-key requests activated Raww's transformation/speed and [Bakunawa's eclipse](evidence/installed-client-eclipse.png); normal presentation returned after the eclipse expired. The world saved cleanly.

At 09:25:03, that client joined the standard dedicated server using the same then-current packaged JAR on both sides. Lumi's saved name and Stay/Passive commands appeared, and an ordinary R request activated [her remedy and haste effect](evidence/installed-client-server-ability.png).

At 09:35–09:47, the final thirteen-creature JAR ran on the standard server and **both production clients simultaneously**. All three installed JAR hashes matched the final SHA-256 above. The chef's egg, gifts, controls, custom name, guide and ability were exercised through the actual client UI. The remote client saw its active stirring pose and particles and was refused control of someone else's companion. Both clients reconnected after a clean server restart; Ember retained its name and commands and responded to its owner's R key at 09:47. This completes the packaged-JAR multiplayer check in addition to the earlier per-creature and mount tests.

## Persistence and performance

All thirteen companions were tagged in the isolated QA world, set to Stay and moved outside spawn chunks. Moving both production clients away produced `QA_THIRTEEN_UNLOADED` at 09:45:18, confirming no tagged entities remained loaded. On returning, thirteen freshly read records matched their prior UUID, owner, gift giver, friendship, commands, variant, custom name, position and persistence flag. After saving, cleanly stopping and restarting the standard server, the same comparison passed again at 09:46. A normal owner R request then activated Ember's supper. [Before/after records](evidence/persistence/) preserve the thirteen-creature comparisons and the parser used to compare them.

With thirteen creatures loaded and both production clients connected, `forge tps` reported **20.000 TPS**, 2.466 ms mean overworld tick time and 3.125 ms overall mean tick time at 09:44:53. An earlier eleven-creature/two-client view showed approximately 47–50 FPS on the M1 Pro at 1708×960, six-chunk rendering and Fancy graphics. [Client sample](evidence/mixed-group-performance.png). These are short local observations, not capacity benchmarks or performance guarantees on other hardware.

## Repeating the important checks

1. Build and run the commands above with Java 17. The GameTest server needs no interactive client.
2. Install the JAR on a Forge server and matching clients. In an empty Creative test world, use the README's `/island showcase` sequence, or befriend individual eggs with three gifts.
3. Let a second player observe transformations, abilities and riders. Have that player try to control someone else's companion; ownership should be refused.
4. Test each mount in open space, below a low ceiling and over water. Use Space/C for flying or swimming movement and Sneak to exit. Ground Datu uses WASD.
5. Name a companion with a vanilla name tag; change its commands and a supported variant. Move both players well beyond simulation distance, return, then save/restart and reconnect. Confirm the name, ownership and commands remain and an owner ability still works.

Server console commands positioned fixtures and camera viewpoints; item interactions, ability keys and movement were performed in the actual clients. Test-only Sneak was rebound to V for native automation; distributed controls remain vanilla Shift and are rebindable. Teleport warnings caused by fixture positioning are not treated as mount movement failures.

## Remaining limitations

- The flaming aswang chef is an original gameplay interpretation authorized by the user, with no claim of an inspected film likeness. The evidence-bounded inventory does not claim an exhaustive census of every creature in the full film; speculative Lambana/Santelmo labels are not duplicate species.
- Sarangay follows primary concept art, with original adaptations for obscured parts; final-film fidelity is not established. Bakunawa is an explicitly required original folklore adaptation, not a verified reconstruction of the alleged opening silhouette.
- Bakunawa has one practical collision/interaction hull at the front and an articulated visual tail. Aim at the head/body; the tail is not separately damageable. Manang's separated parts remain one entity. There are no orphanable child entities, terrain destruction or uncontrolled fire.
- All thirteen share tested follow/stay/defend and ownership systems. The report does not claim an exhaustive combat, pathfinding, terrain, latency or mod-compatibility matrix. Ordinary movement/ability reactions were inspected; every animation frame was not individually captured.
- Audio files were structurally validated; human listening assessment was not performed. The Minecraft Launcher application UI, other operating systems and public authenticated multiplayer were not tested; the installed-JAR client itself was tested as described above.

The isolated server and clients were stopped after testing. Their local QA worlds remain outside the distributable archives.
