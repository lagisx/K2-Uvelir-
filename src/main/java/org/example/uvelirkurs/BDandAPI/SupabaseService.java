package org.example.uvelirkurs.BDandAPI;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class SupabaseService {

    private static final String SUPABASE_URL = "https://ainxqbtyeqiwsmtgjfud.supabase.co";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFpbnhxYnR5ZXFpd3NtdGdqZnVkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzAwNDc1NzgsImV4cCI6MjA4NTYyMzU3OH0.bfWoSDHGR_nDPqzhoJsYUWjiqGO25GH_JHGcKhmomNE";

    private static final OkHttpClient client = new OkHttpClient();

    public static boolean registerUser(String email, String password, String fullname, String phone) {
        try {
            URL checkUrl = new URL(SUPABASE_URL + "/rest/v1/users?email=eq." + email);
            HttpURLConnection checkConn = (HttpURLConnection) checkUrl.openConnection();
            checkConn.setRequestMethod("GET");
            checkConn.setRequestProperty("apikey", API_KEY);
            checkConn.setRequestProperty("Authorization", "Bearer " + API_KEY);

            String checkResp;
            try (Scanner scanner = new Scanner(checkConn.getInputStream())) {
                checkResp = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            }
            if (!checkResp.equals("[]")) {
                return false;
            }

            URL url = new URL(SUPABASE_URL + "/rest/v1/users");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setDoOutput(true);

            JSONObject userRow = new JSONObject();
            String username = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
            userRow.put("username", username);
            userRow.put("email", email);
            userRow.put("password", password);
            userRow.put("fullname", fullname);
            userRow.put("phone", phone);
            userRow.put("role", "CLIENT");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(userRow.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                return true;
            }
                return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loginUser(String email, String password) {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/users?email=eq." + email);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

            String response;
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            }

            JSONArray arr = new JSONArray(response);
            if (arr.isEmpty()) return false;

            JSONObject user = arr.getJSONObject(0);
            return password.equals(user.getString("password"));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static CompletableFuture<JSONArray> getProductsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/products");
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

    public static JSONArray getProducts() {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/products");
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
    }

    private static JSONArray request(String endpoint) {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + endpoint);
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
    }

    public static CompletableFuture<JSONArray> getCategoriesAsync() {
        return CompletableFuture.supplyAsync(() -> request("categories"));
    }

    public static JSONArray getCategories() {
        return request("categories");
    }

    public static JSONArray getProductsByCategory(int categoryId) {
        return request("products?category_id=eq." + categoryId);
    }

    public static JSONArray getProductImages(int productId) {
        return request("product_images?product_id=eq." + productId + "&order=position.asc");
    }

    public static CompletableFuture<Boolean> updateUser(int userId, String username, String fullname, String email, String phone, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL getUrl = new URL(SUPABASE_URL + "/rest/v1/users?id=eq." + userId);
                HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
                getConn.setRequestMethod("GET");
                getConn.setRequestProperty("apikey", API_KEY);
                getConn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                String response;
                try (Scanner scanner = new Scanner(getConn.getInputStream())) {
                    response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }

                JSONArray arr = new JSONArray(response);
                if (arr.isEmpty()) return false;

                JSONObject currentUser = arr.getJSONObject(0);

                JSONObject updateData = new JSONObject();
                if (username != null && !username.equals(currentUser.optString("username"))) {
                    updateData.put("username", username);
                }
                if (fullname != null && !fullname.equals(currentUser.optString("fullname"))) {
                    updateData.put("fullname", fullname);
                }
                if (email != null && !email.equals(currentUser.optString("email"))) {
                    updateData.put("email", email);
                }
                if (phone != null && !phone.equals(currentUser.optString("phone"))) {
                    updateData.put("phone", phone);
                }
                if (password != null) {
                    updateData.put("password", password);
                }

                if (updateData.isEmpty()) {
                    return true;
                }

                RequestBody body = RequestBody.create(
                        updateData.toString(),
                        MediaType.parse("application/json")
                );

                Request patchRequest = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/users?id=eq." + userId)
                        .patch(body)
                        .addHeader("apikey", API_KEY)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .build();

                try (Response patchResponse = client.newCall(patchRequest).execute()) {
                    return patchResponse.isSuccessful();
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static JSONObject getCurrentUserByEmail(String email) {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/users?email=eq." + email);
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
    }

    public static CompletableFuture<Boolean> saveCartItem(int userId, int productId, int quantity, String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL checkUrl = new URL(SUPABASE_URL + "/rest/v1/cart_items?user_id=eq." + userId + "&product_id=eq." + productId);
                HttpURLConnection checkConn = (HttpURLConnection) checkUrl.openConnection();
                checkConn.setRequestMethod("GET");
                checkConn.setRequestProperty("apikey", API_KEY);
                checkConn.setRequestProperty("Authorization", "Bearer " + API_KEY);

                String checkResp;
                try (Scanner scanner = new Scanner(checkConn.getInputStream())) {
                    checkResp = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }

                JSONArray existingItems = new JSONArray(checkResp);

                if (!existingItems.isEmpty()) {
                    JSONObject updateData = new JSONObject();
                    updateData.put("quantity", quantity);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        updateData.put("image_url", imageUrl);
                    }

                    RequestBody body = RequestBody.create(
                            updateData.toString(),
                            MediaType.parse("application/json")
                    );

                    Request patchRequest = new Request.Builder()
                            .url(SUPABASE_URL + "/rest/v1/cart_items?user_id=eq." + userId + "&product_id=eq." + productId)
                            .patch(body)
                            .addHeader("apikey", API_KEY)
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .addHeader("Content-Type", "application/json")
                            .build();

                    try (Response patchResponse = client.newCall(patchRequest).execute()) {
                        return patchResponse.isSuccessful();
                    }
                } else {
                    URL url = new URL(SUPABASE_URL + "/rest/v1/cart_items");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("apikey", API_KEY);
                    conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Prefer", "return=minimal");
                    conn.setDoOutput(true);

                    JSONObject cartData = new JSONObject();
                    cartData.put("user_id", userId);
                    cartData.put("product_id", productId);
                    cartData.put("quantity", quantity);
                    cartData.put("image_url", imageUrl);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(cartData.toString().getBytes(StandardCharsets.UTF_8));
                    }

                    int code = conn.getResponseCode();
                    return code >= 200 && code < 300;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static CompletableFuture<JSONArray> getCartItems(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            return request("cart_items?user_id=eq." + userId);
        });
    }

    public static CompletableFuture<Boolean> removeCartItem(int userId, int productId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/cart_items?user_id=eq." + userId + "&product_id=eq." + productId);
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

    public static CompletableFuture<Boolean> clearCart(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "/rest/v1/cart_items?user_id=eq." + userId);
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

    public static int createOrder(int userId, double totalAmount, String shippingAddress,
                                  String shippingCity, String shippingPhone,
                                  String paymentMethod, String notes) {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/orders");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");
            conn.setDoOutput(true);

            JSONObject orderData = new JSONObject();
            orderData.put("user_id", userId);

            String orderNumber = "ORD-" + System.currentTimeMillis();
            orderData.put("order_number", orderNumber);

            orderData.put("total_amount", totalAmount);
            orderData.put("shipping_address", shippingAddress);
            orderData.put("shipping_city", shippingCity);
            orderData.put("shipping_phone", shippingPhone);
            orderData.put("payment_method", paymentMethod);

            if (notes != null && !notes.isEmpty()) {
                orderData.put("notes", notes);
            }

            try (OutputStream os = conn.getOutputStream()) {
                os.write(orderData.toString().getBytes(StandardCharsets.UTF_8));
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
            } else {
                String errorResponse;
                try (Scanner scanner = new Scanner(conn.getErrorStream())) {
                    errorResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }
            }

            return -1;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean addOrderItem(int orderId, int productId, int quantity, double unitPrice) {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/order_items");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setDoOutput(true);

            JSONObject itemData = new JSONObject();
            itemData.put("order_id", orderId);
            itemData.put("product_id", productId);
            itemData.put("quantity", quantity);
            itemData.put("unit_price", unitPrice);

            double subtotal = quantity * unitPrice;
            itemData.put("subtotal", subtotal);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(itemData.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            return code >= 200 && code < 300;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static CompletableFuture<JSONArray> getUserOrdersAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            return request("orders?user_id=eq." + userId + "&order=created_at.desc");
        });
    }

    public static JSONArray getUserOrders(int userId) {
        return request("orders?user_id=eq." + userId + "&order=created_at.desc");
    }

    public static JSONArray getOrderItems(int orderId) {
        return request("order_items?order_id=eq." + orderId);
    }

    public static JSONObject getProductById(int productId) {
        try {
            JSONArray arr = request("products?id=eq." + productId);
            if (!arr.isEmpty()) {
                return arr.getJSONObject(0);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
