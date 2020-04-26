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
import java.nio.file.attribute.FileTime;

public class LastAccessTimeAttribute implements EntryAttribute<FileTime> {
	private FileTime time = FileTime.fromMillis(0);

	public LastAccessTimeAttribute() {
	}

	public LastAccessTimeAttribute(FileTime time) {
		if (null == time) {
			throw new NullPointerException();
		}

		this.time = time;
	}

	@Override
	public String name() {
		return NAME_LAST_ACCESS_TIME;
	}

	@Override
	public FileTime value() {
		return time;
	}

	@Override
	public Class<FileTime> valueType() {
		return FileTime.class;
	}

	@Override
	public void writeTo(DataOutput out) throws IOException {
		out.writeLong(time.toMillis());
	}

	@Override
	public void readObject(DataInput in) throws IOException {
		long millis = in.readLong();
		time = FileTime.fromMillis(millis);
	}

	@Override
	public String toString() {
		return name() + "=" + time;
	}

	@Override
	public void setValue(FileTime newTime) {
		if (null != newTime) {
			this.time = newTime;
		}
	}
}
