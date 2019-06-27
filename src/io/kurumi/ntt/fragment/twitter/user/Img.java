package io.kurumi.ntt.fragment.twitter.user;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.coobird.thumbnailator.Thumbnails;
import java.io.File;

public class Img {

	private BufferedImage image;
	private Graphics2D graphics;

	public final int width;
	public final int height;

	public Img(int width,int height) {

		this(width,height,null);

	}

	public Img(int width,int height,Color backgroundColor) {

		this.width = width; this.height = height;

		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

		graphics = image.createGraphics();


		if (backgroundColor != null) {

			graphics.setBackground(backgroundColor);
			graphics.clearRect(0,0,width,height);

		} else {

			image = graphics.getDeviceConfiguration().createCompatibleImage(width,height,Transparency.TRANSLUCENT);
			graphics.dispose();
			graphics = image.createGraphics();

		}

		// graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(Color.BLACK);
        graphics.setPaint(Color.BLACK);

	}

	public Img fontSize(int size) {

		font(graphics.getFont().getFontName(),size);

		return this;

	}
	
	public Img font(String newFont) {
		
		font(newFont,graphics.getFont().getSize());
		
		return this;
		
	}

	public Img font(String newFont,int size) {

		graphics().setFont(new Font(newFont,graphics.getFont().getStyle(),size));

		return this;

	}

	public int stringWidth(String string) {

		return graphics.getFontMetrics().stringWidth(string);

	}

	public int stringHeight() {

		return graphics.getFontMetrics().getHeight();

	}
	
	public Img drawImageCenter(int xPedding,int yPedding,int xMargin,int yMargin,File newImage,int width,int height) {

		drawImageCenter(xPedding,yPedding,xMargin,yMargin,newImage,width,height,null);

		return this;

	}

	public Img drawImageCenter(int xPedding,int yPedding,int xMargin,int yMargin,File newImage,int width,int height,Color bgColor) {

		int realX = xPedding + ((this.width - xMargin - width) / 2);
		int realY = yPedding + ((this.height - yMargin - height) / 2);

		drawImage(realX,realY,newImage,width,height,bgColor);

		return this;

	}
	
	public Img drawImageCenter(int xPedding,int yPedding,int xMargin,int yMargin,BufferedImage newImage,int width,int height) {
		
		drawImageCenter(xPedding,yPedding,xMargin,yMargin,newImage,width,height,null);
		
		return this;
		
	}

	public Img drawImageCenter(int xPedding,int yPedding,int xMargin,int yMargin,BufferedImage newImage,int width,int height,Color bgColor) {

		int realX = xPedding + ((this.width - xMargin - width) / 2);
		int realY = yPedding + ((this.height - yMargin - height) / 2);

		drawImage(realX,realY,newImage,width,height,bgColor);
		
		return this;
		
	}
	
	public Img drawImage(int x,int y,File image,int width,int height) {
		
		drawImage(x,y,image,width,height,null);
		
		return this;
		
	}

	public Img drawImage(int x,int y,File image,int width,int height,Color bgCplor) {

		try {

			drawImage(x,y,Thumbnails.of(image).size(width,height).asBufferedImage(),width,height,bgCplor);

		} catch (IOException e) {}

		return this;

	}

	public Img drawImage(int x,int y,BufferedImage newImage,int width,int height) {
		
		drawImage(x,y,newImage,width,height,null);
		
		return this;
		
	}
	
	public Img drawImage(int x,int y,BufferedImage newImage,int width,int height,Color bgColor) {

		if (bgColor == null) {

			graphics.drawImage(newImage,x,y,width,height,null);

		} else {

			graphics.drawImage(newImage,x,y,width,height,bgColor,null);

		}

		return this;

	}

	public Img drawTextCenter(int xPedding,int yPedding,int xMargin,int yMargin,String text) {

		int realX = xPedding + ((width - xPedding - xMargin) / 2) + stringWidth(text);
		int realY = yPedding + ((height - yPedding - yMargin) / 2) + stringHeight();

		drawText(realX,realY,text);

		return this;

	}

	public Img drawText(int x,int y,String text) {

		Paint paint = graphics.getPaint();

		graphics.setPaint(new Color(0,0,0,64));

		graphics.drawString(text,x,y);

		graphics.setPaint(paint);

		graphics.drawString(text,x,y);

		return this;

	}

	public byte[] getBytes() {

		return getBytes("png");

	}

	public byte[] getBytes(String format) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            Thumbnails.of(image)
                .size(width,height)
                .outputFormat(format)
                .outputQuality(1f)
                .toOutputStream(out);

        } catch (IOException e) {}

		return out.toByteArray();

	}

	public Img toFile(File outPut) {

		toFile(outPut,"png");

		return this;

	}

	public Img toFile(File outPut,String format) {

		try {

			Thumbnails.of(image)
				.size(width,height)
				.outputFormat(format)
				.outputQuality(1f)
				.toFile(outPut);

		} catch (IOException e) {}

		return this;

	}

	public BufferedImage image() {

		return image;

	}

	public Graphics2D graphics() {

		return graphics;

	}

}
