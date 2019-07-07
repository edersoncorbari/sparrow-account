#!/usr/bin/env bash

DIRECTORY=`pwd`

function get-root-dir() {
  if [[ ! ${0} =~ "scripts" ]]; then
    DIRECTORY=`cd -`
  fi
}

function create-docker-image() {
  #sudo docker build -t sparrow-account:v0.1 .
  sudo docker build -t sparrow-account:v0.1 ${DIRECTORY}
}

read -p "Want to create a docker image. Are you sure? (y/n) " resp
if [ "${resp}" = "y" ]; then 
  get-root-dir && create-docker-image
fi

exit 0
