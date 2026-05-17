#!/bin/bash
# Integration test: verify OpenRouter chat with Nemotron 30B
# Usage: bash test_openrouter.sh <api_key>

set -e

if [ -z "$1" ]; then
    echo "Usage: bash test_openrouter.sh <openrouter_api_key>"
    exit 1
fi

API_KEY="$1"
API_BASE="https://openrouter.ai/api/v1"
MODEL="nvidia/nemotron-4-340b-instruct"

echo "=== OpenRouter Integration Test ==="
echo "Provider: OpenRouter"
echo "Model: $MODEL"
echo "Endpoint: $API_BASE/chat/completions"
echo ""

# Step 1: Test model fetch (GET /models)
echo "--- Step 1: Fetch available models ---"
MODEL_RESPONSE=$(curl -s -w "\n%{http_code}" "$API_BASE/models" \
    -H "Authorization: Bearer $API_KEY")
MODEL_HTTP_CODE=$(echo "$MODEL_RESPONSE" | tail -1)
MODEL_BODY=$(echo "$MODEL_RESPONSE" | head -n -1)

if [ "$MODEL_HTTP_CODE" = "200" ]; then
    MODEL_COUNT=$(echo "$MODEL_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('data',[])))" 2>/dev/null || echo "parse_failed")
    echo "✓ Models fetched successfully (HTTP $MODEL_HTTP_CODE, $MODEL_COUNT models)"
else
    echo "✗ Model fetch failed (HTTP $MODEL_HTTP_CODE)"
    echo "  Response: $MODEL_BODY"
    exit 1
fi
echo ""

# Step 2: Test chat completion
echo "--- Step 2: Send chat message ---"
CHAT_RESPONSE=$(curl -s -w "\n%{http_code}" "$API_BASE/chat/completions" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $API_KEY" \
    -H "HTTP-Referer: https://clawdroid.app" \
    -H "X-Title: ClawDroid" \
    -d "$(cat <<EOF
{
    "model": "$MODEL",
    "messages": [
        {"role": "user", "content": "Hello! Please respond with exactly: 'OpenRouter integration works. Model: $MODEL'"}
    ]
}
EOF
)")

CHAT_HTTP_CODE=$(echo "$CHAT_RESPONSE" | tail -1)
CHAT_BODY=$(echo "$CHAT_RESPONSE" | head -n -1)

if [ "$CHAT_HTTP_CODE" = "200" ]; then
    REPLY=$(echo "$CHAT_BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
try:
    content = d['choices'][0]['message']['content']
    print(content)
except (KeyError, IndexError):
    print('ERROR: unexpected response format')
    print(json.dumps(d, indent=2)[:500])
" 2>/dev/null)

    echo "✓ Chat completion succeeded (HTTP $CHAT_HTTP_CODE)"
    echo ""
    echo "=== Agent Response ==="
    echo "$REPLY"
else
    ERROR_BODY=$(echo "$CHAT_BODY" | python3 -c "
import sys, json
try:
    d = json.load(sys.stdin)
    print(d.get('error', {}).get('message', json.dumps(d)[:500]))
except:
    print(sys.stdin.read()[:500])
" 2>/dev/null || echo "parse_failed")
    echo "✗ Chat completion failed (HTTP $CHAT_HTTP_CODE, $ERROR_BODY)"
    exit 1
fi
echo ""
echo "=== Test Complete ==="
