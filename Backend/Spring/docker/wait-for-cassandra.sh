#!/bin/bash
until cqlsh -e "describe keyspaces"; do
  echo "Cassandra is unavailable - waiting"
  sleep 2
done

echo "Cassandra is up - executing CQL script"
cqlsh -f /docker-entrypoint-initdb.d/init.cql
