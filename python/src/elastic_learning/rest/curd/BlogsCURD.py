#!/usr/bin/env python3

""" 对blogs 索引的操作类
    :author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email> 
    :sine 2017/9/18
    :version 1.0
    
    localhost
"""
import elastic_learning.rest.ESConfigue as configure

es_request = configure.ESRequest(configure.URL, index="a/")


def getInfo():
    """"
    查看es的安装信息
    """
    return es_request.get()


def create_articles_index():
    """
    创建一个articles 索引
    :return: 
    """
    return es_request.put(None)

if __name__ == '__main__':
    print(create_articles_index())