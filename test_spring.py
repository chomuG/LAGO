import requests; r=requests.post("http://localhost:8081/api/news/collect/realtime", timeout=90); print(f"Status: {r.status_code}, Response: {r.text}")
