import requests; r=requests.get("http://localhost:8000/health", timeout=10); print(f"Status: {r.status_code}, Response: {r.json()}")
