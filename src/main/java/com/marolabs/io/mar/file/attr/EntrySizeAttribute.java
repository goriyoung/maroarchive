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
package com.marolabs.io.mar.file.attr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EntrySizeAttribute implements EntryAttribute<Long> {
	private Long size;

	public EntrySizeAttribute() {
	}

	public EntrySizeAttribute(long size) {
		this.size = size;
	}

	@Override
	public String name() {
		return NAME_SIZE;
	}

	@Override
	public Long value() {
		return size;
	}

	@Override
	public Class<Long> valueType() {
		return Long.class;
	}

	@Override
	public void writeTo(DataOutput out) throws IOException {
		out.writeLong(size);
	}

	@Override
	public void readObject(DataInput in) throws IOException {
		size = in.readLong();

	}

	@Override
	public String toString() {
		return name() + "=" + size;
	}

	@Override
	public void setValue(Long val) {
		if (null != val) {
			this.size = val;
		}
	}
}
