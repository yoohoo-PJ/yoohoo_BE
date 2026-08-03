import urllib.request
import urllib.parse
import json
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

BASE_URL = "http://localhost:8080"

endpoints = [
    # 2. Dashboard
    ("GET", "/api/dashboard/idle-books/count", None),
    ("GET", "/api/dashboard/damage-pending/count", None),
    ("GET", "/api/dashboard/transfer-pending/count", None),
    ("GET", "/api/dashboard/loans/monthly", None),
    ("GET", "/api/dashboard/users/distribution", None),
    ("GET", "/api/dashboard/libraries/network-distances", None),
    
    # 3. Checklists
    ("GET", "/api/checklists/results/completed", None),
    ("GET", "/api/checklists/books?status=DAMAGE_PENDING", None),
    ("POST", "/api/checklists/idle-classify", None),
    ("GET", "/api/checklists/1/detail", None),
    ("GET", "/api/checklists/1/history", None),
    ("POST", "/api/checklists/1/decision", {"decision": "TRANSFERRED", "reason": "Test"}),
    
    # 4. Transfers
    ("GET", "/api/transfers?status=PENDING,IN_TRANSIT", None),
    ("POST", "/api/transfers/1/execute", None)
]

def make_req(method, path, body=None, token=None):
    url = BASE_URL + path
    req = urllib.request.Request(url, method=method)
    if body:
        req.data = json.dumps(body).encode("utf-8")
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", token)
        
    try:
        with urllib.request.urlopen(req, context=ctx) as response:
            status = response.status
            resp_body = response.read().decode("utf-8")
            return status, resp_body, response.headers
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8")
        return e.code, resp_body, None
    except Exception as e:
        return "ERR", str(e), None

print("Registering user...")
reg_body = {
    "id": "testuser_api", 
    "password": "password123!", 
    "name": "tester", 
    "nickname": "testnickname", 
    "email": "test@test.com", 
    "librarianCode": "LIB001"
}
s, b, h = make_req("POST", "/api/users/register", reg_body)
print(f"Register: {s} {b[:50]}")

print("Logging in...")
login_body = {"username": "testuser_api", "password": "password123!"}
s, b, h = make_req("POST", "/api/users/login", login_body)
print(f"Login: {s} {b[:50]}")
token = h.get("Authorization") if h else None

if not token:
    print("FAILED TO GET TOKEN! Exiting.")
    exit(1)
    
print(f"Got Token: {token[:20]}...")

print(f"\n{'Method':<6} {'URL':<45} {'Status':<6} {'Response Preview'}")
print("-" * 100)

for method, path, body in endpoints:
    status, resp_body, _ = make_req(method, path, body, token)
    print(f"{method:<6} {path:<45} {status:<6} {resp_body[:80].replace(chr(10), ' ')}")
