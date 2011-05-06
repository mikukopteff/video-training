#!/usr/bin/env bash

cd $(dirname $0)
java -Xmx1024m -XX:MaxPermSize=1024m -jar sbt-launch-*.jar "$@"

