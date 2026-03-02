import os
import re

files_to_process = [
    r'c:\Users\73633\Desktop\agri-traceability-system\backend\src\main\java\com\agritrace\entity\User.java',
    r'c:\Users\73633\Desktop\agri-traceability-system\backend\src\main\java\com\agritrace\entity\Product.java',
    r'c:\Users\73633\Desktop\agri-traceability-system\backend\src\main\java\com\agritrace\entity\TraceRecord.java'
]

for filepath in files_to_process:
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replace imports
    content = content.replace("import javax.persistence.*;", "import com.baomidou.mybatisplus.annotation.*;")
    
    # Replace class annotations
    content = content.replace("@Entity\n", "")
    content = content.replace("@Table(name =", "@TableName(")
    
    # Replace field annotations
    # @Id -> @TableId(type = IdType.INPUT)
    content = content.replace("@Id\n", "")
    content = re.sub(r'@Column\(name\s*=\s*"([^"]+)"(?:[^)]*)\)', r'@TableId(value = "\1", type = IdType.INPUT)', content, count=1)
    # The rest of @Column
    content = re.sub(r'@Column\(name\s*=\s*"([^"]+)"(?:[^)]*)\)', r'@TableField("\1")', content)
    
    # @Transient
    content = content.replace("@Transient", "@TableField(exist = false)")

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

print("Entities updated!")
