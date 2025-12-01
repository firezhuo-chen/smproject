package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.UserStudentMapper;
import is.smbackend.pojo.UserStudent;
import is.smbackend.service.UserStudentService;
import org.springframework.stereotype.Service;

@Service
public class UserStudentServiceImpl extends ServiceImpl<UserStudentMapper, UserStudent> implements UserStudentService {
}
