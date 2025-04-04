package feature1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String filePath = "C:\\Users\\omars\\OneDrive\\Desktop\\Large_File_toTest.txt";
    private static final String folderPath = "C:\\Users\\omars\\OneDrive\\Desktop\\Large_Folder_to Test";

    public static void main(String[] args) {
        while (true) {
            try {
                int choice = showMenu();

                if (choice == 3) {
                    System.out.println("Exiting program...");
                    break;
                }

                if (choice < 1 || choice > 3) {
                    System.out.println("Wrong input, please try again.");
                    continue;
                }

                executeChoice(choice);

            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static int showMenu() {
        System.out.println("\n=== Parallel Text Analyzer ===");
        System.out.println("1. Count word frequencies in file");
        System.out.println("2. Search word in folder");
        System.out.println("3. Exit");
        System.out.print("Choose operation (1-3): ");

        try {
            int input = scanner.nextInt();
            scanner.nextLine();  // Consume newline (Clearing Buffer)
            return input;
        } catch (InputMismatchException e) {
            scanner.nextLine();  // Clear invalid input
            return -1;  // Will trigger "Wrong input" message
        }
    }

    private static void executeChoice(int choice) throws IOException {
        long startTime = System.currentTimeMillis();
        String methodName = "";

        if (choice == 1) {
            int method = selectMethod();
            methodName = (method == 1) ? "Parallel Streams" : "ForkJoin Framework";
            processFileAnalysis(method);
        } else if (choice == 2) {
            int method = selectMethod();
            methodName = (method == 1) ? "Parallel Streams" : "ForkJoin Framework";
            processFolderAnalysis();
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("%nOperation (%s) completed in %d ms%n", methodName, duration);

        // Additional comparison suggestion
        if (choice == 1 || choice == 2) {
            System.out.println("----- Performance Note -----");
            System.out.println("Streams generally better for small-medium workloads");
            System.out.println("ForkJoin often better for large, uneven workloads");
        }
    }

    private static void processFileAnalysis(int method) throws IOException {
        String path = readPath(false);
        Map<String, Long> counts = (method == 1) ?
                FileWordCounter.countWithStreams(path) :
                FileWordCounter.countWithForkJoin(path);

        System.out.println("\nWord frequencies:");
        FileWordCounter.printResults(counts);
    }

    private static void processFolderAnalysis() throws IOException {
        String path = readPath(true);
        String targetWord = readTargetWord();

        Map<String, Long> results = FolderWordSearcher.search(path, targetWord);
        System.out.println("\nOccurrences by file:");
        FolderWordSearcher.printResults(results);
    }

    private static int selectMethod() {
        System.out.println("\n=== Processing Method ===");
        System.out.println("1. Parallel Streams");
        System.out.println("2. ForkJoin Framework");
        System.out.print("Choose method: ");
        return readInt();
    }

    private static String readPath(boolean isFolder) throws IOException {
        //System.out.print(prompt);
        String path = isFolder ? folderPath : filePath;
        validatePath(path, isFolder);
        return path;
    }

    private static String readTargetWord() {
        System.out.print("Enter target word: ");
        return scanner.nextLine().trim().toLowerCase();
    }

    private static int readInt() {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine();
                if (input >= 1 && input <= 2) return input;
                System.out.printf("Please enter %d-%d: ", 1, 2);
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Enter number: ");
                scanner.nextLine();
            }
        }
    }

    private static void validatePath(String path, boolean isFolder) throws IOException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            throw new IOException(isFolder ? "Folder not found" : "File not found");
        }
        if (isFolder != Files.isDirectory(p)) {
            throw new IOException(isFolder ? "Not a directory" : "Not a file");
        }
    }
}