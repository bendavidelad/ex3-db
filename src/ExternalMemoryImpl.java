import java.io.*;
import java.util.*;

public class ExternalMemoryImpl implements IExternalMemory {

    //parameters of input file syntax
    private final static int CHARS_PER_COL = 20;
    private final static int COL_DELIMITER_LEN = 1;
    private final static int MAX_LINE_LEN = 1000;


    public void sort(String in, String out, int colNum, String tmpPath) {

        try {

            /*

                TODO: if we decide to make Y a function of the number of colPerRow, we can use this code to find it

                BufferedReader bReader = new BufferedReader(new FileReader(in));
                BufferedReader bReader = new BufferedReader
                bReader.mark(MAX_LINE_LEN);
                final int colsPerRow = bReader.readLine().split(" ").length;
                bReader.reset();

            */

            final int Y = 2000; //number of rows in each initial sorted sequence (stage 1)
            final int M = 100; //number of sequences to merge in each merge operaetion (stage 2)

            //TODO: last 3 parameters are a ugly hack and shouldn't be here, but who really cares.
            ArrayList<File> sortedSequenceFiles = getArrayOfSortedSequences(in, Y, colNum, tmpPath, false, 1, "");

            while (sortedSequenceFiles.size() > 1) {
                sortedSequenceFiles = mergeSortedSequences(sortedSequenceFiles, M, colNum, tmpPath);
            }

            writeResultToOutput(sortedSequenceFiles, out);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /* comparator function to sort input Strings lexicographically by a specific column */
    private Comparator<String> compareByCol(int colNum) {

        int colBeginIndex = (CHARS_PER_COL + COL_DELIMITER_LEN) * (colNum - 1) - 1;

        if (colNum == 1) return Comparator.comparing((String s) -> s);
        else return Comparator.comparing((String s) -> s.substring(colBeginIndex));

    }

    //splits input file into Files, each with Y rows, such that each file is sorted.
    //if select == true, then also removes rows where column colNumSelect DOES NOT contain subStrSelect
    private ArrayList<File> getArrayOfSortedSequences(String in, int Y, int colNum, String tmpPath, boolean select, int colNumSelect, String substrSelect) throws IOException {

        BufferedReader bReader = new BufferedReader(new FileReader(in));

        ArrayList<File> sequenceFiles = new ArrayList<>();

        File workDir = new File(tmpPath);
        workDir.mkdirs(); //create the temp directory if it doesn't exist.

        while (bReader.ready()) {

            ArrayList<String> nextSequence = new ArrayList<>();
            String line;
            for (int i = 0; i < Y && ((line = bReader.readLine()) != null); i++) {
                if (!select || isSubstringOfColumn(line, colNumSelect, substrSelect)) {
                    nextSequence.add(line);
                }

            }

            nextSequence.sort(compareByCol(colNum));

            File sequenceFile = File.createTempFile("sortedSequence", "", workDir);
            sequenceFile.deleteOnExit();
            sequenceFiles.add(sequenceFile);

            BufferedWriter bWriter = new BufferedWriter(new FileWriter(sequenceFile));
            for (String row : nextSequence) {
                bWriter.write(row + "\n");
            }

            bWriter.close(); //otherwise file will not be written into, and won't allow to be deleted

        }

        bReader.close();

        return sequenceFiles;

    }

    /* does a M-merge of a list of sorted sequences,
       where each sequence has Y lines.

       returns a new list of merged sequences that each have M*Y lines.
     */
    private ArrayList<File> mergeSortedSequences(ArrayList<File> sequencesToMerge, int M, int colNum, String tmpPath) throws IOException {


        ArrayList<File> newMergedSequences = new ArrayList<>();

        File workDir = new File(tmpPath);
        workDir.mkdirs();

        ArrayList<String> mWay = new ArrayList<>();
        ArrayList<BufferedReader> readerQueue = new ArrayList<>();
        ArrayList<String> writeBuffer = new ArrayList<>();
        BufferedWriter mergedWriter = null;

        while (!sequencesToMerge.isEmpty())
        {
            //create file for next merge
            File mergeFile = File.createTempFile("sortedSequence", "", workDir);
            mergeFile.deleteOnExit();
            newMergedSequences.add(mergeFile);
            mergedWriter = new BufferedWriter(new FileWriter(mergeFile));

            //add the next M files that will be merged
            for (int i = 0; i < M && !sequencesToMerge.isEmpty(); i++) {

                File sequence = sequencesToMerge.get(sequencesToMerge.size() - 1);
                sequencesToMerge.remove(sequencesToMerge.size() - 1);

                BufferedReader bReader = new BufferedReader(new FileReader(sequence));
                readerQueue.add(bReader);
                String nextLine = bReader.readLine();
                mWay.add(nextLine);

            }

            while (!readerQueue.isEmpty()) {
                //find minimum
                String minString = Collections.min(mWay, compareByCol(colNum));
                int minIndex = mWay.indexOf(minString);


                //advance reader with minimum element. if reader is finished, get rid of it
                String line;
                if ((line = readerQueue.get(minIndex).readLine()) != null) {

                    mWay.set(minIndex, line);

                } else { //then the file end is reached

                    readerQueue.get(minIndex).close();
                    mWay.remove(minIndex);
                    readerQueue.remove(minIndex);

                }

                //add minimum line to buffer
                writeBuffer.add(minString);

            }

            //write buffer to file
            for (String row : writeBuffer) {
                mergedWriter.write(row + "\n");
            }

            writeBuffer.clear();
            mergedWriter.close();

        }

        return newMergedSequences;
    }

//assumes there is only one file left.
    private void writeResultToOutput(ArrayList<File> sequenceFiles, String out) throws IOException {

        BufferedWriter bWriter = new BufferedWriter(new FileWriter(out));
        for (File seqFile : sequenceFiles) {

            BufferedReader bReader = new BufferedReader(new FileReader(seqFile));

            //workaround to avoid appending newline after last line
            String line = bReader.readLine();
            bWriter.write(line);
            while ((line = bReader.readLine()) != null) {
                bWriter.write("\n" + line);
            }


            bReader.close();

        }

        bWriter.close();
    }


    @Override
    public void select(String in, String out, int colNumSelect,
                       String substrSelect, String tmpPath) {
        sort(in, out, colNumSelect, tmpPath);

        try {

            BufferedReader bReader = new BufferedReader(new FileReader(in));
            BufferedWriter bWriter = new BufferedWriter(new FileWriter(out));

            //TODO: avoid adding \n after last line
            String line;
            while ((line = bReader.readLine()) != null) {
                if (isSubstringOfColumn(line, colNumSelect, substrSelect)) {
                    bWriter.write(line + "\n");
                }
            }

            bReader.close();
            bWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //takes a row with at least colNum columns, with a single whitespace as column delimiter
    //returns true iff column colNum of row contains subStr.
    private boolean isSubstringOfColumn(String row, int colNumSelect, String subStrSelect) {

        String[] columns = row.split(" ");
        String column = columns[colNumSelect - 1];

        return column.contains(subStrSelect);
    }

    @Override
    public void sortAndSelectEfficiently(String in, String out, int colNumSort,
                                         String tmpPath, int colNumSelect, String substrSelect) {

        final int Y = 2000; //number of rows in each initial sorted sequence (stage 1)
        final int M = 100; //number of sequences to merge in each merge operaetion (stage 2)

        try {

            ArrayList<File> sortedSequenceFiles = getArrayOfSortedSequences(in, Y, colNumSort, tmpPath, true,colNumSelect, substrSelect);

            while (sortedSequenceFiles.size() > 1) {
                sortedSequenceFiles = mergeSortedSequences(sortedSequenceFiles, M, colNumSort, tmpPath);
            }

            writeResultToOutput(sortedSequenceFiles, out);

        } catch (Exception e) {
            e.printStackTrace();
        }

        }

}