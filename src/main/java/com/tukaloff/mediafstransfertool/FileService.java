package com.tukaloff.mediafstransfertool;

import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileService {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withZone(ZoneId.systemDefault());

    public long filesCountInSource(String source, String extension) {
        try(Stream<Path> paths = Files.walk(Paths.get(source))) {
            return paths.filter(path -> path.toString().endsWith(extension))
                .count();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return 0;
    }

    public void processFolder(String source, String dest, String extension, String deviceFolder) throws IOException {
        try(Stream<Path> paths = Files.walk(Paths.get(source))) {
            Path destination = Paths.get(dest, deviceFolder);
            List<Entry<Path,Path>> copyed = paths
                .filter(path -> path.toString().endsWith(extension))
                .map(path -> copyFile(path, destination))
                .collect(Collectors.toList());
            long notCopyed = copyed.stream().filter(entry -> entry.getValue() == null).count();
            List<String> updatedFolders = copyed.stream().filter(entry -> entry.getValue() != null)
                .map(entry -> entry.getValue().getParent().toString())
                .distinct()
                .collect(Collectors.toList());
            copyed.stream().filter(entry -> entry.getValue() != null)
                .filter(entry -> ensureEquals(entry.getKey(), entry.getValue()))
                .map(entry -> entry.getKey())
                .forEach(this::safeDelete);
            copyed.stream().filter(entry -> entry.getValue() != null)
                .map(entry -> entry.getKey().getParent())
                .distinct()
                .filter(parent -> isEmpty(parent))
                .forEach(this::safeDelete);
            log.info("Not copyed: " + notCopyed);
            log.info("New/updated folders:");
            updatedFolders.stream().forEach(folder -> log.info("\t" + folder));
        }
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
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return false;
    }

    private boolean ensureEquals(Path oldMediaFile, Path newMediaFile) {
        try {
            BasicFileAttributes oldAttributes = Files.readAttributes(oldMediaFile, BasicFileAttributes.class);
            BasicFileAttributes newAttributes = Files.readAttributes(newMediaFile, BasicFileAttributes.class);
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
            Path dateFolder = Files.createDirectories(Paths.get(destination.toString(), isoDateString));
            Path destFile = dateFolder.resolve(path.getFileName());
            Files.copy(path, destFile, StandardCopyOption.REPLACE_EXISTING);
            log.info(path.toFile().getName() + " " + isoDateString);
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

