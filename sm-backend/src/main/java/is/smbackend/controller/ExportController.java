package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import is.smbackend.pojo.*;
import is.smbackend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 数据导出控制器
 */
@Tag(name = "数据导出")
@RestController
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private StudentBasicInfoService studentBasicInfoService;

    @Autowired
    private StudentStatusInfoService studentStatusInfoService;

    @Autowired
    private AwardService awardService;

    @Autowired
    private PunishmentService punishmentService;

    @Operation(summary = "导出学生基本信息")
    @GetMapping("/students")
    public void exportStudents(HttpServletResponse response) throws IOException {
        List<StudentBasicInfo> list = studentBasicInfoService.list();
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学生基本信息");
        
        // 创建标题行
        String[] headers = {"学号", "姓名", "性别", "身份证号", "出生日期", "国籍", "民族", "籍贯", "政治面貌", "手机号"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowNum = 1;
        for (StudentBasicInfo student : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getStuId() != null ? student.getStuId() : "");
            row.createCell(1).setCellValue(student.getName() != null ? student.getName() : "");
            row.createCell(2).setCellValue(student.getGender() != null ? student.getGender() : "");
            row.createCell(3).setCellValue(student.getIdCard() != null ? student.getIdCard() : "");
            row.createCell(4).setCellValue(student.getBirthDate() != null ? student.getBirthDate().toString() : "");
            row.createCell(5).setCellValue(student.getNationality() != null ? student.getNationality() : "");
            row.createCell(6).setCellValue(student.getNation() != null ? student.getNation() : "");
            row.createCell(7).setCellValue(student.getNativePlace() != null ? student.getNativePlace() : "");
            row.createCell(8).setCellValue(student.getPoliticalStatus() != null ? student.getPoliticalStatus() : "");
            row.createCell(9).setCellValue(student.getPhone() != null ? student.getPhone() : "");
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 设置响应头
        setExcelResponse(response, "学生基本信息");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Operation(summary = "导出学籍信息")
    @GetMapping("/status")
    public void exportStatus(HttpServletResponse response) throws IOException {
        List<StudentStatusInfo> list = studentStatusInfoService.list();
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学籍信息");
        
        String[] headers = {"学号", "学院", "专业", "班级", "学籍状态", "入学日期", "毕业日期", "警示等级", "注册状态", "辅导员ID", "辅导员"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (StudentStatusInfo info : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(info.getStuId() != null ? info.getStuId() : "");
            row.createCell(1).setCellValue(info.getDepartment() != null ? info.getDepartment() : "");
            row.createCell(2).setCellValue(info.getMajor() != null ? info.getMajor() : "");
            row.createCell(3).setCellValue(info.getClassName() != null ? info.getClassName() : "");
            row.createCell(4).setCellValue(info.getAcademicStatus() != null ? info.getAcademicStatus() : "");
            row.createCell(5).setCellValue(info.getAdmissionDate() != null ? info.getAdmissionDate().toString() : "");
            row.createCell(6).setCellValue(info.getGraduationDate() != null ? info.getGraduationDate().toString() : "");
            row.createCell(7).setCellValue(info.getWarningLevel() != null ? info.getWarningLevel() : "");
            row.createCell(8).setCellValue(info.getRegisterStatus() != null ? info.getRegisterStatus() : "");
            row.createCell(9).setCellValue(info.getAdvisorId() != null ? info.getAdvisorId() : "");
            row.createCell(10).setCellValue(info.getAdvisor() != null ? info.getAdvisor() : "");
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        setExcelResponse(response, "学籍信息");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Operation(summary = "导出奖励记录")
    @GetMapping("/awards")
    public void exportAwards(
            @RequestParam(required = false) String stuId,
            HttpServletResponse response) throws IOException {
        
        LambdaQueryWrapper<Award> wrapper = new LambdaQueryWrapper<>();
        if (stuId != null && !stuId.isEmpty()) {
            wrapper.eq(Award::getStuId, stuId);
        }
        List<Award> list = awardService.list(wrapper);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("奖励记录");
        
        String[] headers = {"奖励编号", "学号", "奖励类型", "奖励等级", "奖励名称", "奖励金额", "颁发机构", "申请日期", "获奖日期", "辅导员审核", "管理员审核", "奖励状态"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (Award award : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(award.getAwardId() != null ? award.getAwardId() : "");
            row.createCell(1).setCellValue(award.getStuId() != null ? award.getStuId() : "");
            row.createCell(2).setCellValue(award.getAwardType() != null ? award.getAwardType() : "");
            row.createCell(3).setCellValue(award.getAwardLevel() != null ? award.getAwardLevel() : "");
            row.createCell(4).setCellValue(award.getAwardName() != null ? award.getAwardName() : "");
            row.createCell(5).setCellValue(award.getAwardAmount() != null ? award.getAwardAmount().doubleValue() : 0);
            row.createCell(6).setCellValue(award.getIssueOrg() != null ? award.getIssueOrg() : "");
            row.createCell(7).setCellValue(award.getApplyDate() != null ? award.getApplyDate().toString() : "");
            row.createCell(8).setCellValue(award.getAwardDate() != null ? award.getAwardDate().toString() : "");
            row.createCell(9).setCellValue(award.getAdvisorStatus() != null ? award.getAdvisorStatus() : "");
            row.createCell(10).setCellValue(award.getAdminStatus() != null ? award.getAdminStatus() : "");
            row.createCell(11).setCellValue(award.getAwardStatus() != null ? award.getAwardStatus() : "");
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        setExcelResponse(response, "奖励记录");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Operation(summary = "导出处分记录")
    @GetMapping("/punishments")
    public void exportPunishments(
            @RequestParam(required = false) String stuId,
            HttpServletResponse response) throws IOException {
        
        LambdaQueryWrapper<Punishment> wrapper = new LambdaQueryWrapper<>();
        if (stuId != null && !stuId.isEmpty()) {
            wrapper.eq(Punishment::getStuId, stuId);
        }
        List<Punishment> list = punishmentService.list(wrapper);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("处分记录");
        
        String[] headers = {"处分编号", "学号", "处分类型", "处分原因", "处分机构", "申请日期", "处分日期", "管理员审核", "处分状态"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (Punishment p : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getPunishmentId() != null ? p.getPunishmentId() : "");
            row.createCell(1).setCellValue(p.getStuId() != null ? p.getStuId() : "");
            row.createCell(2).setCellValue(p.getPunishmentType() != null ? p.getPunishmentType() : "");
            row.createCell(3).setCellValue(p.getPunishmentReason() != null ? p.getPunishmentReason() : "");
            row.createCell(4).setCellValue(p.getIssueOrg() != null ? p.getIssueOrg() : "");
            row.createCell(5).setCellValue(p.getApplyDate() != null ? p.getApplyDate().toString() : "");
            row.createCell(6).setCellValue(p.getPunishmentDate() != null ? p.getPunishmentDate().toString() : "");
            row.createCell(7).setCellValue(p.getAdminStatus() != null ? p.getAdminStatus() : "");
            row.createCell(8).setCellValue(p.getPunishmentStatus() != null ? p.getPunishmentStatus() : "");
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        setExcelResponse(response, "处分记录");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 创建标题行样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 设置 Excel 下载响应头
     */
    private void setExcelResponse(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encodedFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
    }
}
