#!/bin/sh

source config

if [ $# -lt 1 -o $# -gt 2 ]; then
   echo "USAGE: `basename "${0}"` <input-file> [<output-file>]"
   exit 1
fi

if [ `uname -m` != "x86_64" ]; then
   echo -n "\nNOTE: You should really be running this on a 64-bit architecture."
   # give the user the chance to CTRL-C here...
   for dot in 1 2 3 4 5 6; do
       sleep 1
       echo -n "."
   done
   echo
fi

# $3: location of line split file, must be absolute path
INPUT_FILE=`readlink -f "${1}"`

# output of FN parser
if [ $# = 2 ]; then
   OUTPUT_FILE=`readlink -f "${2}"`
else
   OUTPUT_FILE="${INPUT_FILE}.out"
fi


CLEAN_INPUT=${TEMP_DIR}/$$.input
grep -v '^\s*$' ${INPUT_FILE} > ${CLEAN_INPUT}
INPUT_FILE=${CLEAN_INPUT}

# The MST dependency parser assumes (hard-wired) that there is a temp
# directory "tmp" under its home directory, so we want to make sure
# that this directory exists.
if [ ! -d "${MST_PARSER_HOME}/tmp" ]; then
   mkdir "${MST_PARSER_HOME}/tmp"
   REMOVE_DOT_TMP=1
else
   REMOVE_DOT_TMP=0
fi

# run semafor script
/bin/sh ${SEMAFOR_HOME}/release/runFNParser.sh \
"${SEMAFOR_HOME}" \
"${TEMP_DIR}" \
"${INPUT_FILE}" \
"${OUTPUT_FILE}" \
"${MST_PARSER_HOME}" \
"${JAVA_HOME_BIN}" \
"${MODEL_DIR}" \
${MST_PORT}

# clean up
if [ ${REMOVE_DOT_TMP} = 1 ]; then
   /bin/rm -rf "${MST_PARSER_HOME}/tmp"
fi
