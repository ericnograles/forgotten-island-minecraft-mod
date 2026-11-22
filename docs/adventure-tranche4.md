# 0.4.0 — Bakunawa and the crystal arsenal

## Confirmed design

The owner supplied nine photographed drawings (IMG_2058–IMG_2066) and confirmed the interpretation on 2026-11-22. Helmet strength is **100**, Chomp damage is **9**, and the new attacks are additive. The new equipment is **craftable**. The horned egg hatches a **friendly Bakunawa that grows**. Sword cooldowns are Crystal 10 seconds, Sapphire 15 seconds, Phoenix 5 seconds. Cinematic effects are built in, with no external shader dependency.

The boss uses a separate original black/red/orange model with angular horns, bright eyes, a huge toothed jaw and electrical tail accents. The friendly companion retains its established appearance and gains growth. The entrance follows the supplied rumble/emergence/roar/reaction/fight storyboard. Defeat uses a white flash, a stylized split and a non-destructive explosion. The drawings remain local references rather than bundled personal photographs.

## Combat and presentation

The eleven-move cycle retains Chomp/swallowing, Tentacles, Holy Beam, Moon Rocks (the revised Moonfall), Lunar Tide and Eclipse Breath. It adds Fireballs, Atomic Breath, Golden Ward, Tail Whip and Horn Slash. Chomp, moon rocks and fireballs deal 9 HP before ordinary mitigation; tail and slash deal 5. Tentacles remain 7. Holy Beam remains 10 HP and retains its documented armor/enchantment bypass and exceptions. Atomic Breath deals 1 HP at one-second intervals during a three-second discharge. Golden Ward prevents all incoming body and weak-point damage for exactly 100 server ticks.

Attack targets lock at the warning. Projectile paths, the articulated neck, sweeping tail and slashing head use the same server timing and geometry as the visible animation. Contacts are limited per move; atomic pulse state persists across actor reloads. The boss is tracked at the arena center while its visible body extends offshore, so its new size does not require a player to load its distant tail chunk. Existing arena blocks are not excavated or replaced.

The head rises about 40 blocks above sea level. Independent body sections form a continuous neck and tail. Built-in emissive eyes, electrical effects, water rings, spray, charge effects, a synthesized roar and choreographed cameras provide the requested cinematic treatment. K skips only the local scene; O retains Full, Reduced and Off. Reduced/Off avoid the full-camera white-flash overlay. Server encounter timing and rewards remain shared.

## Equipment and crafting

All three swords use their stated fully charged base melee damage: Crystal **6,000**, Sapphire Soul **1,000**, Phoenix **2,600**. The correction for the vanilla attack-attribute cap applies only to actual primary melee hits, not spells. Existing Crystal Swords retain their item ID and gain the new art/ability; the boss still awards the sword.

Press the normal Sneak key (Left Shift by default) while holding a sword in the main hand. Each press triggers once; holding Sneak or switching swords while crouched does not retrigger. Cooldowns are server-owned, persisted in player data and separate for each sword. No ability activates while riding. Abilities target hostile monsters, not players or friendly companions.

| Ability | Initial tuning |
| --- | --- |
| Crystal burst | 6-block radius, line of sight, 60 magic damage, five seconds of fire, eight seconds of Poison I; 10-second cooldown |
| Sapphire sight | 32-block radius, eight seconds of Glowing on enemies; 15-second cooldown |
| Phoenix fireball | Aimed projectile, 200 base magic damage, five seconds of fire, four-second lifetime; five-second cooldown |

Normal immunity applies: undead enemies do not receive poison, and fire-immune creatures do not burn. Neither fire ability ignites blocks or explodes terrain. Swords have 4,096 durability and support netherite repair/Mending; using an ability costs one durability.

A **Prismatic Crystal** is crafted with two amethyst shards, one prismarine crystal, one diamond and one blaze powder:

