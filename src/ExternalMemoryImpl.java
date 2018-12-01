import java.io.*;
import java.util.*;

//class ColumnComparator extends

public class ExternalMemoryImpl implements IExternalMemory {

    //parameters of input file syntax
    private final static int CHARS_PER_COL = 20;
    private final static int COL_DELIMITER_LEN = 1;
    private final static int MAX_LINE_LEN = 1000;


    public void sort(String in, String out, int colNum, String tmpPath) {

        try {

            ArrayList<File> sortedSequenceFiles = getArrayOfSortedSequences(in, colNum, tmpPath);
            int M = sortedSequenceFiles.size();

//            mergeSortedSequences(sortedSequenceFiles, M, colNum, tmpPath);

            writeSequenceFilesToOutput(sortedSequenceFiles, out);
//            sequenceFilesCleanup(sortedSequenceFiles);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /* comparator function to sort input Strings lexicographically by a specific column */
    private Comparator<String> compareByCol(int colNum) {

        int colBeginIndex = (CHARS_PER_COL + COL_DELIMITER_LEN) * (colNum - 1) - 1;

        return Comparator.comparing((String s) -> s.substring(colBeginIndex));

    }

    private ArrayList<File> getArrayOfSortedSequences(String in, int colNum, String tmpPath) throws IOException {

        BufferedReader bReader = new BufferedReader(new FileReader(in));

        /*
        find number of columns per line to decide number of sorted arrays
        TODO: change linesPerFile to some function of colsPerLine for performance?
        bReader.mark(MAX_LINE_LEN);
        final int colsPerLine = bReader.readLine().split(" ").length;
        bReader.reset(); //reset back to start of file
        */

        int linesPerFile = 4;
        ArrayList<File> sequenceFiles = new ArrayList<>();

        File workDir = new File(tmpPath);
        //noinspection ResultOfMethodCallIgnored
        workDir.mkdirs(); //create the temp directory if it doesn't exist.

        while (bReader.ready()) {

            ArrayList<String> nextSequence = new ArrayList<>();
            for (int i = 0; bReader.ready() && i < linesPerFile; i++) {
                nextSequence.add(bReader.readLine());
            }

            nextSequence.sort(compareByCol(colNum));

            File sequenceFile = File.createTempFile("sortedSequence", "", workDir);
            sequenceFile.deleteOnExit();
            sequenceFiles.add(sequenceFile);

            BufferedWriter bWriter = new BufferedWriter(new FileWriter(sequenceFile));
            for (String line : nextSequence) {
                bWriter.write(line + "\n");
            }

            bWriter.close(); //otherwise file will not be written into, and won't allow to be deleted

        }

        bReader.close();

        return sequenceFiles;

    }

    /* does a M-merge of a list of sorted sequences.
       returns a new, shorter list of sorted sequences
     */
    private ArrayList<File> mergeSortedSequences(ArrayList<File> sequencesToMerge, int M, int colNum, String tmpPath) throws IOException {




        ArrayList<File> merged = new ArrayList<>();

        File workDir = new File(tmpPath);
        workDir.mkdirs();

        File nextMergedSequence = File.createTempFile("sortedSequence", "", workDir);
        nextMergedSequence.deleteOnExit();
        merged.add(nextMergedSequence);

        ArrayList<String> mWay = new ArrayList<>(M);
        ArrayList<BufferedReader> mergeQueue = new ArrayList<>();
        for (int i = 0; i < M && !sequencesToMerge.isEmpty(); i++) {
            File sequence = sequencesToMerge.get(sequencesToMerge.size() - 1);
            sequencesToMerge.remove(sequencesToMerge.size() - 1);

            mergeQueue.add(new BufferedReader(new FileReader(sequence)));


        }

        while (!mergeQueue.isEmpty()) {

            for (int j = 0; j < mergeQueue.size(); j++) {
//                mergeQueue.get(j).mark(MAX_LINE_LEN);
                mWay.at(j) = mergeQueue.at(j);
//                mergeQueue.get(j).reset();
            }

            String minString = Collections.min(mWay, compareByCol(colNum));
            int minIndex = mWay.indexOf(minString);

        }

        return merged;
    }


    private void writeSequenceFilesToOutput(ArrayList<File> sequenceFiles, String out) throws IOException {

        BufferedWriter bWriter = new BufferedWriter(new FileWriter(out));
        for (File seqFile : sequenceFiles) {

            BufferedReader bReader = new BufferedReader(new FileReader(seqFile));

            //TODO: either find an easy way to remove \n after last line or use the older school test
            while (bReader.ready()) {
                bWriter.write(bReader.readLine() + "\n");
            }

//            bWriter.


            bReader.close();

        }

        bWriter.close();
    }

//    private void sequenceFilesCleanup(ArrayList<File> sequenceFiles) {
//        for (File seqFile : sequenceFiles) {
//            //noinspection ResultOfMethodCallIgnored
//            seqFile.delete();
//        }
//    }



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