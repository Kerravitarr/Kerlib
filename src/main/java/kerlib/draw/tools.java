/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.Color;
import java.awt.Font;
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
 * Утилиты для работы с графикой и UI компонентами.
 * Предоставляет методы для рисования стрелок, текста, окружностей и работы с цветами.
 */
public class tools {
    private static final AffineTransform affinetransform = new AffineTransform();
	private static final FontRenderContext frc = new FontRenderContext(affinetransform,true,true);  
	/**Отмена*/
	private final static String UNDO_ACTION = "Undo";
	/**Примена*/
	private final static String REDO_ACTION = "Redo";
    
    
	/**Направление стрелки.*/
	public static enum derect {
		Up, Down, 
		/**Стрелка смотреть будет в левую сторону - <*/
		Left, 
		/**Стрелка смотреть будет в левую сторону - >*/
		Right,
	}
	/**Выравнивание текста по горизонтали.*/
	public static enum alignmentX {
		/** Текст будет упираться в левую границу, то есть сдвигаться вправо. Значение по умолчанию */
		left, 
		center,
		/** Текст будет упираться в правую границу, то есть сдвигаться влево */
		right
	}
	/**Выравнивание текста по вертикали.*/
	public static enum alignmentY {
		/** Текст будет упираться в нижнюю границу. То есть всегда леэать на оси y сверху */
		top, center,
		/** Текст будет упираться в врехнюю границу. То есть всегда под осью */
		bottom
	}

	/**
	 * Рисует стрелку в заданном направлении.
	 * @param g графический контекст
	 * @param x0 координата X начала стрелки
	 * @param y0 координата Y начала стрелки
	 * @param d направление стрелки
	 * @param lenght длина усов стрелки
	 * @param angl угол между усами стрелки в градусах
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
	 * Рисует стрелку с заданным углом направления.
	 * @param g графический контекст
	 * @param x0 координата X начала стрелки
	 * @param y0 координата Y начала стрелки
	 * @param d направление стрелки в радианах
	 * @param lenght длина усов стрелки
	 * @param angl угол между усами стрелки в радианах
	 */
	public static void arrow(Graphics g, double x0, double y0, double d, double lenght, double angl) {
		double an = d + angl;
		g.drawLine(kerlib.tools.round(x0), kerlib.tools.round(y0), kerlib.tools.round(lenght * Math.cos(an) + x0), kerlib.tools.round(lenght * Math.sin(an) + y0));
		an = d - angl;
		g.drawLine(kerlib.tools.round(x0), kerlib.tools.round(y0), kerlib.tools.round(lenght * Math.cos(an) + x0), kerlib.tools.round(lenght * Math.sin(an) + y0));
	}
    /**
	 * Рисует заполненную стрелку.
	 * @param g графический контекст
	 * @param x0 координата X начала стрелки
	 * @param y0 координата Y начала стрелки
	 * @param d направление стрелки в радианах
	 * @param lenght длина усов стрелки
	 * @param angl угол между усами стрелки в радианах
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

	/**
	 * Рисует текст с выравниванием по горизонтали.
	 * @param g графический контекст
	 * @param x координата X позиции текста
	 * @param y координата Y позиции текста
	 * @param text текст для отображения
	 * @param alX выравнивание по горизонтали
	 * @return прямоугольник, ограничивающий текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y,String text, alignmentX alX){
		return drawString(g, x, y, text, alX, alignmentY.center);
	}
	/**
	 * Рисует текст заданного размера с выравниванием по горизонтали.
	 * @param g графический контекст
	 * @param x координата X позиции текста
	 * @param y координата Y позиции текста
	 * @param size размер шрифта
	 * @param text текст для отображения
	 * @param alX выравнивание по горизонтали
	 * @return прямоугольник, ограничивающий текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y,float size, String text, alignmentX alX){
		return drawString(g, x, y, size, text, alX, alignmentY.center);
	}
	/**
	 * Рисует текст заданного размера с выравниванием по горизонтали и вертикали.
	 * @param g графический контекст
	 * @param x координата X позиции текста
	 * @param y координата Y позиции текста
	 * @param size размер шрифта
	 * @param text текст для отображения
	 * @param alX выравнивание по горизонтали
	 * @param alY выравнивание по вертикали
	 * @return прямоугольник, ограничивающий текст
	 */
	public static Rectangle2D drawString(Graphics g, double x, double y,float size, String text, alignmentX alX, alignmentY alY){
		final var of = g.getFont();
		final var newFont = of.deriveFont(size);
		g.setFont(newFont);
		final var r = drawString(g, x, y, text, alX, alY);
		g.setFont(of);
		return r;
	}
	/**
	 * Рисует текст с выравниванием по горизонтали и вертикали.
	 * @param g графический контекст
	 * @param x координата X позиции текста
	 * @param y координата Y позиции текста
	 * @param text текст для отображения
	 * @param alX выравнивание по горизонтали
	 * @param alY выравнивание по вертикали
	 * @return прямоугольник, ограничивающий текст
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
	
	/**
	 * Возвращает высоту текста для заданного шрифта.
	 * @param font шрифт
	 * @param text текст
	 * @return высота текста в пикселях
	 */
	public static int getTextHeight(Font font, String text) {return (int)(font.getStringBounds(text, frc).getHeight());}
	
	/**
	 * Возвращает высоту текста для текущего шрифта графического контекста.
	 * @param g графический контекст
	 * @param text текст
	 * @return высота текста в пикселях
	 */
	public static int getTextHeight(Graphics g, String text) {return getTextHeight(g.getFont(), text);}
	
