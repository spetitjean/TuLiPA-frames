# TuLiPA-frames

This is an extension for the existing TuLiPA (Tuebingen Linguistic Parsing Architecture), a parser for Lexicalised Tree Adjoining Grammars. 
It is extended with semantic frames paired with the elementary trees. 

## Execution
When you cloned the repostory, you can start the parser with the example grammar by running `run/test.sh`
To start the parser without the example grammar, run 
```
ant jar
java -jar bin/TuLiPA.jar
```

To get more information on the executions of the parser, run `java -jar bin/TuLiPA.jar -h`

## Download
If you want to use an executable version without having to download the code, download `bin/TuLiPA-frames.jar` . 
To run it, run the command `java -jar TuLiPA-frames.jar` 
You can use the test grammar on the parser as described below.

## Test Grammar
The example grammar parses sentences like `John loves Mary` and `John sleeps`. 
The axiom is `s`.
Example Grammars are stored in the folder 'resources/test_grammar'.
The syntactic information and the frames are stored in one file, `verbs_frames.xml`. 
The type hierarchy with respect to which the frames are unified is stored in `more.mac`.
The lexical information processed by TuLiPA is 2-layered. 
The morphological lexicon maps inflected tokens to their lemma, storing morphological information in a feature structure. 
The lemmas are stored among with semantic information and the tree families to which the lemmas can be anchored.

To see an example of adjunction, run `run/test_adj.sh` or use `verbs_frames_adjunction.xml` as a Grammar file. 
This grammar parses sentences like `John really loves Mary`.
