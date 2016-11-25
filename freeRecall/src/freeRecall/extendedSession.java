/*
 */

package freeRecall;

import org.uncommons.maths.random.*;

/**
 *
 * @author katkov
 */
public class extendedSession extends Session {
    
    public double epsilon, beta, delta, fbRatio;
    public Network net;
    public ContinuousUniformGenerator rng1;
    public double pp[][];
    
    public extendedSession(int numWords) throws SeedException {
        super(numWords);
        rng1= new ContinuousUniformGenerator(0.,1., new MersenneTwisterRNG( new SecureRandomSeedGenerator() ) );
    }
    @Override
    public void setListSize( int listSize )
        throws SeedException {
       super.setListSize(listSize);
       if(pp==null){
          pp= new double[listSize][listSize];
       } else if(pp.length!= listSize ){
          pp= new double[listSize][listSize];           
       }
       populatePP();
    }
    public void setBeta( double beta){
        this.beta= beta;
        populatePP();
    }
    private void populatePP(){
        double ee= Math.exp(-beta);
        // populate last row
        for(int k=0;k< pp.length;k++)
            pp[0][k]= 0.;
        pp[pp.length-1][pp.length-2]= ee;
        pp[pp.length-1][pp.length-1]= 0.;
        for( int k= pp.length-3; k>=0; k-- ){
            pp[pp.length-1][k]= pp[pp.length-1][k+1]*ee;
        }
        for( int k=1; k< pp.length-1; k++ ){
           System.arraycopy( pp[pp.length-1], pp.length-1-k, pp[k], 0, k );
           System.arraycopy( pp[0], 0, pp[k], k, pp.length-k );
        }
        System.arraycopy( pp[pp.length-1], 0, pp[0], 0, pp.length-1 );
        pp[0][pp.length-1]=1.;
        
        for( int k=0; k< pp.length; k++ ){
            for(int k1= 1; k1< pp.length; k1++ ){
                pp[k][k1]+=pp[k][k1-1];
            }
            ee=pp[k][pp.length-1];
            for(int k1= 0; k1< pp.length; k1++ ){
                pp[k][k1]/= ee;
            }
        }
    }
    
    /**
     *
     */
    @Override
      public void prepareTransitionMatrix(){
            // select submatrix
            for( int k1=0; k1< listSize; k1++ ){
                for( int k2=0; k2< listSize; k2++ ){
                    transitionMatrix[k1][k2]= similarityMatrix[wordList[k1]][wordList[k2]];
                }
            }
            // add primacy
             double ws1, ws0;
             //System.out.println( net.wordSize +" "+wordList);
             ws0= net.wordSize[wordList[0]]*delta/net.networkSize;
             for( int k1=1; k1< listSize; k1++ ){
                 ws1= net.wordSize[wordList[k1]]*ws0;
                 transitionMatrix[k1][0]+= ws1;
                 transitionMatrix[0][k1]+= ws1;
             }
             int k3;
             //add contiguity
             for( int k1=1; k1< listSize; k1++ ){
                 ws0= net.wordSize[wordList[k1]]*epsilon/net.networkSize;
                 // select assosiation word
                 k3= Util.chooseItem( rng1.nextValue(), pp[k1] );
                 ws1= net.wordSize[wordList[k3]]*ws0;
                 transitionMatrix[k1][k3]+= ws1*fbRatio;
                 transitionMatrix[k3][k1]+= ws1;
             }             
    } 
    @Override
    public int firstRecalledWord(){
        return Util.chooseItem( rng1.nextValue(), pp[0] );
    }
}
