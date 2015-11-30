package za.co.knonchalant.status;

import java.util.HashMap;

/**
 * Created by evan on 15/02/22.
 */
public class Gram {
    private String gramSentence;
    private HashMap<String, Word> words = new HashMap<>();

    public Gram(String gramSentence) {
        this.gramSentence = gramSentence;
    }

    public void addWord(String word) {
        if (!words.containsKey(word)) {
            words.put(word, new Word(word));
        }
        words.get(word).seen();
    }

    public void calculate() {
        int total = 0;
        for (Word word : words.values()) {
            total += word.getCount();
        }

        for (Word word : words.values()) {
            word.setProbability(total);
        }
    }

    public String generateWord() {
        double wordChance = Math.random();
        double current = 0;
        for (Word word : words.values()) {
            if (word.getProbability() + current > wordChance) {
                return word.getWord();
            }
            current += word.getProbability();
        }
        return null;
    }
}
