#!/bin/bash
set -euo pipefail

API_URL="${GITHUB_API}"
NOW_EPOCH=$(date +%s)
REF_TYPE="tag"
TARGET_REF=""

fetch_tags() {
    local releases_json
    releases_json=$(curl -fsSL "${API_URL}/releases?per_page=100" 2>/dev/null || echo "[]")

    # Only process if releases_json is a non-empty array
    if [ "$(echo "$releases_json" | jq -r 'if type == "array" then "yes" else "no" end')" = "yes" ] && \
       [ "$releases_json" != "[]" ] && [ -n "$releases_json" ]; then
        echo "$releases_json" | jq -r '.[] | "\(.published_at)\t\(.tag_name)"' 2>/dev/null > /tmp/tag-lines.txt || true
    fi

    git ls-remote --tags --sort=-v:refname https://github.com/sipeed/picoclaw.git 2>/dev/null \
        | grep -v '\^{}' \
        | awk '{print $2}' \
        | sed 's|refs/tags/||' > /tmp/git-tags.txt || true

    > /tmp/all-tags.txt
    > /tmp/tags-with-dates.txt

    if [ -f /tmp/tag-lines.txt ] && [ -s /tmp/tag-lines.txt ]; then
        while IFS=$'\t' read -r date tag; do
            [ -z "$tag" ] && continue
            echo "$tag" >> /tmp/all-tags.txt
            epoch=$(date -d "$(echo "$date" | sed 's/T/ /; s/Z$//')" +%s 2>/dev/null || echo 0)
            echo "$epoch $tag $date" >> /tmp/tags-with-dates.txt
        done < /tmp/tag-lines.txt
    fi

    while IFS= read -r tag; do
        [ -z "$tag" ] && continue
        if ! grep -qxF "$tag" /tmp/all-tags.txt 2>/dev/null; then
            echo "$tag" >> /tmp/all-tags.txt
            tag_info=$(curl -fsSL "${API_URL}/git/refs/tags/${tag}" 2>/dev/null || echo "{}")
            obj_url=$(echo "$tag_info" | jq -r '.object.url // empty')
            if [ -n "$obj_url" ]; then
                date_str=$(curl -fsSL "$obj_url" 2>/dev/null | jq -r '.tagger.date // .committer.date // .author.date // empty')
                if [ -n "$date_str" ]; then
                    epoch=$(date -d "$date_str" +%s 2>/dev/null || echo 0)
                    echo "$epoch $tag $date_str" >> /tmp/tags-with-dates.txt
                fi
            fi
        fi
    done < /tmp/git-tags.txt
}

fetch_head() {
    local branch_info
    branch_info=$(curl -fsSL "${API_URL}" 2>/dev/null || echo "{}")
    DEFAULT_BRANCH=$(echo "$branch_info" | jq -r '.default_branch // "master"')
    COMMIT_INFO=$(curl -fsSL "${API_URL}/commits/${DEFAULT_BRANCH}?per_page=1" 2>/dev/null || echo "[]")

    # Handle both array and error-object responses
    HEAD_DATE=""
    HEAD_SHA=""
    if [ "$(echo "$COMMIT_INFO" | jq -r 'if type == "array" then "yes" else "no" end')" = "yes" ]; then
        HEAD_DATE=$(echo "$COMMIT_INFO" | jq -r '.[0].commit.committer.date // .[0].commit.author.date // empty')
        HEAD_SHA=$(echo "$COMMIT_INFO" | jq -r '.[0].sha // empty')
    fi

    if [ -n "$HEAD_DATE" ] && [ -n "$HEAD_SHA" ]; then
        HEAD_EPOCH=$(date -d "$HEAD_DATE" +%s 2>/dev/null || echo 0)
        echo "HEAD_EPOCH=$HEAD_EPOCH" > /tmp/head-info.txt
        echo "HEAD_DATE=$HEAD_DATE" >> /tmp/head-info.txt
        echo "HEAD_SHA=$HEAD_SHA" >> /tmp/head-info.txt
        echo "HEAD_BRANCH=$DEFAULT_BRANCH" >> /tmp/head-info.txt
    fi
}

fetch_tags
fetch_head

TAG_COUNT=0
if [ -f /tmp/tags-with-dates.txt ]; then
    TAG_COUNT=$(wc -l < /tmp/tags-with-dates.txt | tr -d ' ')
fi
[ -z "$TAG_COUNT" ] && TAG_COUNT=0

