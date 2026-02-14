package org.example.uvelirkurs.BDandAPI;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SupabaseStorageService {
    
    private static final String SUPABASE_URL = "https://ainxqbtyeqiwsmtgjfud.supabase.co";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFpbnhxYnR5ZXFpd3NtdGdqZnVkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzAwNDc1NzgsImV4cCI6MjA4NTYyMzU3OH0.bfWoSDHGR_nDPqzhoJsYUWjiqGO25GH_JHGcKhmomNE";
    private static final String STORAGE_BUCKET = "product-images";
    
    private static final OkHttpClient client = new OkHttpClient();

    public static CompletableFuture<String> uploadImage(File file, int productId) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                String fileExtension = getFileExtension(file.getName());
                String fileName = "product_" + productId + "/" + UUID.randomUUID().toString() + fileExtension;
                

                String mimeType = getMimeType(fileExtension);

                RequestBody fileBody = RequestBody.create(file, MediaType.parse(mimeType));
                

                String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + STORAGE_BUCKET + "/" + fileName;
                

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("apikey", API_KEY)
                        .addHeader("Content-Type", mimeType)
                        .post(fileBody)
                        .build();
                

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {

                        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + STORAGE_BUCKET + "/" + fileName;
                        return publicUrl;
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        return null;
                    }
                }
                
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static CompletableFuture<java.util.List<String>> uploadImages(java.util.List<File> files, int productId) {
        return CompletableFuture.supplyAsync(() -> {
            java.util.List<String> uploadedUrls = new java.util.ArrayList<>();
            
            for (File file : files) {
                try {
                    String url = uploadImage(file, productId).join();
                    if (url != null) {
                        uploadedUrls.add(url);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            return uploadedUrls;
        });
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot);
        }
        return ".jpg";
    }

    private static String getMimeType(String extension) {
        switch (extension.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            default:
                return "image/jpeg";
        }
    }
    

    public static boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || 
               name.endsWith(".jpeg") || 
               name.endsWith(".png") || 
               name.endsWith(".gif") || 
               name.endsWith(".webp");
    }
    

    public static boolean isFileSizeValid(File file) {
        long maxSize = 5 * 1024 * 1024;
        return file.length() <= maxSize;
    }
}
