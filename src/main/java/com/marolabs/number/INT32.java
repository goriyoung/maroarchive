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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.marolabs.util.Bitz;
import com.marolabs.util.pool.ObjectPool;

public final class INT32 extends SingnedInteger<INT32> {
	public static final int SIZE = 32;
	public static final int BYTES = SIZE / Byte.SIZE;

	private int val;

	private boolean disposed = false;

	private static final ObjectPool<INT32> s_pool = Reuse.getPool(INT32.class);

	public static INT32 create(int val) {
		return s_pool.alloc().set(val);
	}

	public static INT32 create(Number val) {
		return s_pool.alloc().set(val.intValue());
	}

	public INT32 duplicate() {
		return INT32.create(this);
	}

	public INT32 set(int val) {
		this.val = val;
		return this;
	}

	@Override
	public INT32 set(Number newVal) {
		return this.set(newVal.intValue());
	}

	@Override
	public void _init() {
		val = 0;
		disposed = false;
	}

	@Override
	public void _cleanup() {
	}

	@Override
	public boolean disposed() {
		return disposed;
	}

	@Override
	public void dispose() {
		if (!disposed) {
			disposed = true;
			s_pool.free(this);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		Bitz.putInt(out, val, BYTES, true);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException {
		val = Bitz.getInt32(in, BYTES, true);
	}

	@Override
	public int intValue() {
		return val;
	}

	@Override
	public long longValue() {
		return val;
	}

	@Override
	public float floatValue() {
		return val;
	}

	@Override
	public double doubleValue() {
		return val;
	}

	@Override
	public int compareTo(INT32 o) {
		return val == o.val ? 0 : val > o.val ? 1 : -1;
	}

	@Override
	public int bytes(byte[] out, int off, int len) {
		if (off + BYTES > out.length || len < BYTES) {
			throw new IndexOutOfBoundsException(off + " +" + BYTES + "  > " + out.length + " or " + len + "< " + BYTES);
		}

		Bitz.putInt(out, off, val, BYTES);
		return BYTES;
	}

	@Override
	public INT32 add(INT32 val) {
		this.val += val.val;

		return this;
	}

	@Override
	public INT32 subtract(INT32 val) {
		this.val -= val.val;

		return this;
	}

	@Override
	public INT32 mul(INT32 val) {
		this.val *= val.val;

		return this;
	}

	@Override
	public INT32 divide(INT32 val) {
		this.val /= val.val;

		return this;
	}

	@Override
	public INT32 mod(INT32 val) {
		this.val %= val.val;

		return this;
	}

	@Override
	public INT32 shiftLeft(int bits) {
		this.val = val << bits;

		return this;
	}

	@Override
	public INT32 shiftRight(int bits) {
		this.val = val >> bits;

		return this;
	}

	@Override
	public INT32 shiftSignRight(int bits) {
		this.val = val >>> bits;

		return this;
	}

	@Override
	public INT32 and(INT32 val) {
		this.val &= val.val;

		return this;
	}

	@Override
	public INT32 or(INT32 val) {
		this.val |= val.val;

		return this;
	}

	@Override
	public INT32 xor(INT32 val) {
		this.val ^= val.val;

		return this;
	}

	@Override
	public <S extends MutableNumber<S>> S cast(Class<S> type) {
		// TODO Auto-generated method stub
		return null;
	}
}
