# Technical--Documentation
共享技术文档

##  项目简介
  为为日常工作和学习的总结。

### 技术文档主站简介

   主站文档是我在前人的脚步上对研发的学习和总结纪录，在以后的日子里，我会将自己研发工作中所可能遇见问题和心得在这里纪录下来，公开的分享。世界的存在很美好，开源的存在很精彩。从今天起，我便借助开源的力量，向改变人类的生活方式这条路迈进。

#####  点击这里直接进入[为为技术文档主站](https://weiwei02.github.io/Technical--Documentation/)，或者访问 [https://weiwei02.github.io/Technical--Documentation/](https://weiwei02.github.io/Technical--Documentation/)

##### 与文档配套的 [Technical--Documentation](https://github.com/weiwei02/Technical--Documentation)项目的地址是 https://github.com/weiwei02/Technical--Documentation




## *.class文件介绍

  一般来讲*.class文件是*.java文件在编译器编译后生成的jvm能够运行的文件，*.class文件又常被称为字节码文件。java在创始之初，就提倡“一次编写，处处运行的概念”，在当今编程圈中这个概念早已不是什么特例。java通过将开发人员所编写的java代码编译成class文件，然后由jvm虚拟机在执行时将不分平台的class文件中的字节码，再翻译成机器码，交给硬件执行。java就是靠jvm虚拟机的这个设计来实现与平台无关的特性的。class文件不但与硬件平台和操作系统无关，也和具体的编程语言无关，就目前来说，如函数式编程语言scala与Groovy都可以通过自己的编译器将源代码编译成class文件，在jvm上运行。
  综合来讲，class文件有以下两点特性：
  * 与硬件和操作系统平台无关
  * 与源码所使用的编程语言无关

## class类文件的结构
  > 每一个class文件都唯一对应着java类或接口枚举等定义信息，但类不一定都定义在class文件中，类可能是由类加载器动态生成的。

  class文件所以被称之为字节码据我猜测可能是因为class文件以8位（1字节）为单位进行存储的二进制信息。__字节码中各个数据项目严格按照顺序紧凑的排列，中间没有任何分割符。__在需要存储整型或浮点型这些大于8位的数据项目时，则会使用Big-Endian的字节序进行存储，将最高位字节放在地址最低位，最低位字节放在地址最高位。

  class文件格式采用表来存储数据，表中有无符号数和表两种数据类型。对于这两种数据类型说明如下：
  * **无符号数**： 无符号数是基本的数据类型，可以用来表示数字、索引引用、数量值或者按照UTF-8编码构成的字符串。如u1,u2,u4,u8分别代表1,2,4,8个字节字节的无符号数。
  * **表**： 表是由多个无符号数或者其它表作为数据项所组成的复合数据结构，表用于描述层次关系和复合的数据结构，整个class文件就是一张表。通常表以 _info 结尾。如表1就是一个class的表示例。

| 类型 | 名称 | 数量  
| :------: | :------: | :------:  
| u4 | magic | 1
| u2 | minor_version | 1
| u2 | major_version | 1
| u2 | constant_pool_count | 1
| cp_info | constant_pool | constant_pool_count - 1
| u2 | asscess_flags | 1
| u2 | this_class | 1
| u2 | super_class | 1
| u2 | interfaces_count | 1
| u2 | interfaces | interfaces_count
| u2 | fields_count | 1
| field_info | fields | fields_count
| u2 | methods_count | 1
|method_info|methods|method_count
| u2 | attributes_count | 1
| attribute_info | attributes | attributes_count

*表 1 class文件格式*

   class文件的顺序和格式必须严格实现按照上述规则，否则jvm将不能识别执行。

## Magic Number 魔数
> 魔数是一个和文件后缀名相似的用于文件格式识别的约定，一般规定文件内容开头的前几个字节为文件的魔数。不同于文件后缀名很容易被用户以重命名的方式进行更改，文件的魔数作为识别手段可以更安全的确定文件的可用性。

  class文件使用前4个字节作为魔数，来确定.class文件是否是一个能够被虚拟机识别的文件，其值是 0xCAFFEBABE 。

