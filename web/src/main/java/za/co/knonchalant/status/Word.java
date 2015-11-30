package za.co.knonchalant.status;

/**
 * Created by evan on 15/02/22.
 */
public class Word {
    private String word;
    private int count = 0;
    private double probability;

    public Word(String word) {
        this.word = word;
    }

    public void seen() {
        count++;
    }


    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(int total) {
        probability = (double) count / (double) total;
    }

    @Override
    public String toString() {
        return word + " (Probability: " + probability + ")";
    }
}
