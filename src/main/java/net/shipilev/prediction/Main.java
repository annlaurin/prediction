package net.shipilev.prediction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String... args) throws IOException {
        String fileName = null;
        if (args.length >= 1) {
            fileName = args[0];
        } else {
            System.err.println("Первым аргументом должно быть имя файла с данными");
            System.exit(1);
        }

        int expNum = readExpNum();

        List<String> buf = Files.readAllLines(new File(fileName).toPath());

        List<Result> results = new ArrayList<>();

        final int trials = 1;
        for (int c = 0; c < trials; c++) {
            int idx = new Random().nextInt(buf.size());
            String sentence = buf.get(idx);
            buf.remove(idx);
            Result res = doTrial(System.out, sentence, idx);
            results.add(res);
        }

        System.out.println();
        System.out.println("КОНЕЦ ЭКСПЕРИМЕНТА:");
        printSummary(System.out, results);
        System.out.println();

        int age = readAge();
        Gender gender = readGender();
        String lang = readLanguage();

        BufferedWriter writer = Files.newBufferedWriter(new File("result-" + expNum + ".csv").toPath());
        PrintWriter pw = new PrintWriter(writer);
        for (Result res : results) {
            for (int i = 0; i < res.goldenWords.size(); i++) {
                pw.print(expNum);
                pw.print(",");
                pw.print(age);
                pw.print(",");
                pw.print(gender.toString());
                pw.print(",");
                pw.print(lang);
                pw.print(",");
                pw.print(i + 1);
                pw.print(",");
                pw.print(emit(res.goldenWords.get(i)));
                pw.print(",");
                pw.print(emit(res.userWords.get(i)));
                pw.println();
            }
        }

        writer.close();
    }

    private static void printSummary(PrintStream pw, List<Result> results) {
        int allSuccess = 0;
        int allTotals = 0;
        for (Result res : results) {
            allSuccess += res.countMatches();
            allTotals += res.countAll();
        }
        pw.printf("%.2f%% правильных ответов%n", (100.0 * allSuccess) / (allTotals));
    }

    private static String emit(String v) {
        if (v.contains(",") || v.contains(" ") || v.contains("\n") || v.contains("\r") || v.contains("\"")) {
            return "\"" + v.replaceAll("\"", "\"\"") + "\"";
        } else {
            return v;
        }
    }

    private static Result doTrial(PrintStream pw, String s, int idx) {
        Result res = new Result(idx);

        String[] words = s.split("[ ]+");

        System.out.println(Arrays.toString(words));

        for (int i = 0; i < words.length; i++) {
            String w = words[i].replaceAll("[\\.,:;]", "");

            printPrompt(pw, words, i);

            String word = System.console().readLine();
            if (word.isEmpty()) {
                do {
                    printPrompt(pw, words, i);
                    word = System.console().readLine();
                } while (word.isEmpty());
            }

            res.goldenWords.add(w);
            res.userWords.add(word);
        }

        pw.println();
        pw.println(s);
        return res;
    }

    private static void printPrompt(PrintStream pw, String[] words, int i) {
        clearScreen(pw);
        pw.println();
        for (int j = 0; j < i; j++) {
            pw.print(words[j] + " ");
        }
        pw.println();
        pw.print("Введите следующее слово: ");
    }

    private static void clearScreen(PrintStream pw) {
        for (int c = 0; c < 100; c++) {
            pw.println();
        }
    }

    public static class Result {
        private final int idx;
        private List<String> goldenWords = new ArrayList<>();
        private List<String> userWords = new ArrayList<>();

        public Result(int idx) {
            this.idx = idx;
        }

        public int countMatches() {
            int success = 0;
            for (int i = 0; i < goldenWords.size(); i++) {
                String golden = goldenWords.get(i);
                String user = userWords.get(i);
                if (golden.equals(user)) {
                    success++;
                }
            }
            return success;
        }

        public int countAll() {
            return goldenWords.size();
        }
    }

    private static int readExpNum() throws IOException {
        while (true) {
            System.out.print("Номер испытуемого: ");
            String line = System.console().readLine();

            try {
                return Integer.parseInt(line);
            } catch (IllegalArgumentException iae) {
                System.out.println("Not a number: " + line);
            }
        }
    }

    private static int readAge() throws IOException {
        while (true) {
            System.out.print("Введите ваш возраст: ");
            String line = System.console().readLine();

            try {
                return Integer.parseInt(line);
            } catch (IllegalArgumentException iae) {
                System.out.println("Не число, попробуйте снова: " + line);
            }
        }
    }

    private static Gender readGender() throws IOException {
        while (true) {
            System.out.print("Введите ваш пол (М/Ж): ");
            String line = System.console().readLine();

            if (line.equalsIgnoreCase("м")) {
                return Gender.MALE;
            }
            if (line.equalsIgnoreCase("Ж")) {
                return Gender.FEMALE;
            }
        }
    }

    private static String readLanguage() throws IOException {
        System.out.print("Введите ваш родной язык: ");
        return System.console().readLine();
    }

    public enum Gender {
        MALE,
        FEMALE,
    }

}
