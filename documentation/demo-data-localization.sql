USE agritrace;

SET NAMES utf8mb4;

UPDATE users
SET real_name = '张建国',
    company_name = '寿光蔬果种植合作社'
WHERE username = 'demo_producer';

UPDATE users
SET real_name = '李晓梅',
    company_name = '济南农产品加工中心'
WHERE username = 'demo_processor';

UPDATE users
SET real_name = '韩立民',
    company_name = '潍坊示范农场'
WHERE username = 'probe_user';

UPDATE users
SET real_name = '王春生',
    company_name = '寿光蔬菜示范基地'
WHERE username = 'demo_producer_final';

UPDATE users
SET real_name = '周海燕',
    company_name = '济南食品分拣中心'
WHERE username = 'demo_processor_final';

UPDATE users
SET real_name = '张玉东',
    company_name = '山东果蔬合作社'
WHERE username = 'yjdd';

UPDATE users
SET real_name = '袁敬东',
    company_name = '农产品质量检测中心'
WHERE username = 'yjd';

UPDATE users
SET real_name = '指导教师',
    company_name = '信息工程学院'
WHERE username = 'teacher_admin';

UPDATE users
SET real_name = '答辩教师',
    company_name = '信息工程学院'
WHERE username = 'teacher';

UPDATE users
SET real_name = '系统管理员',
    company_name = NULL
WHERE username = 'admin';

UPDATE products
SET product_name = '红富士苹果示范批次B',
    product_category = 'FRUIT',
    producer_name = '张建国',
    origin = '烟台苹果示范基地',
    description = '用于答辩演示的红富士苹果溯源样品',
    current_location = '济南农产品加工中心'
WHERE product_id = 'FR202603171751158217';

UPDATE products
SET product_name = '温室番茄示范批次A',
    product_category = 'VE',
    producer_name = '张建国',
    origin = '山东寿光蔬菜基地',
    description = '用于答辩演示的温室番茄批次样品',
    current_location = '济南农产品加工中心'
WHERE product_id = 'FR202603171913376100';

UPDATE products
SET product_name = '寿光番茄演示样品',
    product_category = 'VE',
    producer_name = '张建国',
    origin = '山东寿光现代农业园',
    description = '用于答辩演示的番茄溯源样品',
    current_location = '济南农产品加工中心'
WHERE product_id = 'FR202603172130115413';

UPDATE trace_records
SET operator_name = '张建国',
    location = '烟台苹果示范基地',
    operation_detail = '完成红富士苹果示范批次B建档上链'
WHERE transaction_id = 'c6ac2404-9287-4c27-b775-2d8a99427771';

UPDATE trace_records
SET operator_name = '李晓梅',
    location = '济南农产品加工中心',
    operation_detail = '完成苹果分级、质检与包装'
WHERE transaction_id = 'a9bf0a48-1821-4f3d-8294-dfa22a7e405d';

UPDATE trace_records
SET operator_name = '张建国',
    location = '山东寿光蔬菜基地',
    operation_detail = '完成温室番茄示范批次A建档上链'
WHERE transaction_id = '9b5b41f7-050d-4835-b2d3-f97dc732879e';

UPDATE trace_records
SET operator_name = '李晓梅',
    location = '济南农产品加工中心',
    operation_detail = '完成番茄分拣与包装'
WHERE transaction_id = 'e102a15a-31ab-464e-a5fa-fe094c530ec9';

UPDATE trace_records
SET operator_name = '张建国',
    location = '山东寿光现代农业园',
    operation_detail = '完成寿光番茄演示样品建档上链'
WHERE transaction_id = '7fc8257c-c9a6-4a9f-a358-c26c002b240a';

UPDATE trace_records
SET operator_name = '李晓梅',
    location = '济南农产品加工中心',
    operation_detail = '完成清洗、分装与包装'
WHERE transaction_id = 'c3ee97fe-e6c0-4749-a6d5-0b0de9b0c23b';
