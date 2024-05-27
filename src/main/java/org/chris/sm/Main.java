package org.chris.sm;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.chris.sm.bo.MyObjectBO;
import org.chris.sm.executor.ParseAndPersistExecutor;

import java.util.Iterator;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {

        ParseAndPersistExecutor parseAndPersistExecutor = new ParseAndPersistExecutor();

        Iterator<MyObjectBO> iterator = parseAndPersistExecutor.getIterator();

        int codeCounter = 0;

        while (iterator.hasNext()) {
            MyObjectBO myobj = iterator.next();

            log.info(myobj);

            if (++codeCounter % 100 == 0) {
                Thread.sleep(10000);
                System.out.println("slept");
            }
        }
    }
}