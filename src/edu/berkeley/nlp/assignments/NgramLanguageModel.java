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

  static final String STOP     = "</S>";
  static final String UNKNOWN  = "*UNKNOWN*";
  static final List<String> BLANK_LIST = new ArrayList<String>();

  Map<Integer, CounterMap<List<String>, String>> wordCounter = new HashMap<Integer, CounterMap<List<String>, String>>();
  int ngram = 2;
  List<Double> interpolation_vector = new ArrayList<Double>();

  public void setInterpolationVector(List<Double> value) {
      this.interpolation_vector = value;
  }

  public void setBeta(double value) {
  }

  public double getWordProbability(List<String> given, String word, int gram) {
      CounterMap<List<String>, String> counter;
      double count;
      double total;

      counter = wordCounter.get(gram);
      count = counter.getCount(given, word);
      total = counter.getCounter(given).totalCount();

      if (count == 0) {
          // System.out.println("UNKNOWN WORD: "+sentence.get(index));
          return 1.0 / (total + 1.0);
      }
      return count / (total + 1.0);
  }

  public double getWordProbability(List<String> given, String word) {
      double probability = 0.0;
      for (int i = ngram; i > 0; i--) {
          double factor = interpolation_vector.get(i - 1) * getWordProbability(given, word, i);
          given = prepareGiven(given, given.size(), i - 1);
          probability += factor;
      }
      if (probability == 0.0) {
          System.out.println("Possible underflow situation");
      }
      return probability;
  }

  public double getWordProbability(List<String> sentence, int index) {
      List<String> given;
      given = prepareGiven(sentence, index, ngram);
      double probability = getWordProbability(given, sentence.get(index));
      if (probability == 0.0) {
          System.out.println("Possible underflow situation");
      }
      return probability;
  }

  public double getSentenceProbability(List<String> sentence) {
    List<String> stoppedSentence = prepareSentence(sentence);
    double probability = 1.0;
    for (int index = 0; index < stoppedSentence.size(); index++) {
      double factor = getWordProbability(stoppedSentence, index);
      probability *= factor;
    }
    return probability;
  }

  protected List<String> prepareGiven(List<String> sentence, int index, int grams) {
      int start_index = Math.max(0, index - grams + 1);
      return start_index >= index ? BLANK_LIST : sentence.subList(start_index, index);
  }

  String generateWord(List<String> given) {
      double sample = Math.random();
      double sum = 0.0;
      for (int i = 1; i <= ngram; i++) {
          sum += interpolation_vector.get(i - 1);
          if (sum > sample) {
              return generateWord(given, i);
          }
      }
      return UNKNOWN;
  }

  String generateWord(List<String> given, int ngram) {
    double sample = Math.random();

    double sum = 0.0;
    Counter<String> counter = wordCounter.get(ngram).getCounter(given);
    for (String word : counter.keySet()) {
        sum += counter.getCount(word) / (counter.size() * 1.0);
      if (sum > sample) {
        return word;
      }
    }
    return UNKNOWN;
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
    List<String> stoppedSentence =  new ArrayList<String>(sentence);
    stoppedSentence.add(STOP);
    return stoppedSentence;
  }

  private void countSentences(Collection<List<String>> sentenceCollection) {
      // Build possible set of counters
      for (int i = ngram; i > 0; i--) {
          wordCounter.put(i, new CounterMap<List<String>, String>());
      }

      // At each index for each ngram, count.
      for (List<String> sentence : sentenceCollection) {
          List<String> stoppedSentence = prepareSentence(sentence);
          for (int gram = ngram; gram > 0; gram--) {
              for (int i = 0; i < stoppedSentence.size(); i++) {
                  List<String> given = prepareGiven(stoppedSentence, i ,gram);
                  wordCounter.get(gram).incrementCount(given, stoppedSentence.get(i), 1.0);
              }
          }
      }
  }

  public NgramLanguageModel(Collection<List<String>> sentenceCollection, int ngram) {
      // The basic ngram is simply a special case of the linear one
      for (int i = 0; i < ngram - 1; i++) {
          interpolation_vector.add(0.0);
      }
      interpolation_vector.add(1.0);
      this.ngram = ngram;

      countSentences(sentenceCollection);
  }

  public NgramLanguageModel(Collection<List<String>> sentenceCollection, int ngram, List<Double> interpolation_vector) {
    this.interpolation_vector = interpolation_vector;
    this.ngram = ngram;
    countSentences(sentenceCollection);
  }
}
