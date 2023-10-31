package org.smartregister.chw.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface FnInterfaces {

    // Define our functional interfaces
    interface Predicate<T> {
        boolean test(T t) throws Exception;
    }

    interface Function<T, R> {
        R invoke(T t) throws Exception;
    }

    interface Accumulator<A, T> {
        A combine(A value1, T value2) throws Exception;
    }

    interface Producer<T> {
        T produce() throws Exception;
    }

    interface Runnable {
        void run() throws Exception;
    }

    interface Action<T> {
        void apply(T t) throws Exception;
    }

    class Mutable<V>{
        public V value;
        public Mutable(V value){
           this.value=value;
        }
    }

    class KeyValue<V> {
        public String key;
        public V value;
        private static final Pattern PATTERN_KEY_VALUE=Pattern.compile("^\\W*([a-z]\\w+)\\W+(\\w.*)");

        public KeyValue(String key, V value) {
            this.key = key;
            this.value = value;
        }
        // Pattern pattern = Pattern.compile("^\\W*(?<key>[a-z]\\w+)\\W+(?<value>\\w.*)");//api >=26;

        public static KeyValue<String> create(String input){
            Matcher m=PATTERN_KEY_VALUE.matcher(input != null ? input : "");
            return m.find()
                    ?new KeyValue<>(m.group(1),m.group(2))
                    :new KeyValue<>("","");
        }
    }
 }