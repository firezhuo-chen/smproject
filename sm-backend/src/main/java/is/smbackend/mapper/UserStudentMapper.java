package is.smbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import is.smbackend.pojo.UserStudent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserStudentMapper extends BaseMapper<UserStudent> {
}
