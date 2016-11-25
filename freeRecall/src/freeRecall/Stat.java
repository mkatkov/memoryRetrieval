/*
 */

package freeRecall;

/**
 * Collect the statistics from the trials running in Session
 * @author katkov
 */
public class Stat {
    
    Session ses;
    public boolean storeWordsP, storeTransitionMatrixP, 
            storeTransitionsP, computeRecallP;
    public int presentedWords[][], recalledWords[][], nRecalled[], 
            recalledPositions[][], transitions[][], recalledTime[][],
            positionHist[], nRecHist[], chainHist[];
    public double transitionMatrix[][][];
    
    public void prepare( int nTrials, Session ses ){
        this.ses= ses;
        
        if( storeWordsP ){
            presentedWords= new int[nTrials][ses.listSize];
        }
        if( storeTransitionMatrixP ){
            transitionMatrix= new double[ nTrials][ses.listSize][ses.listSize];
        }
        if( storeTransitionsP ){
            transitions= new int[nTrials][ ses.trialSim.getNumTransitions()];
        }
        if(computeRecallP){
            recalledWords= new int[nTrials][ses.listSize];
            nRecalled= new int[nTrials];
            recalledPositions=new int[nTrials][ses.listSize];
            recalledTime= new int[nTrials][ses.listSize];
            positionHist= new int[ses.listSize];
            nRecHist= new int[ses.listSize];
            chainHist= new int[ses.listSize*2+1];
            for(int k=0; k<ses.listSize; k++ ){
                positionHist[k]=0;
                nRecHist[k]=0;
            }
            for(int k=0; k<ses.listSize*2+1; k++ ){
                chainHist[k]=0;
            }
        }
    }
    
    public void store( int trIdx ){
        if( storeWordsP ){            
            System.arraycopy(ses.wordList, 0, presentedWords[trIdx], 0, ses.wordList.length);
        }
        if( storeTransitionMatrixP ){
            for( int k1=0; k1< ses.transitionMatrix.length; k1++ ){
                System.arraycopy(ses.transitionMatrix[k1], 0, transitionMatrix[trIdx][k1], 0, ses.transitionMatrix.length);                
            }
         }
        if( storeTransitionsP ){
            int resp[]=ses.trialSim.getResp();
           System.arraycopy( resp, 0, transitions[trIdx], 0, resp.length);
        }
        if(computeRecallP){
            int recall[]= new int[ses.listSize],
                    recallTime[]= new int[ses.listSize],
                    reqSeq[]= new int[ses.listSize],
                    reqWords[]= new int[ses.listSize];
            int w, k1=0;
            int resp[]=ses.trialSim.getResp();
            for(int k=0; k< resp.length; k++ ){
                w= resp[k];
                if(w<0)
                    break;
                if( recall[w] == 0 ){
                    recall[w]= 1;
                    recallTime[k1]= k;
                    reqSeq[k1]= w;
                    reqWords[k1]= ses.wordList[w]+1;
                    k1++;
                    positionHist[w]++;
                }
            } 
            nRecalled[trIdx]= k1;
            nRecHist[k1-1]++;
            System.arraycopy( recallTime, 0, recalledTime[trIdx], 0, k1 );
            System.arraycopy( reqSeq, 0, recalledPositions[trIdx], 0, k1 );
            System.arraycopy( reqWords, 0, recalledWords[trIdx], 0, k1 );
            
            // compute chains
            int state= 0; // -1 backward chain, 0 alone, 1 forward
            int diff, count=1;
            for(int k=1; k< k1; k++ ){
                diff= reqSeq[k]- reqSeq[k-1];
                //System.out.println( state+" diff: "+ diff+" count: "+ count);
                if( diff == 1 ){
                    // forward chain
                    if( (state== 0) || (state== 1) ){
                        count++;
                        state=1;
                    } else { // this is not possible
                        chainHist[ ses.listSize - count ]++;
                        count = 2;
                        state= 1;
                    }
                } else if( diff== -1){
                    // backward chain
                    if( (state== 0) || (state== -1) ){
                        count++;
                        state= -1;
                    } else { // this is not possible
                        chainHist[ ses.listSize + count ]++;
                        count = 2;
                        state= -1;
                    }
                } else {
                    // stand alone.
                    if( state == 0 ){
                        chainHist[ ses.listSize ]++;
                    } else if ( state==1 ){
                        chainHist[ ses.listSize + count ]++;
                        count= 1;
                        state= 0;
                    } else {
                        chainHist[ ses.listSize - count ]++;
                        count= 1;
                        state= 0;                        
                    }
                }
            }
                    if( state == 0 ){
                        chainHist[ ses.listSize ]++;
                    } else if ( state==1 ){
                        chainHist[ ses.listSize + count ]++;
                    } else {
                        chainHist[ ses.listSize - count ]++;
                    }
        }
    }
}
