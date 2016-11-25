/*
 */

package freeRecall;

import org.uncommons.maths.random.*;

/**
 * class @Network is used to compute similarity matrix
 * after instantiating an object one needs to use {@link setNumWords} in order to allocate similarity matrix
 * and then use @computeSimilarity to compute @property similarityMatrix
 * 
 * @author katkov
 */
public class Network {

    /**
     * network sparsity - average number of active units encoding an item
     */
    public double f;
    public int networkSize;
    
    public int wordSize[];
    public int similarityMatrix[][];
    
    public ContinuousUniformGenerator rng;
    
    public Network() throws SeedException{
        rng= new ContinuousUniformGenerator(0.,1., new MersenneTwisterRNG( new SecureRandomSeedGenerator() ) );
    }
    
    public void setSimilarityMatrix( int similarityMatrix[][] ){
        this. similarityMatrix= similarityMatrix;
    }
    
    public void setWordsSize( int wordSize[] ){
        this. wordSize= wordSize;
    }
    
    public void setNumWords( int numWords ){
        if( similarityMatrix == null ){
            similarityMatrix= new int[numWords][numWords];
        } else if( similarityMatrix. length != numWords ){
            similarityMatrix= new int[numWords][numWords];            
        } else {
            for (int[] similarityMatrix1 : similarityMatrix) {
                if (similarityMatrix1.length != numWords) {
                    similarityMatrix= new int[numWords][numWords];                    
                    return;
                }
            }
            
        }
    }
    
    public void computeSimilarity( double f ){
        int k1, k2, k3;
        int w1, w2;
        //int wordNodes[]= new int[similarityMatrix.length];
        int selectedWords[]= new int[similarityMatrix.length];
        int curSelectedWord;
        
        this.f= f;
        System.out.println(f);
        wordSize= new int[similarityMatrix.length];
        // clear similarityMatrix
        for( k1= 0; k1< similarityMatrix.length; k1++ ){
            wordSize[k1]= 0;
            for(k2=0; k2< similarityMatrix.length; k2++ )
                similarityMatrix[k1][k2]= 0;
        }
        for( k1=0; k1< networkSize; k1++ ){
            curSelectedWord= 0;
            // selecting words here
            for( k2= 0; k2< similarityMatrix.length; k2++ ){                
                if( rng.nextValue()< f ){
                    // this is 1. - the word is selected
                    selectedWords[ curSelectedWord]= k2;
                    curSelectedWord++;
                    wordSize[k2]++;
                    //System.out.print(k2+" ");
                }
            }
            //System.out.println();
            // update similarity matrix
            for( k2= 0; k2< curSelectedWord; k2++ ){
                for( k3= 0; k3<= k2; k3++ ){
                    w1= selectedWords[k2];
                    w2= selectedWords[k3];
                    similarityMatrix[w1][w2]++;
                    similarityMatrix[w2][w1]++;
                }                
            }
        }
    }
    
    public void computeSimilarity( int networkSize, double f ){        
        this. networkSize= networkSize;
        computeSimilarity(f);
    }
    
}
