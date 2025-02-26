package com.ap10er.yandex_disk.java.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class YandexDiskService {

    @Value(("${yandex.token}"))
    private String token;

    private final RestTemplate restTemplate;

    public YandexDiskService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String downloadFile(String path) throws IOException {
        final String baseURL = "https://cloud-api.yandex.net/v1/disk/resources/download";
        RequestEntity<Void> requestEntity = RequestEntity.get(
                        UriComponentsBuilder.fromUriString(baseURL)
                                .queryParam("path", path)
                                .build().toUri()
                )
                .header("Authorization", "OAuth " + token)
                .build();
        ResponseEntity<Link> response = restTemplate.exchange(requestEntity, Link.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Error during getting link");
        }
        return response.getBody().href();
    }

    public void uploadAllFilesFromFolder(String localFolderPath, String yandexTargetPath) throws IOException {
        // Создаем целевую директорию на Яндекс.Диске
        createDirectory(yandexTargetPath);

        File folder = new File(localFolderPath);
        File[] files = folder.listFiles();

        if (files == null) {
            throw new IOException("Folder not found: " + localFolderPath);
        }

        for (File file : files) {
            if (file.isFile()) {
                try (InputStream is = new FileInputStream(file)) {
                    String yandexFilePath = yandexTargetPath + "/" + file.getName();
                    uploadFile(is, yandexFilePath);
                    System.out.println("File successfully uploaded: " + file.getName());
                } catch (IOException e) {
                    System.err.println("Error during upload " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }


    public void uploadFile(InputStream is, String yandexFilePath) throws IOException {
        final String baseURL = "https://cloud-api.yandex.net/v1/disk/resources/upload";

        RequestEntity<Void> requestEntity = RequestEntity.get(
                        UriComponentsBuilder.fromUriString(baseURL)
                                .queryParam("path", yandexFilePath)
                                .build()
                                .toUri()
                ).header("Authorization", "OAuth " + token)
                .build();

        ResponseEntity<Link> responseEntity = restTemplate.exchange(requestEntity, Link.class);
        String link = responseEntity.getBody().href();

        RequestEntity<byte[]> requestToUpload = RequestEntity.put(
                UriComponentsBuilder.fromUriString(link)
                        .build()
                        .toUri()
        ).body(is.readAllBytes());

        ResponseEntity<String> responseToUpload = restTemplate.exchange(
                requestToUpload, String.class
        );

        if (!responseToUpload.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Ошибка загрузки: " + responseToUpload.getStatusCode());
        }
    }

    public void createDirectory(String path) {
        final String baseURL = "https://cloud-api.yandex.net/v1/disk/resources";
        RequestEntity<Void> requestEntity = RequestEntity.put(
                        UriComponentsBuilder.fromUriString(baseURL)
                                .queryParam("path", path)
                                .build().toUri()
                )
                .header("Authorization", "OAuth " + token)
                .build();

        try {
            //Получаю link
            ResponseEntity<String> exchange = restTemplate.exchange(requestEntity, String.class);
            HttpStatusCode statusCode = exchange.getStatusCode();
            if (statusCode.equals(HttpStatus.valueOf(201))) {
                System.out.println("Directory created" + path);
            }
        } catch (Exception e) {
            System.out.println("Folder already exists: " + e.getMessage());
        }

        try {
            ResponseEntity<String> exchange = restTemplate.exchange(requestEntity, String.class);
            if (exchange.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("Directory created: " + path);
            } else if (exchange.getStatusCode() == HttpStatus.CONFLICT) {
                System.out.println("Directory already exists: " + path);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

}


record Link(String method, String href, boolean templated) {

}
