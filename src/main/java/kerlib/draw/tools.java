/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author zeus
 */
public class tools {
    private static final AffineTransform affinetransform = new AffineTransform();
	private static final FontRenderContext frc = new FontRenderContext(affinetransform,true,true);  
	/**Отмена*/
	private final static String UNDO_ACTION = "Undo";
	/**Примена*/
	private final static String REDO_ACTION = "Redo";
    
    
    ///Направление стрелки
	public static enum derect {
		Up, Down, 
		/**Стрелка смотреть будет в левую сторону - <*/
		Left, 
		/**Стрелка смотреть будет в левую сторону - >*/
		Right,
	}
    ///Выравнивание текста по оси Х
	public static enum alignmentX {
		/** Текст будет упираться в левую границу, то есть сдвигаться вправо. Значение по умолчанию */
		left, 
		center,
		/** Текст будет упираться в правую границу, то есть сдвигаться влево */
		right
	}
    ///Выравнивание текста по опи У
	public static enum alignmentY {
		/** Текст будет упираться в нижнюю границу. То есть всегда леэать на оси y сверху */
		top, center,
		/** Текст будет упираться в врехнюю границу. То есть всегда под осью */
		bottom
	}

	/**
	 * Рисует стрелку
	 * @param g
	 * @param x0 - положение
	 * @param y0
	 * @param d - направление
	 * @param lenght - длина усов
	 * @param angl - угол между стрелками, в градусах
	 */
	public static void arrow(Graphics g, double x0, double y0, derect d, double lenght, double angl) {
		angl = Math.toRadians(angl);
		var move = switch (d) {
            case Up ->  Math.PI / 2;
            case Left -> 0;
            case Down -> -Math.PI / 2;
            case Right -> Math.PI;
            default -> 0;
		};
        arrow(g, x0, y0, move, lenght, angl);
	}
    /**
	 * Рисует стрелку
	 * @param g
	 * @param x0 - положение
	 * @param y0
	 * @param d - направление, в радианах
	 * @param lenght - длина усов
	 * @param angl - угол между стрелками, в радианах
	 */
	public static void arrow(Graphics g, double x0, double y0, double d, double lenght, double angl) {
		double an = d + angl;
		g.drawLine(kerlib.tools.round(x0), kerlib.tools.round(y0), kerlib.tools.round(lenght * Math.cos(an) + x0), kerlib.tools.round(lenght * Math.sin(an) + y0));
		an = d - angl;
		g.drawLine(kerlib.tools.round(x0), kerlib.tools.round(y0), kerlib.tools.round(lenght * Math.cos(an) + x0), kerlib.tools.round(lenght * Math.sin(an) + y0));
	}
    /**
	 * Рисует заполненную стрелку
	 * @param g
	 * @param x0 - положение
	 * @param y0
	 * @param d - направление, в радианах
	 * @param lenght - длина усов
	 * @param angl - угол между стрелками, в радианах
	 */
	public static void fillarrow(Graphics g, double x0, double y0, double d, double lenght, double angl) {
        var cos1 = Math.cos(d + angl);
        var sin1 = Math.sin(d + angl);
        var cos2 = Math.cos(d - angl);
        var sin2 = Math.sin(d - angl);
        g.fillPolygon(new int[]{
            kerlib.tools.round(x0),
            kerlib.tools.round(lenght * cos1 + x0),
            kerlib.tools.round(lenght * cos2 + x0)
        }, new int[]{
            kerlib.tools.round(y0),
            kerlib.tools.round(lenght * sin1 + y0),
            kerlib.tools.round(lenght * sin2 + y0)
        }, 3);
	}

