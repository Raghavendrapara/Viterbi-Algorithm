package hmm;

public class Cell {
    public Cell backpointer;
    public double prob;
    public String tag;
    public String word;

    public Cell(String word, String tag, Cell backpointer, double prob){
        this.word = word;
        this.backpointer = backpointer;
        this.tag = tag;
        this.prob = prob;
    }

    public Cell(String word, String tag) {
        this(word, tag, null, 0.0);
    }

}
