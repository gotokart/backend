
package com.gotokart.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SayHi {
    @RequestMapping("/upload-file")
    public String addPathVar(
            @RequestParam("image") MultipartFile file
    ) {
        System.out.println("file name:" + file.getOriginalFilename());
        System.out.println("file size:" + file.getSize());
        System.out.println("file Type:" + file.getContentType());

        return "file uploaded successfully";
    }
}
