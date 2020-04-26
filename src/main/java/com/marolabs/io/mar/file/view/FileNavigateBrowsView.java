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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import com.marolabs.io.mar.file.EntryPath;
import com.marolabs.io.mar.file.attr.EntryAttributes;
import com.marolabs.io.stream.NInputStream;
import com.marolabs.io.stream.NOutputStream;
import com.marolabs.plugin.PluginException;
import com.marolabs.plugin.PluginManager;
import com.marolabs.util.Utils;
import com.marolabs.util.Vendor;

public class FileNavigateBrowsView extends AbstractFileBrowsView {
	private final static long pluginUUID = 0xffff0001;

	static
	{
		try {
			PluginManager.register(FileNavigateBrowsView.class);
		} catch (PluginException e) {
			System.out.println("Load Plugin Failed. " + e);
		}
	}
	private FileNavigateBrowsView() {
	}

	public static FileNavigateBrowsView instance() {
		return new FileNavigateBrowsView();
	}

	@Override
	public String toString() {
		return pluginUUID + " FileNavigateBrowsView[name=" + name() + " info=" + info() + "->" + root + "]";
	}

	public boolean existNode(EntryPath path) {
		Node child = root;
		for (int i = 0; i < path.getNameCount(); i++) {
			Node parent = child;
			child = new Node(parent);

			String name = path.getStringName(i);

			child.name = name;
			int index = parent.indexOf(child);

			// find a exist one
			if (index >= 0) {
				child = parent.childAt(index);
			} else {
				return false;
			}
		}

		return true;
	}

	public Node getNode(EntryPath path) {
		Node child = root;
		for (int i = 0; i < path.getNameCount(); i++) {
			Node parent = child;
			child = new Node(parent);

			String name = path.getStringName(i);

			child.name = name;
			int index = parent.indexOf(child);

			// find a exist one
			if (index >= 0) {
				child = parent.childAt(index);
			} else {
				return null;
			}
		}
		return child;
	}

	public void removeNode(EntryPath path) {
		Node child = root;
		for (int i = 0; i < path.getNameCount(); i++) {
			Node parent = child;
			child = new Node(parent);

			String name = path.getStringName(i);

			child.name = name;
			int index = parent.indexOf(child);

			// find a exist one
			if (index >= 0) {
				child = parent.childAt(index);
			} else {
				return;
			}
		}

		if (child.parent() != null) {
			child.parent().removeChild(child);
		}
	}

	@Override
	public void removeEntry(EntryAttributes attributes) {
		removeNode(attributes.path());
	}

	@Override
	public void addEntry(EntryAttributes attributes, int chunkID) {
		Node child = root;
		for (int i = 0; i < attributes.path().getNameCount(); i++) {
			Node parent = child;
			child = new Node(parent);

			String name = attributes.path().getStringName(i);

			child.name = name;
			int index = parent.indexOf(child);

			// find a exist one
			if (index >= 0) {
				child = parent.childAt(index);
			} else {
				parent.addChild(child);
			}
		}

		child.chunkID = chunkID;
	}

	public void print(PrintStream ps) {
		root.print(ps);
	}

	public static void main(String[] args) throws IOException {
		File file = new File("/Volumes/Ramdisk/1");

		final FileNavigateBrowsView view = new FileNavigateBrowsView();
		final AtomicInteger count = new AtomicInteger();
		final Path start = Paths.get("");
		Utils.vist_file_tree(start, null, null, param -> {
			// view.addPath(new EntryPath(param), count.getAndIncrement());

			if (count.get() % 1000 == 0) {
				System.out.println(count);
			}
			return FileVisitResult.CONTINUE;
		});

		// view.root.print(System.out);
		System.out.println(view.root);
		if (file.exists()) {
			file.delete();
		}

		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		NOutputStream os = NOutputStream.wrap(new FileOutputStream(file));

		view.writeTo(os);
		os.close();

		NInputStream is = NInputStream.wrap(new FileInputStream(file));

		FileNavigateBrowsView v2 = new FileNavigateBrowsView();
		long tmil = System.currentTimeMillis();
		v2.readObject(is);
		System.out.println(System.currentTimeMillis() - tmil);
		File out2 = new File("/Volumes/Ramdisk/2.txt");
		OutputStream fos = new BufferedOutputStream(new FileOutputStream(out2));
		v2.print(new PrintStream(fos));
		is.close();
	}

	@Override
	public Vendor vendor() {
		Vendor vendor = new Vendor();
		vendor.name = "Funtune";
		vendor.company = "Marolabs Co,Ltd";
		return vendor;
	}

	@Override
	public String name() {
		return "File Navigate Brows View";
	}

	@Override
	public String info() {
		return "File Navigate Brows View";
	}

	@Override
	public boolean concurrent() {
		return false;
	}
}
