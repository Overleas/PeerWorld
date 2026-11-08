# Free 24/7 Cloud Deployment Guide for PeerWorld Coordinator

This guide explains how to deploy your own PeerWorld coordinator server in the cloud **100% free with zero credit card requirements**, allowing you and your friends to play at any time without needing your personal PC to stay powered on.

---

## Option A: Hugging Face Spaces (Recommended — 50 GB Free Storage, No Cards)

1. Sign up at **[huggingface.co](https://huggingface.co/join)** (email only, no credit card).
2. Click **New Space** (or navigate to `https://huggingface.co/new-space`).
3. Fill in the fields:
   * **Space name**: e.g., `peerworld-server`
   * **Space SDK**: Select **Docker** -> **Blank**
   * **Space visibility**: **Public** (required so friends can reach the API)
   * Click **Create Space**.
4. In the **Files** tab, upload the files from `deploy/docker`:
   * `Dockerfile`
   * `swcore` (Rust Linux x86_64 binary)
   * `data/` folder (with database and blob storage)
5. Within 1-2 minutes, your Space will build and deploy automatically!
6. Your permanent coordinator URL will be:  
   `https://<your-username>-peerworld-server.hf.space`

---

## Option B: Koyeb (Free Eco Tier, Automated Git Deploys)

1. Sign up at **[koyeb.com](https://www.koyeb.com)**.
2. Create a new service deploying via Docker or your GitHub repository.
3. Service port: `7860` (or `8080`).
4. Obtain your permanent free domain:  
   `https://<app-name>.koyeb.app`.

---

## Connecting the Minecraft Client to Your Self-Hosted Cloud:

Once you have your coordinator URL (e.g., `https://alex-peerworld.hf.space`):
1. Open Minecraft with the PeerWorld mod installed.
2. In the PeerWorld settings, set your backend URL to your custom domain, or share invite codes in the format:  
   `INVITE_CODE@https://alex-peerworld.hf.space`
3. The mod will coordinate and synchronize sessions directly through your dedicated cloud instance.