### 版本

   class文件的第5-6个字节代表的可执行该class文件的目标虚拟机的最低次版本号(Minor Version)，第7-8个字节是主版本号(Major Version)。java虚拟机可以运行比当前虚拟机版本号低的class文件，拒绝运行版本号不合法，或比自己版本高的class文件。JDK1.1的版本号是45，之后的每个大版本发布都把主版本号加1，如JDK1.2主版本号是46，JDK8的版本号是52。  
   JDK在编译java文件是可以通过 `javac -target 1.6 ...`命令来指定编译后的class文件可以在1.6的虚拟机版本上运行。

### 常量池
> 常量池是class文件中第一个表类型的数据项目，常量池是class文件中的资源仓库，是class文件结构中与其它项目关联最多的数据类型，也是占用class文件空间最大的数据项目之一。

   class文件里紧随版本之后的数据项是常量池，由于常量池的数量是不固定的，所以在常量池的数据项之前放置的有一个u2类型的数据，代表常量池的大小。常量池大小的初始值是1，如常量池的大小的数据如果显示的是10，就代表该class文件中有9个常量。  
   常量池中主要存放两大类常量：Literal(字面常量)、Symbolic Reference(符号引用常量)。字面常量类似于java中常量的概念，如文本字符串、final关键字所声明的常量等。而符号引用常量则是编译中的概念，主要包括以下三种类型的常量。
   - 符号常量
    + 类和接口的全限定名(Fully Qualified Name)
    + 字段名称和描述符(Descriptor)
    + 方法的名称和描述符

  class文件中不会保存各个方法或字段在内存中的布局信息，而是在虚拟机加载class文件时进行动态的连接。虚拟机在运行class文件时从常量池中获取对应的符号引用，再在创建类或者运行时解析连接到具体的内存地址当中。  
  常量池中的每一个常量都是一个表，在JDK8中有14种表结构的常量表。如表 2 所示。

