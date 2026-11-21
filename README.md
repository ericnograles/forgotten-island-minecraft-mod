# Forgotten Island: Wild Companions

An unofficial Minecraft Java mod inspired by DreamWorks' *Forgotten Island* and Philippine folklore. **Thirteen playable creatures** with original models, companion abilities and three rideable creatures. See [the verification ledger](docs/verification.md) for automated tests, single-player and multiplayer evidence, and remaining limits.

The current build includes **thirteen creatures**: Bakunawa, Raww, Manang, Sarimanok, Datu, Duwende, Bungi, Batiba, Prince Mermahn, Zippy the Potion Fairy, Tikbalang, Sarangay and the Flaming Aswang Chef. The full identified roster is implemented. The chef is an explicitly authorized **original gameplay interpretation**, not a claim of film-design accuracy. Sarangay is an adaptation of primary production concept art; comparison with its final-film design remains open. See [the research inventory](docs/creature-roster.md). Bakunawa is a required folklore-inspired addition; its reported film silhouette is unverified.

## Install

Requires **Minecraft Java Edition 1.20.1**, **Java 17**, and **Forge 47.4.10** (the pinned test version). No animation or other mod dependencies are required. The declared Forge range is 47.4.10–47.x; other Forge releases have not been tested.

1. Install the matching [official Forge client](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html).
2. Copy `forgotten-island-0.1.0.jar` from the release archive (or `build/libs` after building the source) into that Minecraft profile's `mods` folder.
3. Launch the Forge profile. Put the same JAR on a Forge server and every connecting client for multiplayer.
4. Open a Creative world. Find **Forgotten Island** on the second Creative-tab page. Take the guide, showcase wand, whistle and spawn eggs.

## First adventure

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

`python3 tools/package_release.py` packages the built JAR, guide/research/verification documents and screenshots, plus a separate complete source archive, into `build/distributions`. The package includes SHA-256 checksums. Build inputs are pinned and two consecutive recompiled JARs were byte-for-byte identical in the test environment.

The checked-in game assets are sufficient to build. To regenerate original meshes, textures and sounds:

```sh
python3 -m venv .tools-venv
.tools-venv/bin/pip install -r tools/requirements-assets.txt
.tools-venv/bin/python tools/generate_assets.py
```

The generator is the source for initial mesh definitions. Individual JSON meshes in `assets-src/models` are editable; copy an edited file to `src/main/resources/assets/forgottenisland/models/creature` to use it without regenerating. A model's bone pivots are relative to its parent, dimensions are in sixteenths of a block, negative Y points up, and the material index selects a 128-pixel atlas region. Animation lives in `CreatureModel.setupAnim`.

Consequential settings are in each world's `serverconfig/forgottenisland-server.toml`: ability cooldown, eclipse duration, effect radius, mount speed multiplier, and defensive damage. Ranges and particle rates are bounded. There is no terrain destruction, block fire, natural spawning, new dimension, or quest system.

See [asset provenance](docs/asset-licenses.md) and [LICENSE](LICENSE). This fan mod is not endorsed by DreamWorks, Universal, Mojang or Microsoft.