if [ -z "$PICO_RELEASE" ] || [ "$PICO_RELEASE" = "" ]; then
    if [ "$TAG_COUNT" -gt 0 ]; then
        CUTOFF_AGE=48
        echo "PICO_RELEASE not set: selecting most recent tag older than ${CUTOFF_AGE}h" >&2
        sort -t' ' -k1,1nr /tmp/tags-with-dates.txt > /tmp/tags-sorted.txt
        while read -r epoch tag date; do
            age_h=$(( (NOW_EPOCH - epoch) / 3600 ))
            if [ "$age_h" -ge "$CUTOFF_AGE" ]; then
                TARGET_REF="$tag"
                break
            fi
        done < /tmp/tags-sorted.txt
        if [ -z "$TARGET_REF" ]; then
            TARGET_REF=$(tail -1 /tmp/tags-sorted.txt | awk '{print $2}')
            echo "WARNING: All tags are newer than ${CUTOFF_AGE}h; using oldest tag $TARGET_REF" >&2
        fi
    else
        echo "No tags found; falling back to HEAD (default branch)" >&2
        REF_TYPE="branch"
        TARGET_REF="HEAD"
    fi

elif echo "$PICO_RELEASE" | grep -qE '^\+([0-9]+)h$'; then
    HOURS=$(echo "$PICO_RELEASE" | sed -E 's/^\+([0-9]+)h$/\1/')
    if [ "$TAG_COUNT" -gt 0 ]; then
        echo "PICO_RELEASE=$PICO_RELEASE: selecting most recent tag older than ${HOURS}h" >&2
        sort -t' ' -k1,1nr /tmp/tags-with-dates.txt > /tmp/tags-sorted.txt
        while read -r epoch tag date; do
            age_h=$(( (NOW_EPOCH - epoch) / 3600 ))
            if [ "$age_h" -ge "$HOURS" ]; then
                TARGET_REF="$tag"
                break
            fi
        done < /tmp/tags-sorted.txt
        if [ -z "$TARGET_REF" ]; then
            TARGET_REF=$(tail -1 /tmp/tags-sorted.txt | awk '{print $2}')
            echo "WARNING: All tags are newer than ${HOURS}h; using oldest tag $TARGET_REF" >&2
        fi
    else
        echo "No tags found; falling back to HEAD" >&2
        REF_TYPE="branch"
        TARGET_REF="HEAD"
    fi

elif [ "$PICO_RELEASE" = "latest" ]; then
    if [ "$TAG_COUNT" -gt 0 ]; then
        echo "PICO_RELEASE=latest: selecting newest tag" >&2
        TARGET_REF=$(sort -t' ' -k1,1nr /tmp/tags-with-dates.txt | head -1 | awk '{print $2}')
    else
        echo "No tags found; falling back to HEAD" >&2
        REF_TYPE="branch"
        TARGET_REF="HEAD"
    fi

elif [ "$PICO_RELEASE" = "HEAD" ]; then
    echo "PICO_RELEASE=HEAD: selecting latest commit on default branch" >&2
    REF_TYPE="branch"
    TARGET_REF="HEAD"

elif echo "$PICO_RELEASE" | grep -qE '^v[0-9]'; then
    echo "PICO_RELEASE=$PICO_RELEASE: selecting exact tag" >&2
    if [ -f /tmp/all-tags.txt ] && grep -qxF "$PICO_RELEASE" /tmp/all-tags.txt; then
        TARGET_REF="$PICO_RELEASE"
    else
        echo "ERROR: Tag $PICO_RELEASE not found in repo" >&2
        cat /tmp/all-tags.txt >&2 || true
        exit 1
    fi

else
    echo "ERROR: Unknown PICO_RELEASE format: $PICO_RELEASE" >&2
    echo "Expected: latest, vX.Y.Z, +Nh, HEAD, or unset (default +48h or HEAD fallback)" >&2
    exit 1
fi

if [ "$REF_TYPE" = "tag" ]; then
    echo "$TARGET_REF"
    echo "$REF_TYPE" > /tmp/ref-type.txt
elif [ "$REF_TYPE" = "branch" ]; then
    if [ "$TARGET_REF" = "HEAD" ]; then
        if [ -f /tmp/head-info.txt ]; then
            . /tmp/head-info.txt
            echo "HEAD $HEAD_SHA $HEAD_BRANCH"
            echo "$REF_TYPE" > /tmp/ref-type.txt
            echo "$HEAD_SHA" > /tmp/head-sha.txt
            echo "$HEAD_BRANCH" > /tmp/head-branch.txt
        else
            echo "HEAD"
            echo "$REF_TYPE" > /tmp/ref-type.txt
        fi
    else
        echo "$TARGET_REF"
        echo "$REF_TYPE" > /tmp/ref-type.txt
    fi
fi
