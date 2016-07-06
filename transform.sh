#!/bin/bash
set -euo pipefail # See http://redsymbol.net/articles/unofficial-bash-strict-mode/
IFS=$'\n\t'

# Execute via crontab by hduser@weywot1:
# 20 06 * * * ssh hduser@weywot2 "cd ~/git/mabxml-elasticsearch ; bash transform.sh"

# Determine the latest update file and store it locally:
updates=http://dataproxy.lobid.org/alephxml/export/update/
date=$(date "+%Y%m%d")
updateFile=$(curl $updates | grep 'tar.gz' | cut -d '"' -f2 | grep $date)
cd updates ; wget $updates$updateFile ; cd ..

# Run the transformation with the latest file (and possibly unprocessed previous files):
curl --fail -XPOST "http://lobid.org/hbz01/transform?dir=/home/hduser/git/mabxml-elasticsearch/updates/&suffix=gz&cluster=quaoar1&hostname=193.30.112.170&index=hbz01" >> logs/processMabxml.sh.$date.log 2>&1

# Clean up and move updates to the full data directory (skipped if transformation fails, due to -e option):
cp updates/* /files/open_data/open/DE-605/mabxml/
rm updates/*

