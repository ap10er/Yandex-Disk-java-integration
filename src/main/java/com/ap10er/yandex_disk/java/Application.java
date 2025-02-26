package com.ap10er.yandex_disk.java;

import com.ap10er.yandex_disk.java.service.YandexDiskService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException {

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        YandexDiskService yandexDiskService = context.getBean(YandexDiskService.class);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите путь к локальной папке для загрузки:");
        String localFolder = scanner.nextLine();

        System.out.println("Введите целевую директорию на Яндекс.Диске:");
        String yandexFolder = scanner.nextLine();

        try {
            yandexDiskService.uploadAllFilesFromFolder(localFolder, yandexFolder);
            System.out.println("Все файлы успешно загружены!");
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке файлов: " + e.getMessage());
        }

        context.close();
    }
}
