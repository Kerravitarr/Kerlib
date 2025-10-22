/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package kerlib.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import kerlib.draw.tools;
import kerlib.draw.tools.alignmentX;
import kerlib.draw.tools.alignmentY;
import kerlib.graphs.Graph.GraphUpdateEvent;

/**Панель графика. На нём будет всё рисоваться
 * @author Kerravitarr
 */
public class ChartPanel extends javax.swing.JPanel implements EventListener, Graph.GraphChangeListener {
	///Все вертикальные оси
	private List<Axis> Y = new ArrayList<>();
	///Все горизонтальные оси
	private List<Axis> X = new ArrayList<>();


	/**Все графика*/
	private List<Graph<?,?,?>> graphs = new ArrayList<>();
	/**Длина в пикселях образца линии графика*/
	private static final int LENGHT_GRAPH_TEST = 20;
	/**Всплывающая подсказка около мышки*/
	protected Popup popup = null;
	/**А это всплывающая подсказка, её физическое воплащение*/
	protected final JToolTip toolTip = new JToolTip();

	/** Creates new form ChartPanel */
	public ChartPanel() {
		initComponents();
	}
	///Добавляет график для отображения
	/// @param graph новый график
	/// @return график, который добавили
	public <T,XT,YT> Graph<T,XT,YT> addGraph(Graph<T,XT,YT> graph){
		if(graph.X == null){
			if(X.size() == 1)
				graph.X = X.get(0);
			else 
				throw new IllegalArgumentException("Нужно указать ось Х для графика " + graph.name + " или указать ось Х для всех графиков");
		}
		if(graph.Y == null){
			if(Y.size() == 1)
				graph.Y = Y.get(0);
			else 
				throw new IllegalArgumentException("Нужно указать ось Y для графика " + graph.name + " или указать ось Y для всех графиков");
		}
        graphs.add(graph);
		graph.set(this);
		if(!Y.contains(graph.Y)){
			Y.add(graph.Y);
		}
		if(!X.contains(graph.X)){
			X.add(graph.X);
		}
		return graph;
    }
	/**Очищает поле*/
	public void clear(){
		graphs.clear();
		Y.clear();
		X.clear();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		try {
			super.paintComponent(g);
			paintComponent((Graphics2D) g);
		}catch(java.util.ConcurrentModificationException ex){
		} catch (Exception ex) {
			Logger.getLogger(ChartPanel.class.getName()).log(Level.SEVERE, "Не смогли отрисовать", ex);
		}
	}
	
