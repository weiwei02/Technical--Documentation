---
title: 源码分析-jdk8源码-Map接口源码分析
date: 2017-08-25 20:40:22
tags:
- java
- source
categories:
- source
author: 为为
avatar: /images/favicon.png
---
## 摘要
  工欲善其事，必先利其器。作为JAVA的开发者，如果说会使用jdk是工作的基础，那么了解jdk的实现原理，甚至自己写一些框架或包就是工作的进阶。尤记得听一名老工程师跟我说过，等你把jdk都研究透了，你就是一名合格的高级工程师。Map集合在JAVA开发中很重要，就常用程度来讲，其在现代业务开发的代码里出现的频率位居前列。今天这篇文章主旨就是探索Map及其常见的实现类。  
  本文着重从jdk8代码出发来分析Map，与之响应的会讨论部分数据结构和算法方面的知识。我希望读者你已经有了很好的java基础，能够了解常用的设计模式并对面向对象编程思想拥有深刻的理解。本文会忽略部分过于基础的内容和不在本文讨论范围内的部分API。  
  首先，文章会简要的介绍Map接口，接着去分析jdk中几个最常见的Map接口的实现类。文章会花些笔墨在HashMap这个最常见的Map类上，其次会去分析TreeMap等类。最后文章还会对比HashMap来研究适合在并发场景下使用的ConcurrentHashMap，来分析它究竟是如何优化才能保证HashMap的线程安全性的。

