# Changelog

## 0.2.0

### Highlights
- **Requires Spotube with plugin API 0.1.0.** Spotube now only lets a plugin read WebView cookies for domains it declares. This version declares `www.jiosaavn.com`, the only domain whose cookies it reads at login. Spotube shows it when installing or updating the plugin.
- **Update checks.** The plugin now checks GitHub releases for new versions.

### Features
- Declare `cookieDomains` for the WebView login, target plugin API 0.1.0, and update `spotube-gradle` to 0.2.0 (`6b85b6e`).
- Check GitHub releases for plugin updates (`df269ee`).

### Chore
- Bump plugin version to 0.2.0 (`c90ca84`).

## 0.1.0

### Highlights
- **First release** of the JioSaavn plugin for Spotube, providing metadata and audio (`e438745`).

### Fixes
- Update plugin interface imports and dependency references (`15e25e0`, `f81d5f5`).

### Chore
- Add the release workflow and update `spotube-gradle` to 0.1.2 (`fb70a99`).
