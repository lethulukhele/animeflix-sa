
package com.animeflix.sa;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AnimeDetailsController {

    private final AnimeRepository animeRepository;

    public AnimeDetailsController(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    @GetMapping("/anime/{id}")
    public String animeDetails(
            @PathVariable Long id,
            Model model) {

        Anime anime = animeRepository.findById(id)
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Anime not found"
                        ));

        model.addAttribute("siteName", "AnimeFlix SA");
        model.addAttribute("anime", anime);

        return "anime-details";
    }
}
