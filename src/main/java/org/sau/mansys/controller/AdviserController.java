package org.sau.mansys.controller;

import org.sau.mansys.models.Adviser;
import org.sau.mansys.service.AdviserService;
import org.sau.mansys.service.HdfsService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class AdviserController {

    private final AdviserService adviserService;
    private final HdfsService hdfsService;

    public AdviserController(AdviserService adviserService, HdfsService hdfsService) {
        this.adviserService = adviserService;
        this.hdfsService = hdfsService;
    }

    /* ---------- the page ---------- */

    @GetMapping("/")
    public String report(Model model) {
        model.addAttribute("rows", adviserService.report());
        model.addAttribute("advisers", adviserService.findAll());
        model.addAttribute("hdfsFiles", hdfsService.list());
        return "advisers";
    }

    /* ---------- every <img> on that page comes through here, from HDFS ---------- */

    @GetMapping(value = "/image/{name}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] image(@PathVariable String name) {
        if (name == null || name.isBlank() || !hdfsService.exists(name)) {
            name = "nophoto.jpg";
        }
        return hdfsService.read(name);
    }

    /* ---------- upload: writes HDFS, updates MySQL, invalidates Redis ---------- */

    @PostMapping("/upload")
    public String upload(@RequestParam("adviserId") Long adviserId,
                         @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return "redirect:/?error=empty";
        }

        Adviser adviser = adviserService.findById(adviserId);
        if (adviser == null) {
            return "redirect:/?error=noadviser";
        }

        String original = file.getOriginalFilename() == null ? "upload.jpg" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".jpg";
        String stored = adviser.getName().toLowerCase() + "_" + System.currentTimeMillis() + ext;

        hdfsService.write(stored, file.getBytes());   // 1. the bytes go into Hadoop
        adviser.setImageUrl(stored);                  // 2. the pointer goes into MySQL
        adviserService.save(adviser);                 // 3. @CacheEvict clears Redis

        return "redirect:/?uploaded=" + stored;
    }
}
