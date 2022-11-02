package analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    private static final long a = 53L;
    private static final long aInverse = 188_679_247L;
    private static final long m = 1_000_000_009L;

    @FunctionalInterface
    private interface TriFunction<P1, P2, P3, R> {
        R call(P1 p1, P2 p2, P3 p3);
    }

    public static void main(String[] args) {

        try {
            var xctr = Executors.newCachedThreadPool();
            Files
                    .list(Paths.get(args[0]))
                    .map(path -> (Callable<String>) () -> {
                        try {
                            return multiLenRKarp(new String(
                                            Files.readAllBytes(path)),
                                             Files
                                            .lines(Paths.get(args[1]))
                                            .map(str -> str.replace("\"", ""))
                                            .map(str -> str.split(";"))
                                            .collect(Collectors
                                                    .groupingBy(str -> str[1].length(),
                                                            HashMap::new,
                                                            Collectors
                                                                    .toMap(str -> hashString(str[1]),
                                                                            Function.identity(),
                                                                            (str, value) ->
                                                                                    str,
                                                                            HashMap::new))))
                                    .map(str -> path + ": " + str)
                                    .orElseGet(() -> path + ": " + "Unknown file type");
                        } catch (IOException e) {
                            return "ERROR: " + e.getMessage();
                        }
                    })
                    .map(xctr::submit).toList()
                    .forEach(file -> {
                try {
                    System.out.println(file.get());
                } catch (Exception e){
                    System.out.println("ruh roh");
                }
            });

            xctr.shutdown();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    private static boolean naive(String txt,
                                  String ptn) {
        if (txt.length() < ptn.length())
            return false;
        for (int i = 0; i < txt.length() -
                            ptn.length() + 1; ++i) {
            for (int j = 0; j < ptn.length(); ++j)

                if (txt.charAt(i + j) != ptn.charAt(j))
                    break;
                else if (j == ptn.length() - 1)
                    return true;
        }
        return false;
    }

    private static Optional<String> multiLenRKarp(String txt,
                                              HashMap<Integer, HashMap<Long, String[]>> lenMap) {
        return lenMap
                .values()
                .stream()
                .map(strMap -> singleLenRKarp(txt, strMap))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingInt(s -> Integer.parseInt(s[0])))
                .map(s -> s[2]);
    }

    private static Optional<String[]> singleLenRKarp(String text,
                                                     HashMap<Long, String[]> idMap) {
        if (idMap == null || idMap.isEmpty())
            throw new IllegalArgumentException(
                    "Empty map passed to singleLengthRabinKarp");
        var len = idMap
                          .entrySet()
                          .stream()
                          .findAny()
                          .get()
                          .getValue()[1]
                          .length();
        if (len > text.length())
            return Optional.empty();
        var hash = 0L;
        var pow = 0L;
        for (var i = 0; i < len; ++i) {
            if (i == 0) {
                hash = charToLong(text.charAt(i));
                pow = 1;
            } else {
                pow = Math.floorMod(pow * a,
                                    m);
                hash = Math.floorMod(hash +
                                     charToLong(text.charAt(i)) * pow,
                                                m);
            }
        }
        String[] match;
        TriFunction<Integer,
                   Long,
                   String[],
                   String[]> updateMatch = (i, h, m) -> {
            if (idMap.containsKey(h)) {
                var typeMatch = idMap.get(h);
                if (text
                        .substring(i, i + len)
                        .equals(typeMatch[1])) {
                    if (m == null)
                        m = typeMatch;
                    else if (Integer.parseInt(m[0]) < Integer.parseInt(typeMatch[0]))
                        m = typeMatch;
                }
            }
            return m;
        };
        match = updateMatch.call(0, hash, null);
        for (int i = 1; i < text.length() - len + 1; ++i) {
            hash = Math.floorMod(hash -
                                charToLong(text.charAt(i - 1)),
                                 m);
            hash = Math.floorMod(hash * aInverse, m);
            hash = Math.floorMod(hash +
                                 charToLong(text.charAt(i + len - 1)) * pow,
                                  m);
            match = updateMatch.call(i, hash, match);
        }
        return Optional.ofNullable(match);
    }

    private static long charToLong(char ch) {
        return ch - 'A' + 1;
    }

    private static long hashString(String key) {
        var hash = 0L;
        var pow = 0L;
        for (var i = 0; i < key.length(); ++i) {
            if (i == 0) {
                hash = charToLong(key.charAt(i));
                pow = 1;
            } else {
                pow = Math.floorMod(pow * a,
                                    m);
                hash = Math.floorMod(hash +
                                     charToLong(key.charAt(i)) * pow,
                                      m);
            }
        }
        return hash;
    }
    private static boolean KMP(String text,
                               String ptn) {
        int[] prefixF = prefixFunction(ptn);
        var j = 0;
        for (var i = 0; i < text.length(); ++i) {
            while (j > 0 &&
                    text.charAt(i) != ptn.charAt(j))
                j = prefixF[j - 1];
            if (text.charAt(i) == ptn.charAt(j))
                ++j;
            if (j == ptn.length())
                return true;
        }
        return false;
    }

    private static int[] prefixFunction(String str) {
        int[] prefixFunc = new int[str.length()];
        for (var i = 1; i < str.length(); ++i) {
            var j = prefixFunc[i - 1];
            while (j > 0
                    && str.charAt(i) != str.charAt(j))
                j = prefixFunc[j - 1];
            if (str.charAt(i) == str.charAt(j))
                ++j;
            prefixFunc[i] = j;
        }
        return prefixFunc;
    }
}