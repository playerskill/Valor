package ru.playerskill.logwriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class LogFileService {
    private final Path directory;
    private final Object[] fileLocks = new Object[64];

    public LogFileService(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
        for (int index = 0; index < fileLocks.length; index++) {
            fileLocks[index] = new Object();
        }
    }

    public boolean isSafeFileName(String fileName) {
        return fileName != null
                && !fileName.isEmpty()
                && !fileName.equals(".")
                && !fileName.equals("..")
                && fileName.indexOf('/') < 0
                && fileName.indexOf('\\') < 0
                && fileName.indexOf(':') < 0
                && fileName.indexOf('\0') < 0;
    }

    public boolean hasSupportedExtension(String fileName) {
        String lowerCaseName = fileName.toLowerCase(Locale.ROOT);
        return lowerCaseName.endsWith(".txt") || lowerCaseName.endsWith(".yml");
    }

    public void append(String fileName, String message) throws IOException {
        Path file = directory.resolve(fileName).normalize();
        if (!directory.equals(file.getParent())) {
            throw new IOException("Путь к файлу выходит за пределы папки логов.");
        }

        Object lock = fileLocks[(file.toString().hashCode() & Integer.MAX_VALUE) % fileLocks.length];
        synchronized (lock) {
            if (Files.isSymbolicLink(file)) {
                throw new IOException("Запись в символические ссылки запрещена.");
            }

            String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
            String singleLineMessage = message.replace('\r', ' ').replace('\n', ' ');
            try (BufferedWriter writer = Files.newBufferedWriter(
                    file,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND,
                    LinkOption.NOFOLLOW_LINKS)) {
                writer.write("[" + timestamp + "] " + singleLineMessage);
                writer.newLine();
            }
        }
    }
}
