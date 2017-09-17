---
title: -Elasticsearch-1初步使用
date: 2017-09-16 03:25:00
tags:
- Elsticsearch
- ELK
- java
- big-data
categories:
- java
- ElasticSearch
author: 为为
avatar: /images/favicon.png
---
# elastic search初步使用

   ElasticSearch是一个基于Lucene的搜索引擎，是当前世界上最受欢迎的全文搜索引擎，其主要特点如下：

   * 横向可拓展性： 往集群中增加机器时只需要更改一点配置就可以将新机器加入集群
   * 分片机制： 同一个索引切分成不同的分片
   * 高可用： 提供复制集机制，一个分片可以设置多个复制集，某台机器如果宕机不至于使集群无法工作
   * 使用简单，基于 REST api就可以完成搜索引擎的全部工作，所需学习成本低。

> 如无特殊声明，本文和后续文档将 Elastic Search 简称为ES


## 全文搜索
   全文搜索是指搜索程序扫描整个文档，通过一定的分词方法，对每一个词建立索引，并指明该词在文档中出现的位置和次数，最后搜索引擎再通过索引关键字搜索出文档并返回给用户的过程。

#### Lucene
   Lucene是Apache下的一个开源全文搜索引擎工具，提供了完整的查询引擎和索引引擎和部分文本分析引擎。不过Lucene仅仅是一个工具包，其目的是为了让研发人员能够通过这些工具包快速的为自己的应用搭建一个搜索引擎或者基于这些工具包开发出一个完整的搜索引擎。

#### 倒排序索引
   Lucene中的索引采用的是倒排序索引的模式。所谓的倒排序索引(Inverted Index)是通过属性的值来确定整条纪录的位置，而不是由纪录来确定属性的值。倒排索引把普通索引中的文档编号和值的关系倒过来，变成：“关键词”对“拥有该关键词的所有文章号”。带有倒排索引的文件我们称为倒排索引文件，简称倒排文件(inverted file)。倒排索引主要就由索引关键字和倒排文件所组成。

   建立搜索引擎的关键步骤就在于建立倒排索引，倒排索引一般以以下数据结构出现：



关键字 | 文章号[出现频率]| 出现位置
---|---|---
简单   | 1[3]           |  2,9,89
美女   | 5[1]           |  1
tip     | 9[2]          |  2,7,22

表1 倒排索引数据结构示例

倒排索引在存储上使用LSM树，维护其索引方法是当需要新增文档进入系统时，首先解析文档，之后更新内存中维护的临时索引，文档中出现的每个单词，在其倒排表列表末尾追加倒排表列表项；一旦临时索引将指定内存消耗光，即进行一次索引合并，这里需要倒排文件里的倒排列表存放顺序已经按照索引单词字典顺序由低到高排序，这样直接顺序扫描合并即可。

#### 实现
   lucene在实现倒排索引时将索引划分为词典文件(Term Dictionary)、频率文件(Frequencies)、位置文件(Positions)保存。其中词典文件不仅包含关键词，还有该关键词指向频率文件和位置文件的指针。

   为了节省存储空间，提升索引效率。Lucene还对索引进行了压缩。

## 安装
   ES使用JAVA语言开发，所以在安装ES之前需要在系统中安装有JDK。

   表2是本文中的示例所使用的软件环境信息。


类型    |   名称     |   版本
---|---|---
操作系统 | ubuntu server| 16.04.2 LTS
内核     |  Kernel      | 4.4.0-62-generic
容器     | docker       | 1.28
java     | openjdk      | 1.8.0_141
搜索引擎 | elasticsearch| 5.5.2

表2 本机软件环境信息



#### docker镜像安装
   为了方便部署，我直接采用docker镜像的方式搭建ES。镜像启用命令为：

        $ docker run -d -v "$PWD/esdata":/usr/share/elasticsearch/data --name elasticsearch -H elasticsearch elasticsearch  -Etransport.host=0.0.0.0 -Ediscovery.zen.minimum_master_nodes=1 elasticsearch

