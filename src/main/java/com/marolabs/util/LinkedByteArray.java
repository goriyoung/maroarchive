package com.marolabs.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import com.marolabs.data.ByteArray;
import com.marolabs.io.serial.Serializers;
import com.marolabs.io.stream.NOutputStream;
import com.marolabs.util.Reusable;
import com.marolabs.util.pool.ArrayPool;
import com.marolabs.util.pool.ObjectPool;

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
public final class LinkedByteArray implements Reusable, Externalizable {
    private static final ObjectPool<LinkedByteArray> s_pool = Reuse
	    .getPool(LinkedByteArray.class);

    private boolean disposed = false;
    private static final int ELE_SIZE = 1 << 12;
    private ArrayList<byte[]> list = new ArrayList<>();
    private int off_end;

    public LinkedByteArray() {
	list.add(ArrayPool.alloc_byte(ELE_SIZE));
	off_end = 0;
    }

    public void init() {
	this.clear();
	list.add(ArrayPool.alloc_byte(ELE_SIZE));
	off_end = 0;
    }

    public void append(LinkedByteArray la) {
	if (null != la) {
	    for (int i = 0; i < la.list.size() - 1; i++) {
		byte[] ele = la.list.get(i);
		append(ele, 0, Math.min(ele.length, ELE_SIZE));
	    }

	    append(la.list.get(la.list.size() - 1), 0, la.off_end);
	}
    }

    public void append(ByteArray array) {
	if (null != array && array.len > 0) {
	    append(array.array, array.offset, array.len);
	}
    }

    public void append(byte[] vals) {
	this.append(vals, 0, vals.length);
    }

    public void append(byte[] vals, int off, int len) {
	this.set(length(), vals, off, len);
    }

    public int length() {
	return (list.size() - 1) * ELE_SIZE + off_end;
    }

    public void set(int index, byte val) {
	byte[] one = ArrayPool.alloc_byte(1);

	one[0] = val;
	set(index, one, 0, 1);

	ArrayPool.free(one);
    }

    public void set(int start_index, byte[] vals) {
	this.set(start_index, vals, 0, vals.length);
    }

    private byte[] elementAt(int ele_index, boolean force_create) {
	if (ele_index >= list.size()) {
	    if (!force_create) {
		throw new IndexOutOfBoundsException("elementAt " + ele_index
			+ " from size = " + list.size());
	    } else {
		int count = ele_index - list.size() + 1;
		for (int i = 0; i < count; i++) {
		    list.add(ArrayPool.alloc_byte(ELE_SIZE, true));
		}
	    }

	}

	return list.get(ele_index);
    }

    public void set(int start_index, byte[] vals, int off, int len) {
	if (vals.length < off + len) {
	    throw new IndexOutOfBoundsException(
		    "set " + start_index + " from size = " + length());
	}

	if (len <= 0) {
	    return;
	}

	if (start_index + len > length()) {
	    off_end = (start_index + len) % ELE_SIZE;

	    if (off_end == 0) {
		off_end = ELE_SIZE;
	    }
	}

	int idx_ele = start_index / ELE_SIZE;
	int off_array = start_index % ELE_SIZE;

	byte[] array = elementAt(idx_ele, true);

	// copy first array
	int rest = ELE_SIZE - off_array;
	if (len <= rest) {
	    System.arraycopy(vals, off, array, off_array, len);
	} else {
	    System.arraycopy(vals, off, array, off_array, rest);
	    len -= rest;
	    off += rest;

	    while (len > 0) {
		idx_ele++;
		array = elementAt(idx_ele, true);

		int min = Math.min(len, ELE_SIZE);
		System.arraycopy(vals, off, array, 0, min);

		len -= min;
		off += min;
	    }
	}
    }

    public byte get(int index) {
	if (index >= length()) {
	    throw new IndexOutOfBoundsException(
		    "get " + index + " from size = " + length());
	}

	return list.get(index / ELE_SIZE)[index % ELE_SIZE];
    }

