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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import com.marolabs.Environment;
import com.marolabs.crypto.CryptoUtils;
import com.marolabs.data.ByteArray;
import com.marolabs.data.MutableByteArray;
import com.marolabs.data.record.StoredRecordSet;
import com.marolabs.data.record.StoredSortedRecordSet;
import com.marolabs.exception.BadFormatException;
import com.marolabs.exception.IllegalVolumeException;
import com.marolabs.exception.UnsupportedFunctionException;
import com.marolabs.io.PathFilters;
import com.marolabs.io.mar.file.attr.EntryAttributes;
import com.marolabs.io.source.DataSource;
import com.marolabs.io.source.FileDataSource;
import com.marolabs.io.source.VolumeFileDataSource;
import com.marolabs.io.stream.NOutputStream;
import com.marolabs.io.stream.data.NDataOutputStream;
import com.marolabs.media.image.ImageGraper;
import com.marolabs.media.image.ImageGraper.ImageType;
import com.marolabs.util.Bitz;
import com.marolabs.util.Filter;
import com.marolabs.util.ImageUtils;
import com.marolabs.util.Utils;
import com.marolabs.util.pool.ArrayPool;
import com.marolabs.util.pool.ByteBufferPool;

//V6

public class MaroArchive implements MaroArchiveConstants, Closeable {
	public static void log(String item, String operation, String message) {
		if (DEBUG) {
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			sb.append(item);
			sb.append(']');
			sb.append('[');
			sb.append(operation);
			sb.append(']');
			sb.append(' ');
			sb.append(message);
			System.out.println(sb);
		}
	}

	private static boolean DEBUG = false;

	static {
		if (SIZ_HDR_ID != HDR_MAGIC.length) {
			System.err.println("MAGIC is not match the size " + SIZ_HDR_ID);
		}
	}

	DataSource data_source;
	BlockedDataAccess data_access;
	HeapManagement space_manager;
	PageManagement page_manage;

	// header datas
	private int version = DEFAULT_VER;

	// private int volume_count;
	private int zip_algorithm = 0;
	private int crypt_algorithm = 0;
	private long create_ms;
	private int state = STATE_OPENED;

	private static final int STATE_OPENED = 0;
	private static final int STATE_CLOSED = 1;

	private int chunk_count;
	private int block_count;

	int maxBlockSize = DEFAULT_MAX_BLK_SIZ;

	private long fragment_offset;
	private int fragment_length;

	MaroArchiveConfig config;

	private boolean readonly;

	// TODO add a chunk must pass a valid chunk entry to

	// TODO add solid mode ,this mode don't save md5 to archive just use a temp
	// to hold
	// TODO add configured & external Compressor and Encypter support
	// TODO at last must let the modify of page and user data avalible
	public MaroArchive(DataSource source) throws IOException {
		this(source, false);
	}

	public MaroArchive(DataSource source, boolean forceCreateNew) throws IOException {
		this(source, forceCreateNew, new MaroArchiveConfig());
	}

