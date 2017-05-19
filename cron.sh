#!/bin/bash
set -euo pipefail # See http://redsymbol.net/articles/unofficial-bash-strict-mode/
IFS=$'\n\t'

# Execute via crontab by hduser@weywot1:
# 30 5 * * * ssh sol@quaoar1 "cd /home/sol/git/mabxml-elasticsearch ; bash -x cron.sh update >> logs/cron.sh.log 2>&1"
# 23 23 * * Sat ssh sol@quaoar1 "cd /home/sol/git/mabxml-elasticsearch ; bash -x cron.sh basedump >> logs/cron.sh.log 2>&1"

TARGET_PATH=daily
# Determine the latest update file and store it locally:
DATE=$(date "+%Y%m%d")
DATE_YESTERDAY=$(date --date yesterday "+%Y%m%d")

DOWNLOAD_FILE="DE-605-aleph-update-marcxchange-$DATE_YESTERDAY-$DATE.tar.gz"
if [ $1 == "basedump" ]; then
	DOWNLOAD_FILE="DE-605-aleph-baseline-marcxchange-$DATE.tar.gz"
fi

cd $TARGET_PATH 
wget -nv http://lobid.org/download/dumps/DE-605/mabxml/$DOWNLOAD_FILE

cd ..
# Run the transformation with the latest file (and possibly unprocessed previous files):
curl --fail -XPOST "http://localhost:7300/hbz01/transform?dir=/home/sol/git/mabxml-elasticsearch/$TARGET_PATH/&suffix=gz&cluster=gaia-aither&hostname=193.30.112.82&index=hbz01" >> logs/processMabxml.sh.$DATE-gaia.log 2>&1

# Clean up
rm $TARGET_PATH/*.tar.gz
