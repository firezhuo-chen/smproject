-- 学生基本信息表
DROP TABLE IF EXISTS `student_basic_info`;
CREATE TABLE `student_basic_info` (
  `stu_id` CHAR(10) NOT NULL COMMENT '学号',
  `name` VARCHAR(10) NOT NULL COMMENT '姓名',
  `gender` ENUM('男','女') NOT NULL COMMENT '性别',
  `id_card` CHAR(18) NOT NULL COMMENT '身份证',
  `birth_date` DATE NOT NULL COMMENT '出生日期',
  `nationality` VARCHAR(10) NOT NULL COMMENT '国籍',
  `nation` VARCHAR(10) NOT NULL COMMENT '民族',
  `native_place` VARCHAR(30) NOT NULL COMMENT '籍贯',
  `political_status` ENUM('群众','共青团员','中共党员','中共预备党员') NOT NULL COMMENT '政治面貌',
  `phone` CHAR(11) NOT NULL COMMENT '手机号码',
  PRIMARY KEY (`stu_id`) USING BTREE,
  UNIQUE INDEX `id_card`(`id_card` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  CHECK (stu_id LIKE 'U_________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 学生学籍信息表
DROP TABLE IF EXISTS `student_status_info`;
CREATE TABLE `student_status_info` (
  `stu_id` CHAR(10) NOT NULL COMMENT '学号',
  `department` VARCHAR(20) NOT NULL COMMENT '院系',
  `major` VARCHAR(20) NOT NULL COMMENT '专业',
  `class_name` VARCHAR(10) NOT NULL COMMENT '班级',
  `academic_status` ENUM('在读','毕业','退学','休学','转学','保留学籍') NOT NULL COMMENT '学籍状态',
  `admission_date` DATE NOT NULL COMMENT '入学日期',
  `graduation_date` DATE NULL COMMENT '预计毕业日期',
  `warning_level` ENUM('无','一级','二级','三级') DEFAULT '无' COMMENT '学业警示等级',
  `register_status` ENUM('已注册','未注册') NOT NULL COMMENT '注册状态',
  `advisor_id` CHAR(10) NOT NULL COMMENT '辅导员工号',
  `advisor` VARCHAR(10) NOT NULL COMMENT '辅导员',
  PRIMARY KEY (`stu_id`) USING BTREE,
  FOREIGN KEY (`stu_id`) REFERENCES `student_basic_info`(`stu_id`),
  FOREIGN KEY (`advisor_id`) REFERENCES `user_advisor`(`user_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 奖励审批表
DROP TABLE IF EXISTS `award`;
CREATE TABLE `award` (
  `award_id` CHAR(12) NOT NULL COMMENT '奖励编号',
  `stu_id` CHAR(10) NOT NULL COMMENT '学号',
  `award_type` ENUM('学业类','科研类','竞赛类','社会实践类','志愿服务类','文体类','创新创业类','学生工作类') NOT NULL COMMENT '奖励类型',
  `award_level` ENUM('国家级','省级','市级','校级','院级','企业级') NOT NULL COMMENT '奖励等级',
  `award_name` VARCHAR(50) NOT NULL COMMENT '奖励名称',
  `award_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '奖金金额',
  `issue_org` VARCHAR(20) NOT NULL COMMENT '颁发单位',
  `apply_date` DATE NOT NULL COMMENT '申请日期',
  `award_date` DATE NOT NULL COMMENT '获奖日期',
  `advisor_status` ENUM('待审批','已通过','未通过') DEFAULT '待审批' COMMENT '辅导员审批状态',
  `advisor_opinion` TEXT NULL COMMENT '辅导员审核意见',
  `admin_status` ENUM('待审批','已通过','未通过') DEFAULT '待审批' COMMENT '教务处审批状态',
  `admin_opinion` TEXT NULL COMMENT '教务处审核意见',
  `award_status` ENUM('审批中','已通过','未通过') DEFAULT '审批中' COMMENT '当前状态',
  PRIMARY KEY (`award_id`) USING BTREE,
  FOREIGN KEY (`stu_id`) REFERENCES `student_basic_info`(`stu_id`),
  CHECK (award_id LIKE 'A___________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 处分审批表
DROP TABLE IF EXISTS `punishment`;
CREATE TABLE `punishment` (
  `punishment_id` CHAR(12) NOT NULL COMMENT '处分编号',
  `stu_id` CHAR(10) NOT NULL COMMENT '学号',
  `punishment_type` ENUM('警告','严重警告','记过','留校察看','开除学籍') NOT NULL COMMENT '处分类型',
  `punishment_reason` TEXT NOT NULL COMMENT '处分原因',
  `issue_org` VARCHAR(20) NOT NULL COMMENT '决定单位',
  `apply_date` DATE NOT NULL COMMENT '申请日期',
  `punishment_date` DATE NOT NULL COMMENT '生效日期',
  `applicant_id` CHAR(10) NULL COMMENT '申请人ID（提交处分申请的管理员）',
  `applicant_role` VARCHAR(20) NULL COMMENT '申请人角色',
  `admin_status` ENUM('待审批','已通过','未通过') DEFAULT '待审批' COMMENT '教务处审批状态',
  `admin_opinion` TEXT NULL COMMENT '教务处审核意见',
  `punishment_status` ENUM('审批中','已生效','申诉中','已撤销') DEFAULT '审批中' COMMENT '当前状态',
  PRIMARY KEY (`punishment_id`) USING BTREE,
  FOREIGN KEY (`stu_id`) REFERENCES `student_basic_info`(`stu_id`),
  FOREIGN KEY (`applicant_id`) REFERENCES `user_admin`(`user_id`),
  CHECK (punishment_id LIKE 'P___________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 申诉记录表
DROP TABLE IF EXISTS `appeal`;
CREATE TABLE `appeal` (
  `appeal_id` CHAR(13) NOT NULL COMMENT '申诉编号',
  `punishment_id` CHAR(12) NOT NULL COMMENT '处分编号',
  `stu_id` CHAR(10) NOT NULL COMMENT '申诉学生学号',
  `appeal_reason` TEXT NOT NULL COMMENT '申诉理由',
  `appeal_date` DATE NOT NULL COMMENT '申诉日期',
  `advisor_status` ENUM('待审理','已通过','未通过') DEFAULT '待审理' COMMENT '辅导员审理状态',
  `advisor_opinion` TEXT NULL COMMENT '辅导员审理意见',
  `admin_status` ENUM('待审理','已通过','未通过') DEFAULT '待审理' COMMENT '教务处审理状态',
  `admin_opinion` TEXT NULL COMMENT '教务处审理意见',
  `appeal_status` ENUM('受理中','已通过','未通过') DEFAULT '受理中' COMMENT '当前状态',
  PRIMARY KEY (`appeal_id`) USING BTREE,
  FOREIGN KEY (`punishment_id`) REFERENCES `punishment`(`punishment_id`),
  FOREIGN KEY (`stu_id`) REFERENCES `student_basic_info`(`stu_id`),
  CHECK (appeal_id LIKE 'AP___________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 学籍变动申请表
DROP TABLE IF EXISTS `status_change`;
CREATE TABLE `status_change` (
  `change_id` CHAR(12) NOT NULL COMMENT '变动编号',
  `stu_id` CHAR(10) NOT NULL COMMENT '学号',
  `change_type` ENUM('转专业','交换','退学','休学','复学','转学') NOT NULL COMMENT '变动类型',
  `change_reason` TEXT NOT NULL COMMENT '变动原因',
  `current_school` VARCHAR(20) NOT NULL COMMENT '当前学校',
  `current_college` VARCHAR(20) NOT NULL COMMENT '当前学院',
  `current_major` VARCHAR(20) NOT NULL COMMENT '当前专业',
  `target_school` VARCHAR(20) NULL COMMENT '目标学校',
  `target_college` VARCHAR(20) NULL COMMENT '目标学院',
  `target_major` VARCHAR(20) NULL COMMENT '目标专业',
  `apply_date` DATE NOT NULL COMMENT '申请日期',
  `start_date` DATE NULL COMMENT '生效日期',
  `end_date` DATE NULL COMMENT '结束日期',
  `advisor_id` CHAR(10) NULL COMMENT '辅导员ID',
  `advisor_status` ENUM('待审核','已通过','未通过') NOT NULL DEFAULT '待审核' COMMENT '辅导员审核状态',
  `advisor_opinion` TEXT NULL COMMENT '辅导员审核意见',
  `admin_id` CHAR(10) NULL COMMENT '教务管理员ID',
  `admin_status` ENUM('待审核','已通过','未通过') NOT NULL DEFAULT '待审核' COMMENT '教务审核状态',
  `admin_opinion` TEXT NULL COMMENT '教务审核意见',
  `apply_status` ENUM('待审核','已通过','未通过') NOT NULL DEFAULT '待审核' COMMENT '最终申请状态',
  PRIMARY KEY (`change_id`) USING BTREE,
  FOREIGN KEY (`stu_id`) REFERENCES `student_basic_info`(`stu_id`),
  FOREIGN KEY (`advisor_id`) REFERENCES `user_advisor`(`user_id`),
  FOREIGN KEY (`admin_id`) REFERENCES `user_admin`(`user_id`),
  CHECK (change_id LIKE 'C___________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 离校手续办理表
DROP TABLE IF EXISTS `leave_school`;
CREATE TABLE `leave_school` (
  `leave_id` CHAR(12) NOT NULL COMMENT '离校编号',
  `stu_id` CHAR(10) NOT NULL COMMENT '学号',
  `leave_type` ENUM('毕业','退学','休学','转学','交换') NOT NULL COMMENT '离校类型',
  `leave_reason` TEXT NOT NULL COMMENT '离校原因',
  `apply_date` DATE NOT NULL COMMENT '申请日期',
  `leave_date` DATE NOT NULL COMMENT '离校日期',
  `dormitory_reviewer_id` CHAR(10) NULL COMMENT '宿管审核人',
  `dormitory_status` ENUM('待审核','已通过','未通过') DEFAULT '待审核' COMMENT '宿管审核状态',
  `dormitory_opinion` TEXT NULL COMMENT '宿管审核意见',
  `library_reviewer_id` CHAR(10) NULL COMMENT '图书馆审核人',
  `library_status` ENUM('待审核','已通过','未通过') DEFAULT '待审核' COMMENT '图书馆审核状态',
  `library_opinion` TEXT NULL COMMENT '图书馆审核意见',
  `finance_reviewer_id` CHAR(10) NULL COMMENT '财务处审核人',
  `finance_status` ENUM('待审核','已通过','未通过') DEFAULT '待审核' COMMENT '财务处审核状态',
  `finance_opinion` TEXT NULL COMMENT '财务处审核意见',
  `admin_id` CHAR(10) NULL COMMENT '教务处审核人',
  `admin_status` ENUM('待审核','已通过','未通过') DEFAULT '待审核' COMMENT '教务处审核状态',
  `admin_opinion` TEXT NULL COMMENT '教务处审核意见',
  `overall_status` ENUM('审核中','已通过','未通过') DEFAULT '审核中' COMMENT '整体状态',
  PRIMARY KEY (`leave_id`) USING BTREE,
  FOREIGN KEY (`stu_id`) REFERENCES `student_basic_info`(`stu_id`),
  FOREIGN KEY (`dormitory_reviewer_id`) REFERENCES `user_admin`(`user_id`),
  FOREIGN KEY (`library_reviewer_id`) REFERENCES `user_admin`(`user_id`),
  FOREIGN KEY (`finance_reviewer_id`) REFERENCES `user_admin`(`user_id`),
  FOREIGN KEY (`admin_id`) REFERENCES `user_admin`(`user_id`),
  CHECK (leave_id LIKE 'L___________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 系统通知表
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `notice_id` CHAR(18) NOT NULL COMMENT '通知编号',
  `title` VARCHAR(20) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `notice_type` VARCHAR(20) NOT NULL COMMENT '通知类型',
  `target_user` CHAR(10) NOT NULL COMMENT '目标用户ID',
  `target_type` ENUM('student','advisor') NOT NULL DEFAULT 'student' COMMENT '目标用户类型',
  `publish_user` VARCHAR(20) NOT NULL COMMENT '发布人',
  `publish_user_id` CHAR(10) NULL COMMENT '发布人ID',
  `publish_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `priority` ENUM('普通','重要') DEFAULT '普通' COMMENT '优先级',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读，1已读',
  `read_time` DATETIME NULL COMMENT '阅读时间',
  PRIMARY KEY (`notice_id`) USING BTREE,
  CHECK (notice_id LIKE 'N_________________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 附件表
DROP TABLE IF EXISTS `attachment`;
CREATE TABLE `attachment` (
  `attachment_id` CHAR(20) NOT NULL COMMENT '附件编号',
  `file_name` VARCHAR(100) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `file_type` VARCHAR(50) NOT NULL COMMENT '文件MIME类型',
  `related_id` VARCHAR(20) NOT NULL COMMENT '关联业务ID',
  `related_type` ENUM('award','punishment','statusChange','leaveSchool') NOT NULL COMMENT '关联业务类型',
  `upload_user_id` CHAR(10) NOT NULL COMMENT '上传用户ID',
  `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`attachment_id`) USING BTREE,
  INDEX `idx_related` (`related_id`, `related_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 操作日志表（包含登录日志和操作日志）
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log` (
  `log_id` CHAR(23) NOT NULL COMMENT '日志编号',
  `user_id` CHAR(10) NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(10) NOT NULL COMMENT '用户名',
  `user_type` VARCHAR(20) NULL COMMENT '用户类型：student/advisor/admin',
  `operation` VARCHAR(20) NOT NULL COMMENT '操作类型',
  `operation_detail` TEXT NOT NULL COMMENT '操作详情',
  `target_type` VARCHAR(50) NULL COMMENT '操作对象类型：award/punishment/appeal等',
  `target_id` VARCHAR(50) NULL COMMENT '操作对象ID',
  `old_value` TEXT NULL COMMENT '变更前的值（JSON格式）',
  `new_value` TEXT NULL COMMENT '变更后的值（JSON格式）',
  `ip_address` VARCHAR(50) NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) NULL COMMENT '浏览器/设备信息',
  `request_url` VARCHAR(200) NULL COMMENT '请求URL',
  `request_method` VARCHAR(10) NULL COMMENT '请求方法：GET/POST/PUT/DELETE',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `result` BOOL NOT NULL COMMENT '操作结果',
  `error_message` VARCHAR(500) NULL COMMENT '错误信息',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_operation` (`operation`),
  INDEX `idx_operation_time` (`operation_time`),
  INDEX `idx_target` (`target_type`, `target_id`),
  CHECK (log_id LIKE 'LOG____________________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 学生用户表
DROP TABLE IF EXISTS `user_student`;
CREATE TABLE `user_student` (
  `user_id` CHAR(10) NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(10) NOT NULL COMMENT '用户名',
  `passwd` VARCHAR(20) NOT NULL COMMENT '密码',
  `phone` VARCHAR(11) NOT NULL COMMENT '手机号码',
  `email` VARCHAR(50) NOT NULL COMMENT '邮箱',
  `status` ENUM('active','inactive') DEFAULT 'active' COMMENT '状态',
  `last_login_time` DATETIME NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  FOREIGN KEY (`user_id`) REFERENCES `student_basic_info`(`stu_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 辅导员用户表
DROP TABLE IF EXISTS `user_advisor`;
CREATE TABLE `user_advisor` (
  `user_id` CHAR(10) NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(10) NOT NULL COMMENT '用户名',
  `passwd` VARCHAR(20) NOT NULL COMMENT '密码',
  `phone` VARCHAR(11) NOT NULL COMMENT '手机号码',
  `email` VARCHAR(50) NOT NULL COMMENT '邮箱',
  `status` ENUM('active','inactive') DEFAULT 'active' COMMENT '状态',
  `last_login_time` DATETIME NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  CHECK (user_id LIKE 'G_________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 管理员用户表
DROP TABLE IF EXISTS `user_admin`;
CREATE TABLE `user_admin` (
  `user_id` CHAR(10) NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(10) NOT NULL COMMENT '用户名',
  `passwd` VARCHAR(20) NOT NULL COMMENT '密码',
  `role` ENUM('宿管管理员','图书馆管理员','财务处管理员','教务管理员') DEFAULT '教务管理员' COMMENT '管理员角色',
  `phone` VARCHAR(11) NOT NULL COMMENT '手机号码',
  `email` VARCHAR(50) NOT NULL COMMENT '邮箱',
  `status` ENUM('active','inactive') DEFAULT 'active' COMMENT '状态',
  `last_login_time` DATETIME NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  CHECK (user_id LIKE 'G_________')
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;