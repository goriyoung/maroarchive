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
import java.nio.file.attribute.FileAttribute;

public interface EntryAttribute<T> extends FileAttribute<T> {
	public static final String NAME_CREATION_TIME = "creation_time";
	public static final String NAME_LAST_ACCESS_TIME = "last_access_time";
	public static final String NAME_LAST_MODIFY_TIME = "last_modify_time";
	public static final String NAME_SIZE = "size";
	public static final String NAME_Path_Type = "path_type";
	public static final String NAME_PATH = "path";

	public Class<T> valueType();

	public void writeTo(DataOutput out) throws IOException;

	public void readObject(DataInput in) throws IOException;

	public void setValue(T val);
}
