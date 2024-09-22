package ru.Tim.Proj.moneyAnalyzer.Controllers;


import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.Tim.Proj.moneyAnalyzer.Config.MyUserDetails;
import ru.Tim.Proj.moneyAnalyzer.Services.ExcelService;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/profile")
public class ExcelDataController {

    private final ExcelService excelService;

    @Value("${myapp.excelStoragePath}")
    private String filePath;

    @Autowired
    public ExcelDataController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping("/export-excel-user-data")
    public void downloadUserData(@AuthenticationPrincipal MyUserDetails myUserDetails,
                                 HttpServletResponse response) throws IOException {
        try {
            User user = myUserDetails.getUser();
            byte[] excelFile = excelService.generateUserDataExcel(user);
            String excelFileName = user.getLogin() + "_data_" + LocalDate.now();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + excelFileName + ".xlsx\"");
            response.setContentLength(excelFile.length);

            try (ServletOutputStream out = response.getOutputStream()) {
                out.write(excelFile);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating file");
        }
    }

    @PostMapping("/import-excel-user-data")
    public String importExcel(@RequestParam("file") MultipartFile file,
                              @RequestParam(name = "isDeleteOldData", required = false) boolean isDeleteOldData,
                              RedirectAttributes redirectAttributes,
                              @AuthenticationPrincipal MyUserDetails myUserDetails) {
        User user = myUserDetails.getUser();
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/profile";
        }

        try {
            if(isDeleteOldData){
                excelService.deleteOldData(user);
            }
            excelService.importUserDataExcel(user, file);
            redirectAttributes.addFlashAttribute("message", "File successfully imported.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to import file: " + e.getMessage());
        }

        return "redirect:/profile";
    }

}
