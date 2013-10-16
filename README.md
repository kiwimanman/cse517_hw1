cse517_hw1
==========

Keith Stone

Fall 2013


How to run
----------
```bash
cd $PROJECT_ROOT/src
mkdir classes
javac -d classes */*/*/*.java */*/*/*/*.java
java -cp classes edu.berkeley.nlp.Test
java -Xmx2g -cp classes edu.berkeley.nlp.assignments.LanguageModelTester $YOUR_ARGS_HERE
```

$YOU_ARGS_HERE will almost always start with -path data1.

List of args
------------

-path 

    path to the data files

-model

    [ baseline, ngram, linear, smooth]
    
-n 

    Max ngram level to build model to.
    
-vector

    Comma seperated list of weights to be calculated in the linear model. Must sum to one, cannot be negative. First one applies to unigram, then bigram and so forth.
    
-b

    Discounting factor. Must be between 0 and 1.
    
-search

    Turns on searching for ideal parameters and then uses the best one. Using with a vector will start the search at the vector and when used with b will search all b values from 0.1 to 0.9 in steps of 0.1.
    
    
Example args lists
------------------

These do not include the path param:

```-model ngram -n 3```

```-model linear -n 4 -vector 0.05,0.1,0.1,0.75 -search```

    Performs a search from the starting vector and will likely test with a better one
    
```-model smooth -n 2 -b 0.7```

```-model smooth -n 3 -b 0.7 -search```

