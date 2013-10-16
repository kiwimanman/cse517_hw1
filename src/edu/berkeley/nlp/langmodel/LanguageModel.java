package edu.berkeley.nlp.langmodel;

import java.util.List;

/**
 * Language models assign probabilities to sentences and generate sentences.
 * @author Dan Klein
 */
public interface LanguageModel {
  double getSentenceProbability(List<String> sentence);
  List<String> generateSentence();
  void setInterpolationVector(List<Double> value);
  void setBeta(double value);
  double getWordProbability(List<String> given, String word);
}
