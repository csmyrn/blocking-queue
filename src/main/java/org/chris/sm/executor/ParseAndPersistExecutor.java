package org.chris.sm.executor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.chris.sm.Main;
import org.chris.sm.bo.MyObjectBO;
import org.chris.sm.parser.CSVFileParser;
import org.chris.sm.utils.FileParserException;
import org.chris.sm.utils.ParserField;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * In this Class we parse the data from the CSV file to our BO and then we persist the Object to our DB by using a BlockingQueue
 */
public class ParseAndPersistExecutor
{

    private static final Logger log = LogManager.getLogger(ParseAndPersistExecutor.class);

    private int queueCapacity = 500;
    private int queueWaitMillis = 2000;

    public Iterator<MyObjectBO> getIterator()
    {
        return new Iterator<MyObjectBO>()
        {
            private BlockingQueue<MyObjectBO> queue = new ArrayBlockingQueue<MyObjectBO>(queueCapacity);
            boolean firstRun = true;
            int pageCounter = 0;
            int codeCounter = 0;
            boolean end = false;
            {
                new Thread()
                {
                    @Override
                    public void run()
                    {

                        CSVFileParser csvParser = new CSVFileParser();
                        csvParser.setHasHeader(false);
                        csvParser.setIgnoreSeperatorInsideDoubleQuotes(true);
                        csvParser.setSeparator(";");
                        csvParser.setParserFields(ParserField.fromMetadataList("id,name,description,quantity"));
                        csvParser.setCharacterSet("UTF-8");

                        File rqDir = new File(Main.class.getClassLoader().getResource("./DataFilesFolder/").getPath());

                        if(rqDir == null || !rqDir.isDirectory())
                            throw new RuntimeException("Invalid DataFilesFolder");

                        if(ArrayUtils.isEmpty(rqDir.list()))
                            throw new RuntimeException("DataFilesFolder is empty!");

                        List<File> dataFileList = Arrays.asList(rqDir.listFiles());
                        int counter = 0;
                        for(File rqFile : dataFileList) {

                            if (!StringUtils.containsAnyIgnoreCase(rqFile.getName(), ".csv"))
                                continue;

                            try {
                                FileInputStream fio = new FileInputStream(rqFile);

                                while(true)
                                {
                                    Map<String, Object> stringObjectMap = csvParser.nextRow(fio);

                                    if ((stringObjectMap ==null))
                                        break;

                                    MyObjectBO myObjBO = new MyObjectBO();
                                    myObjBO.setId(Integer.valueOf((String) stringObjectMap.get("id")));
                                    myObjBO.setName((String) stringObjectMap.get("description"));
                                    myObjBO.setDescription((String) stringObjectMap.get("description"));
                                    myObjBO.setQuantity(Integer.valueOf((String)stringObjectMap.get("quantity")));

                                    queue.put(myObjBO);

                                    log.info("adding line "+counter++);
                                }

                            } catch (FileNotFoundException | FileParserException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        end = true;
                    }

                }.start();
            }

            @Override
            public boolean hasNext()
            {
                MyObjectBO data = queue.peek();

                if(data == null && !end)
                {
                    log.info("queue is empty. Waiting..");
                    try {

                        Thread.sleep(queueWaitMillis);
                    } catch (InterruptedException e) {
                    }
                    return this.hasNext();
                }

                if(end)
                {
                    queue.poll();

                    log.info("Pages processed: " + pageCounter);
                    log.info("Codes Checked: " + codeCounter);
                    log.info("Queue size: " + queue.size() + "/" + queueCapacity);

                    return false;
                }

                return true;
            }

            @Override
            public MyObjectBO next()
            {
                MyObjectBO nextItem = null;
                try {
                    nextItem = queue.take();
                }
                catch (InterruptedException e) {
                }

                if(++codeCounter % 100 == 0) {
                    log.info("Codes Checked: " + codeCounter);
                    log.info("Queue size: " + queue.size() + "/" + queueCapacity);
                }

                return nextItem;
            }

            @Override
            public void remove()
            {
                throw new RuntimeException("not implemented");

            }
        };
    }

}
