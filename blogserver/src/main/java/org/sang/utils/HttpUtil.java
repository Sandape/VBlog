package org.sang.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP工具类，用于发送HTTP请求
 * 兼容JDK 8
 * 
 * @author sang
 * @date 2024
 */
public class HttpUtil {
    
    private static final int CONNECT_TIMEOUT = 30000; // 30秒连接超时
    private static final int READ_TIMEOUT = 60000;    // 60秒读取超时
    
    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static HttpResponse get(String url, Map<String, String> headers) throws IOException {
        return sendRequest(url, "GET", null, headers);
    }
    
    /**
     * 发送POST请求
     * 
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static HttpResponse post(String url, String jsonBody, Map<String, String> headers) throws IOException {
        return sendRequest(url, "POST", jsonBody, headers);
    }
    
    /**
     * 发送HTTP请求的通用方法
     * 
     * @param urlString 请求URL
     * @param method 请求方法
     * @param body 请求体
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException IO异常
     */
    private static HttpResponse sendRequest(String urlString, String method, String body, Map<String, String> headers) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // 设置请求方法
            connection.setRequestMethod(method);
            
            // 设置超时时间
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            
            // 设置通用请求头
            connection.setRequestProperty("User-Agent", "Java-HttpUtil/1.0");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            
            // 设置自定义请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            
            // 如果有请求体，设置相关属性
            if (body != null && !body.isEmpty()) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                
                // 写入请求体
                try (OutputStream os = connection.getOutputStream();
                     OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                    writer.write(body);
                    writer.flush();
                }
            }
            
            // 获取响应码
            int responseCode = connection.getResponseCode();
            
            // 读取响应内容
            String responseBody;
            InputStream inputStream = null;
            try {
                if (responseCode >= 200 && responseCode < 300) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }
                
                if (inputStream != null) {
                    responseBody = readInputStream(inputStream);
                } else {
                    responseBody = "";
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            
            return new HttpResponse(responseCode, responseBody);
            
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 读取输入流内容
     * 
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws IOException IO异常
     */
    private static String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }
        return response.toString().trim();
    }
    
    /**
     * HTTP响应结果类
     */
    public static class HttpResponse {
        private final int statusCode;
        private final String body;
        
        public HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getBody() {
            return body;
        }
        
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
        
        @Override
        public String toString() {
            return "HttpResponse{" +
                    "statusCode=" + statusCode +
                    ", body='" + body + '\'' +
                    '}';
        }
    }
}
