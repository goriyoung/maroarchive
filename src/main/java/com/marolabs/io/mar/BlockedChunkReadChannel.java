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
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.marolabs.crypto.CryptoUtils;
import com.marolabs.util.pool.ByteBufferPool;

// implment native compress and crypto to speedup
class BlockedChunkReadChannel implements ReadableByteChannel, MaroArchiveConstants {
	/**
	 * 
	 */
	private final MaroArchive archive;
	private final BlockedChunk chunk;
	private byte[] pwd_bytes;
	private final int[] blockIDs;
	private int cur_block_idx = -1;
	private ByteBuffer cur_block_data;
	private boolean closed;

	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	public BlockedChunkReadChannel(MaroArchive maroArchive, int chunkID, byte[] passkey) {
		archive = maroArchive;
		this.chunk = archive.data_access.getChunk(chunkID);

		if (passkey == null) {
			pwd_bytes = new byte[0];
		} else {
			pwd_bytes = passkey;
		}

		if (chunk.isEncrypted()) {
			// check the pwd is the pair of this chunk

			if (!chunk.verify(pwd_bytes)) {
				throw new RuntimeException("Wrong password.");
			}
		}

		blockIDs = archive.get_chunk_blocks(chunkID);
	}

	@Override
	public boolean isOpen() {
		return !closed;
	}

	@Override
	public void close() {
		if (!closed) {
			closed = true;
			if (null != cur_block_data) {
				ByteBufferPool.free(cur_block_data);
			}
		}
	}

	@Override
	protected void finalize() {
		try {
			super.finalize();
		} catch (Throwable e) {
		} finally {
			close();
		}
	}

	private void loadNextBlockData() throws IOException {
		if (!hasNextBlock()) {
			return;
		}
		rwl.writeLock().lock();
		try {
			int blockID = blockIDs[++cur_block_idx];
			Block blockEntry = archive.data_access.getBlock(blockID);

			ByteBuffer processed_data = ByteBufferPool.alloc(blockEntry.length, cur_block_data.isDirect());
			archive.data_source.position(blockEntry.offset);
			archive.data_source.read(processed_data);
			processed_data.flip();

			// step 1 decrypt
			if (chunk.isEncrypted()) {
				// for the encrypt algrithm may be have some extend bits
				ByteBuffer tmp_decrypt = ByteBufferPool.alloc(blockEntry.length + CryptoUtils.extend(), cur_block_data.isDirect());
				int decrypt_len = CryptoUtils.decrypt(pwd_bytes, processed_data, tmp_decrypt);
				tmp_decrypt.position(decrypt_len);
				tmp_decrypt.flip();

				// release the block which stored the native block data
				ByteBufferPool.free(processed_data);

				// swap the native block to the decrypt block
				processed_data = tmp_decrypt;
			}

			cur_block_data.position(archive.maxBlockSize);
			cur_block_data.flip();
			// step 2 decompress
			if (chunk.isCompressed()) {
				CryptoUtils.decompress(processed_data, cur_block_data);
			} else {
				cur_block_data.put(processed_data);
			}

			cur_block_data.flip();
			// step 1 verify the block
			int crc8 = CryptoUtils.crc8(cur_block_data) & 0xff;
			if (blockEntry.crc8 != crc8) {
				throw new RuntimeException("Block CRC verify failed @id:" + blockID + " [" + crc8 + "!=" + blockEntry.crc8 + "]");
			}

			ByteBufferPool.free(processed_data);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	private boolean hasNextBlock() {
		rwl.readLock().lock();
		try {
			return blockIDs.length > cur_block_idx + 1;
		} finally {
			rwl.readLock().unlock();
		}
	}

	@Override
	public synchronized int read(ByteBuffer dst) throws IOException {
		if (!this.isOpen()) {
			return -1;
		}

		if (null == cur_block_data) {
			cur_block_data = ByteBufferPool.alloc(archive.maxBlockSize, dst.isDirect());
		}
		// load the first block
		if (cur_block_idx < 0) {
			loadNextBlockData();
		}

		int bytes_read = 0;
		// don't make a recursion invoke use loop is better
		while (dst.hasRemaining()) {
			if (dst.remaining() <= cur_block_data.remaining()) {
				bytes_read += dst.remaining();
				// this is the shadow reference of the cur_block_data,don't
				// do any copy
				ByteBuffer part_block_data = cur_block_data.slice();
				part_block_data.limit(dst.remaining());
				dst.put(part_block_data);

				cur_block_data.position(cur_block_data.position() + part_block_data.position());

				break;
			} else if (!hasNextBlock()) {
				bytes_read += cur_block_data.remaining();
				dst.put(cur_block_data);
				break;
			} else {
				bytes_read += cur_block_data.remaining();
				dst.put(cur_block_data);
				loadNextBlockData();
			}
		}

		if (bytes_read == 0) {
			return -1;
		}
		return bytes_read;
	}

}