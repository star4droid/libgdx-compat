#!/usr/bin/env bash
# Writes the release notes to stdout.
# usage: release-notes.sh <version> [extra notes]
set -euo pipefail
VERSION="$1"
EXTRA="${2:-}"
REPO="${GITHUB_REPOSITORY:-star4droid/libgdx-compat}"

PREV_TAG="$(git describe --tags --abbrev=0 HEAD 2>/dev/null || true)"

echo "## libgdx-compat ${VERSION}"
echo
if [ -n "$EXTRA" ]; then
  printf '%b\n\n' "$EXTRA"
fi

echo "### Changes"
echo
if [ -n "$PREV_TAG" ]; then
  RANGE="${PREV_TAG}..HEAD"
else
  RANGE="HEAD"
fi
# one line per commit subject; merge commits and release-bot commits are skipped
CHANGES="$(git log "$RANGE" --no-merges --pretty=format:'- %s (%h)' | grep -v -i '^- \(release\|bump version\)' || true)"
if [ -n "$CHANGES" ]; then
  echo "$CHANGES"
else
  echo "- No code changes since ${PREV_TAG}."
fi
echo

cat <<EOF
### Downloads

| File | What it is |
|---|---|
| \`libgdx-compat-${VERSION}.jar\` | The library. Put it in \`core/libs/\`. |
| \`libgdx-compat-${VERSION}-sources.jar\` | Sources, for your IDE. |
| \`libgdx-compat-demo-${VERSION}.apk\` | The gallery app (Android 5.0+). |

### Install with JitPack

\`\`\`groovy
repositories { maven { url 'https://jitpack.io' } }
dependencies { api 'com.github.${REPO%%/*}:libgdx-compat:${VERSION}' }
\`\`\`

Documentation: https://${REPO%%/*}.github.io/${REPO##*/}/
EOF

if [ -n "$PREV_TAG" ]; then
  echo
  echo "**Full changelog:** https://github.com/${REPO}/compare/${PREV_TAG}...${VERSION}"
fi
