package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.StudentBasicInfoMapper;
import is.smbackend.pojo.StudentBasicInfo;
import is.smbackend.service.StudentBasicInfoService;
import org.springframework.stereotype.Service;

@Service
public class StudentBasicInfoServiceImpl extends ServiceImpl<StudentBasicInfoMapper, StudentBasicInfo> implements StudentBasicInfoService {
}
