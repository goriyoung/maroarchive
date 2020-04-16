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
package com.marolabs.io.mar;

public class MarEntryConfig {
	private MarEntryType entryType = MarEntryType.INDEXED_BLOCK;
	private byte[] passKey;
	private boolean compress;
	private boolean isPrivate;

	public final boolean isPrivate() {
		return isPrivate;
	}

	public final void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public final MarEntryType getEntryType() {
		return entryType;
	}

	public final void setEntryType(MarEntryType entryType) {
		this.entryType = entryType;
	}

	public final byte[] getPassKey() {
		return passKey;
	}

	public final void setPassKey(byte[] passKey) {
		this.passKey = passKey;
	}

	public final boolean isCompress() {
		return compress;
	}

	public final void setCompress(boolean compress) {
		this.compress = compress;
	}

}