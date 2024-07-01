#!/bin/bash

set -x

BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD)

git submodule foreach \
  '( git checkout '"${BRANCH_NAME}"' || git checkout dev_paxuk ) && git pull'
