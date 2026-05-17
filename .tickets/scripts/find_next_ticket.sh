#!/bin/bash

# Find the next ticket with pending tasks, skipping any with IGNORE: true header
# Output format: .tickets/ticket-XXX-name/prd.md:LINE_NUMBER

# Get all ticket directories sorted by number
tickets=$(ls -1d .tickets/ticket-[0-9]* 2>/dev/null | sort -V)

for ticket in $tickets; do
  prd="$ticket/prd.md"

  # Skip if prd.md doesn't exist
  [ -f "$prd" ] || continue

  # Check if ticket has IGNORE: true header or HTML comment ignore
  if grep -qE '^(<!--\s*ignore\s*-->|IGNORE:\s*true)' "$prd"; then
    continue
  fi

  # Check for any pending tasks [ ] in this ticket
  # Use grep to find lines with [ ] status marker
  pending_line=$(grep -n '\[ \]' "$prd" | head -n1)

  if [ -n "$pending_line" ]; then
    # Extract line number and print
    line_num=$(echo "$pending_line" | cut -d: -f1)
    echo "$prd:$line_num"
    exit 0
  fi

  # Also check for in-progress tasks [-] (these become pending if no [ ] exist)
  inprog_line=$(grep -n '\[-\]' "$prd" | head -n1)
  if [ -n "$inprog_line" ]; then
    line_num=$(echo "$inprog_line" | cut -d: -f1)
    echo "$prd:$line_num"
    exit 0
  fi
done

# No pending tickets found
echo "No pending tickets found"
exit 0
