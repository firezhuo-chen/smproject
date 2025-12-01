package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.StatusChangeMapper;
import is.smbackend.pojo.StatusChange;
import is.smbackend.service.StatusChangeService;
import org.springframework.stereotype.Service;

@Service
public class StatusChangeServiceImpl extends ServiceImpl<StatusChangeMapper, StatusChange> implements StatusChangeService {
}
