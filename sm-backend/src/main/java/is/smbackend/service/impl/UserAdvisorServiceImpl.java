package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.UserAdvisorMapper;
import is.smbackend.pojo.UserAdvisor;
import is.smbackend.service.UserAdvisorService;
import org.springframework.stereotype.Service;

@Service
public class UserAdvisorServiceImpl extends ServiceImpl<UserAdvisorMapper, UserAdvisor> implements UserAdvisorService {
}
