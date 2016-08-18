#!/bin/bash
set -euo pipefail # See http://redsymbol.net/articles/unofficial-bash-strict-mode/
IFS=$'\n\t'

# Execute via crontab by hduser@weywot1:
# 30 5 * * * ssh sol@quaoar1 "cd /home/sol/git/mabxml-elasticsearch ; bash -x cron.sh >> logs/cron.sh.log 2>&1"

TARGET_PATH=updates
# Determine the latest update file and store it locally:
date=$(date "+%Y%m%d")
date_yesterday=$(date --date yesterday "+%Y%m%d")

cd $TARGET_PATH 
wget -nv http://lobid.org/download/dumps/DE-605/mabxml/DE-605-aleph-update-marcxchange-$date_yesterday-$date.tar.gz
cd ..
# Run the transformation with the latest file (and possibly unprocessed previous files):
curl --fail -XPOST "http://localhost:7300/hbz01/transform?dir=/home/sol/git/mabxml-elasticsearch/$TARGET_PATH/&suffix=gz&cluster=quaoar1&hostname=193.30.112.170&index=hbz01" >> logs/processMabxml.sh.$date.log 2>&1

# Clean up
rm $TARGET_PATH/*.tar.gz
