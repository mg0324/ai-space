@echo off
echo === Key Card 2 Dev ===

echo [%time%] Checking ports...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    echo [%time%] Killing process on :8080 (PID %%a)
    taskkill /PID %%a /F >nul 2>&1
)

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    echo [%time%] Killing process on :5173 (PID %%a)
    taskkill /PID %%a /F >nul 2>&1
)

timeout /t 2 /nobreak >nul

echo [%time%] Starting backend (Spring Boot on :8080)...
start "key-card2-backend" cmd /k "cd /d %~dp0 && mvn spring-boot:run"

echo [%time%] Starting frontend (Vite on :5173)...
start "key-card2-frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo   Backend:  http://localhost:8080
echo   Frontend: http://localhost:5173
echo.
pause
