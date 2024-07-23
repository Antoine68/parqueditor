package dev.arichard.parqueditor.parser;

import java.io.File;

public interface Parser<O> {
	O parse(File file);
}
