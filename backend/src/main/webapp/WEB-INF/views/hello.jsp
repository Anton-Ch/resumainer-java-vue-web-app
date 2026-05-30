<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${appName}</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
        }
        .card {
            background: rgba(255, 255, 255, 0.95);
            color: #333;
            padding: 3rem;
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            text-align: center;
            max-width: 480px;
        }
        h1 { margin: 0 0 0.5rem; font-size: 2rem; color: #333; }
        .time { font-size: 1.25rem; color: #666; margin: 1rem 0; }
        .profile { font-size: 0.875rem; color: #999; }
        .badge {
            display: inline-block;
            background: #667eea;
            color: #fff;
            padding: 0.25rem 0.75rem;
            border-radius: 12px;
            font-size: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
    </style>
</head>
<body>
    <div class="card">
        <h1>${appName}</h1>
        <p class="badge">Spring MVC · Java 21 · Tomcat 10</p>
        <p class="time">🕐 ${serverTime}</p>
        <p class="profile">Active Profile: <strong>${activeProfile}</strong></p>
    </div>
</body>
</html>
