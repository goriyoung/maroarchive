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

class BlockedChunk extends IndexedChunk {
	public BlockedChunk(int id) {
		super(id);
	}

	long offset_block_ids = -1;
	int block_num = -1;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(", offset_block_ids = " + offset_block_ids);
		sb.append(", block_num = " + block_num);

		return super.toString() + sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BlockedChunk)) {
			return false;
		}

		BlockedChunk that = (BlockedChunk) o;
		return id == that.id;
	}

	@Override
	public synchronized void writeTo(long offset, DataSource source) throws IOException {

		ByteBuffer chunkBuffer = ByteBufferPool.alloc(SIZ_CHK);

		try {
			chunkBuffer.order(BYTE_ORDER);
			MarEntryType type = MarEntryType.INDEXED_BLOCK;
			Bitz.putInt(chunkBuffer, type.ordinal(), SIZ_CHK_TYPE);

			byte state = Bitz.bool8Bit(valid, compressed, encrypted, isPrivate);
			Bitz.putInt(chunkBuffer, state, SIZ_CHK_STATE);

			if (encrypted) {
				chunkBuffer.put(pwd_md5);
			} else {
				chunkBuffer.position(chunkBuffer.position() + SIZ_CHK_PWD_VFY);
			}

			Bitz.putInt(chunkBuffer, offset_block_ids, SIZ_CHK_OFF_BLK_IDS);
			Bitz.putInt(chunkBuffer, block_num, SIZ_CHK_BLK_NUM);
			Bitz.putInt(chunkBuffer, data_len_out, SIZ_CHK_DAT_SIZ_OUT);
			Bitz.putInt(chunkBuffer, data_len, SIZ_CHK_DAT_SIZ);
			Bitz.putInt(chunkBuffer, offset_attrs, SIZ_CHK_OFF_ATT);
			Bitz.putInt(chunkBuffer, length_attrs, SIZ_CHK_LEN_ATT);

			chunkBuffer.flip();
			source.position(offset);
			source.write(chunkBuffer);

		} finally {
			ByteBufferPool.free(chunkBuffer);
		}
	}

	@Override
	public MarEntryType type() {
		return MarEntryType.INDEXED_BLOCK;
	}

	@Override
	public void loadFrom(long offset, DataSource source) throws IOException {
		ByteBuffer buffer = ByteBufferPool.alloc(SIZ_CHK);
		buffer.order(BYTE_ORDER);
		try {
			source.position(offset);
			source.read(buffer);
			buffer.flip();

			int chunk_type = Bitz.getInt32(buffer, SIZ_CHK_TYPE);
			if (chunk_type != type().ordinal()) {
				throw new RuntimeException("Wrong type of chunk");
			}

			byte state = (byte) Bitz.getInt32(buffer, SIZ_CHK_STATE);

			setValid(Bitz.bitBoolAt(state, IDX_CHK_STATE_VALID));
			setCompressed(Bitz.bitBoolAt(state, IDX_CHK_STATE_COMPRESS));
			setEncrypted(Bitz.bitBoolAt(state, IDX_CHK_STATE_ENCRYPT));

			setPrivate(Bitz.bitBoolAt(state, IDX_CHK_STATE_PRIVATE));

			if (isEncrypted()) {
				pwd_md5 = new byte[SIZ_CHK_PWD_VFY];
				buffer.get(pwd_md5);
			} else {
				// if don't have password skip this data
				buffer.position(buffer.position() + SIZ_CHK_PWD_VFY);
			}

			offset_block_ids = Bitz.getInt64(buffer, SIZ_CHK_OFF_BLK_IDS);
			block_num = Bitz.getInt32(buffer, SIZ_CHK_BLK_NUM);
			setLength(true, Bitz.getInt64(buffer, SIZ_CHK_DAT_SIZ_OUT));
			setLength(false, Bitz.getInt64(buffer, SIZ_CHK_DAT_SIZ));

			offset_attrs = Bitz.getInt64(buffer, SIZ_CHK_OFF_ATT);
			length_attrs = Bitz.getInt32(buffer, SIZ_CHK_LEN_ATT);

		} catch (IOException e) {
			throw new RuntimeException("Load Chunk id@ " + id + "Failed.", e);
		} finally {
			ByteBufferPool.free(buffer);
		}

	}
}