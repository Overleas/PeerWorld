# 🌐 PeerWorld

<p align="center">
  <strong>Free, decentralized peer-to-peer friend-hosted Minecraft worlds with seamless host handoff and smart delta synchronization.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20.1%20|%201.21.1%20|%201.21.11%20|%2026.x-brightgreen.svg" alt="Minecraft Versions">
  <img src="https://img.shields.io/badge/Mod%20Loader-Fabric%20%7C%20NeoForge-blue.svg" alt="Loaders">
  <img src="https://img.shields.io/badge/Hosting%20Cost-%240.00-success.svg" alt="Cost">
  <img src="https://img.shields.io/badge/Languages-11%20Available-orange.svg" alt="Languages">
  <img src="https://img.shields.io/badge/License-MIT-lightgrey.svg" alt="License">
</p>

---

## 📖 Overview

**PeerWorld** allows you to play Minecraft together in shared worlds with your friends **without paying for dedicated server hosting ($0.00)**, without configuring router port-forwarding, and without complex VPN tools (like Hamachi or Radmin).

When the host player leaves the game, any authorized friend can instantly take over hosting and keep the world running for everyone. Only modified chunks, blocks, and inventory data are uploaded and downloaded using an ultra-fast delta synchronization engine.

---

## 🌟 Key Features

* 🔄 **Seamless Host Takeover (Handoff):** Never wait for the "server owner" to log on again. Any permitted friend can launch the shared world as host whenever they want to play.
* ⚡ **Smart Delta Synchronization:** Only modified region files and chunk changes are transferred. Join in seconds without downloading the entire gigabyte-sized world repeatedly.
* 🌐 **Built-in Zero-Config P2P Tunnels:** Direct, low-latency connectivity powered by encrypted tunnels (`e4mc` / `QuicLime`). Friends can connect from anywhere on the internet.
* 📦 **World-Bound Datapacks & Resource Packs:**
  * **Datapacks:** Automatically sync across all friends as part of the world save.
  * **Resource Packs:** Push directly to guests upon joining the host's session, with direct SHA-1 verification.
* 👥 **Customizable Player Limits (2–64):** Tailor player capacity per world right from the in-game settings slider.
* ☁️ **Isolated Free Cloud Storage:**
  * **Google Drive:** Uses strictly isolated `appDataFolder` storage (PeerWorld cannot see or access your personal files or photos).
  * **S3 Compatible:** Seamless integration with Cloudflare R2, AWS S3, or MinIO.
  * **Local Offline:** Pure local LAN mode with zero cloud accounts required.
* 🛡 **Rock-Solid Security:**
  * Strict ZipSlip and path traversal defenses across all world packs and manifests.
  * Automatic retry mechanisms and atomic disk writes protect against corruption.
* 🌍 **11 Fully Supported Languages (100% Parity):**
  English, Ukrainian, Polish, German, Spanish, French, Portuguese, Russian, Chinese, Japanese, and Catalan.

---

## 🚀 Quick Start Guide

### 1. Installation
1. Install **Fabric Loader** (or **NeoForge**) for your target Minecraft version.
2. Ensure **Fabric API** is installed in your `.minecraft/mods` directory.
3. Drop `peerworld-*.jar` and `e4mc` into your `mods` folder.

### 2. Hosting a World
1. Click the **PeerWorld** button in the Minecraft main menu.
2. Click **Create Shared World** (or import an existing singleplayer world).
3. Choose your storage provider (Google Drive, Cloudflare R2, or Offline Local).
4. Launch the world!

### 3. Inviting Friends
1. While in-game, press `Escape` and click **PeerWorld Invite** (or type `/peerworld invite`).
2. Copy your unique invite code or link and send it to your friend.
3. Your friend clicks **Redeem Code** in their PeerWorld menu — the world is permanently added to their multiplayer list!

---

## 📦 Supported Minecraft Versions

| Minecraft Version | Fabric Loader | NeoForge | Recommended e4mc |
| :--- | :---: | :---: | :--- |
| **1.20 / 1.20.1** | ✅ Supported | — *(Forge legacy)* | `6.2.1` |
| **1.21 / 1.21.1** | ✅ Supported | ✅ Supported | `6.2.1` |
| **1.21.2 – 1.21.8** | ✅ Supported | ✅ Supported | `6.2.1` |
| **1.21.9 – 1.21.10** | ✅ Supported | ✅ Supported | `6.2.1` |
| **1.21.11** | ✅ Supported | ✅ Supported | `6.2.1` |
| **26.1 / 26.1.1 / 26.1.2** | ✅ Supported | ⏳ In development | `6.2.3-modern` |
| **26.2** | ✅ Supported | ⏳ In development | `6.2.3-modern` |
| **26.3** | ✅ Supported | ⏳ In development | `6.2.3-modern` |

---

## 🔒 Security & Privacy

* **Strict Cloud Isolation:** When using Google Drive, PeerWorld requests access **solely** to its dedicated application folder (`appDataFolder`). It possesses zero access to your Google Docs, Google Photos, or general drive storage.
* **Path Traversal Protection:** All world files and pack archives are sanitized against directory traversal attacks before being written to disk.
* **Open Source & Transparent:** No hidden telemetry, no adware, no cryptocurrency miners, and no paid paywalls.

---

## 📜 License

PeerWorld is open-source software licensed under the [MIT License](./LICENSE).