```text
 A     A = amethyst shard
PDB    P = prismarine crystal, D = diamond, B = blaze powder
 A
```

Each sword uses three Prismatic Crystals, a netherite sword and its accent ingredient (spider eye for Crystal, lapis lazuli for Sapphire Soul, blaze rod for Phoenix):

```text
 C     C = Prismatic Crystal
CSC    S = netherite sword
 A     A = accent ingredient
```

Each armor piece uses four Prismatic Crystals around the matching netherite armor piece (` C ` / `CAC` / ` C `). Crafting consumes the original equipment; it is not a smithing upgrade that preserves its enchantments. All recipes unlock in the vanilla recipe book. These intentionally powerful weapons are craftable before bosses, as requested, and can trivialize their health pools.

**Crystal Armor strength:** helmet 100, chestplate 900, leggings 250, boots 69; full set 1,319. A mod-specific correction preserves these literal values beyond the vanilla attribute cap while retaining the ordinary armor/toughness formula and its maximum 80% reduction. Enchantments, Resistance and absorption remain subsequent normal steps. Armor-bypassing damage still bypasses Crystal Armor. Strength is not durability or extra health. Armor supports Prismatic Crystal repair and Mending; durability is 2,048 / 4,096 / 3,584 / 1,792 respectively. The chestplate occupies the same slot as Manang Wings.

## Friendly egg and growth

The existing `bakunawa_spawn_egg` ID now places a horned hatching egg, preserving old inventory references. Recipe: `PAP` / `PEP` / ` P `, where P is a prismarine crystal, A an amethyst shard and E a chicken egg.

Incubation lasts 1,200 loaded server ticks (60 seconds). Shell cracks progress through four stages. The placer becomes the hatchling's owner, including when offline at hatch time. An unclaimed command-placed egg can be claimed by interacting with it. Obstructed hatching waits for free space; one successful hatch removes the egg. Breaking an egg resets incubation.

The hatchling starts at 35% adult scale and grows over 24,000 loaded ticks (20 minutes). Feeding an owned baby consumes one prismarine crystal and advances age by 10% of its remaining growth, with a minimum one-minute advance. Babies cannot be ridden; adults retain flight, swimming, companion abilities and controls. Incubation, ownership and growth persist across saves. Existing adults stay adults.

## Verification ledger

Verified on 2026-11-22 with Minecraft 1.20.1, Forge 47.4.10 and Java 17.0.15. The final installable JAR is `forgotten-island-0.4.0.jar`, SHA-256 `ec32b02bc1ee95a110ec54707017cd67f42858af22b0f8dd093886ef0164fc2e`. The same file was installed in both native clients and the dedicated server for the final pass. See [artifact provenance](evidence/tranche4/artifact.json).

### Automated checks

- **13/13 JUnit tests** and **36/36 Forge GameTests passed** in the final `build runGameTestServer` run. [Build/server log](evidence/tranche4/build-and-gametests.log), [JUnit totals](evidence/tranche4/junit-results.json).
- Actual primary melee hits were measured at 6,000 / 1,000 / 2,600. Full armor measured 1,319 strength, reduced a 100-HP hit to 20 HP, and preserved the 10-HP Holy Beam bypass.
- New attacks were checked for exact damage, one contact per move, slash geometry/timing, atomic pulse spacing and reload behavior, and the 100-tick invulnerability window.
- Ability tests cover target filtering, poison, cooldown rejection and death persistence, plus Phoenix projectile contact without terrain destruction. Egg tests cover saved ownership/incubation, obstruction, one hatch, saved baby age, feeding and legacy adult compatibility. All nine new recipes resolve to registered results with valid ingredients.
- Existing portal, necklace, stomach, encounter, reward and companion regressions passed. The reward fixture now clears only its disposable test dais before running, so repeated test worlds cannot mistake an earlier test's reward structure for a player build.
- Both [asset validators](evidence/tranche4/assets.txt) passed: all model/texture references, boss bones and UVs, emissive atlas, mono audio, nine recipes, wearable armor and four egg stages.

