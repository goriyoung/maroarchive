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

import java.nio.ByteOrder;

public interface MaroArchiveConstants {
	// the sizeof address, use 48bit to save space
	static final int SIZ_PAGE_OFFSET = 6;

	static final int MAX_CHUNK_PAGE = 1 << 15;
	static final int MAX_CHUNK_IN_PAGE = 1 << 15;

	static final int MAX_BLOCK_PAGE = 1 << 15;
	static final int MAX_BLOCK_IN_PAGE = 1 << 15;

	static final int DEFAULT_MAX_BLK_SIZ = 1 << 20; // 1 MB
	static final int DEFAULT_VER = 6;

	static final byte[] HDR_MAGIC = { (byte) 0xff, 0x4d, 0x41, 0x43, 0x41, 0x46 };// MACAF

	// size of the field in header
	static final int SIZ_HDR_ID = 6;
	/* BIG_ENDIAN = 0, LITTLE_ENDIAN = 1 */
	static final int SIZ_HDR_ODR = 1;
	/* version */
	static final int SIZ_HDR_VER = 1;
	/* zip algorithm */
	static final int SIZ_HDR_ZIP = 1;
	/* crypt algorithm */
	static final int SIZ_HDR_CYP = 1;
	/* the time in ms when the bundle create */
	static final int SIZ_HDR_CRT_TIM = 8;

	/*
	 * check if the file is Interrupt, the data may be not complete.
	 */
	static final int SIZ_HDR_STATE = 1;
	/* max block size */
	static final int SIZ_HDR_MAX_BLK_SIZ = 3;
	/* current chunk amount */
	static final int SIZ_HDR_CHK_NUM = 4;
	/* current block amount */
	static final int SIZ_HDR_BLK_NUM = 4;

	// TODO this is option item need remove from header
	static final int SIZ_FRAG_DAT_OFF = 6;
	static final int SIZ_FRAG_DAT_LEN = 4;

	/* offset_block_ids for each field in header */
	static final int OFF_HDR_ODR = SIZ_HDR_ID;
	static final int OFF_HDR_VER = OFF_HDR_ODR + SIZ_HDR_ODR;
	static final int OFF_HDR_ZIP = OFF_HDR_VER + SIZ_HDR_VER;
	static final int OFF_HDR_CYP = OFF_HDR_ZIP + SIZ_HDR_ZIP;
	static final int OFF_HDR_CRT_TIM = OFF_HDR_CYP + SIZ_HDR_CYP;
	static final int OFF_HDR_STATE = OFF_HDR_CRT_TIM + SIZ_HDR_CRT_TIM;
	static final int OFF_HDR_MAX_BLK_SIZ = OFF_HDR_STATE + SIZ_HDR_STATE;
	static final int OFF_HDR_CHK_NUM = OFF_HDR_MAX_BLK_SIZ + SIZ_HDR_MAX_BLK_SIZ;
	static final int OFF_HDR_BLK_NUM = OFF_HDR_CHK_NUM + SIZ_HDR_CHK_NUM;
	static final int OFF_FRAG_DAT_OFF = OFF_HDR_BLK_NUM + SIZ_HDR_BLK_NUM;
	static final int OFF_FRAG_DAT_LEN = OFF_FRAG_DAT_OFF + SIZ_FRAG_DAT_OFF;

	static final int END_HDR = OFF_FRAG_DAT_LEN + SIZ_FRAG_DAT_LEN;

	static final int SIZ_IDX_CHK_PAG = MAX_CHUNK_PAGE * SIZ_PAGE_OFFSET;
	static final int SIZ_IDX_BLK_PAG = MAX_BLOCK_PAGE * SIZ_PAGE_OFFSET;

	static final int OFF_IDX_CHK_PAG = END_HDR;
	static final int OFF_IDX_BLK_PAG = OFF_IDX_CHK_PAG + SIZ_IDX_CHK_PAG;

