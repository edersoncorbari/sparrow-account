#!/usr/bin/env bash

HOST='127.0.0.1'
PORT=8080
USER='jack.sparrow'
AMOUNT=1000.0

function put-line() {
  printf '%85s\n' | tr ' ' -
}

function add-fill-account() {
  put-line
  cmd="curl -i -H \"Content-Type: application/json\" -X POST -d \
	  '{\"uuid\":\"${USER}\", \"amount\":${AMOUNT}}' http://${HOST}:${PORT}/account"
  echo "${cmd}" && echo
  eval "${cmd}" && echo 
}

function get-balance-account() {
  put-line
  cmd="curl -i -H \"Content-Type: application/json\" -X GET \
	  http://${HOST}:${PORT}/balance/${USER}"
  echo "${cmd}" && echo
  eval "${cmd}" && echo 
}

function rm-fill-account() {
  put-line
  cmd="curl -i -H \"Content-Type: application/json\" -X POST -d \
	  '{\"uuid\":\"${USER}\", \"amount\":-${AMOUNT}}' http://${HOST}:${PORT}/account"
  echo "${cmd}" && echo
  eval "${cmd}" && echo 
}

[[ "$(command -v curl)" ]] || { 
  echo "Sorry! Command (curl) is not installed...." 1>&2 && exit -1; 
}

add-fill-account &&
get-balance-account &&
rm-fill-account &&
rm-fill-account &&
get-balance-account

exit 0
