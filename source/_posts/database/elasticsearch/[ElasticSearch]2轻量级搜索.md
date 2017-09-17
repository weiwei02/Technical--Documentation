---
title: -Elasticsearch-2轻量级搜索
date: 2017-09-18 02:00:00
tags:
- Elsticsearch
- ELK
- java
- big-data
categories:
- java
author: 为为
avatar: /images/favicon.png
---
# 概述
   ES是一个搜索引擎，我们之所以要使用它就是为了借助它快速构建全文索引，帮助我们快速检索数据。

   本文接着上篇文章[ElasticSearch1初步使用](http://www.jianshu.com/p/b338b37dd5e2)继续来通过blogs索引实例说明如何简单的借助ES实现轻量级搜索功能。

# 问题
   本文主要以应用ES为基本问题，主要探索ES通过GET方法进行搜索的使用方法。在实验的示例中本文也会简要的描述ES相关的理论知识。

   如何使用索引进行搜索，对搜索结果进行分页，并使用简单的条件来过滤搜过结果是本文需要探讨的问题。

# 方法
   本文采用对照RDBMS中SELECT功能的方法来描述ES中的轻量级搜索的概念，从总体上来讲，在ES中搜索数据其实和在RDBMS中SELECT数据是一样的，都可以指定搜索（查询）条件，都可以设置返回字段，也都可以进行一定的聚合运算。只不过ES搜索引擎使用全文搜索得到的结果会根据内容与搜索关键字的匹配程度给与每个结果一个权重，这个权重就作为搜索结果排序的依据。而RDBMS完全是依照ORDER BY子句中指定的排序规则进行排序的。

   说明：

   ES REST API所提供的完全在URL中描述参数的接口就是轻量级搜索接口，换句话说轻量级搜索接口都是使用GET方法的。所以文中示例使用的地址和API语法总结如没有对HTTP请求方法做出特殊声明，均使用GET方法。

   对本文中语法说明特殊字符的声明：
   * {a} 代表a变量,如{host}代表主机名或ip地址，{index}代表索引名
   * [] 代表可出现也可不出现，一般情况下带有[]标识的变量出现和不出现会有不同的含义。对于URL开头的[http[s]://]表达式，如果省略整个表达式则系统会默认使用http协议。
   * ... 对返回结果或请求内容进行部分省略。

### 全量数据搜索
##### 语法
   请求信息：

    [http[s]://]{host}:{port}/[{index}/[{type}/]]_search

   响应信息：

    {
        "took": 7,//执行搜索请求的耗时，单位毫秒
        "timed_out": false,//搜索是否超时
        "_shards":{//参与本次搜索的分片信息
            "total": 5,//参与本次搜索的分票总个数
            "successful": 5,//有多少个分片成功的完成了搜索任务
            "failed": 0 //有多少分片执行搜索任务失败
        },
        "hits":{ //搜过结果信息
            "total": 1,//匹配到的文档总数
            "max_score": 1.0193838,//查询所匹配文档的 _score 的最大值。
            "hits":[...]//匹配到的文档详细信息结果集
        }
    }


   在没有指定分页条件的情况下，响应信息默认返回10条结果。


##### 示例
   搜索blogs索引下，articles类型下的全部文档。这是一个没有任何过滤条件的最简单的搜索。

   请求信息：

    http://ubuntu:9200/blogs/articles/_search

   响应信息：

    {
      ...,
        "hits" : [
          {
            "_index" : "blogs",
            "_type" : "articles",
            "_id" : "AV6GlxJvP5Roqj_P-AOw",
            "_score" : 1.0,
            "_source" : {
              "title" : "ES自动生成id索引",
              "author" : "为为",
              "since" : "2017-09-16 20:20:20",
              "categorie" : "搜索引擎",
              "tags" : [
                "java",
                "研发"
              ],
              "body" : "如果你无法手动对文档进行编号，可以使用POST方法向ES中索引一个新文档，其操作方法和链接规则如图8所示。"
            }
          },
          ...//更多内容暂时省略
        ]
      }
    }

### 简单条件筛选
   简单的条件筛选就像是给SQL中的SELECT语句加上WHERE子句，从而限定只查找满足某些条件的结果。

##### 语法
   在请求路径中使用q参数，并将查询条件赋值给q

    [http[s]://]{host}:{port}/[{index}/[{type}/]]_search?q={param_name}:{param_value}

##### 示例
   我们搜索一下文章的tags包含 标签1 的文章。

   请求参数：

    http://ubuntu:9200/blogs/articles/_search?q=tags:标签1

   响应参数：

    {
      ...,
      "hits" : {
        ...
        "hits" : [
          {
            "_index" : "blogs",
            "_type" : "articles",
            "_id" : "1",
            "_score" : 1.0193838,
            "_source" : {
              "title" : "第一篇文章",
              "author" : "马华",
              "since" : "2017-09-10 20:20:20",
              "categorie" : "科学读物",
              "tags" : [
                "标签1",
                "标签2"
              ],
              "body" : "测试文章内容"
            }
          }
        ]
      }
    }

### 分页
   我们在SQL中SELECT语句可以使用LIMIT关键字进行分页，来保证我们每次查询只拿符合需求的数据条数。刚刚也提到过ES也支持分页，默认每页有10条数据。

##### 语法
   在搜索URL中可以使用`size`参数指定页大小，`from`应跳过的结果集条数。

   请求信息：

    [http[s]://]{host}:{port}/[{index}/[{type}/]]_search[?[size={size}][[&]from={from}]]

##### 示例
   不使用任何过滤条件搜索blogs索引type类型下的所有文档，指定页大小为2，从第跳过1条结果。

   请求信息：

    http://ubuntu:9200/blogs/articles/_search?size=2&from=1

   响应信息请自行演示。

### 多索引和类型
   如果你需要在一个或多个特殊的索引并且在一个或者多个特殊的类型中进行搜索。我们可以通过在URL中指定特殊的索引和类型达到这种效果，下面举例说明如何使用多索引或多类型。
   在所有的索引中搜索所有的类型

    http://ubuntu:9200/_search

   在 blogs 索引中搜索所有的类型

    http://ubuntu:9200/blogs/_search

   在 blogs 和 pictures 索引中搜索所有的文档

    http://ubuntu:9200/blogs,pictures/_search

   在任何以 b 或者 p 开头的索引中搜索所有的类型

    http://ubuntu:9200/b*,g*/_search

   在 blogs 索引中搜索 aiticles 类型

    http://ubuntu:9200/blogs/articles/_search

   在 blogs 和 pictures 索引中搜索 articles 和 a_images 类型的文档

    http://ubuntu:9200/blogs,pictures/articles,a_images/_search

   在所有的索引中搜索 articles 和 a_images 类型的文档

    http://ubuntu:9200/_all/articles,a_images/_search

   当在单一的索引下进行搜索的时候，Elasticsearch 转发请求到索引的每个分片中，可以是主分片也可以是副本分片，然后从每个分片中收集结果。多索引搜索恰好也是用相同的方式工作的，只是会涉及到更多的分片。

### 多个搜索条件
   刚刚介绍的条件搜索只能使用一个搜索条件，而我们一般的业务都需要更为复杂的搜索条件。

##### _all字段
   在 blogs/aiticles 中搜索“为为”的相关信息，注意该搜索中并未指定“为为”属于哪个字段。

    http://ubuntu:9200/blogs/_search?q=为为

##### 同时搜索多个字段
   在 blogs/aiticles中搜索author包含"为为"，**或** title包含"ES"的信息。

    http://ubuntu:9200/blogs/articles/_search?q=+title:ES自动生成id索引 +author:为为

   > + 前缀表示必须与查询条件匹配。类似地， -前缀表示一定不与查询条件匹配。没有 + 或者 -的所有其他条件都是可选的——匹配的越多，文档就越相关。在存在多个条件时，如果没有明确使用default_operator=AND指定多个条件的关系为AND，则多个条件的关系为OR。

##### 同一个字段下多种可能性
   在 blogs/aiticles中搜索author包含"为为"，**或**tags为"java"或"编码"的信息。

    http://ubuntu:9200/blogs/articles/_search?q=+tags:(java 研发)  +author:为为

   在 blogs/aiticles中搜索author包含"为为"，**且**tags为"java"或"编码"的信息。

    http://ubuntu:9200/blogs/articles/_search?q=+tags:(java 研发)  +author:为为&default_operator=AND

# 总结
   经过本文示例我们可以看出ES不仅可以作为一个NoSQL数据库，存储格式化的JSON数据，其更强大的功能在于搜索。ES不仅会存储文档，文档中的每个字段都将被索引并且可以被查询 。不仅如此，在简单查询时，Elasticsearch 可以使用 所有（all）索引字段，快度返回结果，我们甚至不必指定具体要搜索哪个字段就。

   总之ES的搜索可以完成以下任务：
   * 在结构化的数据(JSON)中使用结构化查询。
   * 全文检索。

   轻量级搜索虽然简单方便，但其也有缺点：
   * 当查询字符串中很小的语法错误，像 - ， : ， / 或者 " 不匹配等，将会返回错误而不是搜索结果。
   * 允许任何用户在索引的任意字段上执行可能较慢且重量级的查询，这可能会暴露隐私信息，甚至将集群拖垮。

   基于以上两点原因，不推荐向用户直接开放轻量级搜索功能，一般情况下只在开发调试中使用。

# 引用
  本文是我在学习使用ES时的笔记，在本文的写过过程中参考了大量其它资料，有些材料来源于网络，我由衷的表示感谢，但由于原作者不明，恕不能一一记述。
  1. Elasticsearch 权威指南.——https://www.elastic.co/
  2. Elasticsearch技术解析与实战/朱林编著.——北京：机械工业出版社，2016.12(数据分析与决策技术丛书)

# 关于
  本项目和文档中所用的内容仅供学习和研究之用，转载或引用时请指明出处。如果你对文档有疑问或问题，请在项目中给我留言或发email到
  weiwei02@vip.qq.com   我的[github](https://github.com/weiwei02/):
  https://github.com/weiwei02/   我相信技术能够改变世界 。

# 链接
 * 上篇文章[ElasticSearch-1初步使用](http://www.jianshu.com/p/b338b37dd5e2)
 * 下篇文章(待更新)
