# Blob Game

Blob Game is a small JavaFX arcade project where the player grows by eating food and smaller blobs while avoiding larger enemies.

## Controls

- `W`, `A`, `S`, `D` to move the player blob
- The game ends when the player is absorbed by a larger enemy blob

## Project layout

- `src/blobgame/` contains the game logic
- `src/images/` contains the sprite and UI assets
- `tests/` contains the runnable test entrypoint used by the local harness

## Local testing

Run the lightweight test suite with Maven:

```bash
mvn test
```

## Repository notes

- Keep `.git/` inside the submission zip so the platform can restore real commit history.
- Keep the repository private when uploading to GitHub.
