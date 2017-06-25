# 关于MapReduce

   MapReduce是一种可用于数据处理的编程模型。MapReduce程序本质上是并行运行的，
   因此可以将大规模的数据分析任务分发给任何一个拥有足够多机器的数据中心，充分利用Hadoop
   提供的并行计算的优势。

## 使用Hadoop来分析数据

   MapReduce任务过程分为两个处理阶段：map阶段和reduce阶段。每个阶段都以键/值对作为
   输入和输出，其类型由程序员来选择。程序员还需要写两个函数，map函数和reduce函数.
   示例取自《Hadoop权威指南-第三版》

#### 1. 创建查找最高气温的Mapper类

     package com.hadoopbook.ch02;

     import org.apache.hadoop.io.IntWritable;
     import org.apache.hadoop.io.LongWritable;
     import org.apache.hadoop.io.Text;
     import org.apache.hadoop.mapreduce.Mapper;

     import java.io.IOException;

     /**
      * [@author](https://my.oschina.net/arthor) WangWeiwei
      * [@version](https://my.oschina.net/u/931210) 1.0
      * [@sine](https://my.oschina.net/mysine) 17-2-4
      * 查找最高气温的mapper类
      */
     public class MaxTemperatureMapper extends  Mapper<LongWritable,Text,Text,IntWritable> {
         private static final int MISSING = 9999;

         @Override
         protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
             String line = value.toString();
             String year = line.substring(15,19);
             int airTemperature;
             if (line.charAt(87) == '+'){
                 // parseInt doesn't like leading plus signs
                 airTemperature = Integer.parseInt(line.substring(88, 92));
             }else {
                 airTemperature = Integer.parseInt(line.substring(87, 92));
             }
             String quality = line.substring(92, 93);
             if (airTemperature != MISSING && quality.matches("[01459]")) {
                 context.write(new Text(year), new IntWritable(airTemperature));
             }
         }

         public MaxTemperatureMapper() {
             super();
         }
     }

   这个mapper类是一个泛型类型，它有四个形参类型，分别指定map函数的输入键/输入值/输出键和输出值的类型。
   Hadoop本身提供了一套可优化网络序列化传输的基本类型，而不是直接使用java内嵌的类型。这些类型都在
   org.apache.hadoop.io包里。
   map() 方法的输入是一个键和一个值，方法还提供了context实例用于输出内容的写入。

#### 2. 查找最高气温的reducer类
   类似于上的方法，使用Reducer来定义reduce函数.

        package com.hadoopbook.ch02;

        import org.apache.hadoop.io.IntWritable;
        import org.apache.hadoop.io.Text;
        import org.apache.hadoop.mapreduce.Reducer;

        import java.io.IOException;

        /**
         * [@author](https://my.oschina.net/arthor) WangWeiwei
         * @version 1.0
         * @sine 17-2-4
         * 查找最高气温的Reducer类
         */
        public class MaxTemperatureReducer extends Reducer<Text,IntWritable,Text,IntWritable> {
            public MaxTemperatureReducer() {
                super();
            }

            @Override
            protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
                int maxValue = Integer.MIN_VALUE;
                for (IntWritable value : values){
                    maxValue = Math.max(maxValue, value.get());
                }
                context.write(key,new IntWritable(maxValue));
            }
        }

   同样，reduce函数也有四个形式参数类型用于指定输入和输出类型。reduce函数的输入类型必须匹配map函数
   的输出类型：即TEXT类型和IntWritable类型。

#### 3. MapReduce作业
   指定一个作业对象，在这个应用中用来在气象数据集中找出最高气温

        package com.hadoopbook.ch02;

        import org.apache.hadoop.fs.Path;
        import org.apache.hadoop.io.IntWritable;
        import org.apache.hadoop.io.Text;
        import org.apache.hadoop.mapreduce.Job;
        import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
        import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

        /**
         * @author WangWeiwei
         * @version 1.0
         * @sine 17-2-4
         * MaxTemperature Application to find the maximum temperature in the weather dataset
         */
        public class MaxTemperature {
            public static void main(String[] args) throws Exception{
                if (args.length != 2){
                    System.err.println("Usage: MaxTemperature <input path> <output path>");
                    System.exit(-1);
                }

                Job job = new Job();
                job.setJarByClass(MaxTemperature.class);
                job.setJobName("Max Temperature");

                FileInputFormat.addInputPath(job,new Path(args[0]));
                FileOutputFormat.setOutputPath(job,new Path(args[1]));

                job.setMapperClass(MaxTemperatureMapper.class);
                job.setReducerClass(MaxTemperatureReducer.class);

                job.setOutputKeyClass(Text.class);
                job.setOutputValueClass(IntWritable.class);

                System.exit(job.waitForCompletion(true) ? 0 : 1);
            }
        }
   Job对象指定作业的执行规范。我们可以使用它来控制整个作业的运行。我们在Hadoop集群上运行这个作业时，
   要把它打包成一个JAR文件（Hadoop在集群上发布这个文件）。不必明确指定JAR文件的名称，在Job对象的
   setJarByClass方法中传递一个类即可，Hadoop利用这个类来查找包含它的JAR文件，进而找到相关的JAR文件。

   构造Job对象之后，需要指定输入和输出数据路径。调用FileInputFormat类的静态方法addInputPath()
   来定义输入数据的路径，这个路径可以是单个文件/一个目录（此时目录下的所有文件当作输入）或符合特定文件模式的一系列文件。
   由函数名可知，可以多次调用addInputPath()方法。

   调用FIleOutFormat类中的静态方法setOutputPath()来指定输出路径，只能有一个输出路径。这个方法指定的是
   reduce函数输出文件的写入目录。在运行作业前该目录是不应该存在的，否则Hadoop会报错，并拒绝运行作业。
   这种预防措施的目的是防止数据丢失（长时间运行的作业如果结果被意外覆盖，肯定是非常恼人的）

   接着通过setMapperClass()和setReducerClass()指定map类型和reduce类型。

   setOutputKeyClass() 和 setOutputValueClass() 控制map和reduce函数的输出类型。

   在设置定义map和reduce函数的类后，可以开始运行作业。job中的waitForCompletion()方法返回一个布尔值。
