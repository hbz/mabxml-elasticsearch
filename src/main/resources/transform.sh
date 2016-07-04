#!/bin/bash
set -euo pipefail # See http://redsymbol.net/articles/unofficial-bash-strict-mode/
IFS=$'\n\t'

# Execute via crontab by hduser@weywot1:
# 20 06 * * * ssh hduser@weywot2 "cd ~/git/mabxml-elasticsearch/src/main/resources ; bash transform.sh"

export MAVEN_OPTS="-Dfile.encoding=UTF-8 -Xmx1024M -Xss128M -XX:+CMSClassUnloadingEnabled"

# Determine the latest update file and store it locally:
updates=http://dataproxy.lobid.org/alephxml/export/update/
date=$(date "+%Y%m%d")
updateFile=$(curl $updates | grep 'tar.gz' | cut -d '"' -f2 | grep $date)
cd updates ; wget $updates$updateFile ; cd ../../../..

# Run the transformation with the latest file (and possibly unprocessed previous files):
mvn clean install >> log/processMabxml.sh.$date.log 2>&1
mvn exec:java -Dexec.mainClass="flow.Transform" -Dexec.args="src/main/resources/updates/ gz quaoar 193.30.112.171 hbz01-mabxml" >> log/processMabxml.sh.$date.log 2>&1

# Clean up and move updates to the full data directory (skipped if transformation fails, due to -e option):
cd src/main/resources/
cp updates/* /files/open_data/open/DE-605/mabxml/
rm updates/*

