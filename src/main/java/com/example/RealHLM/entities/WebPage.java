package com.example.RealHLM.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "webpage")
@ToString @EqualsAndHashCode
@Getter @Setter
public class WebPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "url")
    private String url;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    public WebPage()
    {
    }

    public WebPage(String url)
    {
        this.url=url;
    }
}
