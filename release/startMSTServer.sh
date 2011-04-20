#!/bin/sh

source config

cd ${MST_PARSER_HOME}
echo "Current directory: ${MST_PARSER_HOME}"
${JAVA_HOME_BIN}/java -classpath ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" -Xms8g -Xmx8g \
mst.DependencyEnglish2OProjParser ${MODEL_DIR}/wsj.model ${TEMP_DIR} ${MST_PORT}
