package io.kurumi.ntt.utils;

import java.io.*;
import java.util.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.*;
import cn.hutool.core.lang.UUID;

public class FFMpeg {

	public static boolean appendMp4(File out,File... medias) {

		LinkedList<String> convertedMedias = new LinkedList<>();

		for (File mp4 : medias) {

			File output = new File(Env.CACHE_DIR,"ffmpeg_cache/" + UUID.randomUUID().toString(true) + ".ts");

			try {

				int exit = RuntimeUtil.exec("ffmpeg -i",mp4.getPath(),"-c copy -bsf:v h264_mp4toannexb -f mpegts ",output.getPath()).waitFor();

				if (exit != 0) {

					return false;

				}
				
				convertedMedias.add(output.getPath());

			} catch (InterruptedException e) {}

		}

		try {

			return RuntimeUtil.exec("ffmpeg -i","\"" + ArrayUtil.join(convertedMedias.toArray(),"|") + "\"","-c copy -bsf:a aac_adtstoasc",out.getPath()).waitFor() == 0;

		} catch (InterruptedException e) {

			return false;

		}


	}

}
