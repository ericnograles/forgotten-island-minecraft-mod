# Continuous adventure recordings — 2026-11-21

Eight real packaged-client recordings, approximately **3 minutes 32 seconds** total, captured after macOS screen permission was enabled. Each file is a continuous take, with no editorial cuts, speed changes or still-image substitution. Files are silent H.264 MP4, 1280 × 800, 30 fps. No microphone or system audio was captured, so these clips do not verify sound playback.

| Recording | Length (rounded) | Visible evidence |
| --- | --- | --- |
| [Portal activation and entry](m8-portal-entry.mp4) | 25 s | Glow ink activates the horizontal star; downward jump, dimension loading and arrival near the market |
| [Festival market](m8-festival-market.mp4) | 20 s | Continuous walk among decorated stalls, star lanterns and moving residents |
| [Manang entrance and meter](m8-manang-entrance.mp4) | 25 s | Approach, full six-second entrance with separation/wings, restored first-person controls, custom meter and attack warnings |
| [Manang defeat](m8-manang-defeat.mp4) | 30 s | Combat meter, lethal transition, full 5.5-second defeat scene, actor cleanup and reward notification |
| [Bakunawa entrance and meter](m8-bakunawa-entrance.mp4) | 25 s | Approach, full eight-second dragon/moon entrance, huge body, reachable anchors and custom meter |
| [Bakunawa eclipse and defeat](m8-bakunawa-eclipse-defeat.mp4) | 38 s | Phase II at 47%, eclipse moon, attack/anchor warnings, full seven-second defeat scene, cleanup and newly manifested exit |
| [Bakunawa rewards and return](m8-bakunawa-rewards-return.mp4) | 24 s | Actual pickup, sword/skull in inventory, jump through the new exit and return beside the personal Overworld star |
| [Manang Wings gliding](m8-wings-gliding.mp4) | 25 s | Third-person folded wings, walk off the launch platform, Space activation, spread custom wings during flight and descent into water |

## Scope and setup

The production client and dedicated server both used the released `forgotten-island-0.2.0.jar`, SHA-256 `525d245a8f868929cdef04d8a5016aeb0bdeb40baa374e476d75fd31ad621b98`. Recording occurred in `adventure-recording-qa`, copied from the interruption QA world. The prior player-earned victory world was preserved.

These are **presentation fixtures**, not unassisted Survival or combat-balance evidence. Console setup supplied the portal frame/glow ink, player position/orientation and diamond sword. Resistance V protected the player during the boss takes; knockback resistance was raised for stable framing after an earlier Manang take reset when the player left the arena. Console damage triggered Manang's lethal transition during its vulnerable window and Bakunawa's phase II/lethal transitions. The failed reset take is excluded. The earlier player-earned victories remain separately documented in [milestones 5–6](../../../adventure-development.md).

Reward delivery, pickup, inventory interactions, portal activation/travel, cinematic playback and glide inputs used the actual mod. The wings are the item delivered by this recorded Manang defeat, equipped through the normal inventory. Before flight, Resistance was removed and knockback resistance restored to zero. A nine-block platform at Y=109 and a teleport supplied the launch height; normal movement and Space initiated the glide. The server logged the same reward token with `Damage: 0` before launch and `Damage: 24` during the later flight query. After capture the player used `/island home` to return safely.

## Capture and validation

The user explicitly authorized ffmpeg. AVFoundation captured Minecraft full-screen on display 0 at 1920 × 1200 and scaled the full frame to 1280 × 800. Windowed capture tests showed the chat instead of the background-controlled game; those tests are excluded. Native recording controls had previously timed out, and macOS permission had previously blocked frames. Full-screen capture worked after permission was enabled.

Capture options: `-f avfoundation -framerate 30 -pixel_format nv12 -capture_cursor 0 -i 3:none`, `scale=1280:800,setsar=1`, constant 30 fps, libx264 CRF 20 / veryfast / level 4.0, yuv420p and faststart. Adjacent `.log` files retain encoder diagnostics.

Every clip was fully decoded with ffmpeg's error-fatal mode, inspected through decoded contact sheets, and checked with ffprobe for frame count, dimensions, codec, rate and absence of audio. [manifest.json](manifest.json) records exact lengths, byte counts and SHA-256 hashes. The eight files contain 6,355 decoded frames, totaling 211.833 seconds and 30,110,186 bytes.

The [server log](../m8-recording-server.log) records setup, transitions, inventory queries and clean shutdown. The [final saved-state inspection](../m8-recording-save.json) verifies 21 unique residents of eleven types, both encounters COMPLETE, no saved boss actors, all three unique collected reward tokens, the personal departure and Bakunawa's exit portal. The server's original world selection was restored after shutdown. Local QA worlds and failed capture tests are excluded from the release archives.
