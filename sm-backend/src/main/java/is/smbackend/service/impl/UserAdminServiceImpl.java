package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.UserAdminMapper;
import is.smbackend.pojo.UserAdmin;
import is.smbackend.service.UserAdminService;
import org.springframework.stereotype.Service;

@Service
public class UserAdminServiceImpl extends ServiceImpl<UserAdminMapper, UserAdmin> implements UserAdminService {
}
