#!/usr/bin/env bash

IMAGE_NAME="distributed-tracing-graph"
docker build -t $IMAGE_NAME  .

FILE="$1"
if [ -z "$FILE" ]
then
  echo "No input file path supplied. Running docker image without any argument"
  docker run  $IMAGE_NAME
else
  MOUNT_PATH=/test.txt
  if test -f "$FILE"; then
      echo "mounting given file inside container as $MOUNT_PATH"
      docker run -v $FILE:$MOUNT_PATH $IMAGE_NAME $MOUNT_PATH
  else
    echo "$FILE does not exists!"
    exit 1
  fi
fi

