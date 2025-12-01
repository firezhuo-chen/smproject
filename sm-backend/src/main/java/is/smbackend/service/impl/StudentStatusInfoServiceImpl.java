package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.StudentStatusInfoMapper;
import is.smbackend.pojo.StudentStatusInfo;
import is.smbackend.service.StudentStatusInfoService;
import org.springframework.stereotype.Service;

@Service
public class StudentStatusInfoServiceImpl extends ServiceImpl<StudentStatusInfoMapper, StudentStatusInfo> implements StudentStatusInfoService {
}
