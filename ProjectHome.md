First described in the paper "Probabilistic Frame-Semantic Parsing" published at the NAACL 2010 conference, and improved in three papers published at ACL 2011, NAACL 2012 and `*`SEM 2012, this project uses the theory of frame semantics (http://framenet.icsi.berkeley.edu) and statistical machine learning to produce shallow semantic structures from raw natural language text.

An example frame-semantic parse of a sentence is shown below:

![http://www.cs.cmu.edu/~dipanjan/g3109.png](http://www.cs.cmu.edu/~dipanjan/g3109.png)

Each row under the sentence correponds to a semantic frame and its set of corresponding arguments. Thick lines indicate targets that evoke frames; thin solid/dotted lines with labels indicate arguments. N\_m under “bells” is short for the Noise\_maker role of the NOISE\_MAKERS frame.


So far, the SEMAFOR parser has been tested for English, and uses the FrameNet 1.5 lexicon as a reference for analyzing text.

This parser has been developed by Dipanjan Das, Andre Martins, Nathan Schneider, Desai Chen and Noah A. Smith at Carnegie Mellon University.

For more details about NLP activity at Carnegie Mellon, please check out the [ARK research group webpage](http://www.ark.cs.cmu.edu).