# media-fs-transfer-tool

Tool for transfering mediafiles from cameras to general store

# Installation

clone this repo and run maven install

`mvn clean install`

# Usage

Simply run build artifact with java:

```
java -jar media-fs-transfer-tool.jar <sourceDir> <destinationDir> <fileExtension> <deviceFolderName>
```

`sourceDir` - directory on disk from your camera, ex: `/Volumes/SonySD128/DCIM`

`destinationDir` - storage destination you prefer, ex: `/Volumes/SanDisk256/Photo`

`fileExtension` - file extension which used for filtering files from disk, ex: `.ARW`

`deviceFolderName` - folder in destinationDir where to create new folders and copy files

# How it works?

Tool select files from source dir filtered with extension and for each copies to `<destinationDir>/<deviceFolderName>/<ISO_DATE>/`

For example `/Volumes/SanDisk256/Photo/SonyA7R/2023-06-05/`

Then tool ensure that file copied to new destination and at least same by size and starts to delete old files and empty dirs

# What's the point?

I make timelapses and some video stuff and every time I do these things: move to folders by date, remove from SD, ensure that all files copied properly, again and again

This tool is a good start for my wide range of tasks to manage my media sources