	/**
	 * Возвращает ширину текста для заданного шрифта.
	 * @param font шрифт
	 * @param text текст
	 * @return ширина текста в пикселях
	 */
	public static int getTextWidth(Font font, String text) {return (int)(font.getStringBounds(text, frc).getWidth());}
	
	/**
	 * Возвращает ширину текста для текущего шрифта графического контекста.
	 * @param g графический контекст
	 * @param text текст
	 * @return ширина текста в пикселях
	 */
	public static int getTextWidth(Graphics g, String text) {return getTextWidth(g.getFont(), text);}
	/**
	 * Рисует окружность.
	 * @param g графический контекст
	 * @param x координата X центра
	 * @param y координата Y центра
	 * @param r радиус
	 */
	public static void circle(Graphics g, double x, double y, double r) {
		final var d = kerlib.tools.round(r*2);
		g.drawOval(kerlib.tools.round(x - r), kerlib.tools.round(y - r), d, d);
	}
	/**
	 * Рисует окружность.
	 * @param g графический контекст
	 * @param x координата X центра
	 * @param y координата Y центра
	 * @param r радиус
	 */
	public static void circle(Graphics g, int x, int y, int r) {
		g.drawOval(x - r, y - r, r*2, r*2);
	}
	/**
	 * Рисует заполненную окружность.
	 * @param g графический контекст
	 * @param x координата X центра
	 * @param y координата Y центра
	 * @param r радиус
	 */
	public static void fillCircle(Graphics g, double x, double y, double r) {
		final var d = kerlib.tools.round(r*2);
		g.fillOval(kerlib.tools.round(x - r), kerlib.tools.round(y - r), d, d);
	}
	/**
	 * Рисует заполненную окружность.
	 * @param g графический контекст
	 * @param x координата X центра
	 * @param y координата Y центра
	 * @param r радиус
	 */
	public static void fillCircle(Graphics g, int x, int y, int r) {
		g.fillOval(x - r, y - r, r*2, r*2);
	}
	/**
	 * Создает цвет из компонентов HSB с альфа-каналом.
	 * @param h оттенок (hue) в диапазоне 0.0-1.0
	 * @param s насыщенность (saturation) в диапазоне 0.0-1.0
	 * @param b яркость (brightness) в диапазоне 0.0-1.0
	 * @param a альфа-канал (прозрачность) в диапазоне 0.0-1.0
	 * @return цвет в формате RGB с альфа-каналом
	 */
	public static Color getHSBColor(double h, double s, double b, double a) {
		int alpha = ( ((int)(255 * a))<<8*3);
		int RGB = Color.HSBtoRGB((float)h, (float)s, (float)b)&(~(0xFF<<(8*3)));
		return new Color(RGB|alpha, true);
	}

	/**
	 * Добавляет функциональность отмены/повтора (Ctrl+Z/Ctrl+Y) к текстовому компоненту.
	 * @param pTextComponent текстовый компонент для добавления функциональности
	 */
	public static void makeUndoable(JTextComponent pTextComponent) {
		final UndoManager undoMgr = new UndoManager();

		// Add listener for undoable events
		pTextComponent.getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent evt) {
				if(evt.getEdit() instanceof javax.swing.text.AbstractDocument.DefaultDocumentEvent de){
				if(de.getType() == DocumentEvent.EventType.CHANGE) return; // Игнорируем изменения стиля
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
    /**
	 * Добавляет слушателя изменений текста к текстовому компоненту.
	 * @param <T> тип текстового компонента
	 * @param text текстовый компонент
	 * @param newText функция, вызываемая при изменении текста
	 * @return текстовый компонент с добавленным слушателем
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
                // Используем Promise для избежания исключений при модификации документа из слушателя
               new kerlib.jpromise.Promise<>(resolve ->{
                   newText.accept(text.getText());
                   return resolve.apply(null);
               });
            }
         });
        return text;
    }
    
    /**
	 * Создает иконку в системном трее для приложения.
	 * @param mainFrame главное окно приложения
	 * @param puth_to_image путь до изображения для иконки, если она находится в ресурсах программы
	 * @param name название приложения для подсказки
	 * @return созданная иконка в трее
	 * @throws RuntimeException если не удалось создать иконку
	 */
    public static java.awt.TrayIcon makeTray(java.awt.Frame mainFrame, String puth_to_image, String name){
        try{
            var constResource = tools.class.getClassLoader().getResource(puth_to_image);
            if (constResource == null) {
                throw new RuntimeException("Файл "+puth_to_image+" не найден в ресурсах");
            }
            var originalIcon = javax.imageio.ImageIO.read(constResource);
            return  makeTray(mainFrame, originalIcon, name);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
		}
    }
    /**
	 * Создает иконку в системном трее для приложения.
	 * @param mainFrame главное окно приложения
	 * @param image изображение для иконки
	 * @param name название приложения для подсказки
	 * @return созданная иконка в трее
	 * @throws RuntimeException если не удалось создать иконку
	 */
    public static java.awt.TrayIcon makeTray(java.awt.Frame mainFrame, java.awt.Image image, String name){
        try {
			var icon = new java.awt.TrayIcon(image, name);
			icon.setImageAutoSize(true);

			icon.addActionListener(_ -> {
				mainFrame.setVisible(true);
				mainFrame.setExtendedState(java.awt.Frame.NORMAL);
			});
			java.awt.SystemTray.getSystemTray().add(icon);
            
            //Добавляем реакцию на кнопку "свернуть"
            mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowIconified(java.awt.event.WindowEvent e) {
                    mainFrame.setVisible(false);
                }
            });
            return icon;
		} catch (Exception e1) {
            throw new RuntimeException(e1);
		}
    }
}
