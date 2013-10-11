package edu.berkeley.nlp.assignments;

import edu.berkeley.nlp.langmodel.LanguageModel;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.CounterMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An ngram language model -- uses empirical ngram counts, plus a single
 * ficticious count for unknown words.
 *
 * @author Keith Stone
 */
class NgramLanguageModel implements LanguageModel {

  static final String STOP  = "</S>";
  static final String START = "<START>";

  double total = 0.0;
  CounterMap<List<String>, String> wordCounter = new CounterMap<List<String>, String>();
  int ngram = 2;

  public double getWordProbability(List<String> sentence, int index) {
    List<String> given = prepareGiven(sentence, index, ngram);
    double count = wordCounter.getCount(given, sentence.get(index));
    if (count == 0) {
//      System.out.println("UNKNOWN WORD: "+sentence.get(index));
      return 1.0 / (total + 1.0);
    }
    return count / (total + 1.0);
  }

  public double getSentenceProbability(List<String> sentence) {
    List<String> stoppedSentence = new ArrayList<String>(sentence);
    stoppedSentence.add(STOP);
    double probability = 1.0;
    for (int index = 0; index < stoppedSentence.size(); index++) {
      probability *= getWordProbability(stoppedSentence, index);
    }
    return probability;
  }

  private List<String> prepareGiven(List<String> sentence, int index, int grams) {
    List<String> given = new ArrayList<String>(sentence.subList(Math.max(index - grams + 1, 0), index));
    while (given.size() < grams - 1) {
        given.add(0, START);
    }
    return given;
  }

  String generateWord(List<String> given) {
    double sample = Math.random();

    double sum = 0.0;
    Counter<String> counter = wordCounter.getCounter(given);
    for (String word : counter.keySet()) {
        sum += counter.getCount(word) / (counter.size() * 1.0);
      if (sum > sample) {
        return word;
      }
    }
    return "*UNKNOWN*";
  }

  public List<String> generateSentence() {
    int i = 0;
    List<String> sentence = new ArrayList<String>();
    String word = generateWord(prepareGiven(sentence, i, ngram));
    while (!word.equals(STOP)) {
      i++;
      sentence.add(word);
      word = generateWord(prepareGiven(sentence, i, ngram));
    }
    return sentence;
  }


  protected List<String> prepareSentence(List<String> sentence) {
    for (int i = 0; i < ngram - 1; i++) {
      sentence.add(0, START);
    }
    sentence.add(STOP);
    return sentence;
  }

  public NgramLanguageModel(Collection<List<String>> sentenceCollection, int ngram) {
    for (List<String> sentence : sentenceCollection) {
      List<String> stoppedSentence = prepareSentence(new ArrayList<String>(sentence));
      for (int i = ngram - 1; i < stoppedSentence.size(); i++) {
        wordCounter.incrementCount(
          stoppedSentence.subList(i - ngram + 1, i),
          stoppedSentence.get(i),
          1.0
        );
      }
    }
    total = wordCounter.totalCount();
  }
}