	public MaroArchive(DataSource source, boolean forceCreateNew, MaroArchiveConfig config) throws IOException {
		this.data_source = source;
		this.config = config;

		// check the magic word is ok
		byte[] new_magic = new byte[HDR_MAGIC.length];
		source.position(0);
		source.read(new_magic);
		if (source.size() <= 0 || !Arrays.equals(HDR_MAGIC, new_magic)) {
			if (forceCreateNew) {
				create_ms = System.currentTimeMillis();
				// init the chunk page index area
				createEmptyPage(OFF_IDX_CHK_PAG, SIZ_IDX_CHK_PAG);

				// init the block page index area
				createEmptyPage(OFF_IDX_BLK_PAG, SIZ_IDX_BLK_PAG);

				this.crypt_algorithm = config.getEncryptAlgrithm();
				this.zip_algorithm = config.getCompressAlgrithm();

				writeHeader();
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			loadHeader();
			if (state != STATE_CLOSED) {
				System.err.println("The file is not closed normally");
			}
		}

		state = STATE_OPENED;
		writeHeader();

		this.space_manager = new HeapManagement();
		this.page_manage = new PageManagement(this);
		this.data_access = new BlockedDataAccess();
	}

	private static byte[] fingerprint(String str) {
		return CryptoUtils.fingerprint(str);
	}

	private void createEmptyPage(long offset, int size) {
		ByteBuffer buffer = ByteBufferPool.alloc(size, true);
		buffer.order(BYTE_ORDER);
		try {
			// init the page tables
			for (int i = 0; i < size; i++) {
				buffer.put((byte) 0);
			}

			buffer.flip();
			data_source.position(offset);
			data_source.write(buffer);
		} catch (Exception e) {
			throw new RuntimeException("create new page off@" + offset + " failed.", e);
		} finally {
			ByteBufferPool.free(buffer);
		}
	}

	private void writeHeader() throws IOException {
		ByteBuffer headerBuffer = ByteBufferPool.alloc(END_HDR);
		try {
			headerBuffer.order(BYTE_ORDER);

			headerBuffer.put(HDR_MAGIC);

			Bitz.putInt(headerBuffer, BYTE_ORDER == ByteOrder.BIG_ENDIAN ? 1 : 0, SIZ_HDR_ODR);
			Bitz.putInt(headerBuffer, version, SIZ_HDR_VER);
			Bitz.putInt(headerBuffer, zip_algorithm, SIZ_HDR_ZIP);
			Bitz.putInt(headerBuffer, crypt_algorithm, SIZ_HDR_CYP);

			Bitz.putInt(headerBuffer, create_ms, SIZ_HDR_CRT_TIM);
			Bitz.putInt(headerBuffer, state, SIZ_HDR_STATE);

			Bitz.putInt(headerBuffer, maxBlockSize, SIZ_HDR_MAX_BLK_SIZ);
			Bitz.putInt(headerBuffer, chunk_count, SIZ_HDR_CHK_NUM);
			Bitz.putInt(headerBuffer, block_count, SIZ_HDR_BLK_NUM);

			Bitz.putInt(headerBuffer, fragment_offset, SIZ_FRAG_DAT_OFF);
			Bitz.putInt(headerBuffer, fragment_length, SIZ_FRAG_DAT_LEN);

			headerBuffer.flip();

			data_source.position(0);
			data_source.write(headerBuffer);
		} finally {
			ByteBufferPool.free(headerBuffer);
		}
	}

	private void loadHeader() {
		ByteBuffer headerBuffer = ByteBufferPool.alloc(END_HDR);
		try {
			headerBuffer.order(BYTE_ORDER);

			data_source.position(0);
			data_source.read(headerBuffer);
			headerBuffer.flip();

			// headerBuffer.position(OFF_HDR_ODR);
			// BYTE_ORDER = headerBuffer.get() == 1 ? ByteOrder.BIG_ENDIAN
			// : ByteOrder.LITTLE_ENDIAN;

			headerBuffer.order(BYTE_ORDER);

			version = Bitz.getInt32(headerBuffer, SIZ_HDR_VER);
			zip_algorithm = Bitz.getInt32(headerBuffer, SIZ_HDR_ZIP);
			crypt_algorithm = Bitz.getInt32(headerBuffer, SIZ_HDR_CYP);
			create_ms = Bitz.getInt64(headerBuffer, SIZ_HDR_CRT_TIM);
			state = Bitz.getInt32(headerBuffer, SIZ_HDR_STATE);

			maxBlockSize = Bitz.getInt32(headerBuffer, SIZ_HDR_MAX_BLK_SIZ);
			chunk_count = Bitz.getInt32(headerBuffer, SIZ_HDR_CHK_NUM);
			block_count = Bitz.getInt32(headerBuffer, SIZ_HDR_BLK_NUM);
			fragment_offset = Bitz.getInt64(headerBuffer, SIZ_FRAG_DAT_OFF);
			fragment_length = Bitz.getInt32(headerBuffer, SIZ_FRAG_DAT_LEN);
		} catch (IOException e) {
			throw new RuntimeException("loadHeader Failed.", e);
		} finally {
			ByteBufferPool.free(headerBuffer);
		}
	}

	public void removeChunk(int chunkID) {
		log("Chunk", "Remove", " id=" + chunkID);

		if (data_access.removeChunk(chunkID)) {

		} else {
			throw new RuntimeException("removeChunck Failed, Chunk not exist. " + chunkID);
		}

	}

	public int readChunk(int chunkID, byte[] out, int offset) {
		return readChunk(chunkID, out, offset, config.getPasskey());
	}

	public int readChunk(int chunkID, byte[] out, int offset, String password) {
		return readChunk(chunkID, out, offset, fingerprint(password));
	}

	public int readChunk(int chunkID, byte[] out, int offset, byte[] passkey) {
		InputStream is = getChunkByStream(chunkID, passkey);
		long chunkSize = this.getChunkSize(chunkID);

		try {
			return is.read(out, offset, (int) chunkSize);
		} catch (IOException e) {
			throw new RuntimeException("Read Chunk id@" + chunkID + " Failed.");
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	public InputStream getChunkByStream(int chunkID) {
		return getChunkByStream(chunkID, config.getPasskey());
	}

	public InputStream getChunkByStream(int chunkID, String password) {
		return getChunkByStream(chunkID, fingerprint(password));
	}

	public InputStream getChunkByStream(int chunkID, byte[] passkey) {
		return Channels.newInputStream(getChunkByChannel(chunkID, passkey));
	}

	public ReadableByteChannel getChunkByChannel(int chunkID) {
		return getChunkByChannel(chunkID, config.getPasskey());
	}

	public ReadableByteChannel getChunkByChannel(int chunkID, String password) {
		return getChunkByChannel(chunkID, fingerprint(password));
	}

	public ReadableByteChannel getChunkByChannel(int chunkID, byte[] passkey) {
		return new BlockedChunkReadChannel(this, chunkID, passkey);
	}

	public int addChunk(boolean compress, byte[] chunkData, int offset, int len, String password) {
		return addChunk(compress, chunkData, offset, len, fingerprint(password));
	}

	public int addChunk(boolean compress, byte[] chunkData, int offset, int len, byte[] passkey) {
		ChunkOutputStream os = this.addChunkByStream(compress, passkey);

		try {
			os.write(chunkData, offset, len);
		} catch (IOException e) {
			throw new RuntimeException("Add Chunk Failed.");
		} finally {
			try {
				os.close();
			} catch (IOException e) {
			}
		}

		return os.getChunkID();
	}

	public ChunkOutputStream addChunkByStream() {
		return addChunkByStream(config.getCompress(), config.getPasskey());
	}

	public ChunkOutputStream addChunkByStream(boolean compress, String password) {
		return addChunkByStream(compress, fingerprint(password));
	}

	public ChunkOutputStream addChunkByStream(boolean compress, byte[] passkey) {
		return new ChunkOutputStream(addChunkByChannel(compress, passkey));
	}

	public BlockedChunkWriteChannel addChunkByChannel() {
		return addChunkByChannel(config.getCompress(), config.getPasskey());
	}

	public BlockedChunkWriteChannel addChunkByChannel(boolean compress, String password) {
		return addChunkByChannel(compress, fingerprint(password));
	}

	public BlockedChunkWriteChannel addChunkByChannel(boolean compress, byte[] passkey) {
		return new BlockedChunkWriteChannel(this, compress, passkey);
	}

	public int modifyChunk(int chunkID, boolean compress, byte[] chunkData, int offset, int len, String password) {
		return modifyChunk(chunkID, compress, chunkData, offset, len, fingerprint(password));
	}

	public int modifyChunk(int chunkID, boolean compress, byte[] chunkData, int offset, int len, byte[] passkey) {
		ChunkOutputStream os = this.modifyChunkByStream(chunkID, compress, passkey);

		try {
			os.write(chunkData, offset, len);
		} catch (IOException e) {
			throw new RuntimeException("Add Chunk Failed.");
		} finally {
			try {
				os.close();
			} catch (IOException e) {
			}
		}

		return os.getChunkID();
	}

	public ChunkOutputStream modifyChunkByStream(int chunkID) {
		return modifyChunkByStream(chunkID, config.getCompress(), config.getPasskey());
	}

	public ChunkOutputStream modifyChunkByStream(int chunkID, boolean compress, String password) {
		return modifyChunkByStream(chunkID, compress, fingerprint(password));
	}

	public ChunkOutputStream modifyChunkByStream(int chunkID, boolean compress, byte[] passkey) {
		return new ChunkOutputStream(modifyChunkByChannel(chunkID, compress, passkey));
	}

	public BlockedChunkWriteChannel modifyChunkByChannel(int chunkID) {
		return modifyChunkByChannel(chunkID, config.getCompress(), config.getPasskey());
	}

	public BlockedChunkWriteChannel modifyChunkByChannel(int chunkID, boolean compress, String password) {
		return modifyChunkByChannel(chunkID, compress, fingerprint(password));
	}

	public BlockedChunkWriteChannel modifyChunkByChannel(int chunkID, boolean compress, byte[] passkey) {
		return new BlockedChunkWriteChannel(this, chunkID, compress, passkey);
	}

	public int addPlaceHold() {
		BlockedChunk chunk = new BlockedChunk(this.chunk_count);
		chunk.data_len = 0;
		chunk.data_len_out = 0;
		chunk.offset_block_ids = -1;
		chunk.setValid(true);

		try {
			this.data_access.addChunk(chunk);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return chunk.id();
	}

	public int addSpareChunk() {
		BlockedChunk chunk = new BlockedChunk(this.chunk_count);

		try {
			this.data_access.addChunk(chunk);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return chunk.id();
	}

	public void addSpareChunk(int count) {
		for (int i = 0; i < count; i++) {
			addSpareChunk();
		}
	}

	// USE for version control system
	public int duplicate(int chunkID) {
		// TODO
		// must be optimize for dedupe mode
		// just make a copy of chunk entry and reference the blocks
		throw new UnsupportedOperationException();
	}

	public long getChunkSize(int chunkID) {
		return data_access.getChunk(chunkID).data_len_out;
	}

	public int getChunkCount() {
		return chunk_count;
	}

	public int getBlockCount() {
		return block_count;
	}

	public boolean exists(int chunkID) {
		return 0 <= chunkID && chunkID < getChunkCount();
	}

	public boolean isValid(int chunkID) {
		return data_access.isValid(chunkID);
	}

	// TODO stub here
	public MarEntry findSpareEntry() {
		int chunk_id = this.findSpareChunk();

		if (chunk_id >= 0) {
			return new MarEntry(this, chunk_id);
		} else {
			BlockedChunk chunk = new BlockedChunk(getChunkCount());
			chunk.setValid(true);

			try {
				data_access.addChunk(chunk);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return new MarEntry(this, chunk.id());
		}
	}

	public MarEntry findSpareEntry(Filter<MarEntry> filter) {
		throw new UnsupportedOperationException();
		// TODO
	}

	public MarEntry createEntry() {
		throw new UnsupportedOperationException();
		// TODO
	}

	public MarEntry createEntry(MarEntryConfig entryConfig) {
		throw new UnsupportedOperationException();
		// TODO
	}

	public boolean removeEntry(MarEntry entry) {
		throw new UnsupportedOperationException();
		// TODO
	}

	public MarEntry getEntry(int entryID) {
		throw new UnsupportedOperationException();
		// TODO
	}

	public boolean isEncrypted(int chunkID) {
		return data_access.getChunk(chunkID).isEncrypted();
	}

	public boolean isCompressed(int chunkID) {
		return data_access.getChunk(chunkID).isCompressed();
	}

	public boolean verifyChunkID(int chunkID) {
		if (!exists(chunkID)) {
			throw new IndexOutOfBoundsException("chunkID id@" + chunkID + " is not in the db");
		}

		return true;
	}

	@Override
	public synchronized void close() {
		try {
			if (state != STATE_CLOSED) {
				state = STATE_CLOSED;
				this.writeHeader();
				space_manager.writeFragInfo();
				data_source.close();

			}
		} catch (IOException e) {
			throw new RuntimeException("close archive failed.", e);
		}
	}

	@Override
	public int hashCode() {
		return data_source.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MaroArchive)) {
			return false;
		}
		MaroArchive that = (MaroArchive) obj;

		return this.data_source.equals(that.data_source);
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}

	// may have multi thead issues
	// find a preferr Removed entry in mft
	int findSpareChunk() {
		// int chunkCount = this.getChunkCount();
		// for (int i = 0; i < chunkCount; i++) {
		// Chunk chunk = data_access.getChunk(i);
		//
		// if (chunk.data_len + chunk.name_len >= targetSize) {
		// return freeChunkID;
		// }
		// }

		// TODO

		return -1;
	}

	int findSpareBlock() {

		if (!config.getRemove()) {
			return -1;
		}

		// if don't use cache will cause read all of the Block
		if (!config.getCache()) {
			return -1;
		}

		// TODO

		return -1;
	}

	synchronized int[] get_chunk_blocks(int chunkID) {
		BlockedChunk chunk = data_access.getChunk(chunkID);

		return get_chunk_blocks(chunk);
	}

	private synchronized int[] get_chunk_blocks(BlockedChunk chunk) {
		if (chunk.block_num <= 0 || chunk.offset_block_ids <= 0) {
			return new int[0];
		}

		int[] blocks = new int[chunk.block_num];

		ByteBuffer byte_blocks = ByteBufferPool.alloc(chunk.block_num * SIZ_BLK_ID);
		try {
			byte_blocks.order(BYTE_ORDER);

			data_source.position(chunk.offset_block_ids);
			data_source.read(byte_blocks);
			byte_blocks.flip();

			for (int i = 0; i < blocks.length; i++) {
				blocks[i] = byte_blocks.getInt();
			}
		} catch (IOException e) {
		} finally {
			ByteBufferPool.free(byte_blocks);
		}

		return blocks;
	}

	private static class ChunkOutputStream extends OutputStream {
		private BlockedChunkWriteChannel channel;
		private ByteBuffer bb = null;
		private byte[] bs = null; // Invoker's previous array
		private byte[] b1 = null;

		public ChunkOutputStream(BlockedChunkWriteChannel channel) {
			this.channel = channel;
		}

		@Override
		public synchronized void write(int b) throws IOException {
			if (b1 == null) {
				b1 = new byte[1];
			}
			b1[0] = (byte) b;
			this.write(b1);
		}

		@Override
		public synchronized void write(byte[] bs, int off, int len) throws IOException {
			if (off < 0 || off > bs.length || len < 0 || off + len > bs.length || off + len < 0) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return;
			}
			ByteBuffer bb = this.bs == bs ? this.bb : ByteBuffer.wrap(bs);
			bb.limit(Math.min(off + len, bb.capacity()));
			bb.position(off);
			this.bb = bb;
			this.bs = bs;

			while (bb.remaining() > 0) {
				int n = channel.write(bb);
				if (n <= 0) {
					throw new RuntimeException("no bytes written");
				}
			}
		}

		@Override
		public void close() throws IOException {
			channel.close();
		}

		public int getChunkID() {
			return channel.getChunkID();
		}
	}

	// Need use weak reference to the data_access don't useup the memory when
	// system memory is lower
	class BlockedDataAccess {
		// private Index index_block_id;
		private StoredRecordSet<byte[], Integer> index_block_md5;
		private StoredRecordSet<Integer, byte[]> index_spare_block;
		private StoredRecordSet<Integer, byte[]> index_spare_chunk;

		final byte[] empty_value = new byte[0];

		public BlockedDataAccess() {
			if (!readonly) {
				init_index();
			}
		}

		private void init_index() {
			// check the index is valid
			if (check_index_valid()) {

			}

		}

		private boolean check_index_valid() {
			// TODO
			return false;
		}

		public EntryAttributes getAttrs(BlockedChunk chunk) throws IOException {
			EntryAttributes attrs = new EntryAttributes();
			long off_attrs = chunk.offset_attrs;

			data_source.position(off_attrs);
			attrs.readObject(data_source);

			return attrs;
		}

		// TODO must be optimize
		public EntryAttributes setAttrs(BlockedChunk chunk, EntryAttributes attrs) throws IOException {
			long off_attrs = chunk.offset_attrs;
			long len_attrs = chunk.length_attrs;

			if (off_attrs > 0 && len_attrs > 0) {
				space_manager.free(off_attrs, len_attrs);
			}

			MutableByteArray mba = MutableByteArray.create();

			try (NDataOutputStream dos = NOutputStream.wrap(mba).asDataOutputStream()) {
				attrs.writeTo(dos);
			}
			long new_off_attrs = space_manager.alloc(mba.length());

			if (new_off_attrs > 0) {
				// write the attr datas to the source
				data_source.position(new_off_attrs);
				ByteArray ba = mba.toByteArray();
				data_source.write(ba.array, ba.offset, ba.len);

				chunk.offset_attrs = new_off_attrs;
				chunk.length_attrs = mba.length();

				// update chunk datas
				chunk.writeTo(page_manage.chunk_offset(chunk.id()), data_source);
			}

			mba.dispose();
			return attrs;
		}

		private synchronized BlockedChunk loadChunk(int chunkID) {
			ByteBuffer chunkBuffer = ByteBufferPool.alloc(SIZ_CHK);
			chunkBuffer.order(BYTE_ORDER);
			try {
				data_source.position(page_manage.chunk_offset(chunkID));
				data_source.read(chunkBuffer);
				chunkBuffer.flip();

				BlockedChunk chunk = parseChunk(chunkBuffer, chunkID);
				// if (config.getCache()) {
				// chunk_cache.put(chunk.id,
				// new SoftReference<ChunkEntry>(chunk));
				// }
				return chunk;
			} catch (IOException e) {
				throw new RuntimeException("Load Chunk id@ " + chunkID + "Failed.", e);
			} finally {
				ByteBufferPool.free(chunkBuffer);
			}
		}

		private synchronized BlockedChunk parseChunk(ByteBuffer buffer, int chunkID) {

			int chunk_type = Bitz.getInt32(buffer, SIZ_CHK_TYPE);

			if (MarEntryType.INDEXED_BLOCK.ordinal() == chunk_type) {
				BlockedChunk chunk = new BlockedChunk(chunkID);

				byte state = (byte) Bitz.getInt32(buffer, SIZ_CHK_STATE);

				chunk.setValid(Bitz.bitBoolAt(state, IDX_CHK_STATE_VALID));
				chunk.setCompressed(Bitz.bitBoolAt(state, IDX_CHK_STATE_COMPRESS));
				chunk.setEncrypted(Bitz.bitBoolAt(state, IDX_CHK_STATE_ENCRYPT));

				chunk.setPrivate(Bitz.bitBoolAt(state, IDX_CHK_STATE_PRIVATE));

				if (chunk.isEncrypted()) {
					chunk.pwd_md5 = new byte[SIZ_CHK_PWD_VFY];
					buffer.get(chunk.pwd_md5);
				} else {
					// if don't have password skip this data
					buffer.position(buffer.position() + SIZ_CHK_PWD_VFY);
				}

				chunk.offset_block_ids = Bitz.getInt64(buffer, SIZ_CHK_OFF_BLK_IDS);
				chunk.block_num = Bitz.getInt32(buffer, SIZ_CHK_BLK_NUM);
				chunk.setLength(true, Bitz.getInt64(buffer, SIZ_CHK_DAT_SIZ_OUT));
				chunk.setLength(false, Bitz.getInt64(buffer, SIZ_CHK_DAT_SIZ));

				chunk.offset_attrs = Bitz.getInt64(buffer, SIZ_CHK_OFF_ATT);
				chunk.length_attrs = Bitz.getInt32(buffer, SIZ_CHK_LEN_ATT);

				return chunk;
			} else {
				throw new UnsupportedFunctionException("Entry Type =" + chunk_type);
			}
		}

		private synchronized Block loadBlock(int blockID) {
			ByteBuffer blockBuffer = ByteBufferPool.alloc(SIZ_BLK);
			try {
				blockBuffer.order(BYTE_ORDER);

				Block block = new Block(blockID);

				data_source.position(page_manage.block_offset(blockID));
				data_source.read(blockBuffer);
				blockBuffer.flip();

				block.ref_count = Bitz.getInt32(blockBuffer, SIZ_BLK_REF_NUM);
				if (config.getDedupe()) {
					block.md5 = new byte[SIZ_BLK_MD5];
					blockBuffer.get(block.md5);
				}
				block.offset = Bitz.getInt64(blockBuffer, SIZ_BLK_OFF);
				block.length = Bitz.getInt32(blockBuffer, SIZ_BLK_LEN);
				block.crc8 = Bitz.getInt32(blockBuffer, SIZ_BLK_CRC);
				// if (config.getCache()) {
				// block_cache.put(block.id,
				// new SoftReference<BlockEntry>(block));
				// }
				return block;
			} catch (IOException e) {
				throw new RuntimeException("Load Block id@ " + blockID + "Failed.", e);
			} finally {
				ByteBufferPool.free(blockBuffer);

			}
		}

		public synchronized BlockedChunk getChunk(int chunkID) {
			verifyChunkID(chunkID);

			BlockedChunk chunk = null;
			chunk = this.loadChunk(chunkID);

			return chunk;
		}

		public synchronized void addChunk(BlockedChunk chunk) throws IOException {
			if (!exists(chunk.id())) {
				chunk_count++;
			}

			int pageID = chunk.pageID();

			// check if the chunk can store in any chunk page
			if (!page_manage.isChunkPageValid(pageID)) {
				page_manage.create_chunk_page(pageID);
			}

			chunk.writeTo(page_manage.chunk_offset(chunk.id()), data_source);
			// if (config.getCache()) {
			// chunk_cache.put(chunk.id, new SoftReference<ChunkEntry>(chunk));
			// }

		}

		public synchronized boolean removeChunk(int chunkID) {
			if (!config.getRemove()) {
				return false;
			}

			if (verifyChunkID(chunkID)) {
				BlockedChunk chunk = getChunk(chunkID);
				// the chunk is free already
				if (!chunk.isValid()) {
					return true;
				}

				chunk.setValid(false);
				

				try {
					chunk.writeTo(page_manage.chunk_offset(chunkID), data_source);

					// need free the block_ids space
					if (chunk.offset_block_ids > 0) {
						space_manager.free(chunk.offset_block_ids, chunk.block_num * SIZ_BLK_ID);
					}

					// mark chunk entry invalid and remove related blocks
					int[] block_ids = get_chunk_blocks(chunk);
					if (null != block_ids) {
						for (int block_id : block_ids) {
							removeBlock(block_id);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException("removeChunck Failed.", e);
				}

				return true;
			} else {
				return false;
			}

		}

		public synchronized boolean isValid(int chunkID) {
			// TODO optimize with index
			return getChunk(chunkID).isValid();
		}

		private synchronized void removeBlock(int blockID) {
			Block block = getBlock(blockID);

			// make the block ref count reduce 1
			block.ref_count--;

			try {
				long off = page_manage.block_offset(blockID);
				block.writeToSource(off, data_source);

				// release the block data space
				// but the block entry space will maintain to reuse
				if (block.ref_count <= 0) {
					space_manager.free(block.offset, block.length);
				}
			} catch (IOException e) {
				System.err.println("remove Block@id " + blockID + " failed, block entry and data will not removed");
			}

		}

		public synchronized Block getBlock(int blockID) {
			Block block = null;

			block = this.loadBlock(blockID);

			return block;
		}

		public synchronized void addBlock(Block block) throws IOException {
			int pageID = block.pageID();
			// check if the block can store in any block page
			if (!page_manage.isBlockPageValid(pageID)) {
				page_manage.create_block_page(pageID);
			}

			long off = page_manage.block_offset(block.id());
			block.writeToSource(off, data_source);
			if (config.getCache()) {
				// block_cache.put(block.id, new
				// SoftReference<BlockEntry>(block));
			}
			block_count++;
		}

		// TODO optomize with index
		public synchronized Block findBlock(byte[] md5, int len) {
			int blockCount = getBlockCount();

			// Traversal all of the block entry to find the pair
			for (int i = 0; i < blockCount; i++) {
				Block block = this.getBlock(i);

				// must make sure the block is valid first
				if (block.ref_count <= 0) {
					continue;
				}
				if (len == block.length && Arrays.equals(md5, block.md5) && block.ref_count < Short.MAX_VALUE) {
					return block;
				}
			}

			return null;
		}
	}

	class HeapManagement {
		// TODO use a noSQL Database aka Berkeley DB to make a cache
		StoredSortedRecordSet<Long, Long> siz_index;
		StoredSortedRecordSet<Long, Long> off_index;

		private FreeEntry first;

		private class FreeEntry {
			public FreeEntry(long offset, long size) {
				this.offset = offset;
				this.size = size;
			}

			public long offset;
			public long size;

			public FreeEntry previous;
			public FreeEntry next;
		}

		public HeapManagement() {
			first = loadFragment();

			long free_space = 0;
			try {
				free_space = data_source.remaining();
			} catch (IOException e) {
				System.err.println("alloc space failed.");
			}
			if (first == null) {
				first = new FreeEntry(END_IDX, free_space);
			} else {
				// recompute the last size
				FreeEntry last = first;
				while (last.next != null) {
					last = last.next;
				}

				last.size = free_space;
			}

			// free the fragment space
			this.free(fragment_offset, fragment_length);
		}

		private FreeEntry loadFragment() {
			if (fragment_offset <= 0) {
				return null;
			}

			FreeEntry entry = null;
			ByteBuffer buffer = ByteBufferPool.alloc(fragment_length);
			buffer.order(BYTE_ORDER);
			try {
				data_source.position(fragment_offset);
				data_source.read(buffer);
				buffer.flip();

				for (int i = 0; i < fragment_length / 16; i++) {
					long offset = buffer.getLong();
					long length = buffer.getLong();

					if (length == 0 || offset == 0) {
						continue;
					} else {
						if (entry == null) {
							entry = new FreeEntry(offset, length);
						} else {
							entry.next = new FreeEntry(offset, length);
							entry.next.previous = entry;
							entry = entry.next;
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("Load Heap Fragment" + " Failed.", e);
			} finally {
				ByteBufferPool.free(buffer);
			}

			return entry;
		}

		public void writeFragInfo() throws IOException {
			fragment_length = fragmentCount() * 16;
			fragment_offset = this.alloc(fragment_length, true);
			writeHeader();

			ByteBuffer buffer = ByteBufferPool.alloc(fragment_length);
			buffer.order(BYTE_ORDER);
			try {
				FreeEntry last = first;
				while (last != null) {

					buffer.putLong(last.offset);
					buffer.putLong(last.size);
					last = last.next;
				}

				buffer.flip();
				data_source.position(fragment_offset);
				data_source.write(buffer);
			} catch (IOException e) {
				throw new RuntimeException("Save Heap Fragment" + " Failed.", e);
			} finally {
				ByteBufferPool.free(buffer);
			}
		}

		private int fragmentCount() {
			int fragmentCount = 1;
			FreeEntry last = first;
			while (last.next != null) {
				last = last.next;
				fragmentCount++;
			}

			return fragmentCount;
		}

		// two sorted linked list
		private FreeEntry searchBySize(long size) {
			FreeEntry preferredEntry = null;
			FreeEntry entry = first;
			while (entry != null) {

				if (entry.size < size) {
					entry = entry.next;
					continue;
				} else if (entry.size == size || null == preferredEntry) {
					preferredEntry = entry;
					break;
				} else {
					if (entry.size < preferredEntry.size) {
						preferredEntry = entry;
					}

					entry = entry.next;
				}
			}

			return preferredEntry;
		}

		// locate the offset in the free space map
		private FreeEntry searchByOffset(long offset) {
			if (offset < 0 || first.offset > offset) {
				return null;
			} else if (first.offset + first.size >= offset) {
				return first;
			} else {
				FreeEntry entry = first;

				while (entry != null) {
					if (entry.offset <= offset && entry.offset + entry.size >= offset) {
						return entry;
					} else {
						entry = entry.next;
					}
				}
			}
			return null;
		}

		// optimize if the size is very small, it makes sense that this alloc is
		// very tempory
		public long alloc(long size) {
			return alloc(size, false);
		}

		public long alloc(long size, boolean init) {
			FreeEntry entry = searchBySize(size);

			if (entry == null) {
				return -1;
			} else {
				return alloc(entry.offset, size, init);
			}
		}

		public long alloc(long offset, long size, boolean init) {
			if (offset < 0 || size <= 0) {
				return -1;
			}
			log("Heap", "Alloc", "size=" + size + "bytes@" + offset);

			// check if the free space at the offset is more than size bytes
			FreeEntry entry = searchByOffset(offset);
			if (null == entry || entry.offset + entry.size < offset + size) {
				return -1;
			}

			// delete this entry from the linked list
			if (entry.offset + entry.size == offset + size) {
				if (entry.previous != null) {
					entry.previous.next = entry.next;
				} else {
					first = first.next;
				}

				if (entry.next != null) {
					entry.next.previous = entry.previous;
				}
			} else if (entry.offset == offset) {
				// cut the first part of entry
				entry.offset = entry.offset + size;
				entry.size = entry.size - size;
			} else if (entry.size + entry.offset == size + offset) {
				// keep the first part
				entry.size = offset - entry.offset;
			} else {
				// the entry must be divide into two seperate part
				FreeEntry entry1 = new FreeEntry(entry.offset, offset - entry.offset);
				FreeEntry entry2 = new FreeEntry(offset + size, entry.size + entry.offset - size - offset);

				if (entry.previous != null) {
					entry.previous.next = entry1;
				}
				entry1.previous = entry.previous;
				entry1.next = entry2;

				if (entry.next != null) {
					entry.next.previous = entry2;
				}
				entry2.previous = entry1;
				entry2.next = entry.next;
			}

			if (init) {
				setZero(offset, size);
			}
			return offset;
		}

		private void setZero(long offset, long size) {
			final int MAX_BUFFER_SIZE = 1 << 20; // 1 MB
			ByteBuffer buffer = ByteBufferPool.alloc(MAX_BUFFER_SIZE, true);
			buffer.order(BYTE_ORDER);
			try {
				// init the page tables
				for (int i = 0; i < MAX_BUFFER_SIZE / 8; i++) {
					buffer.putLong(0);
				}

				buffer.flip();
				data_source.position(offset);

				int batch = (int) (size / MAX_BUFFER_SIZE);
				int rest = (int) (size % MAX_BUFFER_SIZE);
				for (int i = 0; i < batch; i++) {
					data_source.write(buffer);
					buffer.flip();
				}

				buffer.limit(rest);
				buffer.rewind();
				data_source.write(buffer);
			} catch (IOException e) {
				throw new RuntimeException("create new chunk page offset@" + offset + " failed.", e);
			} finally {
				ByteBufferPool.free(buffer);
			}
		}

		public void free(long offset, long size) {
			if (offset < 0 || size <= 0) {
				return;
			}
			log("Heap", "Free", "size=" + size + "bytes@" + offset);
			FreeEntry entry = this.searchByOffset(offset);
			// the offset maybe part or full contains in a freed place
			if (null != entry) {
				// this entry is fully contains in a freed place
				if (entry.offset + entry.size < offset + size) {
					// partial contain and do a merge
					entry.size = offset - entry.offset + size;
				}
			} else if (offset + size < first.offset) {
				entry = first;
				first = new FreeEntry(offset, size);
				first.previous = null;
				first.next = entry;
				entry.previous = first;
			} else if (offset <= first.offset && offset + size >= first.offset) {
				first.offset = offset;
				first.size = Math.max(offset + size, first.offset + first.size) - offset;
			} else {

				// find a free entry near this offset, to get some merge
				// information
				entry = first;
				while (entry != null && entry.next != null) {
					FreeEntry current = entry;
					FreeEntry next = entry.next;

					if (current.offset <= offset && next.offset >= offset) {
						// this space is between current and next entry without cover
						if (offset + size < next.offset) {
							FreeEntry newEntry = new FreeEntry(offset, size);
							current.next = newEntry;
							newEntry.previous = current;

							next.previous = newEntry;
							newEntry.next = next;

							return;
						} else {
							// new space covers the next space part or full
							next.offset = offset;
							next.size = Math.max(offset + size, next.offset + next.size) - offset;
						}
					}
					entry = next;
				}

				// add to the last of list
				FreeEntry newEntry = new FreeEntry(offset, size);
				entry.next = newEntry;
				newEntry.previous = entry;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		test_massive_data(4000000);

		test_random_pic(24000);

		test_exact(new MaroArchive(VolumeFileDataSource.open(Paths.get("test_random_pic.mar"))), false);

		test_exist_pic(Paths.get("test_exist_pic"));
		Environment.exec_cached.shutdown();
	}

	public static void test_massive_data(int count) throws IOException {
		MaroArchive mar = new MaroArchive(VolumeFileDataSource.create(Paths.get("test_massive_data.mar"), 1 << 23), true);
		boolean debug_save = MaroArchive.DEBUG;
		MaroArchive.DEBUG = false;
		byte[] data = new byte[10];
		Arrays.fill(data, (byte) 0x80);
		for (int i = 0; i < count; i++) {
			if (i % 5000 == 0) {
				System.out.println("#" + i + " : Chunk=" + mar.getChunkCount() + " Block=" + mar.getBlockCount());
			}

			try {

				MarEntry entry = mar.findSpareEntry();

				OutputStream os = entry.openWriteStream();
				os.write(data, 0, data.length);

				os.close();
			} catch (IOException e) {
			}
		}

		mar.close();
		MaroArchive.DEBUG = debug_save;
	}

	public static void test_exact(MaroArchive rpak, boolean grape) throws IOException, BadFormatException, IllegalVolumeException {
		ImageGraper graper = new ImageGraper(1024 * 10240 * 4, 1, false);
		File root = new File("test_exist_pic");
		if (root.isDirectory() && root.exists()) {
			root.delete();
		}
		root.mkdirs();

		if (grape) {
			int start = 0;
			for (int id = 0; id < rpak.getChunkCount(); id++) {
				InputStream fis = rpak.getChunkByStream(id, (String) null);
				BufferedImage image = ImageIO.read(fis);

				Dimension thumbDim = ImageUtils.computeSize(image.getWidth(), image.getHeight(), 240 * 320);
				int thumbW = thumbDim.width;
				int thumbH = thumbDim.height;
				BufferedImage thumb = ImageUtils.scale(image, thumbW, thumbH);

				if (thumb != null && !graper.addImage(thumb)) {

					File grapeFile = new File(root, String.format("%06d", start) + "-" + String.format("%06d", id) + ".jpg");
					if (grapeFile.exists()) {
						grapeFile.delete();
					}

					grapeFile.createNewFile();

					ImageUtils.save_image(graper.getGrape(ImageType.TYPE_USHORT_555_RGB), new FileOutputStream(grapeFile), 0.6f);
					System.out.println("createGrape image " + grapeFile + " cid@" + id);

					graper.rest();
					start = id;
				}

				fis.close();
			}

		} else {
			byte[] array = ArrayPool.alloc_byte(4096);
			for (int id = 0; id < rpak.getChunkCount(); id++) {
				File chunkF = new File(root, id + ".jpg");
				if (chunkF.exists()) {
					chunkF.delete();
				}
				chunkF.createNewFile();

				FileOutputStream outputStream = new FileOutputStream(chunkF);
				InputStream fis = rpak.getChunkByStream(id, "pwd");
				for (int i = fis.read(array); i != -1; i = fis.read(array)) {
					outputStream.write(array, 0, i);
				}
				outputStream.close();
				fis.close();
			}
		}
		rpak.close();
	}

	// test high Concurrent operations
	public static void test_write_cache() throws InterruptedException, IOException {
		FileDataSource src1 = FileDataSource.create(Paths.get("test_write_cache1.tmp"));
		FileDataSource src2 = FileDataSource.create(Paths.get("test_write_cache2.tmp"));
		FileDataSource src3 = FileDataSource.create(Paths.get("test_write_cache3.tmp"));
		FileDataSource src4 = FileDataSource.create(Paths.get("test_write_cache4.tmp"));
		final FileDataSource[] srcs = { src1, src2, src3, src4 };

		int count = 0;
		final ByteBuffer data = ByteBuffer.allocate(1024);
		while (true) {
			final int fCount = count;
			Runnable run = () -> {
				DataSource ds = srcs[fCount % srcs.length];

				try {
					ds.position(fCount * data.capacity());
					ds.write(data);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				data.clear();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};

			count++;
			Environment.exec_cached.submit(run);
			Thread.sleep(5);
		}
	}

	private static void test_exist_pic(Path path) throws IOException {
		final MaroArchive mar = new MaroArchive(VolumeFileDataSource.create(Paths.get("test_exist_pic.mar"), 1 << 23), true);
		Utils.vist_file_tree(path, PathFilters.FILTER_FILE, null, _path -> {

			MarEntry entry = mar.findSpareEntry();
			
			try (OutputStream os = entry.openWriteStream()){
				entry.setAttributes(new EntryAttributes(_path));
				
				byte[] data = Files.readAllBytes(_path);
				
				os.write(data, 0, data.length);

			} catch (IOException e) {
			}

			return FileVisitResult.CONTINUE;
		});

		System.out.println("Chunk Count = " + mar.getChunkCount());
		mar.close();
	}

	private static void test_random_pic(int count) throws IOException {
		final int width = 40;
		final int height = 50;
		MaroArchive rpak = new MaroArchive(VolumeFileDataSource.create(Paths.get("test_random_pic.mar"), 1 << 25), true);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		Font font = g2d.getFont().deriveFont(60f);

		g2d.setFont(font);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		FontMetrics fm = g2d.getFontMetrics(font);
		Random rand = new Random();
		Paint p = g2d.getPaint();
		for (int i = 1; i <= count; i++) {
			String str = String.valueOf(i) + "#";
			g2d.setStroke(new BasicStroke());
			g2d.setPaint(p);

			Rectangle rect = fm.getStringBounds(str, g2d).getBounds();
			float index = count;
			Color bgColor = new Color(Color.HSBtoRGB(i % 28 / 30f, (15 + rand.nextInt(i) % 15) / 30f, (10 + rand.nextInt(i) % 10) / 20f));
			Paint paint = new GradientPaint(0, 0, bgColor.brighter().brighter(), width, height, bgColor.darker());
			g2d.setPaint(paint);
			g2d.fillRect(0, 0, width, height);

			Color rev = new Color(255 - bgColor.getRed(), 255 - bgColor.getGreen(), 255 - bgColor.getBlue());
			g2d.setColor(rev.darker().darker());
			g2d.drawLine(0, height / 2, width, height / 2);
			g2d.drawLine(width / 2, 0, width / 2, height);

			int radias = width / 3;
			float dash[] = { i % 10 + 5 };
			BasicStroke b = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);

			g2d.setStroke(b);
			g2d.drawArc(width / 2 - radias, height / 2 - radias, radias * 2, radias * 2, 90, -(i % 360) + 360);

			Color alpha = new Color(160, 100, 64, 128);
			g2d.setColor(alpha);
			g2d.fillArc(width / 2 - radias, height / 2 - radias, radias * 2, radias * 2, 90, -(i % 360));

			g2d.setColor(rev.brighter());
			g2d.drawString(str, (width - rect.width) / 2, (height + rect.height / 2) / 2);
			OutputStream os = rpak.addChunkByStream(false, "pwd");
			ImageUtils.save_image(image, os, 0.95f, true);
		}

		g2d.dispose();
		System.out.println("Chunk Count = " + rpak.getChunkCount());
		rpak.close();
	}

}
