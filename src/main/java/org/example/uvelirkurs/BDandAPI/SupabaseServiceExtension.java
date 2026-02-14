package org.example.uvelirkurs.BDandAPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class SupabaseServiceExtension extends SupabaseService {


    private static final String SUPABASE_URL = "https://ainxqbtyeqiwsmtgjfud.supabase.co";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFpbnhxYnR5ZXFpd3NtdGdqZnVkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzAwNDc1NzgsImV4cCI6MjA4NTYyMzU3OH0.bfWoSDHGR_nDPqzhoJsYUWjiqGO25GH_JHGcKhmomNE";

    public static CompletableFuture<JSONArray> getAllUsersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/users?order=id.asc");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                String response;
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }

                return new JSONArray(response);

            } catch (Exception e) {
                e.printStackTrace();
                return new JSONArray();
            }
        });
    }


    public static CompletableFuture<Boolean> deleteUser(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {

                clearCart(userId).get();
                deleteUserOrders(userId).get();


                URL url = new URL(SUPABASE_URL + "/rest/v1/users?id=eq." + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    private static CompletableFuture<Boolean> deleteUserOrders(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JSONArray orders = getUserOrders(userId);
                for (int i = 0; i < orders.length(); i++) {
                    int orderId = orders.getJSONObject(i).getInt("id");
                    deleteOrderItems(orderId).get();
                }

                URL url = new URL(SUPABASE_URL + "/rest/v1/orders?user_id=eq." + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
    private static CompletableFuture<Boolean> deleteOrderItems(int orderId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/order_items?order_id=eq." + orderId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
    public static CompletableFuture<Boolean> updateUserRole(int userId, String role) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/users?id=eq." + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject updateData = new JSONObject();
                updateData.put("role", role);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(updateData.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> updateUserByAdmin(int userId, String username,
                                                               String fullname, String email,
                                                               String phone, String role) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/users?id=eq." + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject updateData = new JSONObject();
                if (username != null) updateData.put("username", username);
                if (fullname != null) updateData.put("fullname", fullname);
                if (email != null) updateData.put("email", email);
                if (phone != null) updateData.put("phone", phone);
                if (role != null) updateData.put("role", role);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(updateData.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static CompletableFuture<Integer> addProduct(JSONObject productData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/products");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=representation");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(productData.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    String response;
                    try (Scanner scanner = new Scanner(conn.getInputStream())) {
                        response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    }

                    JSONArray arr = new JSONArray(response);
                    if (!arr.isEmpty()) {
                        return arr.getJSONObject(0).getInt("id");
                    }
                }

                return -1;

            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        });
    }

    public static CompletableFuture<Integer> createProduct(String name, int categoryId, int supplierId, String description, String material, String purity, double weight, String size, double price, double costPrice, int stockQuantity, String collection) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/products");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=representation");
                conn.setDoOutput(true);

                JSONObject productData = new JSONObject();
                productData.put("name", name);
                productData.put("category_id", categoryId);
                productData.put("supplier_id", supplierId);
                productData.put("description", description);
                productData.put("material", material);
                productData.put("purity", purity);
                productData.put("weight", weight);
                productData.put("size", size);
                productData.put("price", price);
                productData.put("cost_price", costPrice);
                productData.put("stock_quantity", stockQuantity);
                productData.put("collection", collection);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(productData.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    String response;
                    try (Scanner scanner = new Scanner(conn.getInputStream())) {
                        response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    }

                    JSONArray arr = new JSONArray(response);
                    if (!arr.isEmpty()) {
                        return arr.getJSONObject(0).getInt("id");
                    }
                }

                return -1;

            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        });
    }


    public static CompletableFuture<Boolean> updateProduct(int productId, String name, Integer categoryId, Integer supplierId, String description, String material, String purity, Double weight, String size, Double price, Double costPrice, Integer stockQuantity, String collection) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/products?id=eq." + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject updateData = new JSONObject();
                if (name != null) updateData.put("name", name);
                if (categoryId != null) updateData.put("category_id", categoryId);
                if (supplierId != null) updateData.put("supplier_id", supplierId);
                if (description != null) updateData.put("description", description);
                if (material != null) updateData.put("material", material);
                if (purity != null) updateData.put("purity", purity);
                if (weight != null) updateData.put("weight", weight);
                if (size != null) updateData.put("size", size);
                if (price != null) updateData.put("price", price);
                if (costPrice != null) updateData.put("cost_price", costPrice);
                if (stockQuantity != null) updateData.put("stock_quantity", stockQuantity);
                if (collection != null) updateData.put("collection", collection);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(updateData.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }


    public static CompletableFuture<Boolean> deleteProduct(int productId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                deleteProductImages(productId).get();
                deleteProductFromCarts(productId).get();
                URL url = new URL(SUPABASE_URL + "/rest/v1/products?id=eq." + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }


    private static CompletableFuture<Boolean> deleteProductImages(int productId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/product_images?product_id=eq." + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }


    private static CompletableFuture<Boolean> deleteProductFromCarts(int productId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/cart_items?product_id=eq." + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> addProductImage(int productId, String imageUrl, int position) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/product_images");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=minimal");
                conn.setDoOutput(true);

                JSONObject imageData = new JSONObject();
                imageData.put("product_id", productId);
                imageData.put("image_url", imageUrl);
                imageData.put("position", position);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(imageData.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                return code >= 200 && code < 300;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static CompletableFuture<JSONArray> getSuppliersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/suppliers");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                String response;
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }

                return new JSONArray(response);

            } catch (Exception e) {
                e.printStackTrace();
                return new JSONArray();
            }
        });
    }
    public static CompletableFuture<JSONArray> getAllOrdersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/orders?order=created_at.desc");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                String response;
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }

                return new JSONArray(response);

            } catch (Exception e) {
                e.printStackTrace();
                return new JSONArray();
            }
        });
    }


    public static CompletableFuture<JSONObject> getUserByIdAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/users?id=eq." + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("apikey", API_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                String response;
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }

                JSONArray arr = new JSONArray(response);
                if (!arr.isEmpty()) {
                    return arr.getJSONObject(0);
                }
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}