/*
 */
package freeRecall;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.uncommons.maths.random.SeedException;

/**
 *
 * @author katkov
 */
public class ChunkedTrial extends SoftMaxTrial {

    private final SortedMap<Double, Integer> map;
    //private Collection<Integer> wordsInCurChunk;
    private Iterator<Integer> curChunkIterator;

    public int[] wordsInChunk, transitionWord, transitionWord2;
    private int outputPos;
    public double [] rngMult;

    public ChunkedTrial() throws SeedException {
        super();
        this.map = new TreeMap<>();
    }

    @Override
    public void setListSize(int listSize) {
        super.setListSize(listSize);
        transitionWord = new int[listSize];
        transitionWord2 = new int[listSize];
        rngMult= new double[listSize];
        for(int k=0; k< listSize; k++)
            rngMult[k]=1.;
    }

    private void fillWordsInChunk(int n) {
        // first find chunk num
        int chunkNum, startWord;
        for (chunkNum = 0, startWord = 0;
                n - startWord >= wordsInChunk[chunkNum];
                chunkNum++) {
            //System.out.println("startWord:"+startWord+" chunkNum:"+chunkNum+
            //        " n:"+n+" wordsInChunk[chunkNum]:"+wordsInChunk[chunkNum]);
            startWord += wordsInChunk[chunkNum];
        }
        // fill chunk words
        map.clear();
        for (int k = startWord; k < wordsInChunk[chunkNum] + startWord; k++) {
            if (k == n) {
                continue;
            }
            map.put(rng.nextValue()*rngMult[k], k);
        }
        // permute chunk words
        //wordsInCurChunk= map.values();
        curChunkIterator = map.values().iterator();
    }

    @Override
    public void trial(double transitionMatrix[][], int startItem) {
        for (int k = 0; k < transitionMatrix.length; k++) {
            int trWord = transitionMatrix.length - k - 1, trWord2=-1;
            if (trWord == k) {
                trWord = k + 1;
            }
            double trVal = transitionMatrix[k][trWord], trVal2=-1.;
            
            for (int k2 = 0; k2 < transitionMatrix[k].length; k2++) {
                if (k2 == k) {
                    continue;
                }
                if (trVal < transitionMatrix[k][k2]) {
                    trVal = transitionMatrix[k][k2];
                    trWord = k2;
                }
            }
            transitionWord[k] = trWord;
            for (int k2 = 0; k2 < transitionMatrix[k].length; k2++) {
                if (k2 == k || k2== trWord ) {
                    continue;
                }
                if (trVal2 < transitionMatrix[k][k2]) {
                    trVal2 = transitionMatrix[k][k2];
                    trWord2 = k2;
                }
            }
            transitionWord2[k] = trWord2;
        }
        resp[0]= startItem;
        fillWordsInChunk(startItem);
        for (outputPos = 1; outputPos < resp.length; outputPos++) {
            if (curChunkIterator.hasNext()) {
                // recall the whole chunk in random order
                resp[outputPos] = curChunkIterator.next();
            } else {
                // make transition to new word deterministically
                int w= transitionWord[resp[outputPos - 1]];
                if( outputPos>1 && w== resp[outputPos-2]){
                    w= transitionWord2[resp[outputPos - 1]];
                }
                resp[outputPos] = w;
                fillWordsInChunk(resp[outputPos]);
            }
        }
    }

    /**
     * @return the wordsInChunk
     */
    public int[] getWordsInChunk() {
        return wordsInChunk;
    }

    /**
     * @param wordsInChunk the wordsInChunk to set
     */
    public void setWordsInChunk(int[] wordsInChunk) {
        this.wordsInChunk = wordsInChunk;
    }
}
