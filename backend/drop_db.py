import mysql.connector

try:
    conn = mysql.connector.connect(
        host="38.55.34.56",
        user="root",
        password="yjd999999"
    )
    cursor = conn.cursor()
    cursor.execute("DROP DATABASE IF EXISTS agritrace")
    cursor.execute("CREATE DATABASE agritrace CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    print("Database recreated successfully.")
    cursor.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")
