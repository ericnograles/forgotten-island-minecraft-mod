# Forgotten Island: Wild Companions

The [CurseForge player guide](docs/curseforge/description.html) collects the installation, companions, controls, recipes, and boss instructions used by the listing. Maintainers can find release synchronization instructions in [CurseForge publishing](docs/curseforge/PUBLISHING.md).

Version **0.4.0** adds the drawing-inspired black/red Bakunawa boss, eleven animated attacks, ocean-rise and split/explosion cinematics, three craftable swords with Sneak abilities, spiked Crystal Armor, and a horned egg that hatches a growing friendly Bakunawa. The necklace, expanded island, stomach adventure and all thirteen companions remain available.

See the [0.4 design and verification ledger](docs/adventure-tranche4.md) for exact damage, recipes, armor behavior, growth, test results and playtest assistance. Previous [0.3 evidence](docs/adventure-tranche3.md) covers the island expansion and save upgrades. Full unassisted Survival progression and fresh solo/cooperative balance review remain assigned to human testers.

An unofficial Minecraft Java mod inspired by DreamWorks' *Forgotten Island* and Philippine folklore. **Thirteen playable creatures** with original models, companion abilities and three rideable creatures. See [the verification ledger](docs/verification.md) for automated tests, single-player and multiplayer evidence, and remaining limits.

The current build includes **thirteen creatures**: Bakunawa, Raww, Manang, Sarimanok, Datu, Duwende, Bungi, Batiba, Prince Mermahn, Zippy the Potion Fairy, Tikbalang, Sarangay and the Flaming Aswang Chef. The full identified roster is implemented. The chef is an explicitly authorized **original gameplay interpretation**, not a claim of film-design accuracy. Sarangay is an adaptation of primary production concept art; comparison with its final-film design remains open. See [the research inventory](docs/creature-roster.md). Bakunawa is a required folklore-inspired addition; its reported film silhouette is unverified.

## Install

Requires **Minecraft Java Edition 1.20.1**, **Java 17**, and **Forge 47.4.10** (the pinned test version). No animation or other mod dependencies are required. The declared Forge range is 47.4.10–47.x; other Forge releases have not been tested.

1. Install the matching [official Forge client](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html).
2. Copy `forgotten-island-0.4.0.jar` from the release archive (or `build/libs` after building the source) into that Minecraft profile's `mods` folder.
3. Launch the Forge profile. Put the same JAR on a Forge server and every connecting client for multiplayer.
4. In Survival, craft the field guide and follow the portal plan below. For the original companion showcase, use the **Forgotten Island** tab on the second Creative-tab page.

## Travel to Forgotten Island

Craft a **Homeward Star Necklace** in a crafting table:

```text
S . S    S = string (2)
. C .    C = copper ingot (1)
. T .    T = torch (1)
```

Use it on the top of dry Overworld ground. Keep it in hand nearby for the two-second formation, then jump down into the luminous horizontal star. Each necklace creates 64 portals. The compact seven-block-wide star fits above small slopes; it needs open air but no frame, orientation or large flat platform. Obstructions, water and protected land produce feedback. Placement is checked again after the animation; an interrupted or obstructed formation changes no blocks and spends no charge.

The normal entry path requires no amethyst or glow ink. Legacy Parol Stone portals still work, and their optional manual recipe/activation remain available. Craft the field guide with a **book and feather** for the new instructions.

The old arrival harbor remains at `(0, 65, 40)`. Follow its eastern shore to the larger festival quarter at `(310, 65, 65)`: sixteen new stalls, a performance stage, parol strings and twenty-four more residents. The junction at `(400, 65, 35)` leads independently to:

- **Memory Hollow** `(475, 67, 190)`: Batiba's explorable cave, luminous chambers, a resting alcove and supplies.
- **Twilight Summit** `(535, 105, -215)`: a lit ascent through the ridge to Manang's mountain encounter.
- **Moonbreak Coast** `(820, 65, -65)`: a sea arch, jetty and a 168-block-wide Bakunawa arena.

The old island's ground envelope was **437 × 351 blocks**; the expanded envelope is **1,268 × 699 blocks**, with paths, terrain, vegetation, regional sounds and small caches. [Route distances, estimated walking times and interpretation notes](docs/adventure-tranche3.md) distinguish calculations from measured play.

