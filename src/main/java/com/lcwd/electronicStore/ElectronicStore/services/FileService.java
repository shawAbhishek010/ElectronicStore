package com.lcwd.electronicStore.ElectronicStore.services;

/*
Purpose:
Defines file upload and retrieval operations for product images.
*/
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface FileService {
    String uploadImage(MultipartFile file ,String path) throws IOException;

//    Upload an image (like .jpg, .png) and save it in a folder.

    InputStream getResource(String path, String name) throws FileNotFoundException;
    //    Fetch the image from the folder when you want to view it.
}
