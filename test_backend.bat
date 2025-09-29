@echo off
echo 测试后端服务连接...
echo.

echo 1. 测试8081端口是否开放:
netstat -an | findstr :8081
if %errorlevel% equ 0 (
    echo ✓ 8081端口有服务在监听
) else (
    echo ✗ 8081端口没有服务在监听
)

echo.
echo 2. 测试/reg接口:
curl -X POST http://localhost:8081/reg -H "Content-Type: application/x-www-form-urlencoded" -d "username=test&password=test&email=test@example.com" -v

echo.
echo 测试完成!
pause
