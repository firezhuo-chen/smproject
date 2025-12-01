package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.AwardMapper;
import is.smbackend.pojo.Award;
import is.smbackend.service.AwardService;
import org.springframework.stereotype.Service;

@Service
public class AwardServiceImpl extends ServiceImpl<AwardMapper, Award> implements AwardService {
}
