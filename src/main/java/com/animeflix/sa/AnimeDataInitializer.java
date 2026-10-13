
package com.animeflix.sa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnimeDataInitializer {

    @Bean
    CommandLineRunner loadAnime(AnimeRepository animeRepository) {
        return args -> {

            if (animeRepository.count() == 0) {

                animeRepository.save(new Anime(
                        "Naruto",
                        "Action, Adventure",
                        "2002",
                        "8.4/10",
                        "Naruto Uzumaki dreams of becoming the strongest ninja and earning the respect of his village.",
                        "",
                        ""
                ));

                animeRepository.save(new Anime(
                        "One Piece",
                        "Action, Adventure",
                        "1999",
                        "9.0/10",
                        "Monkey D. Luffy and his crew sail across the seas in search of the legendary One Piece treasure.",
                        "",
                        ""
                ));

                animeRepository.save(new Anime(
                        "Demon Slayer",
                        "Action, Fantasy",
                        "2019",
                        "8.6/10",
                        "Tanjiro Kamado joins the fight against demons after tragedy strikes his family.",
                        "",
                        ""
                ));

                animeRepository.save(new Anime(
                        "Attack on Titan",
                        "Action, Drama",
                        "2013",
                        "9.1/10",
                        "Humanity fights for survival against mysterious giant humanoid creatures.",
                        "",
                        ""
                ));

                animeRepository.save(new Anime(
                        "My Hero Academia",
                        "Action, Superhero",
                        "2016",
                        "8.2/10",
                        "A young boy without superpowers pursues his dream of becoming a hero.",
                        "",
                        ""
                ));

                animeRepository.save(new Anime(
                        "Jujutsu Kaisen",
                        "Action, Supernatural",
                        "2020",
                        "8.5/10",
                        "Yuji Itadori enters the dangerous world of curses and sorcerers.",
                        "",
                        ""
                ));

                System.out.println("Sample anime inserted successfully!");
            } else {
                System.out.println("Anime records already exist. Skipping sample data.");
            }
        };
    }
}