	/**Рисует текст на экране
	 * @param g холст
	 * @param x положение текста
	 * @param y положение текста
	 * @param size размер текста
	 * @param text текст
	 * @param alX выравнивание по x
	 * @return прямоугольник, который очерчивает написанный текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y,String text, alignmentX alX){
		return drawString(g, x, y, text, alX, alignmentY.center);
	}
	/**Рисует текст на экране
	 * @param g холст
	 * @param x положение текста
	 * @param y положение текста
	 * @param size размер текста
	 * @param text текст
	 * @param alX выравнивание по x
	 * @return прямоугольник, который очерчивает написанный текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y,float size, String text, alignmentX alX){
		return drawString(g, x, y, size, text, alX, alignmentY.center);
	}
	/**Рисует текст на экране
	 * @param g холст
	 * @param x положение текста
	 * @param y положение текста
	 * @param size размер текста
	 * @param text текст
	 * @param alX выравнивание по x
	 * @param alY выравнивание по y
	 * @return прямоугольник, который очерчивает написанный текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y,float size, String text, alignmentX alX, alignmentY alY){
		final var of = g.getFont();
		final var newFont = of.deriveFont(size);
		g.setFont(newFont);
		final var r = drawString(g, x, y, text, alX, alY);
		g.setFont(of);
		return r;
	}
	/**Рисует текст на экране
	 * @param g холст
	 * @param x положение текста
	 * @param y положение текста
	 * @param text текст
	 * @param alX выравнивание по x
	 * @param alY выравнивание по y
	 * @return прямоугольник, который очерчивает написанный текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y, String text, alignmentX alX, alignmentY alY){
		final var fm = g.getFontMetrics();
		final var rect = fm.getStringBounds(text, g);

		final var textHeight = rect.getHeight();
		final var textWidth = rect.getWidth();
		
		final var cornerX = switch(alX){
			case left -> x;
			case center ->  x - (textWidth / 2);
			case right -> x - textWidth;
		};
		final var cornerY = switch(alY){
			case top -> y - textHeight;
			case center -> y - (textHeight / 2);
			case bottom -> y;
		} + fm.getAscent();
		g.drawString(text, kerlib.tools.round(cornerX), kerlib.tools.round(cornerY));
		return rect;
	}
	
	public static int getTextHeight(Font font, String text) {return (int)(font.getStringBounds(text, frc).getHeight());}
	public static int getTextHeight(Graphics g, String text) {return getTextHeight(g.getFont(), text);}
	public static int getTextWidth(Font font, String text) {return (int)(font.getStringBounds(text, frc).getWidth());}
	public static int getTextWidth(Graphics g, String text) {return getTextWidth(g.getFont(), text);}
	/**Рисует окружность
	 * @param g холст
	 * @param x центр 
	 * @param y центр
	 * @param r радиус
	 */
	public static void circle(Graphics g, double x, double y, double r) {
		final var d = kerlib.tools.round(r*2);
		g.drawOval(kerlib.tools.round(x - r), kerlib.tools.round(y - r), d, d);
	}
	public static void circle(Graphics g, int x, int y, int r) {
		g.drawOval(x - r, y - r, r*2, r*2);
	}
	/**Рисует окружность
	 * @param g холст
	 * @param x центр 
	 * @param y центр
	 * @param r радиус
	 */
	public static void fillCircle(Graphics g, double x, double y, double r) {
		final var d = kerlib.tools.round(r*2);
		g.fillOval(kerlib.tools.round(x - r), kerlib.tools.round(y - r), d, d);
	}
	public static void fillCircle(Graphics g, int x, int y, int r) {
		g.fillOval(x - r, y - r, r*2, r*2);
	}
	/**
     * Converts the components of a color, as specified by the HSB
     * model, to an equivalent set of values for the default RGB model.
     * <p>
     * The {@code saturation} and {@code brightness} components
     * should be floating-point values between zero and one
     * (numbers in the range 0.0-1.0).  The {@code hue} component
     * can be any floating-point number.  The floor of this number is
     * subtracted from it to create a fraction between 0 and 1.  This
     * fractional number is then multiplied by 360 to produce the hue
     * angle in the HSB color model.
     * <p>
     * The integer that is returned by {@code HSBtoRGB} encodes the
     * value of a color in bits 0-23 of an integer value that is the same
     * format used by the method
     * This integer can be supplied as an argument to the
     * {@code Color} constructor that takes a single integer argument.
     * @param     h   цветовая составляющая цвета
     * @param     s   насыщенность цвета
     * @param     b   яркость цвета
     * @param 	  a   альфа-компонент
     * @return    the RGB value of the color with the indicated hue,
     *                            saturation, and brightness.
     * @see       java.awt.Color#getRGB()
     * @see       java.awt.Color#Color(int)
     * @see       java.awt.image.ColorModel#getRGBdefault()
     * @since     1.0
     */
	public static Color getHSBColor(double h, double s, double b, double a) {
		int alpha = ( ((int)(255 * a))<<8*3);
		int RGB = Color.HSBtoRGB((float)h, (float)s, (float)b)&(~(0xFF<<(8*3)));
		return new Color(RGB|alpha, true);
	}

