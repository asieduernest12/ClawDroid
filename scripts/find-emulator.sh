#!/bin/bash
# Fast dynamic discovery of Android emulator/device on local network.
# Uses aggressive nmap scan for open port 5555 (ADB).
# Completes in ~5 seconds per /24 subnet.
#
# Usage: bash scripts/find-emulator.sh [subnet1 subnet2 ...]
#   subnets - optional list of CIDRs (default: auto-detect from local IPs)

PORT=5555

detect_subnets() {
    ip -o addr show primary 2>/dev/null \
        | awk '{print $4}' \
        | grep '/[0-9]\+$' \
        | awk -F/ '{print $1}' \
        | awk -F. '!/^127/ && !/^172\.1[6-9]\./ && !/^172\.2[0-9]\./ && !/^172\.3[0-1]\./{print $1"."$2"."$3".0/24"}'
    hostname -I 2>/dev/null | tr ' ' '\n' \
        | grep -v '^127\.' \
        | awk -F. '{print $1"."$2"."$3".0/24"}'
}

scan_subnet() {
    local subnet=$1
    nmap -n -T5 --min-rate 1000 -p "$PORT" \
         --host-timeout 3s --max-rtt-timeout 500ms \
         --initial-rtt-timeout 100ms "$subnet" 2>/dev/null \
        | grep -B1 "open" \
        | grep "Nmap scan report for" \
        | awk '{print $NF}' \
        | sed 's/(//;s/)//'
}

SUBNETS=("$@")
[ ${#SUBNETS[@]} -eq 0 ] && mapfile -t SUBNETS < <(detect_subnets | sort -u | head -3)

found=""

for subnet in "${SUBNETS[@]}"; do
    [ -z "$subnet" ] && continue
    echo "Scanning $subnet for ADB (port $PORT)..." >&2
    while IFS= read -r host; do
        [ -z "$host" ] && continue
        echo "  -> Found: $host:$PORT" >&2
        result=$(adb connect "$host:$PORT" 2>/dev/null)
        echo "  -> $result" >&2
        found="$host"
    done < <(scan_subnet "$subnet")
done

if [ -z "$found" ]; then
    echo "No devices found on port $PORT" >&2
    exit 1
fi

echo "$found"
