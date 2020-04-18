/**
 * 
 * Copyright 2020 Marolabs(TM) Co,Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.marolabs.util;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.marolabs.util.Disposable;
import com.marolabs.util.pool.ObjectPool;

public interface Reusable extends Disposable {
	public static class Reuse {
		private static class ReusablePool<T extends Reusable> extends ObjectPool<T> {
			private Class<T> _type;

			public ReusablePool(Class<T> type) {
				super(128, "ReusePool<" + type.getSimpleName() + "@" + type.hashCode() + ">");
				_type = type;
			}

			@Override
			protected T newElement() {
				Constructor<T> constructor = null;
				try {
					constructor = _type.getDeclaredConstructor();
					constructor.setAccessible(true);

					return constructor.newInstance();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void init(T obj) {
				obj._init();
			}

			@Override
			protected void clean(T obj) {
				obj._cleanup();
			}
		}

		private final static Map<Class<?>, ReusablePool<?>> s_pool = new HashMap<>();

		@SuppressWarnings("unchecked")
		public static synchronized <T extends Reusable> ReusablePool<T> getPool(Class<T> type) {
			if (null == type) {
				return null;
			}

			ReusablePool<T> pool = null;
			if (!s_pool.containsKey(type)) {
				pool = new ReusablePool<T>(type);
				s_pool.put(type, pool);
			} else {
				pool = (ReusablePool<T>) s_pool.get(type);
			}

			return pool;
		}

		public static <T extends Reusable> void register(Class<T> type, ReusablePool<T> pool) {
			if (null == type || pool == null) {
				return;
			}

			s_pool.put(type, pool);
		}

		public static <T extends Reusable> T create(Class<T> type) {
			if (null == type) {
				return null;
			}

			return getPool(type).alloc();
		}

		@SuppressWarnings("unchecked")
		public static <T extends Reusable> void free(T obj) {
			if (null == obj) {
				return;
			}

			getPool((Class<T>) obj.getClass()).free(obj);
		}
	}

	public void _init();

	public void _cleanup();

	public void finalize() throws Throwable;
}