## 方法
### 基本接口
  Map是一种键-值对应的数据结构，`java.util.Map` 接口定义了Map常用的api。接下来我以代码的形式简要的对Map接口进行介绍。

        package java.util;

        import java.util.function.BiConsumer;
        import java.util.function.BiFunction;
        import java.util.function.Function;
        import java.io.Serializable;

        /**
         * 返回map中键值对集合的数目
         */
        int size();

        /**
         * 判断map中是否有键值对信息，如果该Map为空则返回 true 。
         */
        boolean isEmpty();

        /**
         判断map中是否有指定的key元素，如果有该元素，则返回 true 。
         */
        boolean containsKey(Object key);

        /**
         如果map中有一个或多个key所映射的值为value，则返回 true 。
         */
        boolean containsValue(Object value);

        /**
         根据指定的key在map中获取与之对应的value并返回。
         */
        V get(Object key);

        /**
         将key/value对放入到map中，如果map中已经存在了键为key的元素，则将原来的值替换为新value。
         */
        V put(K key, V value);

        /**
         从map中移除键为key的元素并返回
         */
        V remove(Object key);


        /**
        将目标集合中的所有元素全部放到该map中
         */
        void putAll(Map<? extends K, ? extends V> m);

        /**
         清空集合中的所有元素
         */
        void clear();


        /**
         返回map中所有key的集合
         该方法所返回的集合并非创建一个全新集合，如果对该集合进行删除操作会直接影响到原来map中的元素
         */
        Set<K> keySet();

        /**
         返回map中所有value的集合
         该方法所返回的集合也与map有对应关系，对map本身所做的删除操作会进而影响到此集合
         */
        Collection<V> values();

        /**
         返回map中的元素集合
         该元素集合与map还存在映射关系
         */
        Set<Map.Entry<K, V>> entrySet();

        /**
         * map元素接口
         将map元素封装成键-值对的格式
         */
        interface Entry<K,V> {
          ...//Entry中的内容暂不介绍
        }

        boolean equals(Object o);

        int hashCode();

        // Defaultable methods

        /**
         返回指定的key所对应的value或默认值
         * @since 1.8
         */
        default V getOrDefault(Object key, V defaultValue) {
            V v;
            return (((v = get(key)) != null) || containsKey(key))
                ? v
                : defaultValue;
        }

        /**
         对map集合进行遍历操作
         * @since 1.8
         */
        default void forEach(BiConsumer<? super K, ? super V> action) {
            Objects.requireNonNull(action);
            for (Map.Entry<K, V> entry : entrySet()) {
                K k;
                V v;
                try {
                    k = entry.getKey();
                    v = entry.getValue();
                } catch(IllegalStateException ise) {
                    // this usually means the entry is no longer in the map.
                    throw new ConcurrentModificationException(ise);
                }
                action.accept(k, v);
            }
        }

        /**
         将符合条件的map中的元素进行替换
         * @since 1.8
         */
        default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            Objects.requireNonNull(function);
            for (Map.Entry<K, V> entry : entrySet()) {
                K k;
                V v;
                try {
                    k = entry.getKey();
                    v = entry.getValue();
                } catch(IllegalStateException ise) {
                    // this usually means the entry is no longer in the map.
                    throw new ConcurrentModificationException(ise);
                }

                // ise thrown from function is not a cme.
                v = function.apply(k, v);

                try {
                    entry.setValue(v);
                } catch(IllegalStateException ise) {
                    // this usually means the entry is no longer in the map.
                    throw new ConcurrentModificationException(ise);
                }
            }
        }

        /**
         如果map中没有指定的key，或指定的key对应的value值为null，则将key-value中的value放入到map中，并返回新value。如果map中已经存在该key，且value不是null，则返回原值。
         * @since 1.8
         */
        default V putIfAbsent(K key, V value) {
            V v = get(key);
            if (v == null) {
                v = put(key, value);
            }

            return v;
        }

        /**
        以同时指定键值对的方式来从map中移除元素，返回操作成功状态
         * @since 1.8
         */
        default boolean remove(Object key, Object value) {
            Object curValue = get(key);
            if (!Objects.equals(curValue, value) ||
                (curValue == null && !containsKey(key))) {
                return false;
            }
            remove(key);
            return true;
        }

        /**
         如果原元素存在，则将原元素的值替换为新值
         * @since 1.8
         */
        default boolean replace(K key, V oldValue, V newValue) {
            Object curValue = get(key);
            if (!Objects.equals(curValue, oldValue) ||
                (curValue == null && !containsKey(key))) {
                return false;
            }
            put(key, newValue);
            return true;
        }

        /**
         当前的key在map中有对应的映射时，使用新元素替换原有元素
         * @since 1.8
         */
        default V replace(K key, V value) {
            V curValue;
            if (((curValue = get(key)) != null) || containsKey(key)) {
                curValue = put(key, value);
            }
            return curValue;
        }

        /**
         如果指定的key在map中的映射为空（或者map中不包含此key），则尝试使用给定的算法去计算一个值作为key的映射，并将这个元素存放进map中。
         * @since 1.8
         */
        default V computeIfAbsent(K key,
                Function<? super K, ? extends V> mappingFunction) {
            Objects.requireNonNull(mappingFunction);
            V v;
            if ((v = get(key)) == null) {
                V newValue;
                if ((newValue = mappingFunction.apply(key)) != null) {
                    put(key, newValue);
                    return newValue;
                }
            }

            return v;
        }

        /**
         如果map中已有指定的key和其映射，则尝试使用给定的函数重新计算一个value替换原值
         * @since 1.8
         */
        default V computeIfPresent(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            V oldValue;
            if ((oldValue = get(key)) != null) {
                V newValue = remappingFunction.apply(key, oldValue);
                if (newValue != null) {
                    put(key, newValue);
                    return newValue;
                } else {
                    remove(key);
                    return null;
                }
            } else {
                return null;
            }
        }

        /**
         使用给定的函数计算一个在map中与key对应的新值用来替换掉原来与key映射的value
         * @since 1.8
         */
        default V compute(K key,
                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            V oldValue = get(key);

            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue == null) {
                // delete mapping
                if (oldValue != null || containsKey(key)) {
                    // something to remove
                    remove(key);
                    return null;
                } else {
                    // nothing to do. Leave things as they were.
                    return null;
                }
            } else {
                // add or replace old mapping
                put(key, newValue);
                return newValue;
            }
        }

        /**
         如果在map中与key所对应的value为null，则使用新value去替换原来的值，否则就使用remappingFunction在原值和新值之间计算出一个值用来做key的映射。
         * @since 1.8
         */
        default V merge(K key, V value,
                BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            Objects.requireNonNull(remappingFunction);
            Objects.requireNonNull(value);
            V oldValue = get(key);
            V newValue = (oldValue == null) ? value :
                       remappingFunction.apply(oldValue, value);
            if(newValue == null) {
                remove(key);
            } else {
                put(key, newValue);
            }
            return newValue;
        }
        }

        /*

        */


 我们看到，jdk8对map类进行了大量的改动，增加了很多带有默认方法的api，其中包括如foreach等实用的api。

