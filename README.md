# Forgotten Island: Wild Companions

The adventure expansion adds a persistent festival island, a buildable floor-star portal, separate Manang and Bakunawa bosses, four skippable scenes, custom life meters and unique rewards. All thirteen original companions remain available.

**Version 0.2.0 is ready for human playtesting.** The build, eight JUnit tests, eighteen Forge GameTests and both asset validators pass. Packaged clients achieved a cooperative Bakunawa victory without wings and a solo Manang victory with supplied, obtainable pre-boss equipment. Crafting, travel, all four scenes, shared and simultaneous meters, individual skipping, interrupted-scene recovery, rewards, gliding, unload/restart persistence and market/eclipse performance have evidence. [Eight continuous recordings](docs/evidence/adventure/clips/README.md) demonstrate the market, portal travel, both meters, all four cinematics and wing gliding; these silent presentation clips use documented setup and console damage.

A complete unassisted Survival acquisition/progression run remains **unverified and deferred to human playtesting**. The fresh-world automated attempt stopped during ordinary Overworld gathering and recovery, before island entry. On 2026-11-21 the project owner closed this implementation phase and assigned the remaining progression check to human testers. See the [human playtest checklist](docs/adventure-acceptance.md#human-playtest-handoff), [requirement audit](docs/adventure-acceptance.md) and [evidence ledger](docs/adventure-development.md). Phase closure does not claim full gameplay acceptance.

An unofficial Minecraft Java mod inspired by DreamWorks' *Forgotten Island* and Philippine folklore. **Thirteen playable creatures** with original models, companion abilities and three rideable creatures. See [the verification ledger](docs/verification.md) for automated tests, single-player and multiplayer evidence, and remaining limits.

The current build includes **thirteen creatures**: Bakunawa, Raww, Manang, Sarimanok, Datu, Duwende, Bungi, Batiba, Prince Mermahn, Zippy the Potion Fairy, Tikbalang, Sarangay and the Flaming Aswang Chef. The full identified roster is implemented. The chef is an explicitly authorized **original gameplay interpretation**, not a claim of film-design accuracy. Sarangay is an adaptation of primary production concept art; comparison with its final-film design remains open. See [the research inventory](docs/creature-roster.md). Bakunawa is a required folklore-inspired addition; its reported film silhouette is unverified.

## Install

Requires **Minecraft Java Edition 1.20.1**, **Java 17**, and **Forge 47.4.10** (the pinned test version). No animation or other mod dependencies are required. The declared Forge range is 47.4.10–47.x; other Forge releases have not been tested.

1. Install the matching [official Forge client](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html).
2. Copy `forgotten-island-0.2.0.jar` from the release archive (or `build/libs` after building the source) into that Minecraft profile's `mods` folder.
3. Launch the Forge profile. Put the same JAR on a Forge server and every connecting client for multiplayer.
4. In Survival, craft the field guide and follow the portal plan below. For the original companion showcase, use the **Forgotten Island** tab on the second Creative-tab page.

## Travel to Forgotten Island

Craft 32 **Parol Stone** in a crafting table:

```text
S C S    S = cobblestone (4)
C A C    C = copper ingot (4)
S C S    A = amethyst shard (1)
```

Lay the following 9 × 10 pattern flat in the Overworld, with its single point facing north (negative Z). `#` is Parol Stone; `C` is one more Parol Stone at the activation center; dots are empty openings. You need 28 stones, so one recipe is enough. Clear two blocks of headroom. The top row is north.

For assisted construction, hold at least 28 Parol Stone in one stack and **crouch-use the top of the ground beneath the intended center**. This places the same north-facing pattern and consumes 28 stones. It requires flat solid ground, a clear footprint and two blocks of headroom; an invalid site consumes nothing. Ordinary use still places individual blocks.

```text
    #
    #
   #.#
###...###
#.......#
 #..C..#
  #...#
  #.#.#
 ### ###
 #     #
```

Use one **glow ink sac** on C. The opening becomes luminous; step onto the frame and jump down through it. Cobblestone, copper, amethyst and glow ink are all obtainable before island travel. The field guide includes this plan, activation troubleshooting and travel instructions. Its existing recipe is a book plus prismarine crystals.

The arrival plaza opens directly into six colorful stalls, parol lanterns and 21 peaceful residents representing eleven companion types. Interact with residents for greetings. The two marked routes lead independently to the northwest twilight arena and northeast lunar arena; either boss can be attempted first with ordinary pre-boss equipment.

The permanent arrival star is always a retreat route. Each player's last departure portal is stored separately, with a five-second travel cooldown. Returns search for solid, dry space near that portal, then near the Overworld world spawn if necessary. Death retains normal Overworld bed/spawn behavior. Island beds and respawn anchors cannot change it. `/island home` is available without operator permission as emergency recovery, including after reconnecting. The new dimension is added to existing worlds; initialized island chunks and player edits are retained.

## Encounters and rewards

Enter an arena in Survival to awaken its boss. Party health scales at awakening for up to four players; late arrivals join without healing the boss. Separate custom meters display shared server health and the current phase. Leave the arena to hide its meter. If everyone retreats or dies, an unfinished encounter resets after ten seconds. Completed bosses stay defeated in that world.

Bring enchanted armor, a sword, food and healing supplies. The verified victories used Protection II diamond armor, a Sharpness III diamond sword, a shield and ordinary golden apples. These are obtainable before either boss; iron-only victory has not been established. Learn the warning patterns and move back into melee reach after knockback.

- **Manang** stands about 4.7 blocks tall while joined. Her upper body separates for Shadow Dive, Shriek and Wing Cut. Leave the marked circle, jump over the expanding ring, or move out of her facing arc. Strike the lower body when her wings fold after an attack. The second half of the fight accelerates her pattern.
- **Bakunawa** has an approximately 92-block articulated body. Three ground-level moon anchors provide melee access; other body regions can also take damage. Avoid Moonfall circles, jump the Lunar Tide and sidestep Eclipse Breath. Anchors turn gold during recovery. At half health the eclipse begins and attacks accelerate. Neither wings nor the Crystal Sword is required to enter or damage it.

**K** skips your current scene. **O** cycles Full, Reduced and Off; these keys can be rebound in Controls. Reduced keeps your own camera and movement. Off disables the camera/title spectacle and eclipse treatment. The world animation and encounter timing stay synchronized. Late arrivals see the remaining scene time. Everyone in the arena is protected during the shared entrance/defeat interval; your skip never skips another player's scene. Esc/menu, death, travel and disconnect restore local camera and controls.

Rewards appear on the south dais only after the defeated scene finishes. Each Survival participant who joined before defeat receives a personal set. Uncollected drops are reserved for their recipient, do not expire and resist damage; they persist through chunk unload/restart. After pickup, normal inventory, chest, death-drop and sharing rules apply. Picked-up rewards are not replaced if subsequently lost.

| Victory reward | Behavior |
| --- | --- |
| Manang Wings | Chest-slot equipment with a custom bat-wing model. Jump again while falling to glide; normal firework boosts work. 648 durability, one wear per flight second; repair with phantom membranes or Mending. At one durability remaining, repair before flying again. |
| Crystal Sword | An ordinary fully charged, unenchanted, noncritical melee hit deals **6,000 pre-mitigation damage**. Vanilla attack-charge/critical and target mitigation rules still apply. Server damage correction handles Minecraft's attack-attribute cap. 4,096 durability; repair with netherite ingots or Mending. |
| Bakunawa Skull | Distinct collectible trophy with original item art. |
| Bakunawa exit | A permanent floor-star on the south dais, immediately usable after victory. Returns each visitor to their own linked Overworld portal or the safe fallback. |

Effects are confined to the arenas and participants. Attacks never set block fire or destroy terrain; the market residents remain peaceful and protected. A full trading economy and quest system are outside this expansion.

## Original companions

Right-click an egg to spawn its creature. Give it three of its favorite gift to befriend it; each creature tells you what it likes when you interact. In Creative, the Showcase Wand befriends instantly and demonstrates its ability on the next use. It cannot take another player's companion.

| Action | Control |
|---|---|
| Open the field guide | Use the Wild Companion Field Guide |
| Ability, even without a combat target | Use the whistle on your companion, or aim at it and press **R** |
| Bakunawa roar | Aim at or ride Bakunawa and press **G** |
| Follow / Stay | Sneak + empty-hand use |
| Passive / Defend | Sneak + whistle use |
| Change Datu / Duwende appearance | Creative: sneak + showcase wand use |
| Mount | Empty-hand use on your Bakunawa, Sarimanok or Datu |
| Steer | **WASD**, facing the direction you want to travel |
| Ascend / descend | **Space / C** on a flying or swimming mount |
| Dismount | **Shift** (vanilla Sneak key) |
| Name | Use a renamed vanilla name tag |

All custom keys can be rebound in Options → Controls → Forgotten Island. Ground Datu uses ground movement rather than ascent/descent. Dismounts give altitude-scaled slow-falling and brief water-breathing protection. Bakunawa supplies air while swimming with a rider.

The guide is craftable from a book and prismarine crystals; the whistle from a copper ingot and string. `/island guide` also gives a guide. Creatures have spawn eggs and `/summon forgottenisland:<id>` identifiers matching the inventory.

## Repeatable showcase

In a Creative superflat world with cheats enabled:

```mcfunction
/gamerule doMobSpawning false
/time set day
/island showcase
```

This adds thirteen named, befriended, staying companions in a spaced grid and gives the three tools. Running it again adds another group. Walk up and use the whistle/wand to demonstrate each ability. `/island ability raww` requests the nearest owned Raww's ability within 12 blocks; substitute any roster ID. It respects cooldowns. The grid works best on open, flat ground.

The Flaming Aswang Chef befriends with three glowstone dust. Its Sizzling supper extinguishes allies and grants ten seconds of fire resistance; its flame crown and cooking particles never ignite blocks.

Bakunawa's eclipse is a local visual event with an ally protection pulse. It expires automatically and follows loaded living entities; it never changes time, weather, or terrain. Multiple eclipses use the strongest visible event, so removing one source does not leave a global flag behind.

## Build and edit

```sh
./gradlew --gradle-user-home .gradle-home build
./gradlew --gradle-user-home .gradle-home runClient
./gradlew --gradle-user-home .gradle-home runGameTestServer
```

Pinned tools: Java 17, Gradle 8.8 (wrapper checksum verified), ForgeGradle 6.0.54, official 1.20.1 mappings. First build downloads dependencies. Server development launch is `runServer`, using the separate `run-server` directory; it requires the server owner's Minecraft EULA acceptance. Never copy the local QA server's offline/loopback configuration into a public server.

`python3 tools/package_release.py` packages the built JAR, guide/research/verification documents, screenshots and video evidence, plus a separate complete source archive, into `build/distributions`. The package includes SHA-256 checksums. Build inputs are pinned. The original companion release passed a repeat-build comparison; the adventure build has its own validation record in the adventure ledger.

The checked-in game assets are sufficient to build. To regenerate original meshes, textures and sounds:

```sh
python3 -m venv .tools-venv
.tools-venv/bin/pip install -r tools/requirements-assets.txt
.tools-venv/bin/python tools/generate_assets.py
.tools-venv/bin/python tools/generate_adventure_assets.py
.tools-venv/bin/python tools/validate_assets.py
.tools-venv/bin/python tools/validate_adventure_assets.py
```

The generator is the source for initial mesh definitions. Individual JSON meshes in `assets-src/models` are editable; copy an edited file to `src/main/resources/assets/forgottenisland/models/creature` to use it without regenerating. A model's bone pivots are relative to its parent, dimensions are in sixteenths of a block, negative Y points up, and the material index selects a 128-pixel atlas region. Animation lives in `CreatureModel.setupAnim`.

Consequential companion settings are in each world's `serverconfig/forgottenisland-server.toml`: ability cooldown, eclipse duration, effect radius, mount speed multiplier, and defensive damage. Ranges and particle rates are bounded. Companion abilities do not destroy terrain or place fire. The adventure adds a dimension and persistent market inhabitants; it does not add a quest system or a trading economy.

See [asset provenance](docs/asset-licenses.md) and [LICENSE](LICENSE). This fan mod is not endorsed by DreamWorks, Universal, Mojang or Microsoft.
