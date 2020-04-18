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
import java.nio.ByteOrder;

import com.marolabs.util.Bitz;
import com.marolabs.util.pool.ObjectPool;

public final class INT128 extends SingnedInteger<INT128> {
	public static final int SIZE = 128;
	public static final int BYTES = SIZE / Byte.SIZE;

	private long h64b;
	private long l64b;

	private boolean disposed = false;

	private static final ObjectPool<INT128> s_pool = Reuse.getPool(INT128.class);

	public static INT128 create(int val) {
		return s_pool.alloc().set(val);
	}

	public static INT128 create(Number val) {
		return s_pool.alloc().set(val.longValue());
	}

	public INT128 duplicate() {
		return INT128.create(this);
	}

	public INT128 set(long val) {
		l64b = val;
		h64b = 0;
		return this;
	}

	@Override
	public INT128 set(Number newVal) {
		if (newVal instanceof INT128) {
			INT128 new128 = (INT128) newVal;

			this.h64b = new128.h64b;
			this.l64b = new128.l64b;

		} else {
			set(newVal.longValue());
		}

		return this;
	}

	@Override
	public void _init() {
		l64b = 0;
		h64b = 0;
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
		Bitz.putInt(out, h64b, 8, true);
		Bitz.putInt(out, l64b, 8, true);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException {
		h64b = Bitz.getInt64(in, BYTES, true);
		l64b = Bitz.getInt64(in, BYTES, true);
	}

	@Override
	public int intValue() {
		return (int) l64b;
	}

	@Override
	public long longValue() {
		return l64b;
	}

	@Override
	public float floatValue() {
		return longValue();
	}

	@Override
	public double doubleValue() {
		return longValue();
	}

	@Override
	public int compareTo(INT128 o) {
		if (h64b > o.h64b) {
			return 1;
		} else if (h64b < o.h64b) {
			return -1;
		} else {
			return l64b == o.l64b ? 0 : l64b > o.l64b ? 1 : -1;
		}
	}

	@Override
	public int bytes(byte[] out, int off, int len) {
		if (off + BYTES > out.length || len < BYTES) {
			throw new IndexOutOfBoundsException(off + " +" + BYTES + "  > " + out.length + " or " + len + "< " + BYTES);
		}

		if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
			Bitz.putInt(out, off, this.h64b, 8);
			Bitz.putInt(out, off + 8, this.l64b, 8);
		} else {
			Bitz.putInt(out, off + 8, this.l64b, 8);
			Bitz.putInt(out, off, this.h64b, 8);
		}
		return BYTES;
	}

	@Override
	public INT128 add(INT128 val) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public INT128 subtract(INT128 val) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public INT128 mul(INT128 val) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public INT128 divide(INT128 val) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public INT128 mod(INT128 val) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public INT128 shiftLeft(int bits) {
		// this val is less than 64bit
		if ((this.h64b & 0xEFFFFFFFFFFFFFFFL) == 0L) {

		}

		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public INT128 shiftRight(int bits) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public INT128 shiftSignRight(int bits) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public INT128 and(INT128 val) {
		this.h64b &= val.h64b;
		this.l64b &= val.l64b;

		return this;
	}

	@Override
	public INT128 or(INT128 val) {
		this.h64b |= val.h64b;
		this.l64b |= val.l64b;

		return this;
	}

	@Override
	public INT128 xor(INT128 val) {
		this.h64b ^= val.h64b;
		this.l64b ^= val.l64b;

		return this;
	}

	@Override
	public <S extends MutableNumber<S>> S cast(Class<S> type) {
		// TODO Auto-generated method stub
		return null;
	}
}
