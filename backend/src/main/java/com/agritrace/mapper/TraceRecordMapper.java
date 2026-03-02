package com.agritrace.mapper;

import com.agritrace.entity.TraceRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface TraceRecordMapper extends BaseMapper<TraceRecord> {

    @Select("SELECT operation_type as operationType, COUNT(*) as count FROM trace_records GROUP BY operation_type")
    List<Map<String, Object>> countByOperationType();

    @Select("SELECT operator_name as operatorName, COUNT(*) as count FROM trace_records GROUP BY operator_name ORDER BY count DESC")
    List<Map<String, Object>> countByOperator();
}
