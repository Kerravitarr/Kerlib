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

/**Панель графика. На нём будет всё рисоваться
 * @author Kerravitarr
 */
public class ChartPanel extends javax.swing.JPanel implements EventListener {
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
		final var of = g.getFont();
		final var nf = of.deriveFont(12f);
		final var smalF = of.deriveFont(10f);
		final var os = g.getStroke();
		final var oc = g.getColor();
		g.setFont(nf);
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
				g.draw(new Line2D.Double(x,maxY - th/2,x + LENGHT_GRAPH_TEST,maxY - th/2));
				g.setColor(oc);
				if(graph.stroke != null)g.setStroke(os);
				
				x += LENGHT_GRAPH_TEST;
				final var tw = tools.drawString(g, x, maxY, graph.name, tools.alignmentX.left, tools.alignmentY.top);
				x += tw.getWidth();
			}
			maxY -= th; //Отступаем от подписей внизу
			maxY -= th; //Задел под ось X
			//Теперь у нас maxY проходит по нижней границе графика. Это по факту линия на которой рисуем график
			final var maxValues = (maxY - minY) / (th * 2);
			for(kerlib.graphs.Axis y : Y){
				if(!y.isNeedSignature || y.isEmpty()) continue;
				final var wt = Math.max(tools.getTextWidth(nf, y.pname),tools.getTextWidth(smalF, y.unit));
				final var wn = Math.max(tools.getTextWidth(nf, y.format.apply(y.maximum)),tools.getTextWidth(nf, y.format.apply(y.minimum)));
				final var wmax = Math.max(wt, wn) + 5; //5 пикселя для разделения
				minX += wmax;
				final var nr = tools.drawString(g, minX, minY, y.pname, tools.alignmentX.right, tools.alignmentY.bottom);
				double heightUint = 0;
				if(!y.unit.isBlank()){
					g.setFont(smalF);
					heightUint = tools.drawString(g, minX, minY + nr.getHeight(), y.unit, tools.alignmentX.right, tools.alignmentY.bottom).getHeight();
					g.setFont(of);
				}
				final var my = minY + ( nr.getHeight() + heightUint) * 2;
				final var deltaV = y.maximum - y.minimum;
				y.scale = (maxY - minY) / deltaV;
				y.p0 = -maxY;
				final var step = deltaV / maxValues;
				var pv = "";
				for(var i = 0d; i < maxValues; i ++){
					var ty = maxY - i * (th * 2);
					if(ty < my) break;
					final var v = y.format.apply(y.minimum + i * step);
					if(v.equals(pv))continue;
					pv = v;
					ty = y.toPx(Double.parseDouble(v.replaceAll(" ", "").replace(',', '.')));
					tools.drawString(g, minX, ty, v, tools.alignmentX.right, tools.alignmentY.center);
					g.draw(new Line2D.Double(minX-1,ty,minX+1,ty));
				}
				g.draw(new Line2D.Double(minX,minY,minX,maxY));
			}
			//Ну и теперь можно нарисовать Х
			if(!X.isEmpty()){
				g.draw(new Line2D.Double(minX,maxY,maxX,maxY));
				final var wn = Math.max(tools.getTextWidth(nf, X.format.apply(X.maximum)),tools.getTextWidth(nf, X.format.apply(X.minimum)));
				
				final var ur = tools.drawString(g, maxX, maxY, X.unit, tools.alignmentX.right, tools.alignmentY.bottom);
				final var nr = tools.drawString(g,maxX - ur.getWidth(), maxY, X.pname, tools.alignmentX.right, tools.alignmentY.bottom);
				final var mx = maxX + (- ur.getWidth() - nr.getWidth())*2;
				
				final var deltaV = X.maximum - X.minimum;
				X.scale = (maxX - minX) / deltaV;
				X.p0 = minX;
				final var maxXValues = (maxX - minX) / wn;
				final var step = deltaV / maxXValues;
				var pv = "";
				for(var i = 0d; i < maxXValues; i ++){
					var tx = minX + i * wn;
					if(tx >= mx) break;
					final var v = X.format.apply(X.minimum + i * step);
					if(v.equals(pv)) continue;
					pv = v;
					tx = X.toPx(Double.parseDouble(v.replaceAll(" ", "").replace(',', '.')));
					tools.drawString(g, tx, maxY, v, tools.alignmentX.center, tools.alignmentY.bottom);
				}
				g.draw(new Line2D.Double(minX,minY,minX,maxY));
			}
			//А теперь графики...
			{
				for(kerlib.graphs.Graph graph : graphs){
					if(graph.points.isEmpty()) continue;
					final var transformer = new Object(){
						public double x(java.awt.geom.Point2D p){return X.toPx(p.getX());}
						public double y(java.awt.geom.Point2D p){return graph.Y.toPx(p.getY());}
					};
					final var p = new java.awt.geom.Path2D.Double();
					final var start = graph.get(0);
					p.moveTo(transformer.x(start), transformer.y(start));
					graph.points.forEach(point -> p.lineTo(transformer.x((Point2D) point), transformer.y((Point2D) point)));
					
					if(graph.stroke != null)g.setStroke(graph.stroke);
					g.setColor(graph.color);
					g.draw(p);
					g.setColor(oc);
					if(graph.stroke != null)g.setStroke(os);
				}
			}
		}*/
		g.setFont(of);
	}
	
	@Override
	public void setLayout(LayoutManager mgr) {
        if(mgr != null) super.setLayout(mgr);
    }
	/**Пересчитывает по новой все оси*/
	private void updateAxis(){
		/*for(kerlib.graphs.Axis y : Y){
			y.reset();
		}
		for(kerlib.graphs.Graph g : graphs){
			g.points.forEach(p -> {g.X.add(((java.awt.geom.Point2D)p).getX());g.Y.add(((java.awt.geom.Point2D)p).getY());});
		}*/
		repaint();
	}
	/**Показывает всплывающую подсказку с соответствующим текстом около мышки
	 * @param text
	 */
	public void showPopup(String text) {
		if(text.startsWith("<html>"))
			text = text.replace("<html>", "").replace("</html>", "");
		else
			text = text.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>").replaceAll(" ", "&nbsp;");

		toolTip.setTipText("<html>" + text);
		hidePopup();
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
		for(kerlib.graphs.Axis y : Y){
			if(y.isEmpty()) continue;
			if(!text.isEmpty()) text.append("\n");
			text.append(y.name);
			text.append(": ");
			text.append(y.format.apply(y.toVal(p.y)));
			if(!y.unit.isEmpty()){
				text.append(", ");
				text.append(y.unit);
			}
		}
		if(text.isEmpty()) return;
		text.append("\n");
		/*text.append(X.name);
		text.append(": ");
		text.append(X.format.apply(X.toVal(p.x)));
		if(!X.unit.isEmpty()){
			text.append(", ");
			text.append(X.unit);
		}*/
		showPopup(text.toString());
    }//GEN-LAST:event_formMouseMoved


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