	static final int END_IDX = OFF_IDX_BLK_PAG + SIZ_IDX_BLK_PAG;

	// size of the field in chunk entry
	static final int SIZ_CHK_TYPE = 1;

	/* [1byte {1bit valid,compressed,encrypted,reserved:5bits}] */
	static final int SIZ_CHK_STATE = 1;
	/* pwd verfier */
	static final int SIZ_CHK_PWD_VFY = 8;
	/* chunk offset_block_ids */
	static final int SIZ_CHK_OFF_BLK_IDS = 6;
	/* block amount in the chunk */
	static final int SIZ_CHK_BLK_NUM = 4;
	/* real size for output */
	static final int SIZ_CHK_DAT_SIZ_OUT = 6;
	/* size in the archive */
	static final int SIZ_CHK_DAT_SIZ = 6;
	/* attr data start offset */
	static final int SIZ_CHK_OFF_ATT = 6;
	/* attr data length (16bit value) */
	static final int SIZ_CHK_LEN_ATT = 2;

	/* offset_block_ids for each field in chunk entry */
	static final int OFF_CHK_TYPE = 0;
	static final int OFF_CHK_STATE = OFF_CHK_TYPE + SIZ_CHK_TYPE;
	static final int OFF_CHK_PWD_VFY = OFF_CHK_STATE + SIZ_CHK_STATE;
	static final int OFF_CHK_BLK_OFF_IDS = OFF_CHK_PWD_VFY + SIZ_CHK_PWD_VFY;
	static final int OFF_CHK_BLK_NUM = OFF_CHK_BLK_OFF_IDS + SIZ_CHK_OFF_BLK_IDS;
	static final int OFF_CHK_DAT_SIZ_OUT = OFF_CHK_BLK_NUM + SIZ_CHK_BLK_NUM;
	static final int OFF_CHK_OFF_ATT = OFF_CHK_DAT_SIZ_OUT + SIZ_CHK_DAT_SIZ_OUT;
	static final int OFF_CHK_LEN_ATT = OFF_CHK_OFF_ATT + SIZ_CHK_OFF_ATT;

	static final int OFF_CHK_DAT_SIZ = OFF_CHK_LEN_ATT + SIZ_CHK_LEN_ATT;

	static final int SIZ_CHK = OFF_CHK_DAT_SIZ + SIZ_CHK_DAT_SIZ;

	/* this is the bit position in state byte */
	static final int IDX_CHK_STATE_VALID = 0;
	static final int IDX_CHK_STATE_COMPRESS = 1;
	static final int IDX_CHK_STATE_ENCRYPT = 2;
	static final int IDX_CHK_STATE_PRIVATE = 3;

	/*
	 * size of the field in block entry
	 */
	/* the refrerence count by the chunks */
	static final int SIZ_BLK_REF_NUM = 3;
	/* TODO optional item the md5 hash of this block, use to dedupe */
	static final int SIZ_BLK_MD5 = 8;
	/* block offset_block_ids in target page */
	static final int SIZ_BLK_OFF = 6;
	/* block length in target page */
	static final int SIZ_BLK_LEN = 3;
	/* the crc8 of block data */
	static final int SIZ_BLK_CRC = 1;

	// offset_block_ids of the field in block entry
	static final int OFF_BLK_REF_COUNT = 0;
	static final int OFF_BLK_MD5 = OFF_BLK_REF_COUNT + SIZ_BLK_REF_NUM;
	static final int OFF_BLK_OFF = OFF_BLK_MD5 + SIZ_BLK_MD5;
	static final int OFF_BLK_LEN = OFF_BLK_OFF + SIZ_BLK_OFF;
	static final int OFF_BLK_CRC = OFF_BLK_LEN + SIZ_BLK_CRC;

	static final int SIZ_BLK = OFF_BLK_CRC + SIZ_BLK_LEN;

	static final int SIZ_BLK_ID = 4;
	static final int SIZ_CHK_ID = 4;

	static ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
}
