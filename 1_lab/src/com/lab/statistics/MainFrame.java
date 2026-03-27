package com.lab.statistics;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class MainFrame {
    private JFrame frame;
    private static final String DIR_PATH = "files";
    private final int liczbaWyrazowStatystyki;
    private final AtomicBoolean fajrant;
    private final int liczbaProducentow;
    private final int liczbaKonsumentow;
    private ExecutorService executor;
    private List<Future<?>> producentFuture;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame window = new MainFrame();
                    window.frame.pack();
                    window.frame.setAlwaysOnTop(true);
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MainFrame() {
        liczbaWyrazowStatystyki = 10;
        fajrant = new AtomicBoolean(false);
        liczbaProducentow = 1;
        liczbaKonsumentow = 2;
        executor = Executors.newFixedThreadPool(liczbaProducentow + liczbaKonsumentow);
        producentFuture = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                executor.shutdownNow();
            }
        });
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        JButton btnStop = new JButton("Stop");
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fajrant.set(true);
                for (Future<?> f : producentFuture) {
                    f.cancel(true);
                }
            }
        });
        JButton btnStart = new JButton("Start");
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getMultiThreadedStatistics();
            }
        });
        JButton btnZamknij = new JButton("Zamknij");
        btnZamknij.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executor.shutdownNow();
                frame.dispose();
            }
        });
        panel.add(btnStart);
        panel.add(btnStop);
        panel.add(btnZamknij);
    }

    private void getMultiThreadedStatistics() {
        for (Future<?> f : producentFuture) {
            if (!f.isDone()) {
                JOptionPane.showMessageDialog(frame,
                        "Nie można uruchomić nowego zadania! Przynajmniej jeden producent nadal działa!", "OSTRZEŻENIE", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        fajrant.set(false);
        producentFuture.clear();
        final BlockingQueue<Optional<Path>> kolejka = new LinkedBlockingQueue<>(liczbaKonsumentow);
        final int przerwa = 60;

        Runnable producent = () -> {
            final String name = Thread.currentThread().getName();
            System.out.println(String.format("PRODUCENT %s URUCHOMIONY ...", name));
            while (!Thread.currentThread().isInterrupted()) {
                if(fajrant.get()) {
// TODO przekazanie poison pills (kolejka.put(Optional.empty());) konsumentom i zakończenia działania
                    for (int i = 0; i < liczbaKonsumentow; i++) {
                        try { kolejka.put(Optional.empty()); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    }
                    break;
                } else {
// TODO Wyszukiwanie plików *.txt i wstawianie do kolejki ścieżki opakowanej w Optional (Optional<Path>
// optPath = Optional.ofNullable(path); kolejka.put(optPath);) lub oczekiwanie jeśli kolejka
// pełna. Do wyszukiwania plików można użyć metody Files.walkFileTree oraz klasy SimpleFileVisitor<Path>
                    try {
                        Path startPath = Paths.get(DIR_PATH);
                        if (Files.exists(startPath)) {
                            Files.walkFileTree(startPath, new java.nio.file.SimpleFileVisitor<Path>() {
                                @Override
                                public java.nio.file.FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) throws IOException {
                                    if (fajrant.get()) return java.nio.file.FileVisitResult.TERMINATE;
                                    if (file.toString().endsWith(".txt")) {
                                        try {
                                            kolejka.put(Optional.of(file));
                                            System.out.println("Producent wrzucił: " + file.getFileName());
                                        } catch (InterruptedException e) { return java.nio.file.FileVisitResult.TERMINATE; }
                                    }
                                    return java.nio.file.FileVisitResult.CONTINUE;
                                }
                            });
                        }


                        String info = String.format("Producent %s ponownie sprawdzi katalogi za %d sekund", name, przerwa);
                        System.out.println(info);
                        TimeUnit.SECONDS.sleep(przerwa);

                    } catch (IOException e) {
                        break;
                    } catch (InterruptedException e) {
                        String info = String.format("Przerwa producenta %s przerwana!", name);
                        System.out.println(info);
                        if(!fajrant.get()) Thread.currentThread().interrupt();
                        // Po przerwaniu snu (np. przyciskiem Stop), musimy wysłać pigułki przed break
                        for (int i = 0; i < liczbaKonsumentow; i++) {
                            try { kolejka.put(Optional.empty()); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                        }
                        break;
                    }
                }
            }
            System.out.println(String.format("PRODUCENT %s SKOŃCZYŁ PRACĘ", name));
        };

        Runnable konsument = () -> {
            final String name = Thread.currentThread().getName();
            System.out.println(String.format("KONSUMENT %s URUCHOMIONY ...", name));
            while (!Thread.currentThread().isInterrupted()) {
                try {
// TODO pobieranie ścieżki (Optional<Path> optPath = kolejka.take();) i tworzenie statystyki wyrazów
// lub oczekiwanie jeśli kolejka jest pusta. Brak ścieżki tj. !optPath.isPresent() oznacza koniec pracy
                    Optional<Path> optPath = kolejka.take();
                    if (optPath.isPresent()) {
                        Path p = optPath.get();
                        Map<String, Long> statystyka = getLinkedCountedWords(p, liczbaWyrazowStatystyki);

                        // Dodana synchronizacja, żeby wyniki się nie mieszały:
                        synchronized (System.out) {
                            System.out.println("\nSTATYSTYKA DLA: " + p.getFileName());
                            statystyka.forEach((slowo, ile) ->
                                    System.out.printf(" %-15s : %d%n" , slowo, ile));
                        }
                    }
                    else {
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println(String.format("Oczekiwanie konsumenta %s przerwane!", name));
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println(String.format("KONSUMENT %s ZAKOŃCZYŁ PRACĘ", name));
        };

        for (int i = 0; i < liczbaProducentow; i++) {
            producentFuture.add(executor.submit(producent));
        }
        for (int i = 0; i < liczbaKonsumentow; i++) {
            executor.execute(konsument);
        }
    }

    private Map<String, Long> getLinkedCountedWords(Path path, int wordsLimit) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return reader.lines()
//TODO
// 1. Podział linii na słowa, można skorzystać z funkcji map, metody split i wyrażenia regularnego np. "\\s+"
// oznaczającego jeden lub więcej tzw. białych znaków. Po tej operacji pewnie będzie potrzebna konwersja
// strumienia 'String[]' do strumienia 'String' za pomocą .flatMap(Arrays::stream)
                    .flatMap(line -> Arrays.stream(line.split("\\s+")))
// 2. Wycięcie wszystkich znaków, które nie tworzą słów m.in. ;,.?!:
// Można użyć np. .map(word -> word.replaceAll("[^a-zA-Z0-9ąęóśćżńźĄĘÓŚĆŻŃŹ]{1}", ""))
                    .map(word -> word.replaceAll("[^a-zA-Z0-9ąęóśćżńźĄĘÓŚĆŻŃŹ]{1}", ""))
// 3. Filtrowanie słów - tylko z przynajmniej trzema znakami, użyj funkcji filter, metody matches i wyrażenia
// regularnego w lambdzie np. word -> word.matches("[a-zA-Z0-9ąęóśćżńźĄĘÓŚĆŻŃŹ]{3,}")
// (bez uwzględniania polskich znaków byłoby "[a-zA-Z]{3,}")
                    .filter(word -> word.matches("[a-zA-Z0-9ąęóśćżńźĄĘÓŚĆŻŃŹ]{3,}"))
// 4. Konwersja do małych liter, aby porównywanie słów było niewrażliwe na wielkość liter (można zrobić wcześniej,
// przed p. 2, wtedy wyrażenia regularne nie będą musiały uwzględniać wielkich literek)
                    .map(String::toLowerCase)
// 5. Grupowanie słów względem liczebności ich występowania, można użyć
// funkcji collect z Collectors.groupingBy(Function.identity(), Collectors.counting()),
// po tej operacji należy zrobić konwersję na strumień tj. .entrySet().stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream()
// 6. Sortowanie względem przechowywanych w mapie wartości, w kolejności malejącej,
// można użyć Map.Entry.comparingByValue(Comparator.reverseOrder())
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
// 7. Ograniczenie liczby słów do wartości z wordsLimit
                    .limit(wordsLimit)
                    .collect(Collectors.toMap( //umieszczenie elementów strumienia w mapie zachowującej kolejność tj. LinkedHashMap
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (k,v) -> { throw new IllegalStateException(String.format("Błąd! Duplikat klucza %s.", k)); },
                            LinkedHashMap::new));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}