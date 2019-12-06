Flower backshop is doing quite a lot already

therea are already a lot of test cases: existing grammars
idea: load the grammars into test classes manually, then test chart etc. on 

run FindBugs or something similar
move to IntelliJ? maybe makes workflow/builds/debugging easier

0. look at rrg.test package!
1. **RRGParseChart**
    - Javadoc
    - tests
    - improve code? Does that improve extraction time? maybe ask for item with an RRGItem.Builder?
2. **RRGNode** 
    - Javadoc. Which names etc. stand for what?
    - tests
    - reimplement
3. **RRGParseResult**
    - JavaDoc 
4. **RRGTree**
    - JavaDoc
    - tests
    - reimplement hashCode, equals, compareTo
5. **RRGParseTree**
    - JavaDoc
    - tests
    - reimplement hashCode, equals, compareTo?
6. **RRGTools**
    - JavaDoc
    - make superfluous in some cases? tests?
7. **EdgeFeatureUnifier**
    - JavaDoc
    - tests
8. **ExtractionStep**
    - JavaDoc
9. **ParseForestExtractor**
    - JavaDoc
    - tests with lots of gramamrs and testing together with ParseForestPostProcessor?
10. **ParseForestExtractor**
    - JavaDoc
    - tests?
11. **ParseTreePostProcessor**
    - JavaDoc
    - tests
12. **FsFromBracketedStringRetriever**
    - tests, some are in io.tests!
13. **RRGXMLBuilder
    - JavaDoc
    - everything tested with flower_backshop?
14. **TreeFromBracketedStringRetriever**
    - JavaDoc
    - everything tested with flower_backshop?
15. **XMLRRGTreeRetriever**
    - JavaDoc
    - everything tested with flower_backshop?
16. **Deducer**
    - JavaDoc complete?
    - tests
17. **RequirementFinder**
    - JavaDoc complete?
    - tests
18. **RRGParseItem**
    - JavaDoc
    - test logical methods with items that are created in test cases anyway?
19. **RRGParser**
    - JavaDoc
    - everything tested implicitly?
20. test cases for non-RRG classes like FS?