Each player's last departure portal is stored separately, with a five-second travel cooldown. Return stars search for safe, dry ground near that portal, then at Overworld spawn. `/island home` remains an unopped emergency recovery command, including inside Bakunawa. Death retains the normal Overworld bed/spawn. Island beds and anchors cannot change it.

Existing saves upgrade automatically and additively. Previously built old-district chunks remain intact. In the expansion, any column containing non-baseline blocks is left untouched from bedrock to sky; untouched ocean columns can gain terrain even if explored before. Companions, residents, portal links, encounter completion and reward journals persist. Unfinished bosses use the new destinations; completed bosses stay completed, and existing rewards/exits remain where they were. The old arenas become historic courts. Extensive player construction can interrupt a new route or destination; the upgrade preserves that construction.

## Encounters and rewards

Enter an arena in Survival to awaken its boss. Party health scales at awakening for up to four players; late arrivals join without healing the boss. Separate custom meters display shared server health and the current phase. Leave the arena to hide its meter. If everyone retreats or dies, an unfinished encounter resets after ten seconds. Completed bosses stay defeated in that world.

Bring enchanted armor, a sword, food and healing supplies. The **previous 0.2** verified victories used Protection II diamond armor, a Sharpness III diamond sword, a shield and ordinary golden apples. These are obtainable before either boss; iron-only victory has not been established. Learn the warning patterns and move back into melee reach after knockback.

- **Manang** stands about 4.7 blocks tall while joined. Her upper body separates for Shadow Dive, Shriek and Wing Cut. Leave the marked circle, jump over the expanding ring, or move out of her facing arc. Strike the lower body when her wings fold after an attack. The second half of the fight accelerates her pattern.
- **Bakunawa** rises with an articulated black/red body, angular horns, visible fangs and luminous eyes. Chomp/swallowing, Moon Rocks and Fireballs deal **9 HP**; Tentacles **7**; Tail Whip and Horn Slash **5**; Atomic Breath **1 HP each second** during its three-second discharge. Golden Ward grants **five seconds of invincibility**. Holy Beam, Lunar Tide and Eclipse Breath remain. Ground warnings, visible projectiles and sweeping limbs share the server's hit timing. Strike the three gold moon anchors during recovery. Phase II accelerates the pattern.

Newly generated northern coast stays ocean for the rise. Previously expanded terrain and player construction are preserved. Use a client/server view distance of at least **10 chunks** (12 recommended) for the full coastal backdrop; the encounter remains playable at shorter distances.

Swallowed players explore a sealed ribbed interior with a rising passage and three luminous Memory Crystals. Breaking all three is the only normal escape and ejects all swallowed fighters together, safely back into the fight. No special pickaxe, wings or Crystal Sword is required. Everyone inside still counts toward the encounter and rewards. Death, reset, defeat, emergency recovery and stale reconnects are explicit escape safeguards. Offline guests keep durable recovery markers; restart does not erase crystal progress.

Holy Beam deals **10 HP per unblocked discharge**, bypassing ordinary armor and protection enchantments. On Normal, two separate hits kill an otherwise unmodified full-health 20-HP player. A discharge has only one damage tick. **Exceptions:** a correctly facing shield, Resistance (including companion/arrival/ejection protection), absorption, extra maximum health, intervening healing, Totem of Undying, Creative/spectator immunity, scene immunity and other mods/datapacks that change damage. Vanilla damage immunity can reject a hit; no single discharge retries to force damage through. Move sideways out of the aim line or take cover behind solid terrain.

**K** skips your current scene. **O** cycles Full, Reduced and Off; these keys can be rebound in Controls. Reduced keeps your own camera and movement. Off disables the camera/title spectacle and eclipse treatment. The world animation and encounter timing stay synchronized. Late arrivals see the remaining scene time. Everyone in the arena is protected during the shared entrance/defeat interval; your skip never skips another player's scene. Esc/menu, death, travel and disconnect restore local camera and controls.

Rewards appear on the south dais only after the defeated scene finishes. Each Survival participant who joined before defeat receives a personal set. Uncollected drops are reserved for their recipient, do not expire and resist damage; they persist through chunk unload/restart. After pickup, normal inventory, chest, death-drop and sharing rules apply. Picked-up rewards are not replaced if subsequently lost.

