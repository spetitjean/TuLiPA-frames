# TuLiPA-frames

This is an extension for the existing TuLiPA (Tuebingen Linguistic Parsing Architecture), a parser for Lexicalised Tree Adjoining Grammars. 
It is extended with semantic frames paired with the elementary trees. 

## Execution
You can start the parser with the example grammar by running `./test.sh`
To start the parser without the example grammar, run `java -jar bin/TuLiPA.jar`

To get more information on the executions of the parser, run `java -jar bin/TuLiPA.jar -h`

## Example Grammar
The example grammar parses the sentences `John loves Mary` and `John sleeps`. 
The syntactic information and the frames are stored in one file, `verbs_frames.xml`. 
The type hierarchy with respect to which the frames are unified is stored in `more.mac`.
The lexical information processed by TuLiPA is 2-layered. 
The morphological lexicon maps inflected tokens to their lemma, storing morphological information in a feature structure. 
The lemmas are stored among with semantic information and the tree families to which the lemmas can be anchored.
