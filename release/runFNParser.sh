#!/bin/sh

# $1: root of java project
# $2: temp folder location
# $3: location of line split file
# $4: output of FN parser
# $5: location of the MST parser root directory
# $6: java home
# $7: model directory
# $8: graph flag
# $9: use relaxed target identification
# $10: MST port

#************************************ PREPROCESSING *******************************************#
root=$1
jhome=$6
temp=$2
modeldir=$7
echo "Root of Java Project:"
echo ${root}
echo

classpath=".:./lib/semafor-deps.jar"

echo "Tokenizing input: $3"
rm -f ${temp}/file.tokenized
sed -f ${root}/scripts/tokenizer.sed $3 > ${temp}/file.tokenized

echo "POS Tagging tokenized file...."
rm -f ${temp}/file.pos.tagged
cd ${root}/scripts/jmx
./mxpost tagger.project < ${temp}/file.tokenized > ${temp}/file.pos.tagged

echo "Preparing the input for MST Parser..."
rm -f ${temp}/file.conll.input
cd ${root}
$jhome/java -classpath ${classpath} edu.cmu.cs.lti.ark.fn.data.prep.CoNLLInputPreparation ${temp}/file.pos.tagged ${temp}/file.conll.input

rm -f ${temp}/file.conll.output
echo "Dependency parsing the data..."
cd $5
mkdir tmp
$jhome/java -classpath ".:./lib/trove.jar:./lib/mallet-deps.jar:./lib/mallet.jar" -Xms8g -Xmx8g mst.DependencyParser \
test separate-lab \
model-name:${modeldir}/wsj.model \
decode-type:proj order:2 \
test-file:${temp}/file.conll.input \
output-file:${temp}/file.conll.output \
format:CONLL

echo "Merging all annotations...."
cd ${root}
$jhome/java -classpath ${classpath} edu.cmu.cs.lti.ark.fn.data.prep.AllAnnotationsMergingWithoutNE ${temp}/file.tokenized ${temp}/file.conll.output ${temp}/file.all.tags ${root}/lrdata/stopwords.txt ${root}/file_properties.xml ${temp}/file.all.lemma.tags

#**********************************END OF PREPROCESSING********************************************#

