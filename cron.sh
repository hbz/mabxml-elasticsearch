#!/bin/bash
# explicitly without "-e" for it should not exit immediately when failed but write a mail
set -uo pipefail # See http://redsymbol.net/articles/unofficial-bash-strict-mode/
IFS=$'\n\t'

# Execute via crontab by hduser@weywot1:
# 30 5 * * * ssh sol@quaoar1 "cd /home/sol/git/mabxml-elasticsearch ; bash -x cron.sh update >> logs/cron.sh.log 2>&1"
# 23 23 * * Sat ssh sol@quaoar1 "cd /home/sol/git/mabxml-elasticsearch ; bash -x cron.sh basedump >> logs/cron.sh.log 2>&1"

TARGET_PATH=/data/DE-605/mabxml
# Determine the latest update file and store it locally:
DATE=$(date "+%Y%m%d")

RECIPIENT=lobid-admin

# Use "brace extension" as we don't know the appendix of the basedump
if [ $1 == "basedump" ]; then
       TARGET_PATH=$TARGET_PATH/baseline
else 
       TARGET_PATH=$TARGET_PATH/updates
fi

# Run the transformation with the latest file (and possibly unprocessed previous files):
curl --fail -XPOST "http://localhost:7300/hbz01/transform?dir=$TARGET_PATH&suffix=gz&cluster=weywot&hostname=10.9.0.12&index=hbz01" >> logs/processMabxml.sh.$DATE-weywot.log 2>&1

# if check ok, index to productive instance:
if [  $? -ne 0 ]; then
    mail -s "Alert MAB-XML Clobs indexing to hbz01" "$RECIPIENT@hbz-nrw.de" << EOF
"error when indexing MAB-XML Clobs to index hbz01 :("
EOF
else
# Clean up
  rm $TARGET_PATH/*.gz
fi
