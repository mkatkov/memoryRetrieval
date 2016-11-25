/* Session.java
 *
 * implementation of a session as a collection of trials with simulation outcome 
 * stored in Stat object
 */
package freeRecall;

import org.uncommons.maths.random.*;
/**
 * This class is responsible for running session simulation - a series of trials with a common similarity matrix.
 * 
 * In order to run session one need to create a {@link Session} with a specific 
 * number of words in a pool using {@link #Session(int) }. Then set number of 
 * words in a study list using {@link #setListSize(int) }.Set up the parameters 
 * of the {@link #stat} field. Set the object that will perform single trial 
 * simulation using {@link #setTrialSim(freeRecall.Trial)}. Set the similarity 
 * matrix for your model using {@link #setSimilarityMatrix(double[][]) } or 
 * {@link #setSimilarityMatrix(int[][]) }. 
 * 
 * Then you are ready to run simulation using {@link #computeSession(int) }.
 * 
 * All fields made public to have access from MATLAB(C).
 * 
 * @author katkov
 */
public class Session {

    public double similarityMatrix[][], 
            transitionMatrix[][];
    public int numWords, listSize;

    /**
     * The object that will perform the real single trial simulations
     */
    public Trial trialSim;

    /**
     * This object collects the statistics collected during the trials in the session
     */
    public Stat stat;

    /**
     * The words selected for the current trial
     */
    public int wordList[];

    DiscreteUniformGenerator rng[];

    /**
     * This function allocates space for transition matrix and defines random
     * generators for subsequent list selection from the pool
     *
     * @param listSize - number of words in study list
     * @throws SeedException
     */
    public void setListSize(int listSize)
            throws SeedException {
        this.listSize = listSize;
        transitionMatrix = new double[listSize][listSize];
        rng = new DiscreteUniformGenerator[listSize];
        MersenneTwisterRNG mrng = new MersenneTwisterRNG(new SecureRandomSeedGenerator());
        for (int k = 0; k < listSize; k++) {
            rng[k] = new DiscreteUniformGenerator(0, numWords - 1 - k, mrng);
        }
    }

    /**
     * Defines the session with {@literal numWords} words in a pool 
     * 
     * @param numWords this is the number of words in a pool
     */
    public Session(int numWords) {
        this.numWords = numWords;
        this.stat = new Stat();
    }

    /**
     * Each trial {@link #listSize} items are selected from the pool at random
     * and {@link #trialSim} is asked to perform a single trial simulation
     *
     * @param nTrials - number of trials to run
     */
    public void computeSession(int nTrials) {
        int k, k1, k2;
        // int wordIdx;
        // boolean fl;
        wordList = new int[listSize];
        stat.prepare(nTrials, this);
        for (k = 0; k < nTrials; k++) {
            // select words: This is a folded version, we are selecting each 
            // next word from the list excluding all previously selected words 
            for (k1 = 0; k1 < listSize; k1++) {
                wordList[k1] = rng[k1].nextValue();
            }
            // unfold wordList : we just add skipped places from the previous stage
            for (k1 = listSize - 1; k1 >= 0; k1--) {
                for (k2 = k1 - 1; k2 >= 0; k2--) {
                    if (wordList[k1] >= wordList[k2]) {
                        wordList[k1]++;
                    }
                }
            }

            prepareTransitionMatrix();
            // add effects

            // run trial
            trialSim.trial(transitionMatrix, firstRecalledWord());
            stat.store(k);
        }
    }

    /**
     * In the current implementation this method returns last word in a list.
     * Given that there is no serial effects it does not matter from which word
     * recall is started.
     *
     * @return the word that should be recalled first
     */
    public int firstRecalledWord() {
        return listSize - 1;
    }

    /**
     * Selects similarity sub-matrix corresponding to selected words in a list
     */
    public void prepareTransitionMatrix() {
        // select submatrix
        for (int k1 = 0; k1 < listSize; k1++) {
            for (int k2 = 0; k2 < listSize; k2++) {
                transitionMatrix[k1][k2] = similarityMatrix[wordList[k1]][wordList[k2]];
            }
        }
    }

    /**
     * allows to set external sources the similarity matrix
     *
     * @param similarityMatrix square similarity matrix of size
     * {@link #numWords} set in the constructor
     */
    public void setSimilarityMatrix(double similarityMatrix[][]) {
        this.similarityMatrix = similarityMatrix;
    }

    /**
     * Same as {@link #setSimilarityMatrix(double[][]) }.
     *
     * @param similarityMatrix
     */
    public void setSimilarityMatrix(int similarityMatrix[][]) {
        this.similarityMatrix = new double[similarityMatrix.length][similarityMatrix.length];
        for (int k1 = 0; k1 < similarityMatrix.length; k1++) {
            for (int k2 = 0; k2 < similarityMatrix.length; k2++) {
                this.similarityMatrix[k1][k2] = similarityMatrix[k1][k2];
            }
        }
    }

    /**
     * Specifies an instance of {@link Trial} class that will actually perform 
     * single trial simulation 
     * @param tr - an object performing simulation of a single trial
     */
    public void setTrialSim(Trial tr) {
        trialSim = tr;
    }
}
