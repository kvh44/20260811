#!/bin/sh
# Install repository hooks by setting git's core.hooksPath
git config core.hooksPath .githooks
echo "Installed hooks: core.hooksPath set to .githooks"
