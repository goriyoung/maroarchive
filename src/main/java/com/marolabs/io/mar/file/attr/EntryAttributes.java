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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.marolabs.io.PathType;
import com.marolabs.io.mar.file.EntryPath;

public class EntryAttributes implements BasicFileAttributes {
	Map<String, EntryAttribute<?>> attrs = new HashMap<>();

	public EntryAttributes() {
	}

	public EntryAttributes(Path path) throws IOException {
		attrs.put(EntryAttribute.NAME_PATH, new EntryPathAttribute(new EntryPath(path)));
		attrs.put(EntryAttribute.NAME_SIZE, new EntrySizeAttribute(Files.size(path)));

		BasicFileAttributes nfa = Files.readAttributes(path, BasicFileAttributes.class);

		if (null != nfa) {
			attrs.put(EntryAttribute.NAME_LAST_MODIFY_TIME, new LastModifyTimeAttribute(nfa.lastModifiedTime()));

			attrs.put(EntryAttribute.NAME_LAST_ACCESS_TIME, new LastAccessTimeAttribute(nfa.lastAccessTime()));

			attrs.put(EntryAttribute.NAME_CREATION_TIME, new CreationTimeAttribute(nfa.creationTime()));

			PathType path_type = PathType.Undefine;
			if (nfa.isDirectory()) {
				path_type = PathType.Directory;
			} else if (nfa.isRegularFile()) {
				path_type = PathType.RegularFile;
			} else if (nfa.isSymbolicLink()) {
				path_type = PathType.SymbolicLink;
			} else {
				path_type = PathType.Undefine;
			}

			attrs.put(EntryAttribute.NAME_Path_Type, new PathTypeAttribute(path_type));
		}
	}

	public EntryAttribute<?> getAttribute(String name) {
		return attrs.get(name);
	}

	public Iterator<EntryAttribute<?>> iterator() {
		return attrs.values().iterator();
	}

	public void setAttribute(EntryAttribute<?>... attributes) {
		if (null == attributes || attributes.length == 0) {
			return;
		}
		for (EntryAttribute<?> attr : attributes) {
			if (attr != null) {
				attrs.put(attr.name(), attr);
			}
		}
	}

	public int count() {
		return attrs.size();
	}

	public EntryPath path() {
		return (EntryPath) value(EntryAttribute.NAME_PATH);
	}

	@Override
	public long size() {
		return (Long) value(EntryAttribute.NAME_SIZE);
	}

	@Override
	public FileTime lastModifiedTime() {
		return (FileTime) attrs.get(EntryAttribute.NAME_LAST_MODIFY_TIME).value();
	}

	@Override
	public FileTime lastAccessTime() {
		return (FileTime) attrs.get(EntryAttribute.NAME_LAST_ACCESS_TIME).value();
	}

	@Override
	public FileTime creationTime() {
		return (FileTime) attrs.get(EntryAttribute.NAME_CREATION_TIME).value();
	}

	@Override
	public boolean isDirectory() {
		return value(EntryAttribute.NAME_Path_Type) == PathType.Directory;
	}

	@Override
	public boolean isRegularFile() {
		return value(EntryAttribute.NAME_Path_Type) == PathType.RegularFile;
	}

	@Override
	public boolean isSymbolicLink() {
		return value(EntryAttribute.NAME_Path_Type) == PathType.SymbolicLink;
	}

	@Override
	public boolean isOther() {
		return value(EntryAttribute.NAME_Path_Type) == PathType.Undefine;
	}

	private Object value(String attr_name) {
		if (!attrs.containsKey(attr_name)) {
			return null;
		}

		return attrs.get(attr_name).value();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("EntryAttributes");
		sb.append(attrs.values());

		return sb.toString();
	}

	public void readObject(DataInput in) throws IOException {
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			String name = in.readUTF();

			EntryAttribute<?> attr = null;

			switch (name) {
			case EntryAttribute.NAME_Path_Type:
				attr = new PathTypeAttribute();
			case EntryAttribute.NAME_LAST_MODIFY_TIME:
				attr = new LastModifyTimeAttribute();
			case EntryAttribute.NAME_LAST_ACCESS_TIME:
				attr = new LastAccessTimeAttribute();
			case EntryAttribute.NAME_CREATION_TIME:
				attr = new CreationTimeAttribute();
			case EntryAttribute.NAME_PATH:
				attr = new EntryPathAttribute();
			case EntryAttribute.NAME_SIZE:
				attr = new EntrySizeAttribute();
			}

			if (attr != null) {
				attr.readObject(in);
				attrs.put(name, attr);
			}
		}
	}

	public void writeTo(DataOutput out) throws IOException {
		out.writeInt(attrs.size());

		for (EntryAttribute<?> attr : attrs.values()) {
			out.writeUTF(attr.name());
			attr.writeTo(out);
		}

	}

	// TODO
	@Override
	public Object fileKey() {
		return null;
	}

}
