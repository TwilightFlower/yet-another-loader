package io.github.nuclearfarts.yetanotherloader.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

public class Util {
	public static byte[] readStream(InputStream stream) throws IOException {
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
		byte[] buf = new byte[2048];
		int read;
		while((read = stream.read(buf)) != -1) {
			outBuf.write(buf, 0, read);
		}
		return outBuf.toByteArray();
	}
	
	public static Map<String, ?> detoml(TomlTable table) {
		Map<String, Object> map = table.toMap();
		for(Map.Entry<String, Object> e : map.entrySet()) {
			Object val = e.getValue();
			if(val instanceof TomlTable) {
				e.setValue(detoml((TomlTable) val));
			} else if(val instanceof TomlArray) {
				e.setValue(detoml((TomlArray) val));
			}
		}
		return map;
	}
	
	public static List<?> detoml(TomlArray arr) {
		List<Object> list = arr.toList();
		for(int i = 0; i < list.size(); i++) {
			Object val = list.get(i);
			if(val instanceof TomlTable) {
				list.set(i, detoml((TomlTable) val));
			} else if(val instanceof TomlArray) {
				list.set(i, detoml((TomlArray) val));
			}
		}
		return list;
	}
}
