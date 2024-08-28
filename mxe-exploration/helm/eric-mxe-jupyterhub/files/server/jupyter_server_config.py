c.ServerApp.tornado_settings = {
  "websocket_ping_interval": {{ .Values.singleuser.webSocket.pingInterval }},
  "websocket_ping_timeout": {{ .Values.singleuser.webSocket.pingTimeout }},
  "cookie_options": {"SameSite": "None", "Secure": True, "HTTPOnly": True },
  "headers": {
    "Cross-Origin-Embedder-Policy": "require-corp",
    "X-Frame-Options": "SAMEORIGIN"
  }
}