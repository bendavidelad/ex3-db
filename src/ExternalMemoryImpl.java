package edu.hebrew.db.external;

public class ExternalMemoryImpl implements IExternalMemory {

    @Override
    public void sort(String in, String out, int colNum, String tmpPath) {
        //default values
        int numColsPerLine = 4;
        int numCharsPerCol = 20;
        int colDelimiterLength = 1;
        int sizeOfChar = 2;
        int X = sizeOfChar * ( (  numColsPerLine       * numCharsPerCol)     +
                ( (numColsPerLine - 1)  * colDelimiterLength) );
        //max bytes in memory for a line
        int M = 1000; //block size
        int Y = 20000; //size of block

    }

    @Override
    public void select(String in, String out, int colNumSelect,
                       String substrSelect, String tmpPath) {
        // TODO: Implement
    }

    @Override
    public void sortAndSelectEfficiently(String in, String out, int colNumSort,
                                         String tmpPath, int colNumSelect, String substrSelect) {



    }

}