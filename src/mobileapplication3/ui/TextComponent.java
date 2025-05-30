/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.ui;

import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public class TextComponent extends UIComponent {
    
    public static final int HEIGHT_AUTO = -1, FONT_SIZE_AUTO = -1;
    private String text = null;
    private int fontColor;
    private int[][] lineBounds = null;
    private int prevW;
    public Font font;
    private boolean autoFontSize = true;
    private Font prevGetLineBoundsFont;
    public int padding;
    private Thread scrollAnimThread = null;
    private boolean isHorizontalScrollingEnabled = false;
    int horizontalScrollOffset = 0;
    int textW2 = 0;
    int textAlignment = HCENTER | VCENTER;
    
    public TextComponent() {
    	font = Font.getDefaultFont();
    	//setFont(Font.getFont(font.getFace(), font.getStyle(), Font.SIZE_LARGE));
    	prevGetLineBoundsFont = font;
        padding = font.getHeight()/6;
        bgColor = COLOR_ACCENT;
        fontColor = FONT_COLOR;
    }
    
    public TextComponent(String text) {
        this();
        setText(text);
    }
    
    public void init() {
    	super.init();
    	enableHorizontalScrolling(isHorizontalScrollingEnabled);
    }
    
    private int getOptimalHeight() {
        return font.getHeight() * (getLineBounds(text, font, w, padding).length) + font.getHeight() / 2;
    }

    public void onPaint(Graphics g, int x0, int y0, int w, int h, boolean forceSelected) {
        if (text == null) {
            return;
        }
        
        Font prevFont = g.getFont();
        g.setFont(font);
        lineBounds = getLineBounds(text, font, w, padding);
        
        int step = font.getHeight() * 3 / 2;
        if (step * lineBounds.length > h - padding * 2) {
            //step = (h - padding * 2) / (lineBounds.length+1);
            if (step * lineBounds.length > h - padding * 2) {
                step = h / (lineBounds.length);
            }
        }
        
        int offset = padding;
        if ((textAlignment & VCENTER) != 0) {
            offset = -step * (lineBounds.length - 1) / 2 + h/2 - font.getHeight() / 2;
        }
        
        boolean hCenter = (textAlignment & HCENTER) != 0;
        
        if (isActive && !forceSelected) {
        	g.setColor(fontColor);
		} else {
			g.setColor(FONT_COLOR_INACTIVE);
		}
        for (int i = 0; i < lineBounds.length; i++) {
            int[] bounds = lineBounds[i];
            g.drawSubstring(
            		text,
            		bounds[0], bounds[1],
            		x0 + (hCenter ? w/2 : 0) - horizontalScrollOffset, y0 + offset,
            		(hCenter ? Graphics.HCENTER : Graphics.LEFT) | Graphics.TOP);
            offset += step;
        }
        g.setFont(prevFont);
    }
    
    private int[][] getLineBounds(String text, Font font, int w, int padding) {
        if (isHorizontalScrollingEnabled) {
            return new int[][]{{0, text.length()}};
        }
        
        if (lineBounds != null && w == prevW && font.getSize() == prevGetLineBoundsFont.getSize()) {
            return lineBounds;
        }
        
        prevW = w;
        prevGetLineBoundsFont = font;
        return font.getLineBounds(text, w, padding);
    }
    
    public boolean canBeFocused() {
        return false;
    }
    
    public boolean handlePointerClicked(int x, int y) {
        return false;
    }

    public boolean handleKeyPressed(int keyCode, int count) {
        return false;
    }

    public String getText() {
        return text;
    }

    public TextComponent setText(String text) {
        this.text = text;
        textW2 = Font.getDefaultFont().stringWidth(text) / 2;
        lineBounds = null;
        horizontalScrollOffset = 0;
        if (h == HEIGHT_AUTO) {
            onSetBounds(x0, y0, w, h);
        }
        return this;
    }
    
    public void setFontSize(int size) {
    	autoFontSize = size == FONT_SIZE_AUTO;
    	if (!autoFontSize) {
    		font = new Font(size);
    	}
	}
    
    public TextComponent setTextAlignment(int a) {
        textAlignment = a;
        return this;
    }
    
    public TextComponent setFontColor(int fontColor) {
		this.fontColor = fontColor;
		return this;
	}
    
    public int getFontColor() {
		return fontColor;
	}
    
    public TextComponent enableHorizontalScrolling(boolean b) {
        if (b && scrollAnimThread == null) {
            scrollAnimThread = new Thread(new Runnable() {
                public void run() {
                    horizontalScrollOffset = 0;
                    int prevScrollOffset = horizontalScrollOffset;
                    boolean reverse = false;
                    try {
	                    while (isHorizontalScrollingEnabled && isOnScreen()) {
                            long start = System.currentTimeMillis();
                            if (textW2*2 > w) {
	                            if (!reverse) {
	                                if (horizontalScrollOffset < textW2 - w / 3) {
	                                    horizontalScrollOffset += textW2 / 128;
	                                } else {
	                                    Thread.sleep(500);
	                                    reverse = true;
	                                }
	                            } else {
	                                if (horizontalScrollOffset > -textW2 + w / 3) {
	                                    horizontalScrollOffset -= textW2 / 16;
	                                } else {
	                                    Thread.sleep(500);
	                                    reverse = false;
	                                }
	                            }
	                            if (horizontalScrollOffset != prevScrollOffset) {
	                            	repaint();
	                            }
                            } else {
                            	horizontalScrollOffset = 0;
                            }
                            prevScrollOffset = horizontalScrollOffset;
                            Thread.sleep(Math.max(0, 20 - (System.currentTimeMillis() - start)));
	                    }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    scrollAnimThread = null;
                }
            });
            
            scrollAnimThread.start();
        }
        isHorizontalScrollingEnabled = b;
        return this;
    }

    public void onSetBounds(int x0, int y0, int w, int h) {
        this.x0 = x0;
        this.y0 = y0;
        this.w = w;
        this.h = h;
        if (h == HEIGHT_AUTO) {
            if (text != null) {
                this.h = getOptimalHeight();
            }
        }
        if (autoFontSize) {
            font = new Font(Font.getDefaultFontSize());

            lineBounds = getLineBounds(text, font, w, padding);
            if (h / lineBounds.length < font.getHeight()) {
                font = new Font(Font.SIZE_MEDIUM);
                lineBounds = getLineBounds(text, font, w, padding);
                if (h / lineBounds.length < font.getHeight()) {
                    font = new Font(Font.SIZE_SMALL);
                    lineBounds = getLineBounds(text, font, w, padding);
                }
            }
        }
    }
    
}