	/** @param g холст, на котором надо отрисовать панель*/
	protected void paintComponent(Graphics2D g){
		var minX = 0d;
		var maxX = (double)getWidth();
		var minY = 10d;
		var maxY = (double)getHeight();
		var of = g.getFont();
		var smalF = of.deriveFont(10f);
		var g3 = new kerlib.draw.UsableGraphics(g);
		///Этап рисования графика I - отрисовка осей
		{
			var defH = tools.getTextHeight(of, "А");
			var betweenAxis = defH/2; //Запас между двумя соседними осями
			//Мы можем сразу сдвинуть maxY, оставив там запас под подписи осей X
			maxY -= (X.size()/2+X.size()%2) * (defH + betweenAxis);
			//А так же сдвинуь minX, на максимальную велечину мезду запасом под X и подписями под Y
			minY += ((X.size()/2) * (defH + betweenAxis));
			var lengthY = maxY - minY;
			for(var i = 0 ; i < Y.size(); i++){
				//Нужно узнать, сколько оси нужно пространства...
				//Вертикальная ось должна содержать подписи с цифрами
				//Ну и размерность, которая пишется сверху
				var y = Y.get(i);
				var maxW = 0;
				if(y.isNeedSignature){
					//Подписи будут в два ряда, так что находим наибольшую из ширин
					maxW = Math.max(tools.getTextWidth(g, y.name),tools.getTextWidth(smalF, y.unit));
				}
				if(!y.isEmpty()){
					maxW = Math.max(maxW, y.maxWidth(g, (int)lengthY, i % 2 == 0));
				}
				if(i % 2 == 0){
					var xl = minX + maxW + betweenAxis;
					g3.drawLine(xl, minY, xl, maxY);
					var ty = minY;
					if(y.isNeedSignature){
						if(!y.name.isBlank()){
							var size = kerlib.draw.tools.drawString(g, xl, ty, y.name, alignmentX.right, alignmentY.bottom);
							ty += size.getHeight();
						}
						if(!y.unit.isBlank()){
							var size = kerlib.draw.tools.drawString(g, xl, ty,10, y.unit, alignmentX.right, alignmentY.bottom);
							ty += size.getHeight();
						}
						if(ty != minY)
							ty+=defH;
					}
					if(!y.isEmpty())
						y.drawVertical(xl,minY,ty);
					minX += maxW + betweenAxis;
				} else {
					var xl = maxX - maxW - betweenAxis / 2;
					g3.drawLine(xl, minY, xl, maxY);
					var ty = minY;
					if(y.isNeedSignature){
						if(!y.name.isBlank()){
							var size = kerlib.draw.tools.drawString(g, xl, ty, y.name, alignmentX.left, alignmentY.bottom);
							ty += size.getHeight();
						}
						if(!y.unit.isBlank()){
							var size = kerlib.draw.tools.drawString(g, xl, ty,10, y.unit, alignmentX.left, alignmentY.bottom);
							ty += size.getHeight();
						}
						if(ty != minY)
							ty+=defH;
					}
					if(!y.isEmpty())
						y.drawVertical(xl,minY,ty);
					maxX -= maxW + betweenAxis;
				}
			}
			//А теперь оси Х!
			var lengthX = (int)(maxX - minX);
			for(var i = 0 ; i < X.size(); i++){
				var x = X.get(i);
				if(i % 2 == 0){
					var yl = maxY + (i/2) * (defH + betweenAxis);
					g3.drawLine(minX, yl, maxX, yl);
					var mx = maxX;
					if(x.isNeedSignature){
						if(!x.unit.isBlank()){
							var size = kerlib.draw.tools.drawString(g, mx, yl+defH,10, x.unit, alignmentX.right, alignmentY.top);
							mx -= size.getWidth();
						}
						if(!x.name.isBlank()){
							var size = kerlib.draw.tools.drawString(g, mx, yl+defH,x.name, alignmentX.right, alignmentY.top);
							mx -= size.getWidth();
						}
					}
					if(!x.isEmpty())
						x.drawHorizontal(g,true,minX,lengthX,yl,mx);
				} else {
					var yl = minY - (i/2) * (defH + betweenAxis);
					g3.drawLine(minX, yl, maxX, yl);
					var mx = maxX;
					if(x.isNeedSignature){
						if(!x.unit.isBlank()){
							var size = kerlib.draw.tools.drawString(g, mx, yl,10, x.unit, alignmentX.right, alignmentY.top);
							mx -= size.getWidth();
						}
						if(!x.name.isBlank()){
							var size = kerlib.draw.tools.drawString(g, mx, yl,x.name, alignmentX.right, alignmentY.top);
							mx -= size.getWidth();
						}
					}
					if(!x.isEmpty())
						x.drawHorizontal(g,false,minX,lengthX,yl,mx);
				}
			}
		}
		//И этап II - отрисовка графиков
		{
			for(var graph : graphs){
				if(graph.isEmpty()) continue;
				graph.styles.forEach(s -> s.set(g));
				graph.draw(g);
				graph.styles.forEach(s -> s.unset(g));
			}
		}
		/*final var nf = of.deriveFont(12f);
		final var smalF = of.deriveFont(10f);
		g.setFont(nf);*/
		/*{
			var x = 0;
			final var th = tools.getTextHeight(nf, "ВЫСОТА ТЕКСТА") * 1.01;
			//Подписи под графиком, легенда
			for(kerlib.graphs.Graph graph : graphs){
				if(!graph.isNeedSignature) continue;
				
				if(x != 0){
					final var w = tools.getTextWidth(nf, graph.name);
					if(x + LENGHT_GRAPH_TEST + w > maxX){
						x = 0;
						maxY -= th; //Переходим к следующей строке
					}
				}
				
				if(graph.stroke != null)g.setStroke(graph.stroke);
				g.setColor(graph.color);
				g.drawVertical(new Line2D.Double(x,maxY - th/2,x + LENGHT_GRAPH_TEST,maxY - th/2));
				g.setColor(oc);
				if(graph.stroke != null)g.setStroke(os);
				
				x += LENGHT_GRAPH_TEST;
				final var tw = tools.drawString(g, x, maxY, graph.name, tools.alignmentX.left, tools.alignmentY.top);
				x += tw.getWidth();
			}
		}*/
	}
	
	@Override
	public void setLayout(LayoutManager mgr) {
        if(mgr != null) super.setLayout(mgr);
    }
	/**Показывает всплывающую подсказку с соответствующим текстом около мышки
	 * @param text
	 */
	public void showPopup(String text) {
		if(text.startsWith("<html>"))
			text = text.replace("<html>", "").replace("</html>", "");
		else
			text = kerlib.tools.escape(text);

		toolTip.setTipText("<html>" + text);
		hidePopup();
        if(text.isBlank()) return;
		final Point position = getMousePosition();
		final Point loc = getLocationOnScreen();
		if (position == null) {
			popup = PopupFactory.getSharedInstance().getPopup(ChartPanel.this, toolTip, loc.x, loc.y);
		} else {
			popup = PopupFactory.getSharedInstance().getPopup(ChartPanel.this, toolTip, loc.x + position.x + 10, loc.y + position.y);
		}
		popup.show();
	}	
	/**Скрывает всплывающую подсказку */
	public void hidePopup() {
		if (popup != null) {
			popup.hide();
		}
	}
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        final var p = evt.getPoint();
		final var text = new StringBuilder();
		for(var y : Y){
			if(y.isEmpty()) continue;
            var val = y.YtoString(p.getY());
            if(val == null) continue;
			if(!text.isEmpty()) text.append("\n");
			text.append(y.name);
			text.append(": ");
			text.append(val);
			if(!y.unit.isEmpty()){
				text.append(", ");
				text.append(y.unit);
			}
		}
		for(var x : X){
			if(x.isEmpty()) continue;
            var val = x.XtoString(p.getX());
            if(val == null) continue;
			if(!text.isEmpty()) text.append("\n");
			text.append(x.name);
			text.append(": ");
			text.append(val);
			if(!x.unit.isEmpty()){
				text.append(", ");
				text.append(x.unit);
			}
		}
		showPopup(text.toString());
    }//GEN-LAST:event_formMouseMoved

	@Override
	public void graphChanged(GraphUpdateEvent event) {
		revalidate();
		repaint();
	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
