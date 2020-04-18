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
package com.marolabs.io.mar.file.view;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import com.marolabs.data.MutableByteArray;
import com.marolabs.io.NInput;
import com.marolabs.io.NOutput;
import com.marolabs.io.stream.NOutputStream;
import com.marolabs.io.stream.data.NDataInputStream;
import com.marolabs.io.stream.data.NDataOutputStream;
import com.marolabs.util.Utils;

public class Node {
	private transient short depth = 0;
	private transient Node parent;

	public int chunkID = -1;
	private List<Node> children = new LinkedList<>();

	public String name = "";

	private transient NodeStats stats = null;
	private transient boolean dirty = false;

	public Node(Node parent) {
		this.parent = parent;

		if (null == parent) {
			// this node must be root
			depth = 0;
		} else {
			depth = (short) (parent.depth + 1);
		}
	}

	public void writeTo(NOutput out) throws IOException {
		if (out == null) {
			return;
		}
		try (NDataOutputStream dos = out.asProtectStream().asDataOutputStream()) {
			dos.writeInt(children.size());
			if (children.size() == 0) {
				dos.writeInt(this.chunkID);
			}

			dos.writeUTF(name);

			// TODO optimize
			for (Node child : children) {
				child.writeTo(out);
			}
		}
	}

	public void readObject(NInput in) throws IOException {
		if (in == null) {
			return;
		}

		try (NDataInputStream dis = in.asProtectStream().asDataIntputStream()) {
			int childCount = dis.readInt();

			if (childCount == 0) {
				this.chunkID = dis.readInt();
			}
			this.name = dis.readUTF();

			for (int i = 0; i < childCount; i++) {
				Node child = new Node(this);
				child.readObject(in);
				children.add(child);
			}
		}
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public Node parent() {
		return parent;
	}

	public int depth() {
		return depth;
	}

	public int childCount() {
		return children.size();
	}

	public Node removeChild(int index) {
		Node removedNode = children.remove(index);
		dirty = true;
		return removedNode;
	}

	public boolean removeChild(Node child) {
		if (null == child) {
			return false;
		}
		return children.remove(child);
	}

	public int indexOf(Node child) {
		return children.indexOf(child);
	}

	public Node childAt(int index) {
		return children.get(index);
	}

	public void clear() {
		children.clear();
	}

	public NodeStats stats() {
		if (dirty || null == stats) {
			int[] int_stats = new int[3];
			stats_sub(this, int_stats);
			stats = new NodeStats(int_stats);
		}

		return stats;
	}

	private void stats_sub(Node node, int[] stats) {
		if (null == stats || stats.length < 3) {
			return;
		}

		final int IDX_DEPTH = 0;
		final int IDX_LEAF_CNT = 1;
		final int IDX_NODE_CNT = 2;

		stats[IDX_DEPTH] = Math.max(stats[IDX_DEPTH], node.depth());
		stats[IDX_NODE_CNT] += node.childCount();

		for (int i = 0; i < node.childCount(); i++) {
			Node child = node.childAt(i);
			if (child.isLeaf()) {
				stats[IDX_LEAF_CNT] += 1;
			} else {
				stats_sub(child, stats);
			}
		}

	}

	public void addChild(Node child) {
		if (null != child) {
			if (child.isLeaf()) {

			}
			children.add(child);
			dirty = true;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (null == obj || !(obj instanceof Node)) {
			return false;
		}

		Node node = (Node) obj;

		// 1st same reference of parent
		if (node.parent != this.parent) {
			return false;
		}

		if (node.name == null || this.name == null) {
			return false;
		}

		return node.name.equals(this.name);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (null == parent) {
			sb.append("Root [");
		} else {
			sb.append("Node [");
		}

		sb.append(stats());

		sb.append("\nbinary=");
		MutableByteArray mba = MutableByteArray.create();

		try (NOutputStream dos = NOutputStream.wrap(mba)) {
			writeTo(dos);
			byte[] array = mba.toByteArray().array;

			sb.append(Utils.toString(Utils.toString(array, 4096)));
		} catch (IOException e) {
		}

		sb.append("]");
		return sb.toString();
	}

	public void print(PrintStream ps) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			sb.append("--");
		}

		ps.print(sb);

		if (isLeaf()) {
			ps.print("- " + name);
		} else {
			ps.print("+ " + name);
		}

		ps.println();

		for (Node child : children) {
			child.print(ps);
		}
	}

	static int count = 0;

	private static void test_file(Node parent, File file) {
		String name = file.getName();
		Node child = new Node(parent);

		parent.addChild(child);
		count++;
		child.chunkID = count;
		child.name = name;
		// System.out.println(child.depth() +" : "+file);
		File[] files = file.listFiles();

		if (files == null) {
			return;
		}

		for (File f : files) {
			test_file(child, f);
		}
	}

	public static void main(String[] args) throws IOException {
		Node root_node = new Node(null);
		root_node.name = "root";
		File root_file = new File("");
		test_file(root_node, root_file);

		root_node.print(System.out);

		System.out.println(root_node);
	}
}