    public int get(int src_off, byte[] des, int des_off, int len) {
	// if (src_off + len > size()) {
	// throw new IndexOutOfBoundsException("get " + (src_off + len)
	// + " from size = " + size());
	// }

	final int l = len;
	if (src_off >= length()) {
	    return 0;
	}

	int idx_ele = src_off / ELE_SIZE;
	int off_array = src_off % ELE_SIZE;

	int rest_array = ELE_SIZE - off_array;
	byte[] array = list.get(idx_ele);
	if (rest_array >= len) {
	    System.arraycopy(array, off_array, des, des_off, len);
	} else {
	    System.arraycopy(array, off_array, des, des_off, rest_array);
	    des_off += rest_array;
	    len -= rest_array;

	    while (len > 0) {
		idx_ele++;
		array = list.get(idx_ele);
		int min = Math.min(len, ELE_SIZE);
		System.arraycopy(array, 0, des, des_off, min);

		len -= min;
		des_off += min;
	    }
	}

	return Math.min(length() - src_off, l);
    }

    public void clear() {
	for (byte[] b : list) {
	    ArrayPool.free(b);
	}

	list.clear();
	off_end = 0;
    }

    private void trimTo(int size) {

	off_end = size % ELE_SIZE;

	int idx_ele_max = size / ELE_SIZE;
	int num_ele = list.size();

	for (int i = idx_ele_max + 1; i < num_ele; i++) {
	    ArrayPool.free(list.remove(list.size() - 1));
	}
    }

    public void truncate(int size) {
	int cur_size = length();
	if (size == 0) {
	    this.clear();
	} else if (size > cur_size) {
	    // Expand
	    this.set(size, (byte) 0);
	} else if (size < cur_size) {
	    // compact
	    trimTo(size);
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(this.getClass().getSimpleName());
	sb.append('{');
	if (list.size() > 1) {
	    sb.append(list.size() - 1);
	    sb.append("*Byte[");
	    sb.append(ELE_SIZE);
	    sb.append(']');

	}
	if (off_end > 0) {
	    if (list.size() > 1) {
		sb.append('+');
	    }
	    sb.append("Byte[");
	    sb.append(off_end);
	    sb.append(']');
	}
	sb.append('}');
	return sb.toString();
    }

    public static void main(String[] args) {
	LinkedByteArray array = new LinkedByteArray();
	int count = 0;
	for (int i = 1; i <= 20; i++) {
	    byte[] a = ArrayPool.alloc_byte(i);

	    for (int j = 0; j < i; j++) {
		a[j] = (byte) (j + count);
	    }

	    System.out.println(count + " : " + array.length());
	    array.set(count, a);
	    count += i;
	}

	for (int i = 0; i < array.length(); i++) {
	    System.out.printf("%4d ", array.get(i));
	    if ((i + 1) % 15 == 0 && i != 0) {
		System.out.println();
	    }
	}

	System.out.println();
	System.out.println();

	byte[] array20 = ArrayPool.alloc_byte(20);
	array.get(2, array20, 1, 10);

	for (int i = 0; i < array20.length; i++) {
	    System.out.printf("%2d ", array20[i]);
	}
	System.out.println();
	System.out.println(array);
    }

    @Override
    public boolean disposed() {
	return disposed;
    }

    @Override
    public void dispose() throws Exception {
	if (!disposed) {
	    disposed = true;
	    s_pool.free(this);
	}
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
	try (NOutputStream nos = NOutputStream.wrap(out)) {
	    Serializers.writeObject(nos, this);
	}
    }

    @Override
    public void readExternal(ObjectInput in)
	    throws IOException, ClassNotFoundException {
	// TODO Auto-generated method stub

    }

    @Override
    public void _init() {
	disposed = false;
    }

    @Override
    public void _cleanup() {
	// TODO Auto-generated method stub

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
