@echo off
curl -X POST "http://localhost:8081/reg" -H "Content-Type: application/x-www-form-urlencoded" -d "username=testuser&password=testpass&email=test@example.com" -w "HTTP状态码: %%{http_code}\n" -s
