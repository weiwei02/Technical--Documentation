#!/usr/bin/env python3

"""
    :author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email> 
    :sine 2017/9/18
    :version 1.0
"""
import requests
URL = "http://ubuntu:9200/"
BLOGS = "blogs/"
ARTICLES = "articles/"
SEARCH = "_search"


class ESRequest:
    def __init__(self, url="", index="", i_type=""):
        self.__url = url
        self.__index = index
        self.__i_type = i_type
        self.__reconstruct_request_url()

    @property
    def url(self):
        return self.__url

    @property
    def index(self):
        return self.__index

    @index.setter
    def index(self, index):
        self.__index = index
        self.__reconstruct_request_url()

    @property
    def i_type(self):
        return self.__i_type

    @i_type.setter
    def i_type(self, i_type):
        self.__i_type = i_type
        self.__reconstruct_request_url()

    def __reconstruct_request_url(self):
        self.__request_url = self.__url + self.__index + self.__i_type

    def get(self, params=None, request_url=""):
        self.__http_request(requests.get, params, request_url)

    def post(self, param, request_url=""):
        self.__http_request(requests.post, param, request_url)

    def put(self, param, request_url=""):
        self.__http_request(requests.put, param, request_url)

    def delete(self, request_url=""):
        self.__http_request(requests.delete, None, request_url)

    def __http_request(self, method, param, request_url=""):
        if request_url == "" or request_url is None:
            return method(self.__request_url, param).json()
        else:
            return method(self.__request_url + "?" + request_url, param).json()


class BlogsRequest(ESRequest):
    """
    Blogs 索引请求类
    """
    def __init__(self):
        super().__init__(URL, BLOGS, "")


class ArticleRequest(BlogsRequest):
    """
    Blogs索引 articles类型请求类
    """
    def __init__(self):
        super().__init__().i_type = ARTICLES