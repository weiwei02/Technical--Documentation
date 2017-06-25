# *.class文件介绍

  一般来讲*.class文件是*.java文件在编译器编译后生成的jvm能够运行的文件，*.class文件又常被称为字节码文件。java在创始之初，就提倡“一次编写，处处运行的概念”，在当今编程圈中这个概念早已不是什么特例。java通过将开发人员所编写的java代码编译成class文件，然后由jvm虚拟机在执行时将不分平台的class文件中的字节码，再翻译成机器码，交给硬件执行。java就是靠jvm虚拟机的这个设计来实现与平台无关的特性的。class文件不但与硬件平台和操作系统无关，也和具体的编程语言无关，就目前来说，如函数式编程语言scala与Groovy都可以通过自己的编译器将源代码编译成class文件，在jvm上运行。
  综合来讲，class文件有以下两点特性：
  * 与硬件和操作系统平台无关
  * 与源码所使用的编程语言无关

# class类文件的结构
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

## 版本

   class文件的第5-6个字节代表的可执行该class文件的目标虚拟机的最低次版本号(Minor Version)，第7-8个字节是主版本号(Major Version)。java虚拟机可以运行比当前虚拟机版本号低的class文件，拒绝运行版本号不合法，或比自己版本高的class文件。JDK1.1的版本号是45，之后的每个大版本发布都把主版本号加1，如JDK1.2主版本号是46，JDK8的版本号是52。  
   JDK在编译java文件是可以通过 `javac -target 1.6 ...`命令来指定编译后的class文件可以在1.6的虚拟机版本上运行。