### Map骨架
  `java.util.AbstractMap` 类是一个抽象类，其对map接口提供了最简单的模版方法实现，如求map的entrySet()，hashCode()等方法。我们常用的类HashMap就是继承自此类，不过由于此类实现的时间较早（jdk1.2），当前大部分继承自此类的的类激斗都对此类的方法进行了更符合性能的优化重写。

###  哈希表 （HashTable）
  `java.util.HashTable`是数据结构中的哈希表的java实现，其内部存储key-value形式的键值对元素，任何不是null的对象都可以被当做key或value。为了能够正常的检索、存储对象，所有的key都应该有可用的hashCode()方法。哈希表的性能主要由初始化大小和负载因数两个重要的要素所影响，HashTable基于大量数据进行测验与分析，综合考虑存储时的空间兼用与检索时的时间消耗情况比，将负载因数的初始值设置为 0.75f ，如无必要情况，无需修改此值。对于初始化大小这个参数，其默认值为11，我们可以根据实际业务任意进行定制。HashTable是线程安全的，但是在实际开发使用中在没有线程安全的场景下java官方推荐我们直接使用`HashTable`，在高并发的场景下，我们更应该优先考虑使用`java.util.concurrent.ConcurrentHashMap`类。

        public class Hashtable<K,V>
          extends Dictionary<K,V>
          implements Map<K,V>, Cloneable, java.io.Serializable {

          /** 哈希表数据实际存放在Entry数组中 */
          private transient Entry<?,?>[] table;

          /** 哈希表中entry的数量 */
          private transient int count;

          /* 重新进行hash计算的阈值 */
          private int threshold;

          /** 哈希表的负载因数 */
          private float loadFactor;

          /**
           哈希表已修改数据结构的次数
           增加或减少Entry节点的数目或对哈希表进行扩容都会触发该值的变化，modCount主要用来在高并发场景下进行快速失败使用。
           */
          private transient int modCount = 0;

          /** 序列化版本id */
          private static final long serialVersionUID = 1421746759512286392L;


        }
        /**
        while(i.hasBext()){
          Entry e = i.next();
          if(value == e.getValue || value.equals(e.getValue))
            return true;
          }
          return false;
          ****/

  哈希表的基本属性中有一个 threshold 属性，这个属性代表着重新分配哈希空间的阈值，当哈希表中存放的数据超出此值所标识的大小，哈希表就会进行自动扩容并重新计算 threshold 值，其计算公式为 `threshold = (int)(capacity * loadFactor)`。

  首先我们分析一下用来检查是否包含某元素的contains()方法：

      public synchronized boolean contains(Object value) {
            if (value == null) {
                throw new NullPointerException();
            }

            Entry<?,?> tab[] = table;
            for (int i = tab.length ; i-- > 0 ;) {
                for (Entry<?,?> e = tab[i] ; e != null ; e = e.next) {
                    if (e.value.equals(value)) {
                        return true;
                    }
                }
            }
            return false;
        }
        public synchronized V get(Object key) {
            Entry<?,?> tab[] = table;
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % tab.length;
            for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
                if ((e.hash == hash) && e.key.equals(key)) {
                    return (V)e.value;
                }
            }
            return null;
        }

        /** 为哈希表添加节点 ****/

        private void addEntry(int hash, K key, V value, int index) {
            modCount++;

            Entry<?,?> tab[] = table;
            if (count >= threshold) {
                // Rehash the table if the threshold is exceeded
                rehash();

                tab = table;
                hash = key.hashCode();
                index = (hash & 0x7FFFFFFF) % tab.length;
            }

            // Creates the new entry.
            @SuppressWarnings("unchecked")
            Entry<K,V> e = (Entry<K,V>) tab[index];
            tab[index] = new Entry<>(hash, key, value, e);
            count++;
        }

        /** 向哈希表中存放某值 */
        public synchronized V put(K key, V value) {
            // Make sure the value is not null
            if (value == null) {
                throw new NullPointerException();
            }

            // 确认哈希表中不存在该key
            Entry<?,?> tab[] = table;
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % tab.length;
            @SuppressWarnings("unchecked")
            Entry<K,V> entry = (Entry<K,V>)tab[index];
            for(; entry != null ; entry = entry.next) {
                if ((entry.hash == hash) && entry.key.equals(key)) {
                    V old = entry.value;
                    entry.value = value;
                    return old;
                }
            }

            addEntry(hash, key, value, index);
            return null;
        }
        /** 向哈希表中添加节点 ****/
        public synchronized void clear() {
            Entry<?,?> tab[] = table;
            modCount++;
            for (int index = tab.length; --index >= 0; )
                tab[index] = null;
            count = 0;
        }

        public Set<K> keySet() {
            if (keySet == null)
                keySet = Collections.synchronizedSet(new KeySet(), this);
            return keySet;
        }

  HashTable的contains方法只能遍历查找，其时间复杂度是O(n)，在最坏的情况下我们可能需要遍历整个哈希表中的所有Entry。HashTable中可能发生线程安全问题的方法都使用 synchronized 关键字添加互斥锁，如此以来，虽然保证了其线程安全性，但对方法实现互斥会让整个对象都受影响，这也间接的给出了java不推荐在高并发的场景下使用HashTable的解释。其get()方法首先对key进行哈希运算然后通过取模等操作计算出key所在的索引，然后进行遍历操作，其时间复杂度为 O(logn)。这种算法被称之为哈希查找，拥有极高的效率，比较推崇。向哈希表中存放数据时，首先根据key的hash计算索引，然后在索引的table上找到其Entry链表，遍历该链表中是否已存在该key，如果已存在则将value替换，否则就在table上index索引的Entry链表后追加该元素。这种算法原理符合人类的思维形式，比较利于理解，可计算机CPU并不擅长求余计算，进行哈希查找时有一定的计算资源浪费。至于clear()方法就是遍历清除table上的每一个节点，将哈希表的大小置0。等待垃圾回收时直接就将哈希表中的内容全部回收，释放内存。哈希表的keySet()等方法也都是线程安全的，其实现原理就是通过Collections.synchronizedSet()方法将原有的keySet结合哈希表本身作为互斥锁转化成线程安全的集合。keySet的迭代操作使用哈希表内部进行专有优化的迭代器进行迭代，在保证线程安全的前提下，尽量加快迭代速度。

### HashMap 经典容器
  HashMap是我们在开发中最为钟爱的Map类，使用时简单方便，且性能极高。其内部实现由自己定制化的高效hash算法，jdk8更是对HashMap做出了很多的优化。在使用时HashMap允许null作为其键或值，增加了API的灵活性，但是HashMap并不是线程安全的，请注意不要在并发使用此类。HashMap的内部基本存储方式是大致可以看成是哈希表，当HashMap中的某个哈希桶内部的链表存储的元素达到一定数目时，随着链表的不断拓展，查找元素的算法的时间复杂度将上升到O(n)。为了能够保证HashMap的效率，当链表的程度大于阈值（默认为8）时，这个哈希桶就会被转化成效率更高红黑树。

    HashMap的属性代码：
