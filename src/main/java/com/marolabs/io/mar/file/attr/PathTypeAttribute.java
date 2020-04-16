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

import com.marolabs.io.PathType;

public class PathTypeAttribute implements EntryAttribute<PathType> {
	private PathType pathType = PathType.Undefine;

	public PathTypeAttribute() {
	}

	public PathTypeAttribute(PathType pathType) {
		if (null == pathType) {
			throw new NullPointerException();
		}

		this.pathType = pathType;
	}

	@Override
	public String name() {
		return NAME_Path_Type;
	}

	@Override
	public PathType value() {
		return pathType;
	}

	@Override
	public Class<PathType> valueType() {
		return PathType.class;
	}

	@Override
	public void writeTo(DataOutput out) throws IOException {
		out.writeInt(pathType.ordinal());
	}

	@Override
	public void readObject(DataInput in) throws IOException {
		int path_type_ordinal = in.readInt();

		if (PathType.values().length <= path_type_ordinal) {
			throw new EnumConstantNotPresentException(PathType.class, "" + path_type_ordinal);
		} else {
			pathType = PathType.values()[path_type_ordinal];
		}
	}

	@Override
	public String toString() {
		return name() + "=" + pathType;
	}

	@Override
	public void setValue(PathType pathType) {
		if (null != pathType) {
			this.pathType = pathType;
		}
	}
}
