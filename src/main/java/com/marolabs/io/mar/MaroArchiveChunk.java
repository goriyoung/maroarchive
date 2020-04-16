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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.marolabs.crypto.CryptoUtils;
import com.marolabs.io.source.DataSource;
import com.marolabs.util.Bitz;
import com.marolabs.util.pool.ByteBufferPool;

public abstract class MaroArchiveChunk implements MaroArchiveConstants, Comparable<MaroArchiveChunk> {
	public MaroArchiveChunk(int id) {
		this.id = id;
	}

	public final int id() {
		return id;
	}

	protected int id = -1;

	protected boolean valid;
	protected boolean compressed;
	protected boolean encrypted;
	protected boolean isPrivate;

	protected byte[] pwd_md5;
	protected long data_len_out = -1;
	protected long data_len = -1;

	long offset_attrs = -1;
	int length_attrs = 0;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("id = " + id);
		sb.append(", valid = " + valid);
		sb.append(", compressed = " + compressed);
		sb.append(", encrypted = " + encrypted);
		sb.append(", isPrivate = " + isPrivate);
		sb.append(", offset_attrs = " + offset_attrs);
		sb.append(", length_attrs = " + length_attrs);
		sb.append(", data_len = " + data_len);
		sb.append(", data_len_out = " + data_len_out);

		return sb.toString();
	}

	@Override
	public int compareTo(MaroArchiveChunk o) {
		return Integer.compare(id, o.id);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof MaroArchiveChunk)) {
			return false;
		}

		MaroArchiveChunk that = (MaroArchiveChunk) o;
		return id == that.id;
	}

	public abstract void writeTo(long offset, DataSource source) throws IOException;

	public abstract void loadFrom(long offset, DataSource source) throws IOException;

	public abstract MarEntryType type();

	public boolean isValid() {
		return valid;
	}

	public boolean isCompressed() {
		return compressed;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public boolean verify(byte[] passKey) {
		return Arrays.equals(pwd_md5, CryptoUtils.passKeyVerifer(passKey));
	}

	public long length(boolean out) {
		if (out) {
			return data_len_out;
		} else {
			return data_len;
		}
	}

	public long getAttrOffset() {
		return this.offset_attrs;
	}

	public void SetAttrOffset(long offset) {
		this.offset_attrs = offset;
	}

	public int getAttrLength() {
		return this.length_attrs;
	}

	public void SetAttrOffset(int length) {
		this.length_attrs = length;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public void setVerifer(byte[] verifer) {
		this.pwd_md5 = verifer;
	}

	public void setCompressed(boolean isCompressed) {
		this.compressed = isCompressed;
	}

	public void setEncrypted(boolean isEncrypted) {
		this.encrypted = isEncrypted;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public void setLength(boolean out, long length) {
		if (out) {
			this.data_len_out = length;
		} else {
			this.data_len = length;
		}
	}

	public static MaroArchiveChunk load(MaroArchive mar, int chunkID) throws IOException {
		ByteBuffer buffer = ByteBufferPool.alloc(SIZ_CHK);
		buffer.order(BYTE_ORDER);
		try {
			long offset = mar.page_manage.chunk_offset(chunkID);
			DataSource source = mar.data_source;

			source.position(offset);
			source.read(buffer);
			buffer.flip();

			int chunk_type_ord = Bitz.getInt32(buffer, SIZ_CHK_TYPE);

			if (chunk_type_ord == MarEntryType.INDEXED_BLOCK.ordinal()) {
				BlockedChunk chunk = new BlockedChunk(chunkID);

				chunk.loadFrom(offset, source);
				return chunk;
			} else {
				return null;
			}

		} catch (IOException e) {
			throw new RuntimeException("Load Chunk id@ " + chunkID + "Failed.", e);
		} finally {
			ByteBufferPool.free(buffer);
		}

	}
}
