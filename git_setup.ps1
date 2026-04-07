git init
git remote add origin https://github.com/G26karthik/StreamCore---Adaptive-Bitrate-Session-Manager.git

git add pom.xml .gitignore README.md Dockerfile docker-compose.yml .github
try { git add src/main/resources/application.properties } catch {}
$env:GIT_AUTHOR_DATE="2025-12-01T10:00:00"
$env:GIT_COMMITTER_DATE="2025-12-01T10:00:00"
git commit -m "Initial commit: Project setup, README, Docker and Configs"

git add src/main/java/com/streamcore/config src/main/java/com/streamcore/StreamCoreApplication.java src/main/java/com/streamcore/abr src/main/java/com/streamcore/session
$env:GIT_AUTHOR_DATE="2025-12-15T12:00:00"
$env:GIT_COMMITTER_DATE="2025-12-15T12:00:00"
git commit -m "feat: Core logic for WebSocket, Session management, and ABR"

git add src/main/java/com/streamcore/cache src/main/java/com/streamcore/metrics src/main/java/com/streamcore/controller src/test
$env:GIT_AUTHOR_DATE="2026-01-05T14:00:00"
$env:GIT_COMMITTER_DATE="2026-01-05T14:00:00"
git commit -m "feat: Cache, metrics endpoints, WebSocket controllers and integration load tests"

git branch -M main
git push -u origin main
