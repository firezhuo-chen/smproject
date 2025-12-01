package is.smbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import is.smbackend.pojo.Log;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper extends BaseMapper<Log> {
}
