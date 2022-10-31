package analyzer;


import java.io.*;
import java.util.Scanner;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args)
                                    throws InterruptedException {
        var fileList = new File(args[0]).listFiles();
        var xctrSrvc = Executors
                                   .newFixedThreadPool(10);
        assert fileList != null;
        for (File temp: fileList) {
            xctrSrvc.submit(() -> run(temp,
                                             args[1],
                                             args[2]));
            xctrSrvc.awaitTermination(1,
                                      TimeUnit.SECONDS);
        }
        xctrSrvc.shutdown();
    }

    public static void run(File temp,
                           String task,
                           String result) {
        try {
            var rdr = new FileReader(temp);
            var aim = new Scanner(rdr)
                                .nextLine();
            rdr.close();
            System.out.println(aim.contains(task) ?
                    temp.getName() + ": " + result
                    :
                    temp.getName() + ": Unknown file type");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
