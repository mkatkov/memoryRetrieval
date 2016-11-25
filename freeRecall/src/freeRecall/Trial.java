/* Trial.java
 *
 * interface for different implementations of trial
 */

package freeRecall;

/**
 * This an interface to run a single trial
 * 
 * 
 * @author katkov
 */
public interface Trial {

    /**
     * A single trial simulation.
     * Should implement a single trial simulation string the result internally.
     * The size of result should be accessible via {@link #getNumTransitions() }
     * and the result of simulation itself as a sequence of retrieved items in 
     * {@link #getResp() }. Both last functions are used by the {@link Stat} class 
     * to collect the information on the recall
     * 
     * @param transitionMatrix inter-item transition matrix
     * @param startItem the index of the first item to start the recall
     */
    public void trial( double transitionMatrix[][], int startItem );

    /**
     * Set the transition matrix for a single trial.
     * Some implementation do not require this.
     * @param transitionMatrix
     */
    public void setTransitionMatrix( double transitionMatrix[][] );

    /**
     * 
     * @return the maximal number of items in the response
     */
    public int getNumTransitions();

    /**
     * Returns the retrieved sequence. The end of the sequence is determined 
     * either by the size of response or by first -1 value in the response sequence.
     * @return the sequence of retrieved items, they can be repeated. 
     */
    public int[] getResp();
}
