import requests; r=requests.post("http://backend:9000/api/claude/summarize", json={"newsTitle":"Test","newsContent":"Test"}, timeout=30); print(f"Status: {r.status_code}, Response: {r.text[:100]}")
