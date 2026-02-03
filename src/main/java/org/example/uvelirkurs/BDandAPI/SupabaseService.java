package org.example.uvelirkurs.BDandAPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SupabaseService {

    private static final String SUPABASE_URL = "https://ainxqbtyeqiwsmtgjfud.supabase.co";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFpbnhxYnR5ZXFpd3NtdGdqZnVkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzAwNDc1NzgsImV4cCI6MjA4NTYyMzU3OH0.bfWoSDHGR_nDPqzhoJsYUWjiqGO25GH_JHGcKhmomNE";

    public static boolean registerUser(String email, String password, String fullname, String phone) {
        try {
            // 1. Проверка, есть ли уже такой email
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
                System.out.println("Пользователь с таким email уже существует");
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
            } else {
                String response;
                try (Scanner scanner = new Scanner(conn.getErrorStream())) {
                    response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }
                System.out.println("Ошибка записи в Supabase: " + response);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получение пользователя по email и паролю
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
}
