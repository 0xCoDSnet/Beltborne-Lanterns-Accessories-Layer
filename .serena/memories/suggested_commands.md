# Suggested Commands

## Build
```bash
cd /home/alexey/Projects/mods/Beltborne-Lanterns-Accessories-Layer/1.21.8-multiloader
./gradlew build                           # Build all modules
./gradlew :modules:fabric:runClient       # Run Fabric client
./gradlew :modules:neoforge:runClient     # Run NeoForge client
./gradlew classes                          # Compile only
```

## Git (run from worktree)
```bash
cd /home/alexey/Projects/mods/Beltborne-Lanterns-Accessories-Layer/1.21.8-multiloader
git status
git log --oneline -10
git diff
```

## GitHub
```bash
gh issue list -R Shadscure/Beltborne-Lanterns-Accessories-Layer
gh pr list -R Shadscure/Beltborne-Lanterns-Accessories-Layer
```

## Release
```bash
cd /home/alexey/Projects/mods/Beltborne-Lanterns-Accessories-Layer
./release.sh 1.21.8 --dry-run
./release.sh 1.21.8
```