#### 系统内核参数
   由于elastic search需要用到nio和mmap(虚拟内存映射)技术，在启动该镜像之前首先需要检查一下系统内核对虚拟内存映射数目的限制(vm.max_map_count参数)是否大于262144，如果不满足这个条件，elastic search镜像将不会启动。

   我们可以通过以下两种方式去设置系统内核的vm.max_map_count参数：

        sysctl -w vm.max_map_count=262144

   如果想永久修改此参数，可以通过修改 `/etc/sysctl.conf` 文件的方式使其永久性的成为系统内核参数。

#### 测试
   在linux系统下，我们可以使用 `curl` 程序来完成REST接口的调用与测试，《Elasticsearch权威指南》对 `curl` 调用REST接口的方式描述如图1所示。

   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/1.png-github)
   图一 curl命令示意图

   使用以下命令可以检测elastic search是否启动成功。

    curl -i -XGET 'localhost:9200/'

   `-i` 参数是说明要打印http请求头信息，`GET`是请求方法，`localhost:9200/`就是我们服务器的地址和端口，elastic search默认与外部交互的端口就是9200.如果服务启动成功，你会收到类似虾苗的响应信息。

    HTTP/1.1 200 OK
    content-type: application/json; charset=UTF-8
    content-length: 331

    {
      "name" : "Franz Kafka",
      "cluster_name" : "elasticsearch",
      "cluster_uuid" : "JP677C9kRNqjEWYnFae_gQ",
      "version" : {
        "number" : "5.5.2",
        "build_hash" : "b2f0c09",
        "build_date" : "2017-08-14T12:33:14.154Z",
        "build_snapshot" : false,
        "lucene_version" : "6.6.0"
      },
      "tagline" : "You Know, for Search"
    }



## 使用
   为了方便操作，本文与后续内容都将使用chrome浏览器的 Restlet Client插件来模拟REST请求，在这里推荐一下，感谢该插件的作者将本插件开源。
#### 创建索引
   Elastic Search就像是一个nosql数据库一样，存储我们要进行查询的信息，在ES中，数据库名就是索引名。一个 Elastic Search 集群可以包含多个索引，相应的每个索引可以包含多个类型 。这些不同的类型存储着多个文档 ，每个文档又有多个属性。

   我们可以使用PUT方法创建blogs库，详细请求信息如图2所示。

   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/2.png-github)
   图二 创建blogs数据库

   如果收到以下回应内容，则说明blogs数据库创建成功。

   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/3.png-github)
   图3 创建blogs数据库成功的回应信息

   使用GET地址 http://ubuntu:9200/_cat/indices?v可以查看库的状态，返回结果如下：

    health status index uuid                   pri rep docs.count docs.deleted store.size pri.store.size
    yellow open   blogs Y9hkRePSQmiAzjNQ_K_FSw   5   1          0            0       810b           810b


   其中yellow代表健康度，状态是活动，索引为blogs，主分片数量5，复制集1，已存储的文档数为0.

### 新建文档
   ES是一个面向文档的数据库，每一个文档都代表一条完整的实体记录，本文实例中一个文档就代表一篇文章。存储一个文档到ES的行为就叫做索引，在索引一个文档之前，首先应该明确应该将文档存储在哪里。

   本文对于数据存储使用以下设计：

   * 每一篇文章被定义为一个文档，包含作者，发布时间，文章分类，正文等信息。
   * 每个文档都属于articles类型
   * articles类型保存在blogs索引里
   * blogs索引在我们的ES集群中。

   按照这个设计思路，我们向ES中索引一篇文章，其请求信息图4所示。

![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/4.png-github)
   图4 向blogs索引articles类型插入一篇新文档

   如果新建文档成功，则会收到HTTP状态码为201的回应，回应信息如图5所示。

   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/5.png-github)
   图5 向blogs索引articles类型插入一篇新文档回应信息

   接着我们再索引几篇新文档，如图6，图7.
   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/6.png-github)
   图6 向blogs索引articles类型插入更多新文档
   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/7.png-github)
   图7 向blogs索引articles类型插入更多新文档。


   如果你无法手动对文档进行编号，可以使用POST方法向ES中索引一个新文档，其操作方法和链接规则如图8所示。
   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/9.png-github)
   图8 向blogs索引articles类型POST插入一篇新文档

   从其相应结果中可以看出ES通过自己的规则为文档增加了_id字段，相应结果如图9所示.
   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/10.png-github)
   图9 向blogs索引articles类型POST插入一篇新文档响应信息



