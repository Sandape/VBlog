package org.sang.controller;

import org.sang.bean.RespBean;
import org.sang.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * LLM控制器，提供AI调用接口
 * 
 * @author sang
 * @date 2024
 */
@RestController
@RequestMapping("/llm")
public class LLMController {
    
    @Autowired
    private LLMService llmService;
    
    /**
     * 调用AI获取回复
     * 
     * @param request 请求参数
     * @return 响应结果
     */
    @PostMapping("/chat")
    public RespBean chat(@RequestBody Map<String, String> request) {
        try {
            // 获取请求参数
            String apiKey = request.get("apiKey");
            String apiUrl = request.get("apiUrl");
            String modelName = request.get("modelName");
            String message = request.get("message");
            
            // 调用LLM服务
            String response = llmService.getAIResponse(apiKey, apiUrl, modelName, message);
            
            return RespBean.success("调用成功", response);
            
        } catch (IllegalArgumentException e) {
            return RespBean.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            return RespBean.error("调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试接口连通性
     * 
     * @return 测试结果
     */
    @GetMapping("/test")
    public RespBean test() {
        return RespBean.success("LLM服务正常运行");
    }
}
