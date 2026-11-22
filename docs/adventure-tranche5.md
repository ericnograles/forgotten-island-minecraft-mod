# Field notes for Forgotten Island 0.5.0

Minecraft Java 1.20.1 · Forge 47.4.10 · Java 17

Use a Homeward Star Necklace on open Overworld ground and jump into the star. You arrive just south of the festival market. Greet the hosts with an empty hand. The east road leads to a junction at **400, 65, 35**. Both bosses are available from your first visit. Bring food, armor, a sword and a pickaxe.

| From the market center at 310, 65, 65 | Follow | Destination | Trail length |
| --- | --- | --- | ---: |
| Memory Hollow | Southern fork, through the low tunnel | Batiba at 475, 67, 199. The chamber center is at Z 190. | 277 blocks to the chamber |
| Twilight Summit | Northern fork, up the lantern-lined pass | Manang at 535, 105, −215 | 391 blocks |
| Moonbreak Coast | East past the stone sea arch, then north from the overlook | Bakunawa at 820, 65, −65 | 593 blocks |

Five seconds of forward input on a level test track moved a walking player 21.37 blocks and an adult ridden Bakunawa 70.95 blocks. In level flight the mount covered 53.40 blocks. At those measured rates, the signed trails to the hollow, summit and coast take roughly **65 / 92 / 139 seconds on foot**, or **20 / 28 / 42 seconds riding**. A direct flight from the market takes about **19 / 34 / 49 seconds** respectively. These estimates exclude turns, climbing, starting and landing. Ground riding won the straight-track test. Flying lets the rider cut across bends and ridges.

The star at **310, 64, 99** takes you back to your own Overworld departure. From either island dimension, use `/island harbor` to visit the original district and `/island market` to return to the highlands. The guide crafted from a book and feather also includes recipes, companion controls and arena directions.

## Mountain country

The coast bounds run from **X −49,696 through 50,303** and **Z −39,936 through 40,063**, inclusive. The nominal center is 303.5, 63.5. Ocean continues beyond the perimeter. A saltwater inlet reaches inland to Moonbreak Coast. Ridges carry jungle trees, spruce, exposed stone and snow. River valleys cut across the slopes. Look for water sources spilling over ledges, cave mouths and mossy arches hung with parol lanterns. The market has 16 stalls, a stage and 24 resident slots. Batiba has a separate home in the hollow.

Buildable Y runs from 0 through 319. Water tops out at 62 in the open sea. Before choosing that height we sampled **337,610** terrain columns at 157- and 151-block intervals. The lowest sampled terrain surface was 38 and the highest was 260. The pass took about 31 ms in the isolated Java height-function prototype. It excluded decoration, lighting and disk I/O. This leaves headroom for trees, flight and building. The horizon and neighboring peaks are visible with an eight-chunk render distance.

Each explored chunk computes its terrain from continuous world coordinates. The island geography repeats across saves. Trees share the same center calculation on both sides of a chunk border. Only the central 38 × 30-chunk district records settlement installation. Wilderness generation creates ordinary Minecraft chunk files with no mod-wide visited-chunk list. The 31,250,000-chunk footprint describes the bounds. See the measured sample below for actual disk use.

## Fighting at Moonbreak Coast

Enter from the southern overlook facing north. Spray and a rising head mark the water breach, followed by a roar. The center of the lit beach is the melee position. Dodge the warning, listen for the exhaustion cue, then approach the jaw from the side. The snout, head and nine body regions follow the moving animal. Strikes on the hide also count outside recovery. Guard the opening when the golden ward is up.

| Bakunawa | First form | Storm form |
| --- | ---: | ---: |
| Solo health | 400 HP | 500 HP, refilled at transformation |
| Two players present at awakening | 600 HP | 750 HP |
| Three or more at awakening | 800 HP | 1,000 HP |
| Attack cycle at 20 TPS | 11 seconds | 9.25 seconds |
| Recovery after a standard attack | 7 seconds | 5.25 seconds |
| Recovery after Fireballs / Golden Ward / Atomic Breath | 6.4 / 5 / 4.5 seconds | 4.65 / 3.25 / 2.75 seconds |

Allow for the final 0.8 seconds of each window as the neck begins to rise. Late arrivals can participate without raising the current health bar. The first form cycles through Chomp, Tentacles, Holy Beam, Moon Rocks, Lunar Tide, Eclipse Breath, Fireballs, Atomic Breath, Golden Ward, Tail Whip and Horn Slash. Holy Beam deals 10 HP on an unblocked discharge. Cover interrupts the beam. The ward stops damage for five seconds. The storm form opens with two additional moves before returning to the original cycle.

