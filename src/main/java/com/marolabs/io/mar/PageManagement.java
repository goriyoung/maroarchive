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

import com.marolabs.util.Bitz;
import com.marolabs.util.pool.ByteBufferPool;

class PageManagement implements MaroArchiveConstants {
	/**
	 * 
	 */
	private final MaroArchive mar;
	private long[] offset_chunk_page = new long[MAX_CHUNK_PAGE];
	private long[] offset_block_page = new long[MAX_BLOCK_PAGE];

	public PageManagement(MaroArchive maroArchive) {
		mar = maroArchive;
		int maxBufferSize = Math.max(MAX_CHUNK_PAGE, MAX_BLOCK_PAGE);
		ByteBuffer buffer = ByteBufferPool.alloc(maxBufferSize * SIZ_PAGE_OFFSET);
		buffer.order(BYTE_ORDER);
		try {
			// load the chunk pages offset
			buffer.limit(SIZ_IDX_CHK_PAG);
			buffer.rewind();
			mar.data_source.position(OFF_IDX_CHK_PAG);
			mar.data_source.read(buffer);
			buffer.flip();

			Bitz.getInts(buffer, offset_chunk_page, 0, offset_chunk_page.length, SIZ_PAGE_OFFSET);
			// buffer.asLongBuffer().get(offset_chunk_page);

			// load the block pages offset
			buffer.limit(SIZ_IDX_BLK_PAG);
			buffer.rewind();
			mar.data_source.position(OFF_IDX_BLK_PAG);
			mar.data_source.read(buffer);
			buffer.flip();
			// buffer.asLongBuffer().get(offset_block_page);
			Bitz.getInts(buffer, offset_block_page, 0, offset_block_page.length, SIZ_PAGE_OFFSET);
		} catch (IOException e) {
			throw new RuntimeException("Load index map " + "Failed.", e);
		} finally {
			ByteBufferPool.free(buffer);
		}
	}

	public void create_chunk_page(int pageID) {
		ByteBuffer buffer = ByteBufferPool.alloc(8, true);
		buffer.order(BYTE_ORDER);
		try {
			// init the page tables
			long offset = mar.space_manager.alloc(MAX_CHUNK_IN_PAGE * SIZ_CHK);
			// update index int da cache and the source
			offset_chunk_page[pageID] = offset;

			buffer.putLong(offset);
			buffer.flip();

			// wirite the new page offset to the page index
			mar.data_source.position(pageID * SIZ_PAGE_OFFSET + OFF_IDX_CHK_PAG);
			mar.data_source.write(buffer);

			MaroArchive.log("CPage", "Create", "id=" + pageID + "offset@" + offset);

		} catch (Exception e) {
			throw new RuntimeException("create new chunk page" + " failed.", e);
		} finally {
			ByteBufferPool.free(buffer);
		}
	}

	public void create_block_page(int pageID) {
		ByteBuffer buffer = ByteBufferPool.alloc(8, true);
		buffer.order(BYTE_ORDER);
		try {
			// init the page tables
			long offset = mar.space_manager.alloc(MAX_BLOCK_IN_PAGE * SIZ_BLK);
			// update index int da cache and the source
			offset_block_page[pageID] = offset;

			buffer.putLong(offset);
			buffer.flip();
			mar.data_source.position(OFF_IDX_BLK_PAG + pageID * SIZ_PAGE_OFFSET);
			mar.data_source.write(buffer);

			MaroArchive.log("BPage", "Create", "id=" + pageID + "offset@" + offset);
		} catch (Exception e) {
			throw new RuntimeException("create new block page off@" + " failed.", e);
		} finally {
			ByteBufferPool.free(buffer);
		}
	}

	public long chunk_offset(int chunkID) {
		int pageID = chunkID / MAX_CHUNK_IN_PAGE;
		int offsetInPage = chunkID % MAX_CHUNK_IN_PAGE;

		return offset_chunk_page[pageID] + offsetInPage * SIZ_CHK;
	}

	public long block_offset(int blockID) {
		int pageID = blockID / MAX_BLOCK_IN_PAGE;
		int offsetInPage = blockID % MAX_BLOCK_IN_PAGE;

		return offset_block_page[pageID] + offsetInPage * SIZ_BLK;
	}

	public boolean isChunkPageValid(int pageID) {
		return offset_chunk_page[pageID] != 0;
	}

	public boolean isBlockPageValid(int pageID) {
		return offset_block_page[pageID] != 0;
	}

}