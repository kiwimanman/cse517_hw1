package edu.berkeley.nlp.assignments;

import edu.berkeley.nlp.langmodel.LanguageModel;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.CounterMap;

import java.util.*;

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
  Map<Integer, CounterMap<List<String>, String>> wordCounter = new HashMap<Integer, CounterMap<List<String>, String>>();
  int ngram = 2;
  List<Double> interpolation_vector = null;

  public double getWordProbability(List<String> sentence, int index) {
    List<String> given = prepareGiven(sentence, index);
    double count = wordCounter.get(ngram).getCount(given, sentence.get(index));
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

  private List<String> prepareGiven(List<String> sentence, int index) {
      return prepareGiven(sentence, index, ngram);
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
    Counter<String> counter = wordCounter.get(ngram).getCounter(given);
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


  protected List<String> prepareSentence(List<String> sentence, int gram) {
    for (int i = 0; i < gram - 1; i++) {
      sentence.add(0, START);
    }
    sentence.add(STOP);
    return sentence;
  }

  protected List<String> prepareSentence(List<String> sentence) {
    return prepareSentence(sentence, ngram);
  }

  public NgramLanguageModel(Collection<List<String>> sentenceCollection, int ngram) {
    // Build possible set of counters
    for (int i = ngram; i > 0; i--) {
      wordCounter.put(i, new CounterMap<List<String>, String>());
    }
    // The basic ngram is simply a special case of the linear one
    this.interpolation_vector = new ArrayList<Double>(Arrays.asList(1.0));
    for (int i = 0; i < ngram - 1; i++) {
        interpolation_vector.add(0.0);
    }
    // At each index for each ngram, count.
    List<String> blank_list = new ArrayList<String>();
    for (List<String> sentence : sentenceCollection) {
      for (int gram = ngram; gram > 0; gram--) {
        List<String> stoppedSentence = prepareSentence(new ArrayList<String>(sentence), gram);
        for (int i = gram - 1; i < stoppedSentence.size(); i++) {
          wordCounter.get(gram).incrementCount(
                gram == 1 ? blank_list : stoppedSentence.subList(i - gram + 1, i),
                stoppedSentence.get(i),
                1.0
          );
        }
      }
    }
    total = wordCounter.get(ngram).totalCount();
  }

  public NgramLanguageModel(Collection<List<String>> sentenceCollection, int ngram, List<Double> interpolation_vector) {
    this.interpolation_vector = interpolation_vector;
    for (List<String> sentence : sentenceCollection) {
      List<String> stoppedSentence = prepareSentence(new ArrayList<String>(sentence));
        for (int i = ngram - 1; i < stoppedSentence.size(); i++) {
          wordCounter.get(ngram).incrementCount(
                  stoppedSentence.subList(i - ngram + 1, i),
                  stoppedSentence.get(i),
                  1.0
          );
        }
      }
    total = wordCounter.get(ngram).totalCount();
  }
}
