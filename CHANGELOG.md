# Changelog
This file is here to keep track of changes for each version of the SDK.
Each change should be sorted in to one of the following categories:
“Added”, “Changed”, “Deprecated”, “Removed”, “Fixed”

## [Unreleased]
### Added
- registerGame sdk method (without any collections)
- authentication using api key to the GameSDK
- GameSDK.Builder to build a new GameSDK object
### Changed
- GameSDK automatically re-authenticates when the token is requested and the expiry time has passed
- GameSDK automatically authenticates upon construction