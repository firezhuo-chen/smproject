package is.smbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import is.smbackend.pojo.Notice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
