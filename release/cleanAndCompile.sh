#!/bin/sh

source config
cd ${SEMAFOR_HOME}
pwd
classpath=".:./lib/semafor-deps.jar"

find edu \( -name "*.class" \) -exec rm '{}' \;
${JAVA_HOME_BIN}/javac -cp ${classpath} edu/cmu/cs/lti/ark/fn/data/prep/CoNLLInputPreparation.java
${JAVA_HOME_BIN}/javac -cp ${classpath} edu/cmu/cs/lti/ark/fn/data/prep/AllAnnotationsMergingWithoutNE.java
${JAVA_HOME_BIN}/javac -cp ${classpath} edu/cmu/cs/lti/ark/fn/identification/ConvertAlphabetFile.java
${JAVA_HOME_BIN}/javac -cp ${classpath} edu/cmu/cs/lti/ark/fn/identification/FrameIdentificationRelease.java
${JAVA_HOME_BIN}/javac -cp ${classpath} edu/cmu/cs/lti/ark/fn/identification/FrameIdentificationGoldTargets.java
${JAVA_HOME_BIN}/javac -cp ${classpath} edu/cmu/cs/lti/ark/fn/parsing/CreateAlphabet.java
${JAVA_HOME_BIN}/javac -cp ${classpath} edu/cmu/cs/lti/ark/fn/parsing/DecodingMainArgs.java

cd ${MST_PARSER_HOME}
pwd
find mst \( -name "*.class" \) -exec rm '{}' \;
${JAVA_HOME_BIN}/javac -cp ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" mst/DependencyEnglish2OProjParser.java
${JAVA_HOME_BIN}/javac -cp ".:./lib/trove.jar:./lib/mallet.jar:./lib/mallet-deps.jar" mst/DependencyParser.java