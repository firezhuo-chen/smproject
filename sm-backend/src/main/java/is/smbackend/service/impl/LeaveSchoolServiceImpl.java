package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.LeaveSchoolMapper;
import is.smbackend.pojo.LeaveSchool;
import is.smbackend.service.LeaveSchoolService;
import org.springframework.stereotype.Service;

@Service
public class LeaveSchoolServiceImpl extends ServiceImpl<LeaveSchoolMapper, LeaveSchool> implements LeaveSchoolService {
}
