package analyzer;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {

        try  {
            var xctrSrvc = Executors.newCachedThreadPool();
            Files
                    .list(Paths.get(args[0]))
                    .map(p -> (Callable<String>) () ->
                            Files.lines(Paths.get(args[1]))
                            .map(str -> str.replace("\"", ""))
                            .map(str -> str.split(";"))
                            .collect(
                                    Collectors.toMap(s -> s[1],
                                            Function.identity(),
                                            (s, v) -> s,
                                            HashMap::new))
                            .entrySet()
                            .stream()
                            .filter(e -> {
                                try {
                                    return KMP(new String(Files.readAllBytes(p)),
                                            e.getKey());
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            })
                            .max(Comparator.comparingInt(
                                    e -> Integer.parseInt(e.getValue()[0])))
                            .map(stringEntry -> p +
                                                 ": " +
                                           stringEntry.getValue()[2])
                            .orElseGet(() -> p +
                                            ": " +
                                        "Unknown file type"))
                    .map(xctrSrvc::submit)
                    .toList()
                    .forEach(f -> {

                        try {
                    System.out.println(f.get());
                } catch (Exception e){
                    System.out.println("ruh roh");
                }
            });

            xctrSrvc.shutdown();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static boolean naive(String txt,
                                 String ptrn) {
        if (txt.length() < ptrn.length())
            return false;
        for (int i = 0; i < txt.length() -
                            ptrn.length() + 1; ++i) {

            for (int j = 0; j < ptrn.length(); ++j)
                if (txt.charAt(i + j) != ptrn.charAt(j))
                    break;
                else if (j == ptrn.length() - 1)
                    return true;
        }
        return false;
    }

    private static boolean KMP(String txt,
                               String ptrn) {
        int[] prefixF = prefixFunction(ptrn);
        var j = 0;
        for (int i = 0; i < txt.length(); ++i) {
            while (j > 0 && txt.charAt(i) != ptrn.charAt(j))
                j = prefixF[j - 1];
            if (txt.charAt(i) == ptrn.charAt(j))
                ++j;
            if (j == ptrn.length())
                return true;
        }
        return false;
    }

    private static int[] prefixFunction(String str) {
        int[] prefixF = new int[str.length()];
        for (int i = 1; i < str.length(); ++i) {
            int j = prefixF[i - 1];
            while (j > 0 &&
                    str.charAt(i) != str.charAt(j))
                j = prefixF[j - 1];
            if (str.charAt(i) == str.charAt(j))
                ++j;
            prefixF[i] = j;
        }
        return prefixF;
    }
}
