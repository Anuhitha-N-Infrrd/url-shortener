package com.urlshortener.repos;

import com.urlshortener.entities.Url;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UrlRepository extends MongoRepository<Url,Long> {

    public Url findByShortLink(String shortLink);
}
