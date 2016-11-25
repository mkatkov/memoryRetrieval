/* Util.java
 *
 * helper class to run experiment
 */

package freeRecall;

import org.uncommons.maths.random.SeedException;

public class Util {
    static public Session ses;
    static public int resp[][], nWords, nTrials, listSize, wordList[][];

    static public void PrepareExp(int nWords, int nTrials, int listSize, int nTransitions ) throws SeedException{
        Util. nTrials= nTrials;
        Util. nWords= nWords;
        Util. listSize= listSize;

       // resp = new int[nTrials][ nTransitions ];
        wordList= new int[nTrials][listSize];
        
        ses= new Session(nWords);
        ses.setListSize( listSize );
        
    }
    
    public static int chooseItem( double rnd, double p[] ){
        int l=0, r= p.length-1, m;
        while( l!=r ){
             m= (l+r)/2;
            if( p[m] < rnd ){
                l=m+1;
            } else {
                r=m;
            }
        }
        return l;
    }

}
