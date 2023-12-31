package com.tukaloff.mediafstransfertool;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileService {

    private DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withZone(ZoneId.systemDefault());

    public long filesCountInSource(String source, String extension) {
        try (Stream<Path> paths = Files.walk(Paths.get(source))) {
            return filtered(extension, paths).count();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return 0;
    }

    private Predicate<Path> filterPath(String extension) {
        return path -> path.toString().endsWith(extension)
                && !path.getFileName().toString().startsWith(".");
    }

    public long filesSizeInSource(String source, String extension) {
        try (Stream<Path> paths = Files.walk(Paths.get(source))) {
            return filtered(extension, paths).mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 0;
            }).sum();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return 0;
    }

    public void processFolder(String source, String dest, String extension, String deviceFolder)
            throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(source))) {
            final Progress progress = new Progress();
            progress.setFilesCount(filesCountInSource(source, extension));
            Path destination = Paths.get(dest, deviceFolder);
            List<Entry<Path, Path>> copied = filtered(extension, paths)
                    .map(path -> {
                        progress.increment();
                        System.out.print(progress.getString());
                        return copyFile(path, destination);
                    }).collect(Collectors.toList());
            long notCopyed = copied.stream().filter(entry -> entry.getValue() == null).count();
            List<String> updatedFolders = copied.stream().filter(entry -> entry.getValue() != null)
                    .map(entry -> entry.getValue().getParent().toString()).distinct()
                    .collect(Collectors.toList());
            copied.stream().filter(entry -> entry.getValue() != null)
                    .filter(entry -> ensureEquals(entry.getKey(), entry.getValue()))
                    .map(entry -> entry.getKey()).forEach(this::safeDelete);
            copied.stream().filter(entry -> entry.getValue() != null)
                    .map(entry -> entry.getKey().getParent()).distinct()
                    .filter(parent -> isEmpty(parent)).forEach(this::safeDelete);
            log.info("Not copied: " + notCopyed);
            log.info("New/updated folders:");
            updatedFolders.stream().forEach(folder -> log.info("\t" + folder));
        }
    }

    private Stream<Path> filtered(String extension, Stream<Path> paths) {
        return paths.filter(filterPath(extension));
    }

    private void safeDelete(Path path) {
        try {
            log.info("File will be deleted: " + path.toString());
            Files.delete(path);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private boolean isEmpty(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return false;
    }

    private boolean ensureEquals(Path oldMediaFile, Path newMediaFile) {
        try {
            BasicFileAttributes oldAttributes =
                    Files.readAttributes(oldMediaFile, BasicFileAttributes.class);
            BasicFileAttributes newAttributes =
                    Files.readAttributes(newMediaFile, BasicFileAttributes.class);
            if (oldAttributes.size() == newAttributes.size()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return false;
    }

    private Entry<Path, Path> copyFile(Path path, Path destination) {
        try {
            String isoDateString = getIsoDateStringFromPath(path);
            Path dateFolder =
                    Files.createDirectories(Paths.get(destination.toString(), isoDateString));
            Path destFile = dateFolder.resolve(path.getFileName());
            Files.copy(path, destFile, StandardCopyOption.REPLACE_EXISTING);
            // log.info(path.toFile().getName() + " " + isoDateString);
            return new HashMap.SimpleEntry<>(path, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return new HashMap.SimpleEntry<>(path, null);
    }

    private String getIsoDateStringFromPath(Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        Instant creationDateTime = attributes.creationTime().toInstant();
        return formatter.format(creationDateTime);
    }
}
