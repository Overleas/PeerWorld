# 📜 PeerWorld Changelog

All notable changes to the **PeerWorld** Minecraft mod are documented in this file.

---

## [1.0.0] — 2026-11-08 (Official Initial Release)

### 🌟 New Features & Highlights
* **Decentralized Peer-to-Peer Multiplayer:** Play in shared worlds with friends with zero dedicated server costs ($0.00).
* **Seamless Host Takeover (Handoff):** When the current host disconnects, any permitted friend can seamlessly take over hosting the world.
* **Smart Delta Synchronization Engine:**
  * Chunk-level and region-level delta computation.
  * Only modified blocks and entities are uploaded/downloaded, minimizing network usage.
* **World-Bound Datapacks & Automatic Client Resource Pack Push:**
  * **Datapacks:** Synchronize automatically per-world across all players.
  * **Resource Packs:** Automatically pushed via native Minecraft packet to guests upon joining, with direct SHA-1 verification.
* **Customizable Player Capacity (2–64):**
  * Per-world player limit slider integrated directly into World Settings.
  * Synchronized transparently across all participants.
* **Zero-Config P2P Tunnels:** Direct connectivity via `e4mc` and `QuicLime` without requiring port forwarding or VPNs.
* **Free Isolated Cloud Storage:**
  * Google Drive (isolated strictly to `appDataFolder`).
  * Cloudflare R2 / AWS S3 / MinIO compatible.
  * Local Offline mode for pure LAN play.
* **Full Multi-Loader Support:**
  * Fabric Loader (Minecraft 1.20.1, 1.21.1, 1.21.11, 26.1, 26.3).
  * NeoForge Loader (Minecraft 1.21.1, 1.21.11).
* **100% Localization Parity across 11 Languages:**
  * English (`en_us`), Ukrainian (`uk_ua`), Polish (`pl_pl`), German (`de_de`), Spanish (`es_es`), French (`fr_fr`), Portuguese (`pt_br`), Russian (`ru_ru`), Chinese Simplified (`zh_cn`), Japanese (`ja_jp`), and Catalan (`ca_es`).

### 🛡 Security & Reliability
* **Directory Traversal / ZipSlip Protection:** Rigid validation (`SharedWorldPack.resolveSafe` and `DownloadPlanApplier`) guarantees no malicious files can escape the designated world directory.
* **MOTD CRLF Sanitization:** Prevents header injection or display corruption from carriage return characters.
* **Atomic Save Writes:** Safe fallback mechanisms handle cross-device file moves and prevent world corruption during unexpected power cuts.
* **Platform TOS Compliance:** Removed all explicit mentions of unauthorized/pirated launchers to adhere strictly to Modrinth and CurseForge moderation standards.

---

## [0.5.x] — Pre-Release Milestones
* Initial development of peer coordination protocol, cloud sync backends, and host indicator overlay.
