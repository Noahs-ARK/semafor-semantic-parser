    SEMAFOR (a frame-semantic parser for English)
    Copyright (C) 2012
    Dipanjan Das, Andre Martins, Nathan Schneider, Desai Chen, & Noah A. Smith
    Language Technologies Institute, Carnegie Mellon University
    http://www.ark.cs.cmu.edu/SEMAFOR

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.



SEMAFOR v2.1
============

SEMAFOR: Semantic Analysis of Frame Representations is a tool for automatic analysis of the frame-semantic structure of English text.

FrameNet (http://framenet.icsi.berkeley.edu) is a lexical resource that groups predicates in a hierarchy of structured concepts, known as *frames*. Each frame in the lexicon in turn defines several named *roles* corresponding to aspects of that concept (e.g. participants in an event).

This tool attempts to find which words in text evoke which semantic frames, and to find and label each frame's *arguments*--portions of the sentence that fill a role associated with the frame.
It takes as input a file with English sentences, one per line, and performs the following steps:

  0. Preprocessing
     The sentences are lemmatized, part-of-speech tagged, and syntactically parsed (optionally using a syntactic parsing running in server mode.)

  1. Target identification
     Frame-evoking words and phrases ("targets") are heuristically identified in each sentence.

  2. Frame identification
     A log-linear model, trained on FrameNet 1.5 data with full-text frame annotations, produces for each target a probability distribution over frames in the FrameNet lexicon (optionally constrained by a semi-supervised filter). The target is then labeled with the highest-scoring frame.

  3. Argument identification
     A second log-linear model, trained on the same data, considers every role of each labeled frame instance and identifies a span of words in the sentence--or NULL--as filling that role. A subsequent step ensures that none of a frame's overt arguments overlap using beam search; an alternate strategy
using AD^3 (or Alternating Directions Dual Decomposition) uses two other constraints used in FrameNet for argument identification.

  4. Output
     An XML file is produced containing the text of the input sentences, augmented with the frame-semantic information (target-frame and argument-role pairings) predicted by the system. See the papers listed below ("Further Reading") for algorithmic details and experimental evaluation of the components of this system.

An example frame-semantic parse of a sentence is shown below:

![An example frame-semantic parse](http://www.ark.cs.cmu.edu/SEMAFOR/img/g3109.png)

Each row under the sentence correponds to a semantic frame and its set of corresponding arguments.
Thick lines indicate targets that evoke frames;
thin solid/dotted lines with labels indicate arguments.
N_m under “bells” is short for the Noise_maker role of the NOISE_MAKERS frame.


What follows is an overview of the organization of SEMAFOR's directory structure, and how it can be installed and run on new data.


Requirements
============

Running the SEMAFOR tool *requires* Java 1.6 or later. It should run on any platform (Windows, Unix, or Mac OS).

For Mac:
Install "brew install coreutils"

Contents
========

dict/
    WordNet data files. License information can be viewed at: 
    http://wordnet.princeton.edu/wordnet/license/

edu/ 

riso/
    Source files of the SEMAFOR project.

lib/
    Java libraries required for this project, as detailed below.

scripts/
    Executables required for preprocessing raw text.

release/
    Installation and execution code.

samples/
    Some sample text on which SEMAFOR can be run.

file_properties.xml
    File containing WordNet lookup paths.

stopwords.txt
    File containing stopwords used while looking up WordNet


LICENSE
    Text of the GNU General Public License, Version 3.

README
    This file.


Included Libraries
==================

The lib/ directory contains the following library:

semafor-deps.jar: It contains all the required libraries that SEMAFOR needs to run.


Installation
============

Downloads
---------

SEMAFOR v2.1 can be downloaded from http://www.ark.cs.cmu.edu/SEMAFOR/SEMAFOR-2.1.tgz (~56MB).

In preprocessing, SEMAFOR uses the MST Parser, a syntactic dependency parser created by Ryan McDonald. Download and unpack the following packaged version:
http://www.ark.cs.cmu.edu/SEMAFOR/stackedParserServer.tgz (~4MB).
The original MST parser has been modified to add an option of running it in server mode, for ease of use.

Download the model files both for the MST parser and SEMAFOR from here: http://www.ark.cs.cmu.edu/SEMAFOR/SEMAFOR-2.1-models.tgz (~400MB).
The model file for the MST parser was trained on sections 02-21 of the WSJ section of the Penn Treebank, and the model files 
for SEMAFOR were trained on the FrameNet 1.5 datasets.


Environment Variables
---------------------

The file named "config" under release/ lists a set of variables which should be modified within the file before running SEMAFOR:

SEMAFOR_HOME : absolute path where semaforV2.1.tgz has been decompressed.

TEMP_DIR : absolute path to a temporary directory for SEMAFOR to use.

MST_MODE : the provided MST parser package can now run in "server" or "noserver" modes; this variable should be set accordingly. When the parser is running on 
server mode, SEMAFOR takes several minutes less to initialize its models; hence, it is a very advantageous mode given that one has a high memory machine on which 
the MST parser can run.

MST_PARSER_HOME : absolute path where stackedParserServer.tgz has been decompressed.

MST_MACHINE : if MST parser is running on server mode, this variable should be set to the machine's name or IP address

MST_PORT : the port at which the MST parser is running on ${MST_MACHINE}. 

JAVA_HOME_BIN : the absolute path to the bin directory under which the executables javac and java can be found. If the environment variable ${JAVA_HOME} is set, 
${JAVA_HOME_BIN} should be ${JAVA_HOME}/bin.

MODEL_DIR : the absolute path where the tarball SEMAFOR-2.1-models.tgz has been decompressed.

GOLD_TARGET_FILE : the current version of SEMAFOR has a setting where input targets can be provided to it -- this is beneficial for users who are interested 
in a specific set of targets instead of all targets that SEMAFOR identifies using inbuilt heuristics (the target identification stage of the pipeline). To do this, this variable should be set to the absolute path of a file that contains space separated target spans for each sentence, per line. Take a look a ${SEMAFOR_HOME}/samples/sample_gold_targets.txt which corresponds to the input targets for sentences in ${SEMAFOR_HOME}/samples/sample_tokenized.txt. For best results, if an input target file is used, the user should provide tokenized sentences to SEMAFOR, because in this mode, SEMAFOR does not run inbuilt tokenization. If there is no input target file, this variable should be set to null.

AUTO_TARGET_ID_MODE : SEMAFOR has two automatic target identification models: "strict" and "relaxed". The "strict" mode uses morphological variants of all the targets seen in the FrameNet 1.5 lexicon and its training data. The "relaxed" mode labels all content words other than proper nouns as frame-evoking targets. Default is the "strict" mode.

USE_GRAPH_FILE : this should be set to "yes" or "no" depending on whether the user wants to used semi-supervised constraints for frame identification (see Das and Smith, 2011, 2012). This flag does not matter much when strict target identification is used. However, if the user uses an input target file and there are several targets which SEMAFOR did not see during supervised training, then setting the value of this environment variable will yield better results. 
The current release uses the better graph-based semi-supervised learning technique presented by Das and Smith (2012) for the filter.

DECODING_TYPE : this should be "beam" or "ad3" depending on whether the user wants fast inexact beam search that prevents argument overlap
or AD^3, which is an exact dual decomposition algorithm that respects the overlap constraints, as well as two other linguistic constraints.
For more details, see Das et al. (*SEM 2012).


Compilation
-----------

Although Java 1.6 class files are included in this release both for SEMAFOR and the MST parser, one can compile both packages anew.
Assuming that the user is at the root of the directory where SEMAFOR was decompressed, run:

$  cd release/
$  ./cleanAndCompile.sh

For OSX: Before compiling change, we need to update the "readlink" in all the scripts in the "release/" folder with "greadlink". You can also "alias" the "readlink" with "greadlink".

Running the Frame-Semantic Parser
=================================

If the MST parser is to be run in server mode, in other words, if MST_MODE=server, then before running SEMAFOR, 
the user should log on to the chosen server machine (MST_MACHINE), install SEMAFOR exactly as described above and run:

$ cd release/
$ ./startMSTServer.sh

The message: "Waiting for Connection on Port: NNNNN" will appear once the server has loaded 
the parsing model and is ready to accept connections (takes a few minutes). Here NNNNN is the 
value of the MST_PORT variable.
After that log on to the machine where you want to run SEMAFOR.

Run the following commands to execute SEMAFOR.
$ cd release/
$ ./fnParserDriver.sh <absolute-path-to-input-file-with-one-sentence-per-line> [<output-file>]


Known Issues
============

1. Currently, SEMAFOR requires 8GB of RAM to execute because of its dependence on the MST parser,
which loads a large model trained on the English Penn Treebank.	
2. The output of SEMAFOR is an XML file. If there are several thousand sentences to be parsed
by SEMAFOR, then it becomes cumbersome to view the XML file. Modifying fnParserDriver.sh to 
view raw text versions of SEMAFOR output is possible.


Further Reading
===============

If this parser is used, please cite the following papers, depending on the components used:

1. An Exact Dual Decomposition Algorithm for Shallow Semantic Parsing with Constraints
      Dipanjan Das, André F. T. Martins, and Noah A. Smith
      Proceedings of *SEM 2012
(Please cite the above paper if you use AD^3 within SEMAFOR.)


2. Graph-Based Lexicon Expansion with Sparsity-Inducing Penalties
      Dipanjan Das and Noah A. Smith
      Proceedings of NAACL 2012


3. Semi-Supervised Frame-Semantic Parsing for Unknown Predicates
	Dipanjan Das and Noah A. Smith
	Proceedings of ACL 2011
(Please cite the above two papers if you use the graph-based filters within SEMAFOR.)


4. Probabilistic Frame-Semantic Parsing
	Dipanjan Das, Nathan Schneider, Desai Chen, and Noah A. Smith
	Proceedings of NAACL-HLT 2010
(The first paper describing SEMAFOR.)


For further information, please read:
5. SEMAFOR 1.0: A Probabilistic Frame-Semantic Parser
	Dipanjan Das, Nathan Schneider, Desai Chen, and Noah A. Smith
	CMU Technical Report, CMU-LTI-10-001

6. Semi-Supervised and Latent Variable Models of Natural Language Semantics
        Dipanjan Das
        Ph.D. Thesis, Carnegie Mellon University, May 2012

Details of the training and test sections of the FrameNet 1.5 datasets can be 
found in paper 3. The supplementary 
material document for this paper lists the names of the test documents, 
and can be found here: http://www.dipanjandas.com/files/acl-hlt2011-suppl-semafor.pdf


Contact
=======

If you find any bugs or have questions, please email Dipanjan Das (dipanjan@cs.cmu.edu, dipanjand@gmail.com) or Nathan Schneider (nschneid@cs.cmu.edu, neatnate@gmail.com).


Version History
===============

1.0 - First public release (2010-04-26)
1.0.1 - Second public release (2010-09-02)
     Now includes compiled Java .class files for SEMAFOR, so compiling from source is 
     not necessary.
     
     Now includes the SemEval 2007 Task 19 dataset and evaluation resources (see
     README.txt in the SemEval2007/ directory for details).
     
     Bug fixes: 
     1) Fixed an issue of tokenization with 'sed'. Now uses 'sed -f' explicitly
        instead of running the script as an executable.
     2) Included 'mkdir tmp' in the MST parser root directory. This is necessary 
        for running the MST parser.
     3) At the top of the 'mxpost' script that does POS tagging, changed '#!/bin/ksh'
        to '#!/bin/bash' because the former did not run in some machines.
2.0 Third public release (2011-04-23)
    New features:
    1) Added server mode to the MST parser.
    2) Trained on newer, much larger dataset from FrameNet 1.5.
    3) Users can input their own targets file, suiting different domains and requirements.
    4) One class file does all the pipeline processing unlike the previous versions.
    5) The argument identification stage has been made much faster.
    6) Previous releases could not handle large set of sentences; that has been fixed.
    7) Added source to googlecode.com for open source development.
    8) Semi-supervised graph-based constraints have been added for frame identification.

2.1 Fourth public release (2012-05-22)
    New features:
    1) Beam width for beam search was reduced after noticing that it makes no difference empirically
       in FrameNet datasets. This made argument identification much faster. Current beam width=100.
    2) Added a new graph filter based on the NAACL 2012 paper by Das and Smith. This makes frame
       identification more accurate than before.
    3) Finally, added AD^3, a dual decomposition algorithm for argument identitification. See Das et al.
       (*SEM 2012) for more details. Note that the results using the released code will not match
       the results reported in the *SEM paper, because of a new Java implementation different
       from the implementation used for the paper.
