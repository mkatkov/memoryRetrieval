/* 
 * single trial soft max recall model
 */

package freeRecall;

import org.uncommons.maths.random.*;

public class SoftMaxTrial implements Trial {
    public int listSize;
    public double transitionMatrix[][];
    public boolean forbidPrevP, forbidSelfP;
    public double transitionProb[];
    public int currentItem, previousItem;
    public double beta;
    
    public int resp[];

    ContinuousUniformGenerator rng;
    
    @Override
    public int[] getResp(){
        return resp;
    }

    public void setNumTransitions( int numTransitions ){
       resp= new int[numTransitions];
    }
    
    /**
     *
     * @return number of transitions
     */
    @Override
    public int getNumTransitions(){
        return resp.length;
    }
    
    /**
     *
     * @param transitionMatrix
     * @param startItem
     */
    @Override
    public void trial( double transitionMatrix[][], int startItem ){
        int k;
        this. transitionMatrix= transitionMatrix;
        setCurrentItem( startItem );
        resp[0]= previousItem=startItem;
        for( k=1; k< resp.length; k++ ){
            if(!forbidPrevP){
                previousItem= -1;
            }
            computeTransitionProb();
            previousItem= currentItem;
            currentItem= chooseItem();
            resp[k]= currentItem;
        }
    }

    public int chooseItem(){
        return Util.chooseItem( rng. nextValue(), transitionProb );
    }

    public void computeTransitionProb( ){
        int k;
        double sumP= 0., maxP= 0.;
        // first compute min transition entry
        for( k=0; k< this. listSize; k++ ){
            if( forbidSelfP ){
                if( k!= currentItem && k!= previousItem ){
                    //System.out.println( k+" "+currentItem+" "+ transitionProb );
                    if( transitionMatrix[currentItem][k] > maxP )
                        maxP= transitionMatrix[currentItem][k];
                }
            } else {
                if( k!= previousItem ){
                    //System.out.println( k+" "+currentItem+" "+ transitionProb );
                    if( transitionMatrix[currentItem][k] > maxP )
                        maxP= transitionMatrix[currentItem][k];
                }                                
            }
        }
        
        for( k=0; k< this. listSize; k++ ){
            if( forbidSelfP ){
                if( k== currentItem || k==previousItem ){
                    transitionProb[k] = 0.;
                } else {
                    //System.out.println( k+" "+currentItem+" "+ transitionProb );
                    transitionProb[k]= Math.exp(beta*(transitionMatrix[currentItem][k]- maxP ) );
                    sumP+= transitionProb[k];
                }
            } else {
                if( k==previousItem ){
                    transitionProb[k] = 0.;
                } else {
                    //System.out.println( k+" "+currentItem+" "+ transitionProb );
                    transitionProb[k]= Math.exp(beta*(transitionMatrix[currentItem][k]- maxP ) );
                    sumP+= transitionProb[k];
                }                
            }
        }
        // normalization
        transitionProb[0]/= sumP;
        for( k=1; k< listSize; k++ ){
            transitionProb[k]/= sumP;
            transitionProb[k]+= transitionProb[k-1];
            //System.out.print( " "+transitionProb[k] );
        }
        //System.out.println();
    }

    public void setBeta(double beta){
        this.beta= beta;
    }

    public void setCurrentItem( int currentItem ){
        this. currentItem= currentItem;
    }

    public void setListSize( int listSize ){
        this.listSize= listSize;
        transitionProb= new double[listSize];
    }

    public SoftMaxTrial( ) throws SeedException {
        rng= new ContinuousUniformGenerator( 0, 1, new MersenneTwisterRNG( new SecureRandomSeedGenerator() ) );
        forbidPrevP= true;
    }

    /**
     *
     * @param transitionMatrix
     */
    @Override
    public void setTransitionMatrix( double transitionMatrix[][] ){
        this. transitionMatrix= transitionMatrix;
    }
}
