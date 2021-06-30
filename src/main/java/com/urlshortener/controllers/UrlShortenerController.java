package com.urlshortener.controllers;

import com.urlshortener.VOs.UrlVo;
import com.urlshortener.entities.Url;
import com.urlshortener.responses.UrlErrorResponse;
import com.urlshortener.responses.UrlSuccessResponse;
import com.urlshortener.services.UrlService;
import com.urlshortener.utils.UserIdFetcher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/url")
@EnableCaching
@Api(value = "Url Shortening Service", description = "This service can be used to generate a short url when the original url is provided by the user")
public class UrlShortenerController {

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerController.class);

    @Autowired
    private UrlService urlService;

    @Autowired
    private UserIdFetcher userIdFetcher;

    @ApiOperation(value="Generates short link")
    @PostMapping("/generate")
    public ResponseEntity<?> generateShortLink(HttpServletRequest httpServletRequest, @RequestBody UrlVo urlVo)
    {
        logger.info("-----> Entering generateShortLink Controller");
        String userId = userIdFetcher.getUserId(httpServletRequest);
        Url urlToRet = urlService.generateShortLink(userId,urlVo);

        if(urlToRet != null)
        {
            logger.info("-----> Valid url");
            UrlSuccessResponse urlResponse = new UrlSuccessResponse();
            urlResponse.setOriginalUrl(urlToRet.getOriginalUrl());
            urlResponse.setExpirationDate(urlToRet.getExpirationDate());
            urlResponse.setShortLink(urlToRet.getShortLink());
            logger.info("-----> Short link generated");
            return new ResponseEntity<UrlSuccessResponse>(urlResponse, HttpStatus.OK);
        }

        logger.info("-----> Invalid url");
        UrlErrorResponse urlErrorResponse = new UrlErrorResponse();
        urlErrorResponse.setStatus("404");
        urlErrorResponse.setError("There was an error processing your request. please try again.");
        return new ResponseEntity<UrlErrorResponse>(urlErrorResponse,HttpStatus.OK);

    }

    @ApiOperation(value = "Redirects to the original link")
    @GetMapping("/{shortLink}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortLink, HttpServletResponse response) throws IOException {

        logger.info("-----> Entering redirectToOriginalUrl Controller");

        if(StringUtils.isEmpty(shortLink))
        {
            logger.info("-----> Invalid short link");
            UrlErrorResponse urlErrorResponse = new UrlErrorResponse();
            urlErrorResponse.setError("Invalid Url");
            urlErrorResponse.setStatus("400");
            return new ResponseEntity<UrlErrorResponse>(urlErrorResponse,HttpStatus.OK);
        }

        logger.info("-----> Retrieving url");
        Url urlToRet = urlService.getEncodedUrl(shortLink);

        if(urlToRet == null)
        {
            logger.info("-----> Url not valid");
            UrlErrorResponse urlErrorResponse = new UrlErrorResponse();
            urlErrorResponse.setError("Url does not exist or it might have expired!");
            urlErrorResponse.setStatus("400");
            return new ResponseEntity<UrlErrorResponse>(urlErrorResponse,HttpStatus.OK);
        }

        if(urlToRet.getExpirationDate().isBefore(LocalDateTime.now()))
        {
            logger.info("-----> Deleting expired url");
            urlService.deleteShortLink(urlToRet);
            UrlErrorResponse urlErrorResponse = new UrlErrorResponse();
            urlErrorResponse.setError("Url Expired. Please try generating a fresh one.");
            urlErrorResponse.setStatus("200");
            return new ResponseEntity<UrlErrorResponse>(urlErrorResponse,HttpStatus.OK);
        }

        logger.info("-----> Redirecting to original url");
        response.sendRedirect(urlToRet.getOriginalUrl());
        return null;
    }
}
