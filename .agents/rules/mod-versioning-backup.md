# PeerWorld Mod Versioning and Backup Policy

Before making any code modifications or building a new version of the PeerWorld mod, the agent MUST strictly follow these rules:

1. **Pre-Update Backup**:
   - Always copy the current working `.jar` file to `/home/overleas/Стільниця/PeerWorld-Backups/` before replacing it.
   - Name the backup with the version and timestamp: `peerworld-<version>_<timestamp>.jar`.

2. **Changelog Maintenance**:
   - Update `CHANGELOG.md` in the root of the project with:
     - New version number
     - Date
     - Bullet points of exact bug fixes, changes, and new features added.

3. **User Communication**:
   - After compiling and deploying the new `.jar`, always inform the user:
     - The path where the previous version was backed up.
     - A concise, clear summary of what was changed and why.
