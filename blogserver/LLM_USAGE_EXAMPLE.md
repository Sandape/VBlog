# LLM服务使用说明

## 概述

本项目新增了LLM（大语言模型）服务，支持调用各种AI接口获取智能回复。该服务兼容JDK 8，使用HttpURLConnection实现HTTP请求。

## 新增组件

### 1. HttpUtil工具类
位置：`org.sang.utils.HttpUtil`

功能：
- 发送HTTP GET/POST请求
- 支持自定义请求头
- 兼容JDK 8
- 自动处理JSON格式数据

### 2. LLMService服务类
位置：`org.sang.service.LLMService`

功能：
- 调用AI接口获取回复
- 支持OpenAI格式的API
- 自动处理JSON请求和响应
- 完整的错误处理机制

### 3. LLMController控制器
位置：`org.sang.controller.LLMController`

提供REST API接口：
- `POST /llm/chat` - 调用AI获取回复
- `GET /llm/test` - 测试服务状态

## 使用方法

### 1. 通过Service直接调用

```java
@Autowired
private LLMService llmService;

public void example() {
    try {
        String response = llmService.getAIResponse(
            "your-api-key",           // API密钥
            "https://api.openai.com/v1/chat/completions", // API地址
            "gpt-3.5-turbo",          // 模型名称
            "你好，请介绍一下自己"        // 用户消息
        );
        System.out.println("AI回复: " + response);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### 2. 通过REST API调用

#### 请求示例

```bash
curl -X POST http://localhost:8080/llm/chat \
  -H "Content-Type: application/json" \
  -d '{
    "apiKey": "your-api-key",
    "apiUrl": "https://api.openai.com/v1/chat/completions",
    "modelName": "gpt-3.5-turbo",
    "message": "你好，请介绍一下自己"
  }'
```

#### 响应示例

```json
{
  "status": "success",
  "msg": "调用成功",
  "obj": "你好！我是ChatGPT，一个由OpenAI开发的人工智能助手..."
}
```

### 3. 测试服务状态

```bash
curl http://localhost:8080/llm/test
```

响应：
```json
{
  "status": "success",
  "msg": "LLM服务正常运行"
}
```

## 支持的AI服务

### OpenAI
- API地址：`https://api.openai.com/v1/chat/completions`
- 支持模型：gpt-3.5-turbo, gpt-4, gpt-4-turbo等

### Azure OpenAI
- API地址：`https://your-resource.openai.azure.com/openai/deployments/your-deployment/chat/completions?api-version=2023-12-01-preview`
- 请求头需要使用：`api-key: your-api-key`

### 其他兼容OpenAI格式的服务
只要API格式兼容OpenAI Chat Completions API，都可以使用。

## 错误处理

服务包含完整的错误处理机制：

1. **参数校验错误**：API Key、URL、模型名称、消息不能为空
2. **网络错误**：连接超时、读取超时等
3. **API错误**：HTTP状态码非2xx
4. **解析错误**：响应格式不正确

所有错误都会返回详细的错误信息，便于调试。

## 配置说明

### 超时设置
- 连接超时：30秒
- 读取超时：60秒

### 请求限制
- 最大Token数：2000
- 温度参数：0.7

如需修改这些参数，请编辑`LLMService.java`中的相关常量。

## 安全注意事项

1. **API Key保护**：不要在代码中硬编码API Key，建议使用配置文件或环境变量
2. **请求验证**：在生产环境中，建议添加用户认证和请求频率限制
3. **内容过滤**：根据业务需求，可能需要对输入输出内容进行过滤

## 扩展建议

1. **配置化**：将API参数配置到`application.properties`中
2. **缓存机制**：对相同请求进行缓存，减少API调用
3. **异步处理**：对于耗时较长的请求，可以考虑异步处理
4. **监控日志**：添加详细的调用日志和监控指标
