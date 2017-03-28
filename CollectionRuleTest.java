package com.key.rule.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 集合处理.   
 * 
 * 关于hashCode 和 equals 的处理:
 * 
 * 1. 只要重写equals， 就必须重写hashCode;
 * 
 * 2. 因为Set存储的是不重复的对象！！！  依据hashCode和equals 进行判断， 所以  Set 存储的对象就必须重写这两个方法；
 * 
 * 3. 如果自定义对象作为 Map 的键（键是不重复的对象）， 那么自定义对象就必须重写 hashCode和equals 方法；
 * 
 * && 因为String重写了hashCode 和 equals 方法， 所以可以直接使用String来作为Map的 Key值；
 * 
 * @author Key.Xiao
 * @version 1.0
 */
public class CollectionRuleTest {

	public static void main(String[] args) {
		CollectionRuleTest test = new CollectionRuleTest();
		test.collectionToArray();
		test.arrayToList();
		test.foreachTest();
		test.subListTest();
		test.specifiesListSize();
		test.entrySetTest();
		test.removeDuplicateValues();
	}

	/**
	 * [强制] 集合转数组的方法， 必须使用集合的 toArray(T[] array). 传入的是类型完全一样的数据，大小就是list.size
	 */
	private void collectionToArray() {
		List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");
		String[] array = new String[list.size()];
		array = list.toArray(array);
		System.out.println(array);
	}

	/**
	 * [强制] 使用工具类Arrays.asList()把数组转换为集合时， 不能使用其修改集合的相关方法，它的remove/add/clear方法会抛出unsupportedOperationException异常
	 * 
	 * asList的返回对象是一个ArrayList的内部类，并没有实现集合的修改方法，Arrays.asList 体现的是适配器模式，只是转换接口，后台的数据仍是数组。
	 */
	private void arrayToList() {
		String[] str = new String[] { "a", "b" };
		List<String> list = Arrays.asList(str);
		// list.add("c"); 运行时异常:java.lang.UnsupportedOperationException
		str[0] = "c"; // list.get(0) 会随之改变.
		System.out.println(list.get(0));
	}

	/**
	 * [强制] 不要在foreach循环中进行元素的remove和add操作， remove元素使用 Iterator方式， 如果并发操作， 需要对Iterator对象加锁
	 */
	private void foreachTest() {
		List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");
		/* 反例， 会出错。
		 * for (String string : list) {
			if ("b".equals(string)) {
				list.remove(string);
			}
			System.out.println(string);
		}*/
		/** 正解： */
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			String temp = it.next();
			if ("b".equals(temp)) {
				it.remove();
			}
		}
		for (String string : list) {
			System.out.println(string);
		}
	}

	/**
	 * [强制] ArrayList 的subList结果不可强转为 ArrayList， 否则会抛出 ClassCastException异常.
	 * 
	 * java.util.ArrayList$SubList cannot be cast to java.util.ArrayList
	 * 
	 * subList 返回的是ArrayList的内部类SubList，并不是ArrayList， 而是ArrayList的一个视图，
	 * 
	 * 对SubList子列表的所有操作最终都会反映到原列表上。
	 */
	private void subListTest() {
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add("a");
		arrayList.add("b");
		arrayList.add("c");
		arrayList.add("d");
		// ArrayList<String> subList = (ArrayList<String>) arrayList.subList(0, 2); 不可强转
		List<String> subList = arrayList.subList(0, 2); // 正解
		/** 对subList的操作会反映到arrayList上。 */
		subList.add("xiao");
		for (String string : arrayList) {
			System.out.println(string);
		}
		/** 对原列表的改变，会导致子列表的遍历、增加、删除等操作均产生 ConcurrentModificationException 异常。 */
		arrayList.add("3");
		/* 产生异常代码
		 * for (String string : subList) {
			System.out.println(string);
		}*/
	}

	/**
	 * [强制] JDK1.7 以上版本， Comparator 要满足三个条件， 否则Arrays.sort()和 Collections.sort() 会报IllegalArgumentException异常。
	 * 
	 * 1. x,y 的比较结果 和 y,x的比较结果相反；
	 * 
	 * 2. x>y, y>z, 则 x > z
	 * 
	 * 3. x=y, 则x,z比较结果和 y,z比较结果相同；
	 * 
	 * 要注意处理相等的情况，否则可能会出现异常；
	 */
	private void comparatorTest() {
		//
	}

	/**
	 * [推荐] Specifies the list size, 尽量指定集合初始值大小。
	 */
	private void specifiesListSize() {
		int initialCapacity = 100;
		// Constructs an empty list with the specified initial capacity.
		ArrayList<String> list = new ArrayList<>(initialCapacity);
		System.out.println(list.size());
	}

	/**
	 * [推荐!] 使用entrySet 遍历 Map类 集合的 Key、Value， 而不是用keySet方式进行遍历。
	 * 
	 * keySet 其实遍历了两次！！！ 一次是转为Iterator对象， 另一次是从hashMap中取出 key 所对应的value
	 * 
	 * keySet() 返回的是K值集合， 是一个Set 集合对象； values() 返回的是一个V值集合，是一个list集合对象，
	 * 
	 * entrySet() 返回的是K-V值 组合集合。
	 * 
	 * 而entrySet 只是遍历了一次就把key和value都放到了entry中， 效率更高。 如果是JDK8, 直接使用 Map.foreach方法！！！
	 */
	private void entrySetTest() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(10, "A");
		map.put(11, "B");
		map.put(12, "C");
		map.put(13, "D");
		for (Entry<Integer, String> i : map.entrySet()) {
			System.out.println(i.getKey());
			System.out.println(i.getValue());
		}
	}

	/**
	 * 利用Set元素唯一的特性， 可以快速对一个集合进行去重操作， 避免使用List的contain方法进行遍历、对比去重操作。
	 */
	private void removeDuplicateValues() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(1);
		list.add(2);
		list.add(2);
		list.add(3);
		HashSet<Integer> set = new HashSet<Integer>(list); // 将list放入一个set中，自动去重
		list.clear();
		list.addAll(set);
		for (Object object : list) {
			System.out.println(object);
		}
	}
}