#### 检索文档
   在ES中检索已存在的文档只需使用GET方法访问刚刚新增文档时的连接就可以了。

    GET http://ubuntu:9200/blogs/articles/2

   返回结果如图10所示。
   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/8.png-github)
   图10 从blogs索引articles类型检索文档

   _index属性代表文档所属索引， _type是文档所属类型， _id是文档编号, _version是文档的版本（每对文档做一次修改， _version就会加1）， _found代表是否查询到指定文档， _source就是文档中所存储的内容。

#### 更新与删除文档
   使用REST API对ES中的数据进行修改或删除也是极其方便的。如果我们要删除某个文档，只需使用DELETE方法访问这个文档的链接就可以了。

    DELETE http://ubuntu:9200/blogs/articles/2

   我们先介绍一种全量更新文档的方法，使用PUT方法访问我们的文档地址，参数信息加上文档的新内容就可以全量更新文档了。文档更新后，ES会犯规给我们新的文档版本和操作结果。

   示例，对文档2进行全量更新，其请求与回应信息如图11，图12所示。

   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/11.png-github)
   图11 PUT文档全量更新
   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/12.png-github)
   图12 PUT文档全量更新响应

   ES的文档不能被修改，只能被替换。但ES为我们提供了 update API。通过update API操作从整体来看，我们可以对文档的某个位置进行部分更新。不过在底层实现上 update API 还是需要进行完整的检索-修改-重建索引 的处理过程。 区别在于这个过程发生在分片内部，这样就避免了多次请求的网络开销。因为减少了检索和重建索引步骤之间的时间，更新此文档时与其他进程的更新操作冲突的可能性也会减少。
   update 请求最简单的一种形式将待更新的字段作为doc的参数， 它只是与现有的文档进行合并。对象被合并到一起，覆盖现有的字段或者新增字段。

   示例，修改文档2的author和tags属性，其请求、新文档内容如图13、图14所示。
   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/13.png-github)
   图13 POST update文档部分更新
   ![image](http://owbdiwmx1.bkt.clouddn.com/images/java/elasticsearch/base/14.png-github)
   图14 POST update文档部分更新后的结果

   如果更新文档是一个并发操作，当前工作在更新文档的检索步骤会获取当前版本号，在重建索引之前会检查文档此时的版本号是否与检索时的版本号一致，如果版本号不一致，当前线程就会发生操作失败，放弃文档的修改。如果业务上允许重复更新的话，可以通过retry_on_conflict属性，来设置失败重试。该属性用法如下面代码所示。对于不能重试的业务场景，update API提供了version属性进行乐观并发控制。

        PUT http://ubuntu:9200/blogs/articles/2/_update?retry_on_conflict=5

#### 搜索文档
   搜索是ES的核心，不过鉴于篇幅原因，关于搜索功能的使用将放到下篇文章中。

## 引用

 本文是我在学习使用ES时的笔记，在本文的写过过程中参考了大量其它资料，有些材料来源于网络，我由衷的表示感谢，但由于原作者不明，恕不能一一记述。
  1. Elasticsearch 权威指南.——https://www.elastic.co/
  2. Elasticsearch技术解析与实战/朱林编著.——北京：机械工业出版社，2016.12(数据分析与决策技术丛书)

    非原创声明
    >本文并非我的原创文章，而是我学习jvm时的笔记。文中的材料与数据大部分来自于其它资料，详细请查看本文的引用章节。

## 关于

  本项目和文档中所用的内容仅供学习和研究之用，转载或引用时请指明出处。如果你对文档有疑问或问题，请在项目中给我留言或发email到
  weiwei02@vip.qq.com   我的[github](https://github.com/weiwei02/):
  https://github.com/weiwei02/   我相信技术能够改变世界 。

## 链接
  * 上篇文章（无）
  * 下篇文章[ElasticSearch2轻量级搜索](http://www.jianshu.com/p/8fbb867c3d19)
