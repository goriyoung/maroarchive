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

import com.marolabs.io.mar.file.EntryPath;

public class EntryPathAttribute implements EntryAttribute<EntryPath> {
	private EntryPath val = new EntryPath();

	public EntryPathAttribute() {
	}

	public EntryPathAttribute(EntryPath path) {
		if (null == path) {
			throw new NullPointerException();
		}

		this.val = path;
	}

	@Override
	public String name() {
		return NAME_PATH;
	}

	@Override
	public EntryPath value() {
		return val;
	}

	@Override
	public Class<EntryPath> valueType() {
		return EntryPath.class;
	}

	@Override
	public void writeTo(DataOutput out) throws IOException {
		val.writeTo(out);
	}

	@Override
	public void readObject(DataInput in) throws IOException {
		val = new EntryPath();
		val.readObject(in);
	}

	@Override
	public String toString() {
		return name() + "=" + val;
	}

	@Override
	public void setValue(EntryPath val) {
		if (null != val) {
			this.val = val;
		}
	}
}
