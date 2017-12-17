# Viterbi-Algorithm

The algorithm finds the most likely path between a series of states by computing the max probability of a current state based on a series of previous probabilities.  Formuaically for every state (tag) s' calculate the max of product v[s', t-1] * a[s', s] * b[s, w_t] where "a" is our transition probability matrix, "b" is our emission probability matrix, w_t is the current word, and "v" is a trellis of Cell objects. 

We implement this with a HashMap of type <String, HashMap<String, Cell>>.  The key of the outer HashMap is the word being evaluated, the key of the HashMap<String, Cell> is the tag being evaluated.  The Cell object contains the probability of a word being a specific tag and a backpointer to the cell of the previous word that was the most probable. 

To run the code you need vocabulary.txt, a.txt, and b.txt files.  The a.txt file would be the transition probabilities and the b.txt file would the emission probabilities.
