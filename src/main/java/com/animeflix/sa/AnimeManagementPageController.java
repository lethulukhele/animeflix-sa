
package com.animeflix.sa;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnimeManagementPageController {

    @GetMapping("/admin/anime-management")
    public String animeManagementPage() {
        return "admin-anime";
    }
}
