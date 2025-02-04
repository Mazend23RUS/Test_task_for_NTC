package org.example;


import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;


public class Main {


    private static final String FILE_PATH = "C:\\DirectoryForTestTask\\FileForTask.txt";
    private static final Lock lock = new ReentrantLock();
    private static final Condition newRecordAvailable = lock.newCondition();
    private static boolean newData = false;


    public static void main(String[] args) {

        try {
            Path testDirectory = Files.createDirectory(Paths.get("C:\\DirectoryForTestTask"));
            Path testFile1 = Files.createFile(Paths.get("C:\\DirectoryForTestTask\\FileForTask.txt"));
        } catch (IOException r) {
        }


        Thread firstEvenWriterThread = new Thread(new WritingEvenFirsClass());
        Thread secondOddWriterThread = new Thread(new WritingOddSecondClass());
        Thread readLastRecordedNumber = new Thread(new ReadLastInputNumberInFile());


        firstEvenWriterThread.start();
        secondOddWriterThread.start();
        readLastRecordedNumber.start();

    }


    static class WritingEvenFirsClass implements Runnable {
        @Override
        public void run() {
            while (true) {
                int result = 0;
                result = (int) (Math.random() * 100);

                if (result % 2 == 0) {
                    writeToFile(result);
                }
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    static class WritingOddSecondClass implements Runnable {
        @Override
        public void run() {
            while (true) {
                int result = 0;
                result = (int) (Math.random() * 100);

                if (result % 2 != 0) {
                    writeToFile(result);
                }
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    public static class ReadLastInputNumberInFile implements Runnable {
        @Override
        public void run() {
            while (true) {
                lock.lock();
                try {
                    while (!newData) {
                        newRecordAvailable.await(); // Ждем новых данных
                    }
                    try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println("Последнее добавленное число: " + line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    newData = false; // Сбрасываем флаг новых данных
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private static void writeToFile(int number) {

        lock.lock();
        try (FileWriter fw = new FileWriter(FILE_PATH, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(number + "\n");
            newData = true;
            newRecordAvailable.signalAll(); // сигнализируем о новой записи в файле
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


}