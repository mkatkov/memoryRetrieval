/* 
 * single trial deterministic recall model
 */
package freeRecall;

import org.uncommons.maths.random.*;

public class DeterministicTrial implements Trial {

    public int listSize;
    public double transitionMatrix[][];
//    public boolean forbidPrevP, forbidSelfP;
    public int firstItem[];
    public int secondItem[];
    public int currentItem, previousItem;
    public boolean recalledItemsFirst[], recalledItemsSecond[];
    //public double beta;

    public int resp[];

    MersenneTwisterRNG rng;

    @Override
    public int[] getResp() {
        return resp;
    }

    /**
     *
     * @return number of transitions
     */
    @Override
    public int getNumTransitions() {
        return 2*listSize;
    }

    /**
     *
     * @param transitionMatrix
     * @param startItem
     */
    @Override
    public void trial(double transitionMatrix[][], int startItem) {
        int k;
        this.transitionMatrix = transitionMatrix;
        setCurrentItem(startItem);
        resp[0] = previousItem = startItem;
        computeTransitions();
        for (k = 0; k < listSize; k++) {
            recalledItemsFirst[k] = false;
            recalledItemsSecond[k] = false;
        }
        for (k = 1; k < 2 * listSize; k++) {
            //currentItem= chooseItem();
       /*     System.out.println("currentItem " + currentItem + " " + previousItem + " "
             + firstItem[currentItem] + " " + secondItem[currentItem]);
             */
            if (firstItem[currentItem] == previousItem) {
                if (recalledItemsSecond[currentItem]) {
                    resp[k] = -1;
                    break;
                } else {
                    recalledItemsSecond[currentItem] = true;
                    previousItem = currentItem;
                    currentItem = secondItem[currentItem];
                    resp[k] = currentItem;
                }
            } else {
                if (recalledItemsFirst[currentItem]) {
                    resp[k] = -1;
                    break;
                } else {
                    recalledItemsFirst[currentItem] = true;
                    previousItem = currentItem;
                    currentItem = firstItem[currentItem];
                    resp[k] = currentItem;
                }
            }
        }
    }

    public void computeTransitions() {
        int k, k1, i1 = -1, i2 = -1;

        // first compute min transition entry
        for (k = 0; k < listSize; k++) {
            double tLine[];
            tLine = transitionMatrix[k];
            double v, v1 = 0., v2 = 0.;
            for (k1 = 0; k1 < listSize; k1++) {
                if (k1 == k) {
                    continue;
                }
                v = tLine[k1];
                if (v > v1) {
                    v1 = v;
                    i1 = k1;
                }
            }
            for (k1 = 0; k1 < listSize; k1++) {
                if (k1 == k) {
                    continue;
                }
                if (k1 == i1) {
                    continue;
                }
                v = tLine[k1];
                if (v > v2) {
                    v2 = v;
                    i2 = k1;
                }
            }
            firstItem[k] = i1;
            secondItem[k] = i2;
        }
    }

    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }

    public void setListSize(int listSize) {
        this.listSize = listSize;
        firstItem = new int[listSize];
        secondItem = new int[listSize];
        recalledItemsFirst = new boolean[listSize];
        recalledItemsSecond = new boolean[listSize];
        resp = new int[2 * listSize];
    }

    public DeterministicTrial() throws SeedException {
        rng = new MersenneTwisterRNG(new SecureRandomSeedGenerator());
    }

    /**
     *
     * @param transitionMatrix
     */
    @Override
    public void setTransitionMatrix(double transitionMatrix[][]) {
        this.transitionMatrix = transitionMatrix;
    }
}
