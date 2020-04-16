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

import com.marolabs.io.source.DataSource;
import com.marolabs.util.Bitz;
import com.marolabs.util.pool.ByteBufferPool;

class Block implements Comparable<Block>, MaroArchiveConstants {
	public Block(int id) {
		this.id = id;
		pageID = id / MAX_BLOCK_IN_PAGE;
	}

	private int pageID;
	private int id = -1;
	int ref_count = 0;
	byte[] md5;
	long offset = -1;
	int length = -1;
	int crc8 = -1;

	public int id() {
		return id;
	}

	public int pageID() {
		return pageID;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[id = " + id);
		sb.append(", ref_count = " + ref_count);
		sb.append(", offset_block_ids = " + offset);
		sb.append(", length = " + length + "]");

		return sb.toString();
	}

	@Override
	public int compareTo(Block o) {
		return Integer.compare(id, o.id);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Block)) {
			return false;
		}

		Block that = (Block) o;
		return id == that.id;
	}

	public synchronized void writeToSource(long off, DataSource source) throws IOException {

		ByteBuffer blockBuffer = ByteBufferPool.alloc(SIZ_BLK);
		try {
			blockBuffer.order(BYTE_ORDER);

			Bitz.putInt(blockBuffer, ref_count, SIZ_BLK_REF_NUM);

			// TODO dedupe
			if (false) {
				blockBuffer.put(md5);
			} else {
				blockBuffer.position(blockBuffer.position() + SIZ_BLK_MD5);
			}
			Bitz.putInt(blockBuffer, offset, SIZ_BLK_OFF);
			Bitz.putInt(blockBuffer, length, SIZ_BLK_LEN);
			Bitz.putInt(blockBuffer, crc8, SIZ_BLK_CRC);

			blockBuffer.flip();
			source.position(off);
			source.write(blockBuffer);
		} finally {
			ByteBufferPool.free(blockBuffer);
		}
	}

}