	/**Делает текстовый компонент отменяемым (ctrl+z ctr+y)
	 * @param pTextComponent 
	 */
	public static void makeUndoable(JTextComponent pTextComponent) {
		final UndoManager undoMgr = new UndoManager();

		// Add listener for undoable events
		pTextComponent.getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent evt) {
				if(evt.getEdit() instanceof javax.swing.text.AbstractDocument.DefaultDocumentEvent de){
					if(de.getType() == DocumentEvent.EventType.CHANGE) return; //Нас не интересует изменение стиля!
				}
				undoMgr.addEdit(evt.getEdit());
			}
		});

		// Add undo/redo actions
		pTextComponent.getActionMap().put(UNDO_ACTION, new AbstractAction(UNDO_ACTION) {
			public void actionPerformed(ActionEvent evt) {
				try {
					if (undoMgr.canUndo()) {
						undoMgr.undo();
					}
				} catch (CannotUndoException e) {
					e.printStackTrace();
					try {
						if (undoMgr.canRedo()) {
							undoMgr.redo();
						}
					} catch (CannotRedoException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		pTextComponent.getActionMap().put(REDO_ACTION, new AbstractAction(REDO_ACTION) {
			public void actionPerformed(ActionEvent evt) {
				try {
					if (undoMgr.canRedo()) {
						undoMgr.redo();
					}
				} catch (CannotRedoException e) {
					e.printStackTrace();
				}
			}
		});

		// Create keyboard accelerators for undo/redo actions (Ctrl+Z/Ctrl+Y)
		pTextComponent.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), UNDO_ACTION);
		pTextComponent.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), REDO_ACTION);
	}
    /**Добавляет к текстовому компоненту слушателя событий изменения содержимого
     * @param text текстовое поле
     * @param newText будет вызывана, когда текст изменится
     */
    public static<T extends javax.swing.text.JTextComponent> T addTextEditListener(T text, java.util.function.Consumer<String> newText){
        text.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private String old_text = null;
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {warn();}
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) {warn();}
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) {warn();}
            public void warn() {
                var new_text = text.getText();
                if(new_text.equals(old_text)) return;
                old_text = new_text;
                //Используем Promise потому что если вызывающая функция захочет сделать что либо с документом - будет исключение
               new kerlib.jpromise.Promise<>(resolve ->{
                   newText.accept(text.getText());
                   return resolve.apply(null);
               });
            }
         });
        return text;
    }
    
    /**Выдеоляет текст у панели
	 * @param panel панель, на которой выделяем текст
	 * @param startpos с какой позиции выделять. Сивол на панели начиная с 0
	 * @param lenght до какой позиции, включительно, выделять
	 * @param color цвет выделения
	 * @param isBolid жирнить?
	 * @param isUnderline подчёркивать?
	 */
	public static void selectText(javax.swing.JTextPane panel, int startpos, int lenght, Color color, boolean isBolid, boolean isUnderline){
		if(lenght < 0)
			throw new IllegalArgumentException("Длина не может быть отрицательной!!!");
        else if(lenght == 0)
            return;
		final var doc = panel.getStyledDocument();
		final var sas = new javax.swing.text.SimpleAttributeSet();
		
		javax.swing.text.StyleConstants.setForeground(sas, color);
		javax.swing.text.StyleConstants.setBold(sas, isBolid);
		javax.swing.text.StyleConstants.setUnderline(sas, isUnderline);
		doc.setCharacterAttributes(startpos, lenght, sas, false);
		javax.swing.text.StyleConstants.setForeground(sas, Color.BLACK);
		javax.swing.text.StyleConstants.setBold(sas, false);
		javax.swing.text.StyleConstants.setUnderline(sas, false);
		final var end = startpos + lenght;
		doc.setCharacterAttributes(end,0, sas, false);
	}
}
