
package com.animeflix.sa;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AnimeRepository animeRepository;

    public HomeController(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("siteName", "AnimeFlix SA");
        model.addAttribute("animeList", animeRepository.findAll());
        return "home";
    }
}
