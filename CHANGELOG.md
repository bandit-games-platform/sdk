# Changelog
This file is here to keep track of changes for each version of the SDK.
Each change should be sorted in to one of the following categories:
“Added”, “Changed”, “Deprecated”, “Removed”, “Fixed”

## [Unreleased]

## [v1.0.0] - 07/12/2024
### Added
- changeLobbyOwner sdk method
- updateLobbyPlayerCount sdk method
- closeLobby sdk method
- openLobby sdk method
- patchLobby sdk method
- createLobby sdk method
- updateAchievementProgress sdk method
- submitCompletedSession sdk method
- registerGame sdk method
- authentication using api key to the GameSDK
- GameSDK.Builder to build a new GameSDK object
### Changed
- GameSDK automatically re-authenticates when the token is requested and the expiry time has passed
- GameSDK automatically authenticates upon construction
