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
package com.marolabs.number;

import com.marolabs.util.Reusable;
import com.marolabs.util.pool.ObjectPool;

public interface ValueChangeListener<T extends MutableNumber<T>> {
	public static class ValueChangeEvent<T extends MutableNumber<T>> implements Reusable {
		private T before;
		private T current;
		private boolean disposed = false;
		@SuppressWarnings("rawtypes")
		private static ObjectPool<ValueChangeEvent> s_pool = Reuse.getPool(ValueChangeEvent.class);

		@SuppressWarnings("unchecked")
		public static <T extends MutableNumber<T>> ValueChangeEvent<T> create(T before, T current) {
			ValueChangeEvent<T> event = s_pool.alloc();
			event.before = before;
			event.current = current;

			return event;
		}

		private ValueChangeEvent() {
		}

		public T before() {
			return before;
		}

		public T current() {
			return current;
		}

		@Override
		public boolean disposed() {
			return disposed;
		}

		@Override
		public void dispose() {
			s_pool.free(this);
		}

		@Override
		public void _init() {
			disposed = false;
		}

		@Override
		public void _cleanup() {
			before = null;
			current = null;
		}

		public void finalize() throws Throwable {
			try {
				if (!disposed()) {
					_cleanup();
				}
			} finally {
				super.finalize();
			}
		}
	}

	public void valueChanged(ValueChangeEvent<T> event);
}