### Packaged-client checks

Native testing used two independent clients connected to a loopback dedicated server. Commands supplied equipment, targets, positioning and Resistance/Saturation for inspecting combat. The final blow used an operator damage command. These were assisted feature checks, not a measured Survival win.

- Crystal Armor rendered on the player; Sapphire outlined hostile targets; Phoenix and Crystal effects activated while the nearby second player and friendly companion remained unharmed. These arsenal captures precede the later boss-only visual refinements.
- The revised entrance, enormous closing jaw and shared three-crystal escape were inspected. All three stomach crystals were broken through ordinary mouse input, with teleports used to reach them during the assisted check.
- The final JAR ran the enlarged boss, warnings, articulated tail sweep and white-flash/split defeat sequence. The completed encounter produced personal reward drops. Precise attack damage/timing is covered by the automated checks; screenshots are sampled visual evidence rather than a frame-by-frame audit of all eleven moves.
- The final JAR's horned egg naturally hatched after its loaded incubation period. The baby was bonded to its placer. Feeding consumed crystals, visibly increased its size and reached adulthood; empty-hand interaction then mounted it successfully.
- The stopped dedicated-server world was copied into a new singleplayer QA save. The same adult, owner and mounted state reloaded, flight worked, and **Save and Quit** returned to the title screen. Offline NBT comparison also confirmed the completed victory, personal reward tokens, companion UUID, owner and adult age survived. Both clients and the server were then stopped cleanly. [Persistence comparison](evidence/tranche4/persistence.json), [client/singleplayer log](evidence/tranche4/client-and-singleplayer.log), [server log](evidence/tranche4/dedicated-server.log), [second-client log](evidence/tranche4/observer-client.log).
- All five new guide pages fit the native interface without clipped text. The dedicated server reported **20 TPS / 3.361 ms mean tick time** after the encounter with both clients connected. Singleplayer flight showed 95 FPS in one captured sample on an Apple M1 Pro at 1920×1200; this is not a hardware-independent benchmark.

Selected captures: [armor](evidence/tranche4/crystal-armor.png), [Sapphire](evidence/tranche4/sapphire-reveal.png), [Phoenix](evidence/tranche4/phoenix-impact.png), [Crystal](evidence/tranche4/crystal-burst.png), [Chomp](evidence/tranche4/chomp-jaw.png), [stomach escape](evidence/tranche4/crystal-escape.png), [final boss](evidence/tranche4/final-boss.png), [tail sweep](evidence/tranche4/tail-whip.png), [defeat flash](evidence/tranche4/defeat-flash.png), [split](evidence/tranche4/defeat-split.png), [egg](evidence/tranche4/placed-egg.png), [hatchling](evidence/tranche4/friendly-hatchling.png), [growth](evidence/tranche4/feeding-growth.png), [mount](evidence/tranche4/grown-mount.png), [singleplayer reload/flight](evidence/tranche4/singleplayer-reload-flight.png). The [capture index](evidence/tranche4/capture-index.json) identifies source filenames, build stages and hashes.

### Compatibility and testing limits

Use the same 0.4 JAR on clients and server, replacing the older mod JAR. Back up a world before upgrading. Existing Bakunawa adults and inventory egg IDs remain valid; old eggs now place incubating eggs. The ocean bay applies to untouched chunks only; previously expanded coast chunks and player builds are preserved. A render/view distance of 12 shows the full coastal staging; combat tracking stays centered on the arena.

No external shaders or animation libraries are needed. Realms authorization messages in the offline QA clients, a slow GLFW initialization notice and a short join-time server catch-up warning did not prevent play. No mod crash or missing mod asset was observed. Cross-mod compatibility and other Forge versions have not been tested.

Assisted demonstrations and automated checks are distinct from unassisted Survival progression and human balance review. No fresh human sign-off is claimed.
