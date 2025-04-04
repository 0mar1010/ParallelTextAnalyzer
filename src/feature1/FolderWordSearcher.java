package feature1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FolderWordSearcher {

    public static Map<String, Long> search(String folderPath, String targetWord) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths
                    .parallel() // Parallel processing via ForkJoinPool.commonPool()
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toConcurrentMap(
                            Path::toString,
                            file -> countWordInFile(file, targetWord)
                    ));
        }
    }

    public static void printResults(Map<String, Long> fileResults) {
        AtomicInteger counter = new AtomicInteger(0);
        int filesPerRow = 2;

        fileResults.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> {
                    String fileName = getFileName(entry.getKey());
                    System.out.printf("%-30s: %-5d", fileName, entry.getValue());

                    if (counter.incrementAndGet() % filesPerRow == 0) {
                        System.out.println();
                    } else {
                        System.out.print(" | ");
                    }
                });

        if (counter.get() % filesPerRow != 0) {
            System.out.println();
        }
    }

    private static long countWordInFile(Path file, String targetWord) {
        try (Stream<String> lines = Files.lines(file)) {
            return lines.parallel()
                    .flatMap(line -> Arrays.stream(line.split("\\W+")))
                    .filter(word -> word.equalsIgnoreCase(targetWord))
                    .count();
        } catch (IOException e) {
            return 0L;
        }
    }

    private static String getFileName(String fullPath) {
        return Paths.get(fullPath).getFileName().toString();
    }
}