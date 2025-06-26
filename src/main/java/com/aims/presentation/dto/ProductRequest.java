package com.aims.presentation.dto;

import java.math.BigDecimal;

/**
 * Request DTO for Product operations
 * Used for creating and updating products
 */
public class ProductRequest {

    private Long id;
    private String title;
    private String type;
    private BigDecimal price;
    private Integer quantity;
    private String description;
    private String imageUrl;

    // Book-specific fields
    private String author;
    private String genre;
    private String publicationDate;
    private String publisher;
    private Integer numberOfPages;
    private String language;
    private String bookCategory;

    // CD-specific fields
    private String artist;
    private String recordLabel;
    private String musicGenre;
    private String releaseDate;

    // DVD-specific fields
    private String director;
    private Integer runtime;
    private String studio;
    private String subtitles;
    private String filmGenre;

    // Constructors
    public ProductRequest() {
        // Default constructor for JSON deserialization
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getPublicationDate() { return publicationDate; }
    public void setPublicationDate(String publicationDate) { this.publicationDate = publicationDate; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Integer getNumberOfPages() { return numberOfPages; }
    public void setNumberOfPages(Integer numberOfPages) { this.numberOfPages = numberOfPages; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getBookCategory() { return bookCategory; }
    public void setBookCategory(String bookCategory) { this.bookCategory = bookCategory; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getRecordLabel() { return recordLabel; }
    public void setRecordLabel(String recordLabel) { this.recordLabel = recordLabel; }

    public String getMusicGenre() { return musicGenre; }
    public void setMusicGenre(String musicGenre) { this.musicGenre = musicGenre; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public Integer getRuntime() { return runtime; }
    public void setRuntime(Integer runtime) { this.runtime = runtime; }

    public String getStudio() { return studio; }
    public void setStudio(String studio) { this.studio = studio; }

    public String getSubtitles() { return subtitles; }
    public void setSubtitles(String subtitles) { this.subtitles = subtitles; }

    public String getFilmGenre() { return filmGenre; }
    public void setFilmGenre(String filmGenre) { this.filmGenre = filmGenre; }
}
