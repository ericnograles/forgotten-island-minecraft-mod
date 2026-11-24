# Asset provenance

The CurseForge listing icon at `docs/branding/curseforge-icon.png` is an original AI-generated promotional illustration made with the built-in image generation tool. Its prompt is preserved in `docs/branding/icon-prompt.txt`. It depicts a stylized friendly serpent, island, and star lantern; it is not an in-game screenshot or a film asset, and it is not bundled into the mod JAR.

All shipped creature meshes, UV atlases, item icons, the memory-mote sprite, animation code, and synthesized Ogg calls were authored for this project. Their editable sources are `assets-src/models/*.json`, `assets-src/palettes.json`, `tools/generate_assets.py`, and `CreatureModel.java`. They use the project MIT license, subject to underlying character/trademark rights described in LICENSE.

No film frames, film audio, actors' voices, soundtrack recordings, downloaded fan models, or vanilla-mob recolors are included. Publicity stills were inspected as references, not copied into game resources. Attribution and visual-reference links are in creature-roster.md.

Build-time dependencies: Forge/ForgeGradle (their upstream licenses), Gradle wrapper (Apache-2.0, from the official Forge MDK), Mojang official mappings/game binaries (Mojang's licenses, downloaded by ForgeGradle), JUnit 5 (EPL-2.0). Minecraft/Forge are not bundled into the mod JAR. Asset tools: Pillow (HPND), NumPy (BSD-3-Clause), SoundFile (BSD-3-Clause) and its libsndfile binary (LGPL-2.1). None of these asset tools are bundled into the mod JAR.

The sounds are short additive/noise synthesis, rendered to spatial mono Vorbis. No voice impersonation or third-party music is used.

The Flaming Aswang Chef's model, flame crown, checked apron, cooking implements, animations and synthesized sizzle/chime are an original gameplay interpretation requested by the user. No chef film image or audio was used as an asset source; film-design accuracy is not claimed.

## Adventure expansion references and original adaptations

On 2026-11-21 the official [Universal Pictures Philippines new trailer](https://www.youtube.com/watch?v=n2zWJxmwHNU) was inspected in browser playback at approximately 0:45, 0:47 and 0:50. The inspected sequence shows blue-white light, a luminous radial opening on the ground, and the characters above it beginning to fall downward. The Minecraft entrance preserves that ground-plane/downward direction. Its buildable five-point parol silhouette, copper-inlaid stone frame, glow-ink activation, materials, cooldown and personal return linking are original gameplay adaptations, not claims that those construction details appear in the movie. A complete film review is not claimed.

`tools/generate_adventure_assets.py` authors the new star stone, parol lantern, animated floor-light texture and quiet festival sound loop. The sound uses original additive chimes, synthesized drum pulses and a generated sea-noise bed; it samples no music, voices or film audio. The market layout, lantern strings, food displays and branching arena routes are original block architecture. Existing companion textures and meshes are unchanged.


The adventure generator also authors two themed life-meter frames, the Crystal Sword, Manang Wings and Bakunawa Skull item art, a world-space moon sprite, and six original encounter sounds. The sounds use additive oscillators and shaped noise, without recorded voices or samples. Boss animation/camera choreography, lunar anchors, the eclipse effect and attack warnings are original gameplay adaptations. Manang's larger separated form reuses the project's original film-reference-informed skeleton and atlas; Bakunawa's approximately 92-block articulated boss uses the existing original folklore adaptation. Neither size is asserted as a movie measurement. A boss-only mesh adjustment separates overlapping belly faces; the thirteen companion mesh definitions and palettes remain unchanged. The wearable bat-wing geometry is authored in `ManangWingsLayer.java`.

For 0.3, `tools/generate_tranche_assets.py` authors the Homeward Star Necklace icon, stomach tissue and Memory Crystal textures, and three synthesized regional ambience loops. The necklace recipe, expanded block architecture, flexible throat, prismarine tentacles, Holy Beam rendering and sealed body objective are original gameplay designs. Wither Storm is a scale/interior-gameplay inspiration only; no assets or code from another mod are included. [The tranche ledger](adventure-tranche3.md) lists movie sources and identifies the map/interior interpretations.


For 0.4, nine user-supplied paper drawings (IMG_2058–IMG_2066) informed the original black/red boss redesign, angular horns and fangs, sword silhouettes, multicolor spiked armor, and horned hatching egg. `tools/generate_tranche4_assets.py` authors the pixel art, separate boss skeleton and emissive atlas, armor textures, egg models/cracks and synthesized four-second roar. The roar uses generated oscillators and noise; it contains no recordings. The photographs are local references and are not bundled. Expressive jaw/body/fins, warning paths, water rings, split fragments and camera choreography are original Minecraft adaptations. The thirteen companion designs remain intact.

For 0.6, `tools/generate_moon_assets.py` draws the moonrise sky and serpent and generates the Moon Rocket item model using Minecraft block textures. The landing crater, rocket geometry and lunar bite scene were authored for the mod. The official five-minute documentary walkthrough pairs captured Minecraft gameplay with original narration synthesized using George (`bm_george`), a British English voice in [Kokoro-82M](https://huggingface.co/hexgrad/Kokoro-82M/blob/main/VOICES.md#british-english).