| Victory reward | Behavior |
| --- | --- |
| Manang Wings | Chest-slot equipment with a custom bat-wing model. Jump again while falling to glide; normal firework boosts work. 648 durability, one wear per flight second; repair with phantom membranes or Mending. At one durability remaining, repair before flying again. |
| Crystal Sword | An ordinary fully charged, unenchanted, noncritical melee hit deals **6,000 pre-mitigation damage**. Vanilla attack-charge/critical and target mitigation rules still apply. Server damage correction handles Minecraft's attack-attribute cap. 4,096 durability; repair with netherite ingots or Mending. |
| Bakunawa Skull | Distinct collectible trophy with original item art. |
| Bakunawa exit | A permanent floor-star on the south dais, immediately usable after victory. Returns each visitor to their own linked Overworld portal or the safe fallback. |

Effects are confined to the arenas and participants. Attacks never set block fire or destroy terrain; the market residents remain peaceful and protected. A full trading economy and quest system are outside this expansion.

## Crystal arsenal and hatching

Craft a Prismatic Crystal from two amethyst shards, a prismarine crystal, a diamond and blaze powder. Combine three Prismatic Crystals with a netherite sword and a spider eye (Crystal), lapis lazuli (Sapphire Soul), or blaze rod (Phoenix). Exact grids are in the field guide and [0.4 crafting reference](docs/adventure-tranche4.md#equipment-and-crafting).

| Sword | Fully charged base melee damage | Press Sneak while held |
| --- | ---: | --- |
| Crystal | 6,000 | Fire/poison burst within six blocks; 10-second cooldown |
| Sapphire Soul | 1,000 | Reveal enemies within 32 blocks for eight seconds; 15-second cooldown |
| Phoenix | 2,600 | Launch a fireball; five-second cooldown |

Release Sneak between activations. Abilities ignore players and friendly companions. Cooldowns persist through saves and death. These deliberately powerful, craftable weapons can defeat bosses very quickly.

Crystal Armor uses four Prismatic Crystals around each matching netherite piece. Armor strengths are **100 / 900 / 250 / 69** (helmet/chest/legs/boots), totaling **1,319**. Full strength is honored beyond Minecraft's attribute cap, while ordinary armor reduction remains capped at 80%. Holy Beam bypasses it. Repair armor with Prismatic Crystals; Mending works. These are crafting recipes and consume the old equipment, including its enchantments.

Craft the horned Bakunawa egg from five prismarine crystals, an amethyst shard and a chicken egg. Place it with open space above: it cracks over **60 loaded seconds**, then hatches already bonded to its placer. A blocked egg waits. The baby grows for **20 loaded minutes**; feeding prismarine crystals speeds growth. Adults can be ridden. Ownership, incubation and age survive saves.

## Original companions

Right-click a companion egg to spawn its creature. Bakunawa uses the hatching process above. Give it three of its favorite gift to befriend it; each creature tells you what it likes when you interact. In Creative, the Showcase Wand befriends instantly and demonstrates its ability on the next use. It cannot take another player's companion.

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

The guide is craftable from a book and feather; the whistle from a copper ingot and string. `/island guide` also gives a guide. Creatures have spawn eggs and `/summon forgottenisland:<id>` identifiers matching the inventory.

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
.tools-venv/bin/python tools/generate_tranche_assets.py
.tools-venv/bin/python tools/generate_tranche4_assets.py
.tools-venv/bin/python tools/validate_assets.py
.tools-venv/bin/python tools/validate_adventure_assets.py
```

The generator is the source for initial mesh definitions. Individual JSON meshes in `assets-src/models` are editable; copy an edited file to `src/main/resources/assets/forgottenisland/models/creature` to use it without regenerating. A model's bone pivots are relative to its parent, dimensions are in sixteenths of a block, negative Y points up, and the material index selects a 128-pixel atlas region. Animation lives in `CreatureModel.setupAnim`.

Consequential companion settings are in each world's `serverconfig/forgottenisland-server.toml`: ability cooldown, eclipse duration, effect radius, mount speed multiplier, and defensive damage. Ranges and particle rates are bounded. Companion abilities do not destroy terrain or place fire. The adventure adds a dimension and persistent market inhabitants; it does not add a quest system or a trading economy.

See [asset provenance](docs/asset-licenses.md) and [LICENSE](LICENSE). This fan mod is not endorsed by DreamWorks, Universal, Mojang or Microsoft.
