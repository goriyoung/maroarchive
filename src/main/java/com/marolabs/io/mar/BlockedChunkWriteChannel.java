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
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import com.marolabs.crypto.CryptoUtils;
import com.marolabs.util.Bitz;
import com.marolabs.util.pool.ByteBufferPool;

class BlockedChunkWriteChannel implements WritableByteChannel, MaroArchiveConstants {
	/**
	 * 
	 */
	private final MaroArchive archive;
	// Util objects
	private boolean compress;
	// private final String name;
	private byte[] pwd_bytes;
	private BlockedChunk chunk = null;
	private ByteBuffer cur_block_data;
	private long data_write_num;
	private List<Integer> block_id_list = new ArrayList<>();

	private boolean closed;

	public BlockedChunkWriteChannel(MaroArchive maroArchive, boolean compress, byte[] passkey) {
		this(maroArchive, -1, compress, passkey);
	}

	public BlockedChunkWriteChannel(MaroArchive maroArchive, int chunkID, boolean compress, byte[] passkey) {
		archive = maroArchive;
		this.compress = compress;

		if (passkey == null) {
			pwd_bytes = new byte[0];
		} else {
			pwd_bytes = passkey;
		}

		// check the chunk id
		if (chunkID < 0) {
			// find new spare chunk or create new one
			chunkID = archive.findSpareChunk();
		} else {
			// check if the chunkID is exist
			if (!archive.exists(chunkID)) {
				archive.addSpareChunk(chunkID - archive.getChunkCount() + 1);
			}
			// don't remove the chunk for new mode
			// else if (archive.isValid(chunkID)) {
			// // check if the chunk is spare ,if not remove the chunk
			// archive.removeChunk(chunkID);
			// }
		}

		if (chunkID >= 0) {
			chunk = archive.data_access.getChunk(chunkID);
		} else {
			chunk = new BlockedChunk(archive.getChunkCount());
		}
	}

	@Override
	public synchronized boolean isOpen() {
		return !closed;
	}

	@Override
	public synchronized void close() {
		if (!closed) {
			closed = true;
			// write the rest data to the page
			writeEnd();
			ByteBufferPool.free(cur_block_data);
		}
	}

	@Override
	protected synchronized void finalize() {
		try {
			super.finalize();
		} catch (Throwable e) {
		} finally {
			close();
		}
	}

	@Override
	public synchronized int write(ByteBuffer src) throws IOException {
		if (!this.isOpen()) {
			return -1;
		}
		if (null == cur_block_data) {
			cur_block_data = ByteBufferPool.alloc(archive.maxBlockSize, src.isDirect());
		}
		int writeCount = 0;

		while (src.hasRemaining()) {
			if (cur_block_data.remaining() >= src.remaining()) {
				writeCount += src.remaining();
				cur_block_data.put(src);
				break;
			} else {
				writeCount += cur_block_data.remaining();
				ByteBuffer part_src = (ByteBuffer) src.slice().limit(cur_block_data.remaining());
				cur_block_data.put(part_src);
				src.position(src.position() + part_src.position());

				block_id_list.add(writeBlockData());
			}
		}

		data_write_num += writeCount;
		return writeCount;
	}

