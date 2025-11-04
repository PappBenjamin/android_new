#!/bin/bash

cd "$(dirname "$0")"

echo "Step 1: Remove Progress---habit-tracker submodule..."
git rm -f Progress---habit-tracker
rm -rf .git/modules/Progress---habit-tracker

echo "Step 2: Remove the malformed copy folder..."
rm -rf "Progress---habit-tracker másolat/"

echo "Step 3: Remove .DS_Store changes..."
git restore .DS_Store

echo "Step 4: Check git status..."
git status

echo "Step 5: Commit the changes..."
git add -A
git commit -m "Convert Progress---habit-tracker to normal folder and remove malformed copy"

echo "Step 6: Push to remote..."
git push origin main

echo "Done! Submodule has been converted to a normal folder."

