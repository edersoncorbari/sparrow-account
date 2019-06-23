#!/usr/bin/env bash

function remove-files() {
  find . -name .idea -type d -exec rm -rf {} +

  rm -rf project/project
  rm -rf project/target
  rm -rf target
}

read -p "This script will remove all compilation files. Are you sure? (y/n) " resp
if [ "${resp}" = "y" ]; then 
  remove-files 
fi

exit 0
