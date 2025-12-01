package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.AppealMapper;
import is.smbackend.pojo.Appeal;
import is.smbackend.service.AppealService;
import org.springframework.stereotype.Service;

@Service
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements AppealService {
}
