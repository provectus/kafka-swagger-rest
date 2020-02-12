#!/bin/bash

bootstrap=$CREATE_TOPICS_BOOTSTRAP_SERVERS
topics=$CREATE_TOPICS_LIST

if [[ -z $bootstrap ]]; then
  echo Bootstrap servers not found. Exit.
fi

if [[ -z $topics ]]; then
  echo Topic list not found. Exit.
fi

IFS=,

for topic in $topics; do
  echo Creating topic: $topic
  kafka-topics --bootstrap-server $bootstrap --create --topic $topic --partitions 1 --replication-factor 1
done

exit 0
