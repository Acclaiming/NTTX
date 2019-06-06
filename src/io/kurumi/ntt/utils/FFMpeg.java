package io.kurumi.ntt.utils;

import java.io.*;
import java.util.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.*;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.io.*;

public class FFMpeg {

	public static long getDuration(File media) {
		
		return Long.parseLong(RuntimeUtil.execForStr(CharsetUtil.CHARSET_UTF_8,"ffmpeg -i " + media.getPath() + " 2>&1 | grep 'Duration' | cut -d ' ' -f 4 | sed s/,//"));
		
	}
	
	static String filters = "fps=15,scale=320:-1:flags=lanczos";
	
	public static File getGifPalettePic(File media) {
		
		File cacheFile = new File(Env.CACHE_DIR,"palette_pic/" + media.getName() + ".png");
		
		cacheFile.getParentFile().mkdirs();
		
		try {
			
			RuntimeUtil.exec("ffmpeg -i " + media.getPath() + " -b 4096k -vf \"" + filters + ",palettegen\" -y " + cacheFile.getPath()).waitFor();
			
		} catch (InterruptedException e) {}
		
		return cacheFile;

		
	}
	
	public static boolean toGif(File globalPalettePicPath,File in,File out) {
		
		out.getParentFile().mkdirs();
		
		try {
			
			return RuntimeUtil.exec("ffmpeg -i " + in.getPath() + " -i " + globalPalettePicPath.getPath() + " -b 4096k -lavfi \"" + filters + " [x]; [x][1:v] paletteuse\" -y " + out.getPath()).waitFor() == 0;
			
		} catch (InterruptedException e) {
			
			return false;
			
		}

	}
	
	public static boolean appendMp4(File out,File... medias) {

		LinkedList<String> convertedMedias = new LinkedList<>();

		for (File mp4 : medias) {

			File output = new File(Env.CACHE_DIR,"ffmpeg_cache/" + UUID.randomUUID().toString(true) + ".ts");

			try {

				int exit = RuntimeUtil.exec("ffmpeg -i " + mp4.getPath() + "-c copy -bsf:v h264_mp4toannexb -f mpegts ",output.getPath()).waitFor();

				if (exit != 0) {

					return false;

				}

				convertedMedias.add(output.getPath());

			} catch (InterruptedException e) {}

		}

		try {

			try {

				return RuntimeUtil.exec("ffmpeg -i " + "\"" + ArrayUtil.join(convertedMedias.toArray(),"|") + "\" -c copy -bsf:a aac_adtstoasc " + out.getPath()).waitFor() == 0;

			}
			finally {

				for (String cache  : convertedMedias) FileUtil.del(cache);

			}

		} catch (InterruptedException e) {

			return false;

		}


	}

}
