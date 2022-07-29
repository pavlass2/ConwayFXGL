package com.michalec.conwayFXGL.data;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.logging.Logger;
import com.michalec.conwayFXGL.entity.DataMalformedException;
import com.michalec.conwayFXGL.valueObject.CsvHeader;
import javafx.geometry.Point2D;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.List;

import static com.michalec.conwayFXGL.data.StringStore.*;

public class FilesystemDataProvider {
    private static Logger logger = Logger.get(FilesystemDataProvider.class);
    private static FileChooser fileChooser;
    static {
        fileChooser = new FileChooser();
        fileChooser.setInitialFileName(DEFAULT_PRESET_FILE_NAME);
    }
    public static DynamicPreset loadFile() {
        fileChooser.setTitle(LOAD_PRESET);
        File chosenFile = fileChooser.showOpenDialog(FXGL.getPrimaryStage());
        if (chosenFile ==  null) {
            logger.warning("No files were chosen.");
            return null;
        }
        try {
            DynamicPreset dynamicPreset = new DynamicPreset(chosenFile.getName());
            Reader reader = new FileReader(chosenFile);
            Iterable<CSVRecord> records = CSVFormat.Builder.create().setHeader(CsvHeader.x.name(), CsvHeader.y.name()).build().parse(reader);

            for (CSVRecord record : records) {
                if (!record.isConsistent()) {
                    throw new DataMalformedException(record.getRecordNumber(), record.toString(), chosenFile.getName());
                }
                String strX = record.get(CsvHeader.x.name());
                String strY = record.get(CsvHeader.y.name());

                if (strX.equals(CsvHeader.x.name()) || strY.equals(CsvHeader.y.name())) {
                    // Probably just header, ignore this record.
                    continue;
                }

                Double dblX = Double.valueOf(strX);
                Double dblY = Double.valueOf(strY);

                if (dblX < 0 || dblX > 990 || dblX % 10 != 0) {
                    throw new DataMalformedException(record.getRecordNumber(), dblX.toString(), chosenFile.getName());
                }
                if (dblY < 0 || dblY > 990 || dblY % 10 != 0) {
                    throw new DataMalformedException(record.getRecordNumber(), dblY.toString(), chosenFile.getName());
                }

                dynamicPreset.addAliveFieldCoordinates(new Point2D(dblX, dblY));
            }

            if (chosenFile.getParentFile() != null) {
                fileChooser.setInitialDirectory(chosenFile.getParentFile());
            }
            return dynamicPreset;
        } catch (FileNotFoundException e) {
            logger.warning("File " + chosenFile.getName() + " not found.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.warning("An exception was thrown during reading file " + chosenFile.getName() + ".");
            e.printStackTrace();
        } catch (DataMalformedException e) {
            logger.warning("Some values in file " + chosenFile.getName() + " are malformed or invalid coordinates. Record number:" + e.getRecordNumber() + ", malformed data: " + e.getMalformedData() + ", file name: " + e.getFileName());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            logger.warning("Some values in file " + chosenFile.getName() + " are not correctly formatted numbers.");
            e.printStackTrace();
        }
        // If we get here, no file was loaded.
        return null;
    }

    /**
     * Saves preset into a csv file
     * @param aliveFields Coordinates of fields that start alive in this preset.
     * @return File name
     */
    public static String savePreset(List<Point2D> aliveFields) {
        fileChooser.setTitle(SAVE_AS_PRESET);
        File chosenFile = fileChooser.showSaveDialog(FXGL.getPrimaryStage());
        if (chosenFile == null) {
            return null;
        }

        FileWriter out;
        try {
            out = new FileWriter(chosenFile);
            CSVPrinter printer = CSVFormat.Builder.create().setHeader(CsvHeader.x.name(), CsvHeader.y.name()).build().print(out);

            for (Point2D point2D : aliveFields) {
                printer.printRecord((int)point2D.getX(), (int)point2D.getY());
            }
            printer.close(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return FilenameUtils.removeExtension(chosenFile.getName());
    }
}
