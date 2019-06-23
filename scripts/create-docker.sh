#!/usr/bin/env bash

function create-docker-image() {
  sudo docker build -t sparrow-account:v0.1 .
}

read -p "Want to create a docker image. Are you sure? (y/n) " resp
if [ "${resp}" = "y" ]; then 
  create-docker-image
fi

exit 0
