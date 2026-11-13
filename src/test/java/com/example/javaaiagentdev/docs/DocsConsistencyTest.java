package com.example.javaaiagentdev.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Сверяет доску задач, файлы задач и архив: всё это ведётся руками, поэтому расхождения
 * должно ловить не зрение человека, а сборка.
 */
class DocsConsistencyTest {

    private static final Pattern TASK_FILE = Pattern.compile("^(\\d{3})-(.+)\\.md$");
    private static final Pattern BOARD_ROW =
            Pattern.compile("^\\|\\s*(\\d{3})\\s*\\|\\s*([^|]*?)\\s*\\|\\s*([^|]*?)\\s*\\|$");
    private static final Pattern ARCHIVE_DIR = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}-(\\d{3})-(.+)$");
    private static final Set<String> BOARD_STATUSES = Set.of("в работе", "выполнено");

    private final Path root = repositoryRoot();
    private final Path tasks = root.resolve("docs/tasks");
    private final Path backlog = tasks.resolve("backlog");
    private final Path archive = root.resolve("openspec/changes/archive");

    @Test
    void boardRowsPointToExistingTaskFiles() {
        Map<String, String> board = boardRows();

        List<String> missing = new ArrayList<>();
        for (String number : board.keySet()) {
            if (taskNumbers(tasks).get(number) == null) {
                missing.add(number);
            }
        }

        assertEquals(List.of(), missing, "в доске есть номера без файла задачи в docs/tasks/");
    }

    @Test
    void boardHoldsOnlyInWorkOrDoneTasks() {
        Map<String, String> board = boardRows();

        List<String> wrong = new ArrayList<>();
        board.forEach((number, status) -> {
            if (!BOARD_STATUSES.contains(status)) {
                wrong.add(number + " -> " + status);
            }
        });

        assertEquals(List.of(), wrong, "в доске допустимы только статусы " + BOARD_STATUSES);
    }

    @Test
    void taskNumbersAreUniqueAcrossTasksAndBacklog() {
        Map<String, Path> numbers = new LinkedHashMap<>();
        List<String> duplicates = new ArrayList<>();

        for (Path directory : List.of(tasks, backlog)) {
            try (Stream<Path> files = Files.list(directory)) {
                files.filter(Files::isRegularFile)
                        .map(path -> Map.entry(path.getFileName().toString(), path))
                        .forEach(entry -> {
                            Matcher matcher = TASK_FILE.matcher(entry.getKey());
                            if (matcher.matches()) {
                                Path previous = numbers.put(matcher.group(1), entry.getValue());
                                if (previous != null) {
                                    duplicates.add(matcher.group(1));
                                }
                            }
                        });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        assertEquals(List.of(), duplicates, "номер задачи повторяется");
        assertFalse(numbers.isEmpty(), "файлы задач не найдены — проверь путь к репозиторию");
    }

    @Test
    void archivedChangesHaveTaskFile() {
        try (Stream<Path> directories = Files.list(archive)) {
            List<String> missing = directories.filter(Files::isDirectory)
                    .filter(directory -> ARCHIVE_DIR.matcher(directory.getFileName().toString()).matches())
                    .filter(directory -> !Files.isRegularFile(directory.resolve("task.md")))
                    .map(directory -> directory.getFileName().toString())
                    .toList();

            assertEquals(List.of(), missing, "в архивной папке нет task.md");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void closedTaskNumbersAreNotReused() {
        Set<String> archived = archivedNumbers();
        assertFalse(archived.isEmpty(), "архив не найден — проверь путь к репозиторию");

        List<String> reused = new ArrayList<>();
        for (Path directory : List.of(tasks, backlog)) {
            taskNumbers(directory).forEach((number, path) -> {
                if (archived.contains(number)) {
                    reused.add(number + " -> " + directory.relativize(path));
                }
            });
        }

        assertEquals(List.of(), reused, "номер закрытой задачи используется повторно");
    }

    @Test
    void backlogTasksAreNotOnTheBoard() {
        Map<String, String> board = boardRows();

        List<String> listed = new ArrayList<>();
        taskNumbers(backlog).keySet().forEach(number -> {
            if (board.containsKey(number)) {
                listed.add(number);
            }
        });

        assertTrue(listed.isEmpty(), "задачи из docs/tasks/backlog/ попали в доску: " + listed);
    }

    private Map<String, String> boardRows() {
        Map<String, String> rows = new LinkedHashMap<>();
        for (String line : readLines(tasks.resolve("BOARD.md"))) {
            Matcher matcher = BOARD_ROW.matcher(line);
            if (matcher.matches()) {
                rows.put(matcher.group(1), matcher.group(3));
            }
        }
        return rows;
    }

    private Map<String, Path> taskNumbers(Path directory) {
        Map<String, Path> numbers = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(Files::isRegularFile).forEach(path -> {
                Matcher matcher = TASK_FILE.matcher(path.getFileName().toString());
                if (matcher.matches()) {
                    numbers.put(matcher.group(1), path);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return numbers;
    }

    private Set<String> archivedNumbers() {
        Set<String> numbers = new java.util.HashSet<>();
        try (Stream<Path> directories = Files.list(archive)) {
            directories.filter(Files::isDirectory).forEach(directory -> {
                Matcher matcher = ARCHIVE_DIR.matcher(directory.getFileName().toString());
                if (matcher.matches()) {
                    numbers.add(matcher.group(1));
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return numbers;
    }

    private List<String> readLines(Path path) {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/tasks/BOARD.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("docs/tasks/BOARD.md не найден от " + Path.of("").toAbsolutePath());
    }
}