- **Lightning Strike.** Three gold circles show where the rain of bolts will land. The strikes arrive at ticks 52, 60 and 68. A player in range can take one 9-HP hit across the three bolts.
- **Lightning Breath.** A thin charged line shows the aim. Step sideways or take cover before the discharge at tick 60. One intersecting hit deals 8 HP before mitigation.
- **Chomp.** Leave the marked circle. If swallowed, follow the rising floor and break all three luminous Memory Crystals. Everyone swallowed by that same encounter escapes together. Another boss has its own room and objectives. Reconnecting restores membership or ejects you if the attempt ended. Use `/island home` for emergency recovery.

For incoming weapon damage through 8 HP, the boss uses the original value. Above 8, the effective hit is `8 + 3 × ln(1 + (raw − 8) / 8)`. Bakunawa takes 60% of that hit against ordinary hide and 150% during exhaustion. Storm form multiplies incoming damage by a further 0.85. Vanilla attack charge and damage immunity still apply. An unenchanted, fully charged iron sword deals about 9 HP during first-form recovery and 7.65 HP in storm form. A Crystal Sword deals about 41.8 and 35.5 respectively. Crystal, Sapphire and Phoenix keep their normal listed damage and abilities against ordinary mobs.

The ordinary idle pose spans about 72 blocks from the snout to the end of the spine. The corresponding storm-form span is about 85. At the lowered perch the snout reaches the beach, opening the span to roughly 96 and 107 blocks respectively. These are longitudinal rig measurements rounded to whole blocks. The neck, tail sweep and wings change the silhouette during combat. One tracked actor owns eleven moving hit regions. Its center remains at the arena, with a render test that includes the offshore tail. A radius-two region ticket is limited to the encounter and expires after 40 unrefreshed ticks.

The first bar ends in a three-second transformation. The storm form grows, unfolds its wings and gains a black hide with yellow markings and lightning. Its final ten-second scene lifts the articulated body out of the water, separates it at the spine and bursts into light. The southern reward dais opens afterward. Each island participant receives a personal Crystal Sword and Bakunawa Skull. The return star and recorded reward claims survive a restart. The bolt and explosion visuals leave blocks intact.

The camera and effects can be cycled with **O**. **K** skips the current local scene. Full uses the moving camera and ornamental lightning. Reduced and Off retain the shared creature animation and the attack warnings. Other players keep their own settings.

## Manang at Twilight Summit

Manang has one 220-HP solo health bar with the same starting-party multiplier. At half health her combat speeds up. Dodge Shadow Dive, jump Shriek's spreading ring and get behind Wing Cut. Hit the lower body when the wings fold. Victory awards Manang Wings. The friendly companion retains her own size, owner, abilities and spawn egg.

Both arenas respond to Creative, Adventure and Survival players. A Creative-only visit plays the introduction once and holds the presentation. Switch to Survival inside the arena to start fighting. Spectators can watch alongside a participant. Leaving an unfinished combat or entrance empty for ten seconds resets the island attempt and removes an egg encounter. When the server is empty, it releases the encounter ticket and preserves the fight for reconnection.

## Trying the four boss eggs

Find the hostile eggs in the Forgotten Island Creative tab. The **Bakunawa Boss Spawn Egg** runs both bars. The **Phase Two Bakunawa Boss Spawn Egg** starts with the winged lightning form. Aim at the water surface with a deep, open lane extending north and space to fight to the south. The arena center is 31 blocks south of the clicked water. The tested breach lane needs eight blocks of clear water. A wide bay with a southern shore gives the best view and footing.

The **Manang Boss Spawn Egg** plays the normal entrance. The **Separated Manang Boss Spawn Egg** starts the waist-separated test variant. Choose a level 19 × 19 area with open sky. All four work in the Overworld, highlands and original island. An obstruction, another arena or a full set of twelve test arenas produces feedback and preserves the egg. A successful egg fight grants no campaign reward. The horned incubating Bakunawa egg and the friendly Manang companion egg still produce companions.

## Upgrading a 0.4 world

1. Save and close Minecraft or stop the dedicated server. Back up the whole world, including player data and dimensions.
2. Replace the old mod JAR with 0.5.0 on the server and each client. Launch using the same Minecraft and Forge versions.
3. Reuse an existing portal or necklace. The new destination is the highlands. Use `/island harbor` to revisit old buildings and residents in their original dimension.

