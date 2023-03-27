import java.util.List;
import java.util.*;

public class HashListAutocomplete implements Autocompletor {

    private static final int MAX_PREFIX = 10;
    private Map<String, List<Term>> myMap;
    private int mySize;

    public HashListAutocomplete(String[] terms, double[] weights) {
        if (terms == null || weights == null) {
			throw new NullPointerException("One or more arguments null");
		}
        if (terms.length != weights.length) {
			throw new IllegalArgumentException("Terms and weights are not the same length");
		}
		
		initialize(terms,weights);
    }

    @Override
    public List<Term> topMatches(String prefix, int k) {
        // TODO Auto-generated method stub
        if (prefix.length() > MAX_PREFIX) {
            prefix = prefix.substring(0, MAX_PREFIX);
        }

        if (myMap.containsKey(prefix)) {
            List<Term> all = myMap.get(prefix);
            List<Term> list = all.subList(0, Math.min(k, all.size()));
            return list;
        }

        List<Term> empty = new ArrayList<>();
		return empty;
    }

    @Override
    public void initialize(String[] terms, double[] weights) {
        // TODO Auto-generated method stub
        myMap = new HashMap<String, List<Term>>();

        for (int i = 0; i < terms.length; i++) {
            int min = Math.min(MAX_PREFIX, terms[i].length());
            Term term = new Term(terms[i], weights[i]);

            for (int j = 0; j < min + 1; j++) {
                String subterm = terms[i].substring(0, j);
                myMap.putIfAbsent(subterm, new ArrayList<Term>());
                myMap.get(subterm).add(term);
            } 
        }
        
        for (String key : myMap.keySet()) {
            Collections.sort(myMap.get(key), Comparator.comparing(Term::getWeight).reversed());
        }
    }

    @Override
    public int sizeInBytes() {
        // TODO Auto-generated method stub
        if (mySize == 0) {
            for (String key : myMap.keySet()) {
                mySize = mySize + (BYTES_PER_CHAR * key.length());
                List<Term> finalList = myMap.get(key);
                for (int i = 0; i < finalList.size(); i++) {
                    Term t = finalList.get(i);
                    mySize = mySize + ((BYTES_PER_CHAR * t.getWord().length()) + BYTES_PER_DOUBLE);
                }
            }
        }
        return mySize;
    }
    
}

