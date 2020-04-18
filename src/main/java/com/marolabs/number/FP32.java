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

public final class FP32 extends SingnedInteger<FP32> {
	public static final int SIZE = 8;
	public static final int BYTES = SIZE / Byte.SIZE;

	private byte val;

	private boolean disposed = false;
	private boolean readonly = false;

	private static final ObjectPool<FP32> s_pool = Reuse.getPool(FP32.class);

	public static FP32 create(byte val) {
		return create(val, false);
	}

	public static FP32 create(byte val, boolean readonly) {
		FP32 obj = s_pool.alloc();
		obj.val = val;
		obj.readonly = readonly;

		return obj;
	}

	public FP32 duplicate() {
		return FP32.create(this.byteValue(), this.readonly);
	}

	public FP32 set(byte val) {
		if (readonly()) {
			throw new RuntimeException("Readonly");
		}

		if (null != listeners && this.val != val) {
			FP32 before = this.duplicate();

			this.val = val;

			if (before.val != this.val) {
				this.fireValueChanged(before, this);
			}

			before.dispose();
		} else {
			this.val = val;
		}
		return this;
	}

	@Override
	public FP32 set(Number newVal) {
		return this.set(newVal.byteValue());
	}

	@Override
	public void _init() {
		val = 0;
		readonly = false;
		this.disposed = false;
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
		val = (byte) Bitz.getInt32(in, BYTES, true);
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
	public int compareTo(FP32 o) {
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
	public FP32 add(FP32 val) {
		return set((byte) (this.val + val.val));
	}

	@Override
	public FP32 subtract(FP32 val) {
		return set((byte) (this.val - val.val));
	}

	@Override
	public FP32 mul(FP32 val) {
		return set((byte) (this.val * val.val));
	}

	@Override
	public FP32 divide(FP32 val) {
		return set((byte) (this.val / val.val));
	}

	@Override
	public FP32 mod(FP32 val) {
		return set((byte) (this.val % val.val));
	}

	@Override
	public FP32 shiftLeft(int bits) {
		return set((byte) (this.val << bits));
	}

	@Override
	public FP32 shiftRight(int bits) {
		return set((byte) (this.val >> bits));
	}

	@Override
	public FP32 shiftSignRight(int bits) {
		return set((byte) (this.val >>> bits));
	}

	@Override
	public FP32 and(FP32 val) {
		return set((byte) (this.val & val.val));
	}

	@Override
	public FP32 or(FP32 val) {
		return set((byte) (this.val | val.val));
	}

	@Override
	public FP32 xor(FP32 val) {
		return set((byte) (this.val ^ val.val));
	}

	@Override
	public <S extends MutableNumber<S>> S cast(Class<S> type) {
		// TODO Auto-generated method stub
		return null;
	}
}
