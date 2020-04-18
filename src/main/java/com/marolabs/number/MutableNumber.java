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

import java.io.Externalizable;
import java.util.LinkedList;
import java.util.List;

import com.marolabs.number.ValueChangeListener.ValueChangeEvent;
import com.marolabs.util.Reusable;

public abstract class MutableNumber<T extends MutableNumber<T>> extends Number implements Externalizable, Comparable<T>, Reusable {
	protected List<ValueChangeListener<T>> listeners = null;

	protected MutableNumber() {
	}

	public void addListener(ValueChangeListener<T> listener) {
		if (listener == null || readonly()) {
			return;
		}

		if (listeners == null) {
			listeners = new LinkedList<>();
		}

		listeners.add(listener);
	}

	public void removeListener(ValueChangeListener<T> listener) {
		if (listener == null || listeners == null) {
			return;
		}

		listeners.remove(listener);
		if (listeners.isEmpty()) {
			listeners = null;
		}
	}

	public void removeAllListeners() {
		if (listeners == null) {
			return;
		}

		listeners.clear();
		;
		listeners = null;
	}

	// TODO
	public boolean readonly() {
		return false;
	}

	public abstract T set(Number newVal);

	public abstract T duplicate();

	public abstract int bytes(byte[] out, int off, int len);

	public abstract T add(T val);

	public abstract T subtract(T val);

	public abstract T mul(T val);

	public abstract T divide(T val);

	public abstract <S extends MutableNumber<S>> S cast(Class<S> type);

	public abstract void dispose();

	public void finalize() throws Throwable {
		try {
			if (!disposed()) {
				_cleanup();
			}
		} finally {
			super.finalize();
		}
	}

	public int hashCode() {
		return this.intValue();
	}

	public String toString() {
		return String.valueOf(longValue());
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (this.getClass().isInstance(o)) {
			return this.compareTo((T) o) == 0;
		} else {
			return false;
		}
	}

	protected void fireValueChanged(T before, T current) {
		if (null != listeners) {
			ValueChangeEvent<T> event = ValueChangeEvent.create(before, current);

			for (ValueChangeListener<T> listener : listeners) {
				listener.valueChanged(event);
			}
			event.dispose();
		}
	}
}
