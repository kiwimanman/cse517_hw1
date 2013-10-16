package edu.berkeley.nlp.assignments;

import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.CounterMap;

import java.util.*;

public class SmoothedNgramLanguageModel extends NgramLanguageModel {

    Map<String, Double> unigram_discounted_cache = new HashMap<String, Double>();
    double              unigram_remainder = 0.0;

    Map<Integer, CounterMap<List<String>, String>> probabilityCache = new HashMap<Integer, CounterMap<List<String>, String>>();


    double beta = 0.0;

    public double getWordProbability(List<String> given, String word) {
        return getWordProbability(given, word, ngram);
    }

    public double getWordProbability(List<String> given, String word, int gram) {
        CounterMap<List<String>, String> counter;

        double count;
        double total;

        if (gram == 1) {
            if (this.unigram_discounted_cache.containsKey(word)) {
                return unigram_discounted_cache.get(word);
            } else {
                return unigram_remainder;
            }
        }

        counter = wordCounter.get(gram);
        count = counter.getCount(given, word);
        Counter<String> nCounter = counter.getCounter(given);
        total = nCounter.totalCount();

        if (count == 0) {
            if (probabilityCache.get(gram).containsKey(given) &&
                probabilityCache.get(gram).getCounter(given).containsKey(word)) {
              return probabilityCache.get(gram).getCount(given, word);
            } else {
              // System.out.println("UNKNOWN WORD: " + word);
              Set<String> keys = nCounter.keySet();
              double alpha = keys.size() == 0 ? 1.0 : 1.0 - (total - (beta * keys.size())) / total;
              List<String> lesserGiven = given.size() == 0 ? BLANK_LIST : given.subList(1, given.size());
              double sum = 0.0;
              for (String alphaWord : keys) {
                  sum += getWordProbability(lesserGiven, alphaWord, gram - 1);
              }
              double probability =  alpha * getWordProbability(lesserGiven, word, gram - 1) / (1 - sum);
              probabilityCache.get(gram).setCount(given, word, probability);
              return probability;
            }
        }
        return (count - beta) / total;
    }

    public double getWordProbability(List<String> sentence, int index, int gram) {
        List<String> given;

        given = prepareGiven(sentence, index, gram);
        return getWordProbability(given, sentence.get(index), gram);
    }

    public double getWordProbability(List<String> sentence, int index) {
        double probability = getWordProbability(sentence, index, ngram);

        if (probability == 0.0) {
            System.out.println("Possible underflow situation");
        }
        return probability;
    }

    public void setBeta(double value) {
        this.beta = value;
        refreshDiscountCaches();
    }

    public void refreshDiscountCaches() {
        unigram_discounted_cache = new HashMap<String, Double>();

        List<String> given = new ArrayList<String>();
        Counter<String> counter = wordCounter.get(1).getCounter(given);

        for (String word : counter.keySet()) {
            unigram_discounted_cache.put(word, (counter.getCount(word) - beta) / counter.totalCount());
        }
        double sum = 0.0;
        for (Double probability : unigram_discounted_cache.values()) {
            sum += probability;
        }
        unigram_remainder = 1.0 - sum;

        for (int i = 1; i <= ngram; i++) {
          probabilityCache.put(i, new CounterMap<List<String>, String>());
        }
    }

    public SmoothedNgramLanguageModel(Collection<List<String>> sentenceCollection, int ngram) {
        super(sentenceCollection, ngram);
    }

    public SmoothedNgramLanguageModel(Collection<List<String>> sentenceCollection, int ngram, double beta) {
        this(sentenceCollection, ngram);
        this.beta = beta;

        refreshDiscountCaches();
    }
}
