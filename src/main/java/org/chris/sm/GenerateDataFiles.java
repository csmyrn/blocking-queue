package org.chris.sm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateDataFiles {

    private static Logger log = LogManager.getLogger(GenerateDataFiles.class);

    /**
     * This method generates data files with Random data.
     * @param args
     */

    public static void main(String[] args) throws Exception {

        File rqDir = new File(GenerateDataFiles.class.getClassLoader().getResource("./DataFilesFolder/").getPath());

        if(rqDir == null || !rqDir.isDirectory())
            throw new Exception("Invalid DataFilesFolder");

        File file = new File(rqDir+"/myData.csv");

        try (FileWriter writer = new FileWriter(file)) {

            // Add data to the CSV
            for(int i=1;i<30000;i++) {
                writer.write(i+";Chris "+i+";My Description "+i+";"+i+";"+"\n");
            }

            log.info("Plain Java: CSV file created successfully!");

        } catch (IOException e) {
            log.error("IOException occured "+e.getMessage(),e);
        }


    }

}
