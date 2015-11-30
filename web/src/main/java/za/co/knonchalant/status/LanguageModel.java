package za.co.knonchalant.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by evan on 15/02/22.
 */
public class LanguageModel {
    private static final String START = "<START>";
    private static final String END = "<END>";
    private static final int WORD_CUT_OFF = 80;

    private int gramSize;
    private HashMap<Integer, HashMap<String, Gram>> grams = new HashMap<>();

    public LanguageModel(int gramSize) {
        this.gramSize = gramSize;
    }

    public void addSentence(List<String> words) {
        ArrayList<String> workingWords = new ArrayList<>(words);

        workingWords.add(0, START);
        workingWords.add(END);

        for (int i = gramSize; i > 0; i--) {
            addSentenceForGram(i, workingWords);
        }
    }

    private void addSentenceForGram(int gramSize, List<String> words) {
        if (words.size() < gramSize) {
            return;
        }

        if (grams.get(gramSize) == null) {
            grams.put(gramSize, new HashMap<String, Gram>());
        }

        HashMap<String, Gram> currentGram = grams.get(gramSize);

        for (int i = 0; i < words.size() - gramSize - 1; i++) {
            StringBuilder gram = new StringBuilder();
            for (int j = 0; j < gramSize; j++) {
                gram.append(words.get(i + j)).append(" ");
            }
            String resultGram = gram.toString().trim();
            if (!currentGram.containsKey(resultGram)) {
                currentGram.put(resultGram, new Gram(resultGram));
            }

            currentGram.get(resultGram).addWord(words.get(i + gramSize));
        }
    }

    public void calculate() {
        for (int i = gramSize; i > 0; i--) {
            HashMap<String, Gram> stringGramHashMap = grams.get(i);
            if (stringGramHashMap == null) {
                continue;
            }
            for (Gram gram : stringGramHashMap.values()) {
                gram.calculate();
            }
        }
    }

    public String generateSentence() {
        String currentSentence = START;
        String nextWord = getNextWord(currentSentence);
        int totalWords = 0;
        while (nextWord != null) {
            currentSentence += " " + nextWord;
            totalWords++;
            if (totalWords > WORD_CUT_OFF) {
                return currentSentence.substring(START.length()+1) + "...";
            }
            nextWord = getNextWord(currentSentence);
        }

        currentSentence = currentSentence.replace(END, "");
        return currentSentence.substring(START.length() + 1);
    }

    private String getNextWord(String currentSentence) {
        for (int i = gramSize; i > 0; i--) {
            HashMap<String, Gram> currGram = grams.get(i);
            String checkGram = getGram(currentSentence, i);
            if (currGram.containsKey(checkGram)) {
                return currGram.get(checkGram).generateWord();
            }
        }
        return null;
    }

    private String getGram(String currentSentence, int gram) {
        String original = currentSentence;
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < gram; i++) {
            int index = currentSentence.lastIndexOf(" ");
            if (index != -1) {
                result.insert(0, currentSentence.substring(index));
                currentSentence = currentSentence.substring(0, index);
            } else {
                return original;
            }
        }

        return result.toString().trim();
    }

}