|        类型                   | 标识  |          	描述　
| :------:                     | :------: | :------:
| CONSTANT_utf8_info           |    1　|	UTF-8编码的字符串
| CONSTANT_Integer_info	       |    3	 | 整形字面量
| CONSTANT_Float_info          |	  4	 | 浮点型字面量
| CONSTANT_Long_info	         |   ５  |	长整型字面量
| CONSTANT_Double_info	       |   ６	| 双精度浮点型字面量
| CONSTANT_Class_info	         |   ７	| 类或接口的符号引用
| CONSTANT_String_info	       |   ８	|字符串类型字面量
| CONSTANT_Fieldref_info	     |   ９	| 字段的符号引用
| CONSTANT_Methodref_info	     |  １０ |	类中方法的符号引用
| CONSTANT_InterfaceMethodref_info|１１|	接口中方法的符号引用
| CONSTANT_NameAndType_info	   |  １２	|字段或方法的符号引用
| CONSTANT_MothodType_info	   |  １６	| 标志方法类型
| CONSTANT_MethodHandle_info	 |  １５	| 表示方法句柄
| CONSTANT_InvokeDynamic_info	 | １８	  | 表示一个动态方法调用点
   *表 2 常量池数据项目类型表*  

   这14种常量结构表开始的第一个字节都是一个u1类型的标识位，其值就是表2中每项常量表类型所对应的标识列的值，代表当前常量属于哪种常量类型。这14种常量类型各自有自己不同的表结构，详情如表 3 所示。

   |            常量	               |    项目	  | 类型  |      	描述
   | :------:                       | :------:   |:------:|  :------:
   | CONSTANT_Utf8_info             |    	tag	   |    u1 |  值为1
   |                                |   length	 |    u2 | UTF-8编码的字符串占用的字节数
   |                                |   bytes	   |    u1 |	utf-8编码的字符串
   |  
   | CONSTANT_Integer_info          |  	tag      |   	u1 |	值为3
   |                                |   bytes	   |    u4 | 按照Big-Endian存储的int值
   |
   | CONSTANT_Float_info	          |   tag	     |    u1 |	4
   |                                |   bytes	   |    u4 |	按照Big-Endian存储的float值
   |
   | CONSTANT_Long_info             |  	tag	     |    u1 |	5
   |                                |   bytes	   |    u8 |	按照Big-Endian存储的long值
   |
   | CONSTANT_Double_info           | 	tag	     |    u1 |	6
   |                                |   bytes    |  	u8 | 	按照Big-Endian存储的long值double值
   |
   | CONSTANT_Class_info	          |   tag	     |    u1 |	7
   |                                |   index	   |    u2 |	指向全限定名常量项的索引
   |
   | CONSTANT_String_info	          |   tag	     |    u1 |	8
   |                                |   index	   |    u2 | 指向字符串常量的索引
   |
   | CONSTANT_Fieldref_info	        |   tag      |	  u1 |	9
   |                                |   index	   |    u2 |	指向声明字段的类或接口描述符CONSTANT_Class_info的索引值
   |                                |   index	   |    u2 |	指向CONSTANT_NameAndType_info的索引值
   |
   | CONSTANT_Methodref_info	      |   tag      |	  u1 |	10
   |                                |   index	   |    u2 |	指向声明方法的类描述符CONSTANT_Class_info的索引值
   |                                |   index	   |    u2 |	指向CONSTANT_NameAndType_info的索引值
   |
   | CONSTANT_InterfaceMethodref_info|	tag      |   	u1 |	11
   |                                |   index    |   	u2 |	指向声明方法的接口描述符CONSTANT_Class_info的索引值
   |                                |   index	   |    u2 |	指向CONSTANT_NameAndType_info的索引值
   |
   | CONSTANT_NameAndType_info	    |   tag 	   |    u1 |	12
   |                                |   index	   |    u2 |	指向该字段或方法名称常量的索引值
   |                                |   index	   |    u2 |	指向该字段或方法描述符常量的索引值
   |
   | CONSTANT_MethodHandle_info	    |   tag	     |    u1 |	15
   |                                |reference_kind|	u1 |	值必须1~9，它决定了方法句柄的的类型。方法句柄类型的值表示方法句柄的字节码行为
   |                               |reference_index|	u2 |	对常量池的有效索引
   |
   | CONSTANT_MethodType_info	      |    tag      |  	u1 |	16
   |                                |description_index|u2|	对常量池中方法描述符的有效索引常量池在该处的索引必须是CONSTANT_Utf8_info的结构，表示方法的描述符。
   |
   | CONSTANT_InvokeDynamic_info	  |    tag	    |   u1 |	18
   |                   |bootstap_method_attr_index|	  u2 |	对当前class文件中引导方法表的bootstap_methods[]数组的有效索引
   |                           |name_and_type_index|	u2 |	对当前常量池的有效索引，常量池在此处必须是CONSTANT_NameAndType_info结构，表示方法名和方法描述。
   *表 3 常量池中14中常量的结构总表*

   在表 3 中可以看到class文件所支持的所有的常量的结构信息，另外由于class中的类名、方法、字段都要用CONSTANT_Utf8_info型的常量来描述名称，所以CONSTANT_Utf8_info的最大长度也是java中类名、方法名或字段的最大长度65535，如果超出这个最大长度，便会无法编译。



  ## 引用

  在写作本文的过程中引用了以下资料，为为在此深深谢过以下资料的作者。
  1. 《The Java Virtual Machine Specification》
  2. 《深入理解Java虚拟机：JVM高级特性与最佳实践/周志明著.——2版.——北京：机械工业出版社，2013.6》


  ## 关于

  本项目和文档中所用的内容仅供学习和研究之用，转载或引用时请指明出处。如果你对文档有疑问或问题，请在项目中给我留言或发email到 weiwei02@vip.qq.com

  > from weiwei.wang 20170625
