# Asset provenance

All shipped creature meshes, UV atlases, item icons, the memory-mote sprite, animation code, and synthesized Ogg calls were authored for this project. Their editable sources are `assets-src/models/*.json`, `assets-src/palettes.json`, `tools/generate_assets.py`, and `CreatureModel.java`. They use the project MIT license, subject to underlying character/trademark rights described in LICENSE.

No film frames, film audio, actors' voices, soundtrack recordings, downloaded fan models, or vanilla-mob recolors are included. Publicity stills were inspected as references, not copied into game resources. Attribution and visual-reference links are in creature-roster.md.

Build-time dependencies: Forge/ForgeGradle (their upstream licenses), Gradle wrapper (Apache-2.0, from the official Forge MDK), Mojang official mappings/game binaries (Mojang's licenses, downloaded by ForgeGradle), JUnit 5 (EPL-2.0). Minecraft/Forge are not bundled into the mod JAR. Asset tools: Pillow (HPND), NumPy (BSD-3-Clause), SoundFile (BSD-3-Clause) and its libsndfile binary (LGPL-2.1). None of these asset tools are bundled into the mod JAR.

The sounds are short additive/noise synthesis, rendered to spatial mono Vorbis. No voice impersonation or third-party music is used.

The Flaming Aswang Chef's model, flame crown, checked apron, cooking implements, animations and synthesized sizzle/chime are an original gameplay interpretation requested by the user. No chef film image or audio was used as an asset source; film-design accuracy is not claimed.
