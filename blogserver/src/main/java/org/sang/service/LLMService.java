package org.sang.service;

import org.sang.utils.HttpUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * LLM服务类，用于调用AI接口获取回复
 * 
 * @author sang
 * @date 2024
 */
@Service
public class LLMService {
    
    /**
     * 调用AI获取回复
     * 
     * @param apiKey API密钥
     * @param apiUrl API地址
     * @param modelName 模型名称
     * @param message 用户消息
     * @return AI回复内容
     * @throws Exception 调用异常
     */
    public String getAIResponse(String apiKey, String apiUrl, String modelName, String message) throws Exception {
        // 参数校验
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key不能为空");
        }
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("API URL不能为空");
        }
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("模型名称不能为空");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        
        try {
            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey.trim());
            headers.put("Content-Type", "application/json");
            
            // 构建请求体（OpenAI格式）
            String requestBody = buildRequestBody(modelName.trim(), message.trim());
            
            // 发送HTTP请求
            HttpUtil.HttpResponse response = HttpUtil.post(apiUrl.trim(), requestBody, headers);
            
            // 检查响应状态
            if (!response.isSuccess()) {
                throw new RuntimeException("AI接口调用失败，状态码: " + response.getStatusCode() + 
                                         ", 响应内容: " + response.getBody());
            }
            
            // 解析响应内容
            return parseResponse(response.getBody());
            
        } catch (IOException e) {
            throw new Exception("网络请求失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("AI调用异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建请求体（OpenAI Chat Completions格式）
     * 
     * @param modelName 模型名称
     * @param message 用户消息
     * @return JSON格式的请求体
     */
    private String buildRequestBody(String modelName, String message) {
        // 转义JSON字符串中的特殊字符
        String escapedMessage = escapeJsonString(message);
        
        // 构建符合OpenAI API格式的请求体
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{");
        requestBody.append("\"model\":\"").append(modelName).append("\",");
        requestBody.append("\"messages\":[");
        requestBody.append("{\"role\":\"user\",\"content\":\"").append(escapedMessage).append("\"}");
        requestBody.append("],");
        requestBody.append("\"max_tokens\":5000,");
        requestBody.append("\"temperature\":0.5");
        requestBody.append("}");
        
        return requestBody.toString();
    }
    
    /**
     * 解析AI响应内容
     * 
     * @param responseBody 响应体
     * @return 提取的AI回复内容
     */
    private String parseResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new RuntimeException("AI响应内容为空");
        }
        
        try {
            // 简单的JSON解析（避免引入额外依赖）
            // 查找 "content" 字段的值
            String contentKey = "\"content\":\"";
            int contentStart = responseBody.indexOf(contentKey);
            if (contentStart == -1) {
                throw new RuntimeException("无法解析AI响应内容，未找到content字段");
            }
            
            contentStart += contentKey.length();
            int contentEnd = findJsonStringEnd(responseBody, contentStart);
            
            if (contentEnd == -1) {
                throw new RuntimeException("无法解析AI响应内容，content字段格式错误");
            }
            
            String content = responseBody.substring(contentStart, contentEnd);
            // 反转义JSON字符串
            return unescapeJsonString(content);
            
        } catch (Exception e) {
            throw new RuntimeException("解析AI响应失败: " + e.getMessage() + 
                                     ", 原始响应: " + responseBody);
        }
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     * 
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 反转义JSON字符串
     * 
     * @param str 转义后的字符串
     * @return 原始字符串
     */
    private String unescapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\b", "\b")
                  .replace("\\f", "\f")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
    
    /**
     * 查找JSON字符串的结束位置
     * 
     * @param json JSON字符串
     * @param start 开始位置
     * @return 结束位置
     */
    private int findJsonStringEnd(String json, int start) {
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return i;
            }
        }
        return -1;
    }
}
