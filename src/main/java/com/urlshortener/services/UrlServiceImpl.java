package com.urlshortener.services;

import com.google.common.hash.Hashing;
import com.urlshortener.VOs.UrlVo;
import com.urlshortener.entities.Url;
import com.urlshortener.repos.UrlRepository;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@EnableCaching
public class UrlServiceImpl implements UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlServiceImpl.class);

    public static final String HASH_KEY = "OriginalLink";

    @Autowired
    private UrlRepository urlRepository;

    //This method takes url as input and generates the short link if the entered url is valid
    @Override
    public Url generateShortLink(String user_email, UrlVo urlDto) {
        if (StringUtils.isNotEmpty(urlDto.getUrl())) {
            logger.info("-----> Creating short link");
            String encodedUrl = encodeUrl(urlDto.getUrl());
            Url urlToPersist = new Url();
            urlToPersist.setId(RandomUtils.nextInt(1, 10000));
            urlToPersist.setUser_email(user_email);
            urlToPersist.setCreationDate(LocalDateTime.now());
            urlToPersist.setOriginalUrl(urlDto.getUrl());
            urlToPersist.setShortLink(encodedUrl);
            urlToPersist.setExpirationDate(getExpirationDate(urlDto.getExpirationDate(), urlToPersist.getCreationDate()));
            Url urlToRet = saveShortLink(urlToPersist);

            if (urlToRet != null)
                return urlToRet;

            return null;
        }

        logger.info("-----> Invalid url, failed to create shortened url");
        return null;
    }

    //This method sets the expiration time for the short url
    private LocalDateTime getExpirationDate(String expirationDate, LocalDateTime creationDate) {
        if (StringUtils.isBlank(expirationDate)) {
            logger.info("-----> Setting expiration time to the url");
            return creationDate.plusDays(1);
        }
        LocalDateTime expirationDateToRet = LocalDateTime.parse(expirationDate);
        return expirationDateToRet;
    }

    //This method generates the encoded url
    private String encodeUrl(String url) {
        String encodedUrl = "";
        LocalDateTime time = LocalDateTime.now();
        encodedUrl = Hashing.murmur3_32()
                .hashString(url.concat(time.toString()), StandardCharsets.UTF_8)
                .toString();
        logger.info("-----> Returned encoded url");
        return encodedUrl;
    }

    //This method saves the url to the database
    @Override
    public Url saveShortLink(Url url) {
        logger.info("-----> Saving to db");
        Url urlToRet = urlRepository.save(url);
        return urlToRet;
    }

    //This method retrieves the url from the database
    @Override
    @Cacheable(key="#url", value=HASH_KEY)
    public Url getEncodedUrl(String url) {
        logger.info("-----> Retrieving url from db");
        Url urlToRet = urlRepository.findByShortLink(url);
        return urlToRet;
    }

    //This method deletes the url from the database
    @Override
    @CacheEvict(key="#url", value = HASH_KEY)
    public void deleteShortLink(Url url) {
        urlRepository.delete(url);
    }
}
