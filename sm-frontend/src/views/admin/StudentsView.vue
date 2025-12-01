<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { getStudentBasicInfoPage, getStudentStatusInfoList, updateStudentBasicInfo, updateStudentStatusInfo, getAdvisorList, addStudentBasicInfo, addStudentStatusInfo, deleteStudentBasicInfo, deleteStudentStatusInfo } from '@/api/student'
import { addUserStudent, deleteUserStudent } from '@/api/user'
import { exportStudents, exportStatus } from '@/api/business'
import type { StudentBasicInfo, StudentStatusInfo, Advisor, UserStudent } from '@/api/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const loading = ref(false)
const studentList = ref<StudentBasicInfo[]>([])
const statusList = ref<StudentStatusInfo[]>([])
const advisorList = ref<Advisor[]>([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const detailVisible = ref(false)
const addDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const addFormRef = ref<FormInstance>()
const editingStudent = ref<any>(null)
const currentStudent = ref<any>(null)
const newStudent = ref<any>({})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 合并学生基本信息和学籍信息
const mergedStudents = computed(() => {
  return studentList.value.map(student => {
    const status = statusList.value.find(s => s.stuId === student.stuId)
    return { ...student, ...status }
  })
})

const getWarningLevelType = (level: string) => {
  switch (level) {
    case '无': return 'success'
    case '一级': return 'warning'
    case '二级': return 'danger'
    case '三级': return 'danger'
    default: return 'info'
  }
}

const academicStatuses = ['在读', '毕业', '退学', '休学', '转学', '保留学籍']
const warningLevels = ['无', '一级', '二级', '三级']
const registerStatuses = ['已注册', '未注册']
const genders = ['男', '女']
const politicalStatuses = ['群众', '共青团员', '中共党员', '中共预备党员']

// 新增学生表单初始化
const initNewStudent = () => {
  newStudent.value = {
    stuId: '',
    name: '',
    gender: '男',
    idCard: '',
    birthDate: '',
    nationality: '中国',
    nation: '汉族',
    nativePlace: '',
    politicalStatus: '群众',
    phone: '',
    department: '',
    major: '',
    className: '',
    academicStatus: '在读',
    admissionDate: '',
    graduationDate: '',
    warningLevel: '无',
    registerStatus: '未注册',
    advisorId: '',
    advisor: ''
  }
}

// 新增学生表单校验规则（根据数据库 NOT NULL 约束设置）
const addFormRules = {
  // 基本信息 - 全部必填
  stuId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  nationality: [{ required: true, message: '请输入国籍', trigger: 'blur' }],
  nation: [{ required: true, message: '请输入民族', trigger: 'blur' }],
  nativePlace: [{ required: true, message: '请输入籍贯', trigger: 'blur' }],
  politicalStatus: [{ required: true, message: '请选择政治面貌', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  // 学籍信息 - 必填项
  department: [{ required: true, message: '请输入院系', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  className: [{ required: true, message: '请输入班级', trigger: 'blur' }],
  academicStatus: [{ required: true, message: '请选择学籍状态', trigger: 'change' }],
  admissionDate: [{ required: true, message: '请选择入学日期', trigger: 'change' }],
  registerStatus: [{ required: true, message: '请选择注册状态', trigger: 'change' }],
  advisorId: [{ required: true, message: '请选择辅导员', trigger: 'change' }]
  // 选填项：graduationDate（预计毕业日期）、warningLevel（有默认值）
}

const fetchData = async () => {
  loading.value = true
  try {
    const [basicRes, statusRes, advisorRes] = await Promise.all([
      getStudentBasicInfoPage({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        keyword: searchKeyword.value || undefined
      }),
      getStudentStatusInfoList(),
      getAdvisorList()
    ])
    studentList.value = basicRes.data?.records || []
    total.value = basicRes.data?.total || 0
    statusList.value = statusRes.data || []
    advisorList.value = advisorRes.data || []
  } catch (error) {
    console.error('获取学生列表失败', error)
  } finally {
    loading.value = false
  }
}

// 选择辅导员时同时更新ID和姓名
const handleAdvisorChange = (advisorId: string) => {
  const advisor = advisorList.value.find(a => a.userId === advisorId)
  if (advisor) {
    editingStudent.value.advisorId = advisor.userId
    editingStudent.value.advisor = advisor.userName
  }
}

// 分页变化处理
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleEdit = (row: any) => {
  editingStudent.value = { ...row }
  dialogVisible.value = true
}

// 打开新增学生对话框
const handleAdd = () => {
  initNewStudent()
  addDialogVisible.value = true
}

// 新增学生时选择辅导员
const handleNewAdvisorChange = (advisorId: string) => {
  const advisor = advisorList.value.find(a => a.userId === advisorId)
  if (advisor) {
    newStudent.value.advisorId = advisor.userId
    newStudent.value.advisor = advisor.userName
  }
}

// 从身份证号提取出生日期
const handleIdCardChange = (idCard: string) => {
  if (idCard && idCard.length === 18) {
    const year = idCard.substring(6, 10)
    const month = idCard.substring(10, 12)
    const day = idCard.substring(12, 14)
    newStudent.value.birthDate = `${year}-${month}-${day}`
  }
}

// 保存新增学生
const handleAddSave = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      // 保存学生基本信息
      const basicData: StudentBasicInfo = {
        stuId: newStudent.value.stuId,
        name: newStudent.value.name,
        gender: newStudent.value.gender,
        idCard: newStudent.value.idCard,
        birthDate: newStudent.value.birthDate,
        nationality: newStudent.value.nationality,
        nation: newStudent.value.nation,
        nativePlace: newStudent.value.nativePlace,
        politicalStatus: newStudent.value.politicalStatus,
        phone: newStudent.value.phone
      }
      await addStudentBasicInfo(basicData)
      
      // 保存学籍信息
      const statusData: StudentStatusInfo = {
        stuId: newStudent.value.stuId,
        department: newStudent.value.department,
        major: newStudent.value.major,
        className: newStudent.value.className,
        academicStatus: newStudent.value.academicStatus,
        admissionDate: newStudent.value.admissionDate,
        graduationDate: newStudent.value.graduationDate,
        warningLevel: newStudent.value.warningLevel,
        registerStatus: newStudent.value.registerStatus,
        advisorId: newStudent.value.advisorId,
        advisor: newStudent.value.advisor
      }
      await addStudentStatusInfo(statusData)
      
      // 同步创建学生用户信息（默认密码为身份证后6位）
      const userData: UserStudent = {
        userId: newStudent.value.stuId,
        userName: newStudent.value.name,
        passwd: newStudent.value.idCard ? newStudent.value.idCard.slice(-6) : '123456',
        phone: newStudent.value.phone || '',
        email: '',
        status: 'active',
        lastLoginTime: '',
        createTime: new Date().toISOString().slice(0, 19)
      }
      await addUserStudent(userData)
      
      ElMessage.success('新增学生成功')
      addDialogVisible.value = false
      fetchData()
    } catch (error: any) {
      const msg = error.message || ''
      if (msg.includes('Duplicate entry') || msg.includes('PRIMARY')) {
        ElMessage.error('学号已存在，请检查后重新输入')
      } else {
        ElMessage.error(msg || '新增学生失败')
      }
    }
  })
}

const handleViewDetail = (row: any) => {
  currentStudent.value = { ...row }
  detailVisible.value = true
}

const handleSave = async () => {
  try {
    // 更新学籍信息
    const statusData: StudentStatusInfo = {
      stuId: editingStudent.value.stuId,
      department: editingStudent.value.department,
      major: editingStudent.value.major,
      className: editingStudent.value.className,
      academicStatus: editingStudent.value.academicStatus,
      admissionDate: editingStudent.value.admissionDate,
      graduationDate: editingStudent.value.graduationDate,
      warningLevel: editingStudent.value.warningLevel,
      registerStatus: editingStudent.value.registerStatus,
      advisorId: editingStudent.value.advisorId,
      advisor: editingStudent.value.advisor
    }
    
    await updateStudentStatusInfo(statusData)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

// 删除学生
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 ${row.name}（${row.stuId}）吗？此操作将同时删除该学生的基本信息、学籍信息和用户信息，且不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // 按外键依赖顺序删除：先删用户信息，再删学籍信息，最后删基本信息
    await deleteUserStudent(row.stuId)
    await deleteStudentStatusInfo(row.stuId)
    await deleteStudentBasicInfo(row.stuId)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="students-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd" style="margin-right: 12px;">
              <el-icon><Plus /></el-icon>
              新增学生
            </el-button>
            <el-dropdown style="margin-right: 12px;">
              <el-button type="success">
                <el-icon><Download /></el-icon>
                导出数据
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="exportStudents">导出学生基本信息</el-dropdown-item>
                  <el-dropdown-item @click="exportStatus">导出学籍信息</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索学号/姓名/电话"
              style="width: 200px;"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch" style="margin-left: 12px;">
              搜索
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="mergedStudents" v-loading="loading" stripe>
        <el-table-column prop="stuId" label="学号" width="120" fixed />
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="department" label="院系" width="140" />
        <el-table-column prop="major" label="专业" width="160" />
        <el-table-column prop="className" label="班级" width="90" />
        <el-table-column prop="academicStatus" label="学籍状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.academicStatus === '在读' ? 'success' : 'info'" size="small">
              {{ row.academicStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warningLevel" label="警示等级" width="90">
          <template #default="{ row }">
            <el-tag :type="getWarningLevelType(row.warningLevel)" size="small">
              {{ row.warningLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerStatus" label="注册状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.registerStatus === '已注册' ? 'success' : 'danger'" size="small">
              {{ row.registerStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="advisorId" label="辅导员工号" width="110" />
        <el-table-column prop="advisor" label="辅导员" width="90" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="学生详细信息" width="800px">
      <template v-if="currentStudent">
        <el-divider content-position="left">基本信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学号">{{ currentStudent.stuId }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentStudent.gender }}</el-descriptions-item>
          <el-descriptions-item label="民族">{{ currentStudent.nation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ currentStudent.birthDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentStudent.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="籍贯">{{ currentStudent.nativePlace || '-' }}</el-descriptions-item>
          <el-descriptions-item label="政治面貌">{{ currentStudent.politicalStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentStudent.phone || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">学籍信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="院系">{{ currentStudent.department }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentStudent.className }}</el-descriptions-item>
          <el-descriptions-item label="学籍状态">
            <el-tag :type="currentStudent.academicStatus === '在读' ? 'success' : 'info'" size="small">
              {{ currentStudent.academicStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="入学日期">{{ currentStudent.admissionDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预计毕业">{{ currentStudent.graduationDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="警示等级">
            <el-tag :type="getWarningLevelType(currentStudent.warningLevel)" size="small">
              {{ currentStudent.warningLevel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册状态">
            <el-tag :type="currentStudent.registerStatus === '已注册' ? 'success' : 'danger'" size="small">
              {{ currentStudent.registerStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="辅导员">{{ currentStudent.advisor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="辅导员工号">{{ currentStudent.advisorId || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增学生对话框 -->
    <el-dialog v-model="addDialogVisible" title="新增学生" width="800px">
      <el-form ref="addFormRef" :model="newStudent" :rules="addFormRules" label-width="100px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="学号" prop="stuId">
              <el-input v-model="newStudent.stuId" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="newStudent.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="newStudent.gender" style="width: 100%">
                <el-option v-for="g in genders" :key="g" :label="g" :value="g" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="newStudent.idCard" placeholder="请输入身份证号" @input="handleIdCardChange" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="newStudent.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker v-model="newStudent.birthDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="国籍" prop="nationality">
              <el-input v-model="newStudent.nationality" placeholder="请输入国籍" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="民族" prop="nation">
              <el-input v-model="newStudent.nation" placeholder="请输入民族" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="籍贯" prop="nativePlace">
              <el-input v-model="newStudent.nativePlace" placeholder="请输入籍贯" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政治面貌" prop="politicalStatus">
              <el-select v-model="newStudent.politicalStatus" style="width: 100%">
                <el-option v-for="p in politicalStatuses" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">学籍信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="院系" prop="department">
              <el-input v-model="newStudent.department" placeholder="请输入院系" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="专业" prop="major">
              <el-input v-model="newStudent.major" placeholder="请输入专业" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="班级" prop="className">
              <el-input v-model="newStudent.className" placeholder="请输入班级" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="学籍状态" prop="academicStatus">
              <el-select v-model="newStudent.academicStatus" style="width: 100%">
                <el-option v-for="s in academicStatuses" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入学日期" prop="admissionDate">
              <el-date-picker v-model="newStudent.admissionDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预计毕业">
              <el-date-picker v-model="newStudent.graduationDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="警示等级">
              <el-select v-model="newStudent.warningLevel" style="width: 100%">
                <el-option v-for="l in warningLevels" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="注册状态" prop="registerStatus">
              <el-select v-model="newStudent.registerStatus" style="width: 100%">
                <el-option v-for="r in registerStatuses" :key="r" :label="r" :value="r" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="辅导员" prop="advisorId">
              <el-select 
                v-model="newStudent.advisorId" 
                style="width: 100%"
                placeholder="请选择辅导员"
                @change="handleNewAdvisorChange"
              >
                <el-option 
                  v-for="advisor in advisorList" 
                  :key="advisor.userId" 
                  :label="`${advisor.userId} - ${advisor.userName}`" 
                  :value="advisor.userId" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑学生学籍信息" width="600px">
      <el-form v-if="editingStudent" ref="formRef" :model="editingStudent" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input v-model="editingStudent.stuId" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="editingStudent.name" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学籍状态">
              <el-select v-model="editingStudent.academicStatus" style="width: 100%">
                <el-option v-for="s in academicStatuses" :key="s" :label="s" :value="s" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="警示等级">
              <el-select v-model="editingStudent.warningLevel" style="width: 100%">
                <el-option v-for="l in warningLevels" :key="l" :label="l" :value="l" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="注册状态">
              <el-select v-model="editingStudent.registerStatus" style="width: 100%">
                <el-option v-for="r in registerStatuses" :key="r" :label="r" :value="r" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="辅导员">
              <el-select 
                v-model="editingStudent.advisorId" 
                style="width: 100%"
                placeholder="请选择辅导员"
                @change="handleAdvisorChange"
              >
                <el-option 
                  v-for="advisor in advisorList" 
                  :key="advisor.userId" 
                  :label="`${advisor.userId} - ${advisor.userName}`" 
                  :value="advisor.userId" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.students-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: bold;
  }
  
  .header-actions {
    display: flex;
    align-items: center;
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
