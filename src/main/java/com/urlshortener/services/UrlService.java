package com.urlshortener.services;

import com.urlshortener.VOs.UrlVo;
import com.urlshortener.entities.Url;
import org.springframework.stereotype.Service;

@Service
public interface UrlService {
    Url generateShortLink(String user_email,UrlVo urlDto);
    Url saveShortLink(Url url);
    Url getEncodedUrl(String url);
    void deleteShortLink(Url url);
}
