package is.smbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import is.smbackend.mapper.PunishmentMapper;
import is.smbackend.pojo.Punishment;
import is.smbackend.service.PunishmentService;
import org.springframework.stereotype.Service;

@Service
public class PunishmentServiceImpl extends ServiceImpl<PunishmentMapper, Punishment> implements PunishmentService {
}
