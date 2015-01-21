package mktgbi.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import mktgbi.dataio.Config;

public class GaFile {
	
	public static BufferedWriter getFileWriter(String path) throws FileNotFoundException {
		/* Open the file */
		FileOutputStream fos = new FileOutputStream(path);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);
		return bw;
	}
	
	public static void reportError(String path, IOException exception) {
		Config.SM_LOGGER.severe("IO error for file: " + path);
		exception.printStackTrace();
	}

}
