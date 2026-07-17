package com.lcwd.electronicStore.ElectronicStore.services.impl;

/*
Purpose:
Implements local image upload and retrieval for product media.
*/
import com.lcwd.electronicStore.ElectronicStore.exceptions.BadApiRequestException;
import com.lcwd.electronicStore.ElectronicStore.services.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service

public class FileServiceImpl implements FileService {
    Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String uploadImage(MultipartFile file, String path) throws IOException {
        String originalFilename = file.getOriginalFilename();
        logger.info("fileName: {}", originalFilename);

        String fileName = UUID.randomUUID().toString();//creating unique id for each file so they don't interfare
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileNameWithExtension = fileName + extension;
        String fullPathWithFileName = path + fileNameWithExtension;
        if (extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".png")) {
            // file saving process...

            File folder = new File(path);
            if (!folder.exists()) {
                // create a folder
                folder.mkdirs();
            }
            //upload file
            Files.copy(file.getInputStream(), Paths.get(fullPathWithFileName));
        } else
            throw new BadApiRequestException("extension with this current " + extension + " is not allowed ");

        return fileNameWithExtension;
    }

    @Override
    public InputStream getResource(String path, String name) throws FileNotFoundException {
        String fullPath = path + name;
        InputStream inputStream = new FileInputStream(fullPath);
        return inputStream;
    }
}
