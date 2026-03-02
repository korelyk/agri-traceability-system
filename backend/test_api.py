import requests
import json
import time

BASE_URL = "http://localhost:8080/api"

def print_result(step, response):
    print(f"\n--- {step} ---")
    print(f"Status: {response.status_code}")
    try:
        print(json.dumps(response.json(), indent=2, ensure_ascii=False))
    except:
        print(response.text)

# 1. Register a new user (Producer)
register_data = {
    "username": "test_producer_" + str(int(time.time())),
    "password": "password123",
    "realName": "张三",
    "userType": "PRODUCER",
    "companyName": "绿色农场",
    "email": "zhangsan@example.com",
    "phone": "13800138000"
}
res = requests.post(f"{BASE_URL}/users/register", json=register_data)
print_result("1. User Registration", res)
user_id = res.json().get("data", {}).get("userId", "")

# 2. Register a new product
product_data = {
    "productName": "有机富士苹果",
    "productCategory": "FRUIT",
    "producerId": user_id,
    "origin": "山东烟台",
    "description": "新鲜采摘的有机认证富士苹果"
}
res = requests.post(f"{BASE_URL}/products/register", json=product_data)
print_result("2. Product Registration", res)
product_id = res.json().get("data", {}).get("productId", "")

# 3. Add a trace record for the product
trace_data = {
    "productId": product_id,
    "operationType": "种植",
    "operatorId": user_id,
    "location": "山东烟台1号果园",
    "operationDetail": "施用有机肥，修剪树枝",
    "environmentData": "{\"temp\": 25, \"humidity\": 60}"
}
res = requests.post(f"{BASE_URL}/trace/add", json=trace_data)
print_result("3. Add Trace Record", res)

# 4. Query product trace history
res = requests.get(f"{BASE_URL}/trace/{product_id}")
print_result("4. Query Trace History", res)

# 5. Check System Statistics
res = requests.get(f"{BASE_URL}/statistics")
print_result("5. System Statistics", res)
