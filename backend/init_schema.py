import pymysql
import os

try:
    conn = pymysql.connect(
        host="38.55.34.56",
        user="root",
        password="yjd999999",
        database="agritrace"
    )
    cursor = conn.cursor()
    
    with open("src/main/resources/schema.sql", "r", encoding="utf-8") as f:
        sql_script = f.read()
        
    for statement in sql_script.split(';'):
        if statement.strip():
            cursor.execute(statement)
            
    conn.commit()
    print("Schema applied successfully.")
    cursor.close()
    conn.close()
except Exception as e:
    print(f"Error: {e}")