	private synchronized void writeEnd() {

		if (cur_block_data.position() > 0) {
			block_id_list.add(writeBlockData());
		}

		// write chunk entry and chunk-block map datas(the block and block
		// entry have been writen)

		chunk.offset_block_ids = archive.space_manager.alloc(block_id_list.size() * 4);

		if (null != pwd_bytes && pwd_bytes.length > 0) {
			chunk.setEncrypted(true);
			chunk.pwd_md5 = CryptoUtils.passKeyVerifer(pwd_bytes);
		}

		chunk.block_num = block_id_list.size();

		chunk.setCompressed(this.compress);
		chunk.setValid(true);
		chunk.setLength(true, data_write_num);
		// chunk.name_len = (short) StringUtils.getBytes(name).length;
		long data_len_internal = 0;
		// compute the chunk size in the db
		for (int blockID : block_id_list) {
			int block_size = archive.data_access.getBlock(blockID).length;
			data_len_internal += block_size;
		}

		chunk.setLength(false, data_len_internal);

		ByteBuffer byte_block_ids = ByteBufferPool.alloc(chunk.block_num * SIZ_BLK_ID);
		byte_block_ids.order(BYTE_ORDER);
		try {
			// write the block list to the chunk_source
			for (int blockID : block_id_list) {
				Bitz.putInt(byte_block_ids, blockID, SIZ_BLK_ID);
			}
			byte_block_ids.flip();
			archive.data_source.position(chunk.offset_block_ids);
			archive.data_source.write(byte_block_ids);

			archive.data_access.addChunk(chunk);
		} catch (IOException e) {
			throw new RuntimeException("write chunk entry to source " + archive.data_source + "faild.");
		} finally {
			ByteBufferPool.free(byte_block_ids);
		}

	}

	// write the data store in cur_block_data in to the DataSource
	private synchronized int writeBlockData() {

		ByteBuffer tmp_processed = ByteBufferPool.alloc(archive.maxBlockSize + 1024, cur_block_data.isDirect());
		try {
			cur_block_data.flip();

			// test if the compress is working, otherwise don't compress
			if (compress) {
				int compressed_len = CryptoUtils.compress(cur_block_data.slice(), tmp_processed);
				tmp_processed.position(compressed_len);
				tmp_processed.flip();
			} else {
				tmp_processed.put(cur_block_data);
				cur_block_data.flip();
				tmp_processed.flip();
			}

			if (null != pwd_bytes && pwd_bytes.length > 0) {
				// encrypt the data
				ByteBuffer tmp_encrypt = ByteBufferPool.alloc(tmp_processed.remaining() + CryptoUtils.extend(), cur_block_data.isDirect());
				int encrypt_len = CryptoUtils.encrypt(pwd_bytes, tmp_processed, tmp_encrypt);
				tmp_encrypt.position(encrypt_len);
				tmp_encrypt.flip();

				// swap
				ByteBufferPool.free(tmp_processed);
				tmp_processed = tmp_encrypt;
			}

			Block block = null;
			byte[] md5 = null;
			if (archive.config.getDedupe()) {
				// structure optimize ,make a dedupe of block here
				md5 = CryptoUtils.md5(tmp_processed.slice(), SIZ_BLK_MD5 * 8);

				block = archive.data_access.findBlock(md5, tmp_processed.remaining());
			}

			if (null != block) {
				block.ref_count++;
				MaroArchive.log("Block", "Link", " id=" + block.id() + " " + block);
			} else {
				// find a reused entry
				int blockID = archive.findSpareBlock();

				if (blockID < 0) {
					// append the block to the last
					block = new Block(archive.getBlockCount());
				} else {
					block = archive.data_access.getBlock(blockID);
				}

				if (archive.config.getDedupe()) {
					block.md5 = md5;
				}
				block.ref_count = 1;
				block.crc8 = CryptoUtils.crc8(cur_block_data.slice());
				block.length = tmp_processed.remaining();

				// don't find an usable freed chunk
				if (block.offset < 0) {
					// the end of the source
					block.offset = archive.space_manager.alloc(block.length);

					MaroArchive.log("Block", "Add", " id=" + block.id() + " off@" + block.offset);
				} else {
					MaroArchive.log("Block", "Add", "with reuse id=" + block.id());
				}

				// update the block data
				archive.data_source.position(block.offset);
				archive.data_source.write(tmp_processed);
			}

			archive.data_access.addBlock(block);

			return block.id();
		} catch (IOException ex) {
			throw new RuntimeException("add block failed.", ex);
		} finally {
			ByteBufferPool.free(tmp_processed);
		}

	}

	// this method called only when the channel has been closed
	public synchronized int getChunkID() {
		if (closed) {
			return chunk.id();
		} else {
			throw new RuntimeException("this method called only when the channel has been closed");
		}
	}
}