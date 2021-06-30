package com.urlshortener.VOs;

import io.swagger.annotations.ApiModelProperty;

public class UrlVo {
    @ApiModelProperty(value = "Url to be shortened")
    private String url;
    @ApiModelProperty(value = "Not mandatory")
    private String expirationDate;

    public UrlVo() {
    }

    public UrlVo(String url, String expirationDate) {
        this.url = url;
        this.expirationDate = expirationDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return "UrlVo{" +
                "url='" + url + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                '}';
    }
}
