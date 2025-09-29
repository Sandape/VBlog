@echo off
echo === 注册功能诊断工具 ===
echo.

echo 步骤1: 检查前端项目状态
echo ------------------------
cd vueblog
if exist node_modules (
    echo ✓ node_modules存在
) else (
    echo ✗ node_modules不存在，请运行: npm install
    goto :error
)

echo.
echo 步骤2: 检查后端服务状态
echo ------------------------
cd ..\blogserver
if exist target\blogserver-0.0.1-SNAPSHOT.jar (
    echo ✓ 后端JAR文件存在
) else (
    echo ✗ 后端JAR文件不存在，请先编译后端项目
    goto :error
)

echo.
echo 步骤3: 启动后端服务
echo -------------------
echo 启动后端服务...
start /B java -jar target\blogserver-0.0.1-SNAPSHOT.jar
timeout /t 5 /nobreak > nul
echo 后端服务启动完成

echo.
echo 步骤4: 启动前端开发服务器
echo ---------------------------
cd ..\vueblog
echo 启动前端开发服务器...
start /B npm run dev
timeout /t 3 /nobreak > nul
echo 前端开发服务器启动完成

echo.
echo 步骤5: 测试注册接口
echo -------------------
echo 测试注册接口连接...
curl -X POST http://localhost:8081/reg ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "username=testuser&password=testpass&email=test@example.com" ^
  -w "HTTP状态码: %%{http_code}\n" ^
  -s

echo.
echo === 诊断完成 ===
echo.
echo 如果看到"HTTP状态码: 200"，说明后端服务正常
echo 如果看不到，请检查:
echo 1. 后端服务是否成功启动
echo 2. 端口8081是否被占用
echo 3. 防火墙设置
echo.
echo 现在可以访问 http://localhost:8080/register 测试注册功能
pause

:error
echo 错误: 请先解决上述问题
pause
