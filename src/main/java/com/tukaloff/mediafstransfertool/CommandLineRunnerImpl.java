package com.tukaloff.mediafstransfertool;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Scanner;

@Slf4j
@Service
public class CommandLineRunnerImpl implements CommandLineRunner {
    
    @Autowired
    private FileService fileService;

    @Override
    public void run(String... args) throws Exception {
        checkForHelp(args);
        if (args.length < 4) {
            log.error("wrong params");
            printHelp();
            return;
        }
        log.info("Source folder:\t" + args[0]);
        log.info("Destination folder:\t" + args[1]);
        long filesCount = fileService.filesCountInSource(args[0], args[2]);
        log.info("Files to transfer: " + filesCount);
        log.info("you sure 'bout dat?[Y/n]");
        String answer = new Scanner(System.in).next();
        if ("Y".equals(answer)) {
            log.info("Okay, let's go!");
            fileService.processFolder(args[0], args[1], args[2], args[3]);
        }
        else 
            log.info("sure");
    }

    private void checkForHelp(String... args) {
        if (args.length == 1 && "-h".equals(args[0])) {
            printHelp();
            System.exit(0);
        }
    }

    private void printHelp() {
        System.out.println("Usage:");
        System.out.println("java -jar media-fs-transfer-tool.jar <sourceDir> <destinationDir> <fileExtension> <deviceFolderName>");
        System.out.println("Ex.:");
        System.out.println("java -jar media-fs-transfer-tool.jar /Volumes/SonySD128/DCIM /Volumes/SanDisk256/Photo .ARW SonyA7R");
    }
}