The migration keeps the original dimension ID and chunk files. The older generator can finish an untouched harbor chunk when first visited. Already-built districts keep their journals. Existing non-baseline columns in the legacy district are protected from first-time construction. Player inventories, friendly egg blocks, ownership, growth and portal/return records retain their original IDs. Completed encounters stay completed in the old district. A pending old victory can finish there. An interrupted pre-victory 0.4 attempt resets to the new encounter and a traveler already swallowed by the old fight is recovered on login. The historical reward journal prevents a second payout.

## Visual references and original interpretation

The personal sketches supplied the market basin, monumental serpent, storm palette, tired posture, breach and split. We inspected the studio key art and Universal poster for the tropical shoreline, palms and magenta/teal lighting. In the official trailer, 1:14 shows bright beach foliage and blue water, 2:03 shows a warmly lit crossing under hanging vegetation, and 2:28 shows a dark winged figure against a jagged skyline. The map coordinates, mountain shapes, lantern routes and boss attacks are original game design. [DreamWorks key art](https://www.dreamworks.com/storage/cms-uploads/forgotten-island-hero-image-desktop.jpg) · [Universal poster](https://dx35vtwkllhj9.cloudfront.net/universal-pictures-cms/forgotten-island/images/regions/us/onesheet.jpg) · [Official trailer](https://www.youtube.com/watch?v=f7mFVeWnVLw)

The DreamWorks-supplied Manananggal close-up shows a blue-violet woman-like face, angled cheeks and chin, a strong nose bridge, arched brows, yellow eyes with dark vertical pupils, upper and lower fangs, raised dark hair, pointed sides and long framing locks. Pink light traces the edges. The original Minecraft rig uses separate brows, eyes, jaw, teeth, cheek planes, hair joints and wing joints to make those features move and read at block scale. The same head design appears on the boss and companion. The image and selected trailer frames supplied the observed angles. All shipped game meshes, textures and sounds are original. [Vogue Philippines gallery](https://vogue.ph/lifestyle/entertainment/the-mythical-creatures-of-forgotten-island/) · [Studio-supplied close-up](https://assets.vogue.ph/wp-content/uploads/2026/09/10E66_FP_1124402U_00521695-copy.webp)

## Release verification

The installable JAR is **1,803,135 bytes**. Its SHA-256 is `d746f7719067c3c02925e3da549812bb877da4447f619b9f121967797b165cf0`. The same bytes ran on the integrated client and on both ends of the dedicated-server tests. The operator input helper used for QA is outside the distributed JAR. [Artifact manifest](evidence/tranche5/artifact.json)

| Evidence | What was verified |
| --- | --- |
| Automated build and regression tests | The offline Java 17 build and reobfuscation passed. All **15 JUnit tests**, **41 Forge GameTests**, **six release-verification tests** and **both asset validators** passed. Terrain tests sampled negative coordinates, chunk boundaries, sea and mountain heights. Server tests exercised obstructed water, generated reward sites, direct hits with four swords on both forms, strike/breath hit limits, egg rejection, isolated interiors, migration and once-only rewards. [Test record](evidence/tranche5/checks.txt) |
| Assisted single-player, shipping JAR | Created a new Creative world, entered both arenas, then switched to Survival in place. Direct long-distance teleports to Manang and Bakunawa took **189 ms and 304 ms** respectively from command dispatch to first client tick with the tracked, animating boss. A charged Crystal Sword completed both bars. The saved campaign showed two delivered and two collected items after the finale. [Playthroughs](evidence/tranche5/playthroughs.json) |
| Assisted dedicated play, shipping JAR | Two clients saw the same **330-HP Manang**. One stayed in that fight while the other started a separate **500-HP Bakunawa** in the Overworld. All four eggs entered their requested form. Egg use to the first tracked client tick took **37–54 ms**. Two players were swallowed by one Chomp, shared three crystals and returned to the same Overworld arena. Its egg victory left the saved island completion and personal claims intact. [Eggs and Creative entry](evidence/tranche5/boss-eggs.jpg) · [Encounter records](evidence/tranche5/playthroughs.json) |
| Assisted approach, balance and restart checks before the last visual revisions | From just outside the boundary, Manang responded **253 ms** after walking input and Bakunawa **496 ms** after forward input on a flying mount. A player with a plain iron sword completed both bars using charged attacks and assisted healing. A dedicated restart preserved first-form combat and a swallowed guest. Another clean stop saved the transformation at tick 2 and rejoined it at tick 11 with 500 HP. A damaged storm form reloaded at 358.41 HP. During that last reconnect an eligible fighter was absent for over ten seconds, so the normal retreat reset started a new attempt. [Checkpoint and combat records](evidence/tranche5/playthroughs.json) |
| Upgraded 0.4 save, shipping JAR | Loaded a copy in single-player, visited the old harbor and used the market route. The adult mounted Bakunawa retained its UUID, owner and age after dismounting. Both players' inventories and ender inventories matched, including the three occupied inventory stacks. The completed boss and all four historical reward tokens survived. Of 962 previously full legacy chunks, 961 retained identical decoded block states. One previously unbuilt ocean chunk acquired its original harbor structures. A separate 0.4 save retained all 480 blocks of a player-built platform, its necklace portal, its owner’s return link and a placed friendly Bakunawa egg. The egg kept its owner and advanced from 101 to 581 incubation ticks. The same portal delivered a second player to the highlands. In two additional archived fixtures, logging in safely returned the swallowed player to the legacy coast. The linked fixture also kept both individual departures. [Offline save comparison](evidence/tranche5/upgrade.json) |
| Unassisted human play | No unassisted human run was recorded. QA used supplied gear, operator travel and healing, camera positioning, automated charged attacks and a Haste-assisted pickaxe. The saved health transitions and crystal breaks came from the game server. The runs establish mechanical completion. Difficulty, pacing and readability for a new player still need human feedback. |

### Performance on the test Mac

Apple M1 Pro, 10 CPU cores, 32 GB unified memory, macOS 26.5.2, Java 17.0.15. Client images were 1708 × 960 with Fancy graphics, eight-chunk render distance, five-chunk simulation distance, VSync off, a 60-FPS cap and no shader pack. Each client and the dedicated server used a 2 GiB maximum Java heap. CPU, GPU and disk were shared by the local processes.

For the exploration comparison, each version created a fresh Creative single-player world with seed 2070, entered the market and visited six identical X/Z coordinates. Stops included the market, wilderness and eastern edge. The old dimension is mostly ocean at those distant coordinates. We saved and closed each world before taking the baseline and ending Anvil file measurements. Time-to-arrival ends when the client receives the new player position. Background chunk generation can continue afterward. [Measurements and coordinates](evidence/tranche5/measurements.json)

| Measurement | 0.4.0 | 0.5.0 |
| --- | ---: | ---: |
| New single-player world to first playable client tick | 18.35 s | 24.53 s |
| First market teleport | 0.631 s | 0.526 s |
| Six exploration teleports, individual range | 0.415–0.734 s | 0.307–0.509 s |
| Client FPS, median (range) | 55 (45–57) | 56 (50–56) |
| Integrated server mean tick, median (sample range) | 6.00 ms (1.39–35.47) | 5.80 ms (1.13–30.57) |
| Combined client and integrated-server used heap, median (range) | 1,098 MiB (709–1,468) | 973 MiB (634–1,280) |
| Island terrain region-file growth | 31.19 MiB | 31.88 MiB |
| Added Anvil entries including partial chunks | 7,939 | 8,119 |

On the dedicated server, 13 storm-form samples spanning Lightning Strike and Lightning Breath measured **55–56 FPS**, **1.53–1.82 ms mean server tick** and **662–1,060 MiB used server heap**. The median tick was **1.69 ms**. In a separate two-client storm interval, whole-world Anvil files grew **0.87 MiB** between ticks 12 and 237 and the compressed adventure journal went from 2,230 to 2,088 bytes. Other loaded chunks also contribute to those writes. The shipping server reported **3.864 s** to prepare an existing world. For historical context the 0.4 dedicated log reported **2.948 s** at startup and **3.361 ms / 20 TPS after its encounter**. The earlier version has no lightning-form workload. [Storm measurements](evidence/tranche5/measurements.json) · [0.4 dedicated observations](adventure-tranche4.md)

These are short local samples. The FPS cap limits comparisons between fast scenes. Travel across an entire region, high-latency multiplayer, larger groups, modpacks and lower-end hardware were outside this run. The eight-chunk horizon also hides distant ridges in some valleys. At arena-entry distance Manang's outline and eyes read first. Her brow shape, pupils, fangs and layered locks are easiest to distinguish at roughly six to eight blocks for the boss and three to four for the companion.

### Visual evidence

- [Market basin, forested cliffs, Memory Hollow and the coast](evidence/tranche5/highlands.jpg)
- [Water breach, recovery, storm form, lightning attacks, split and reward star](evidence/tranche5/bakunawa.jpg)
- [Manang's joined boss, separated boss and friendly companion from three angles](evidence/tranche5/manang.jpg)
- [Four egg encounters and Creative-to-Survival entry](evidence/tranche5/boss-eggs.jpg)

The image index identifies each source screenshot, hash and crop. Front portraits preserve the full game frame. [Capture index](evidence/tranche5/captures.json)
