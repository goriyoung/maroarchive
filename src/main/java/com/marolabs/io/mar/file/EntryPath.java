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
package com.marolabs.io.mar.file;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author <a href="mailto:gori.young@marolabs.com">Gori Young</a>
 *
 */
public class EntryPath {
	private String[] names;

	public EntryPath(String... paths) {
		if (null == paths) {
			names = new String[0];
		} else {
			names = new String[paths.length];
			System.arraycopy(paths, 0, names, 0, paths.length);
		}
	}

	public EntryPath(Path path) {
		if (null == path) {
			names = new String[0];
		} else {
			names = new String[path.getNameCount()];
			for (int i = 0; i < names.length; i++) {
				names[i] = path.getName(i).toString();
			}
		}
	}

	public int getNameCount() {
		return names.length;
	}

	@Override
	public String toString() {
		if (names.length == 1)
			return names[0];

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names.length; i++) {
			sb.append(names[i]);

			if (i < names.length - 1) {
				sb.append(File.separator);
			}
		}

		return sb.toString();
	}

	public EntryPath getName(int index) {
		return new EntryPath(names[index]);
	}

	public String getStringName(int index) {
		return names[index];
	}

	public void writeTo(DataOutput out) throws IOException {
		if (out == null) {
			return;
		}
		out.writeInt(names.length);
		for (int i = 0; i < names.length; i++) {
			out.writeUTF(names[i]);
		}
	}

	public void readObject(DataInput in) throws IOException {
		if (in == null) {
			return;
		}

		int name_count = in.readInt();
		names = new String[name_count];

		for (int i = 0; i < name_count; i++) {
			names[i] = in.readUTF();
		}
	}
}
