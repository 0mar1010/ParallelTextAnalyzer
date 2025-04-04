package feature1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileWordCounter {
    private static final int FORKJOIN_THRESHOLD = 1000;

    private FileWordCounter() {
    }

    public static Map<String, Long> countWithStreams(String filePath) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            return processLinesStream(lines);
        }
    }

    public static Map<String, Long> countWithForkJoin(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        return processLinesForkJoin(lines);
    }

    public static void printResults(Map<String, Long> counts) {
        AtomicInteger counter = new AtomicInteger(0); // Atomic integer counter
        final int wordsPerRow = 4;
        boolean sortByCountDescending = false;

        Stream<Map.Entry<String, Long>> entryStream = counts.entrySet().stream();
        entryStream
                .sorted(!sortByCountDescending ?
                        Map.Entry.comparingByValue(Comparator.reverseOrder()) // By count (desc)
                        : Map.Entry.comparingByKey() // Alphabetically
                )
//        counts.entrySet().stream()
//                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> {
                    System.out.printf("%-15s: %-5d", entry.getKey(), entry.getValue());

                    if (counter.incrementAndGet() % wordsPerRow == 0) System.out.println(); // New line after every 3 words
                    else System.out.print(" | "); // Separator between words
                });
        // Ensure the last line ends properly
        if (counter.get() % wordsPerRow != 0) System.out.println();
    }

    private static Map<String, Long> processLinesStream(Stream<String> lines) {
        return lines.parallel()
                .flatMap(line -> Arrays.stream(line.split("\\W+")))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingByConcurrent(
                        String::toLowerCase,
                        Collectors.counting()
                ));
    }

    private static Map<String, Long> processLinesForkJoin(List<String> lines) {
        return ForkJoinPool.commonPool().invoke(new WordCountTask(lines, 0, lines.size()));
    }

    private static class WordCountTask extends RecursiveTask<Map<String, Long>> {
        private final List<String> lines;
        private final int start, end;

        WordCountTask(List<String> lines, int start, int end) {
            this.lines = lines;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Map<String, Long> compute() {
            if (end - start <= FORKJOIN_THRESHOLD) {
                Map<String, Long> counts = new HashMap<>();
                for (int i = start; i < end; i++) {
                    Arrays.stream(lines.get(i).split("\\W+"))
                            .filter(word -> !word.isEmpty())
                            .forEach(word ->
                                    counts.merge(word.toLowerCase(), 1L, Long::sum)
                            );
                }
                return counts;
            }

            int mid = (start + end) / 2;
            WordCountTask left = new WordCountTask(lines, start, mid);
            WordCountTask right = new WordCountTask(lines, mid, end);
            left.fork();
            Map<String, Long> rightResult = right.compute();
            Map<String, Long> leftResult = left.join();
            rightResult.forEach((k, v) -> leftResult.merge(k, v, Long::sum));
            return leftResult;
        }
    }
}