@echo off
echo === 测试注册接口修复 ===
echo.

echo 1. 检查后端服务状态...
netstat -an | findstr :8081 >nul
if %errorlevel% equ 0 (
    echo ✓ 8081端口有服务运行
) else (
    echo ✗ 8081端口没有服务运行，请先启动后端服务
    goto :end
)

echo.
echo 2. 测试注册接口...
echo 发送POST请求到 /reg 接口...

curl -X POST ^
  "http://localhost:8081/reg" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "username=testuser&password=testpass&email=test@example.com" ^
  -w "HTTP状态码: %%{http_code}\n" ^
  -s

echo.
echo 3. 测试结果说明:
echo - HTTP状态码 200: 接口正常，可以注册
echo - HTTP状态码 302: 需要权限（已修复）
echo - HTTP状态码 404: 接口不存在
echo - HTTP状态码 500: 服务器内部错误

echo.
echo 如果看到状态码200，说明修复成功！
echo.

:end
pause
