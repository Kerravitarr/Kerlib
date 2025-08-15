/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author Kerravitarr
 */
public class LComboBox<T> extends JComboBox<T> {
	public LComboBox(){
		super();
	}
	public void setModel(T[] values){
		setModel(Arrays.stream(values).toList());
	}
	public void setModel(List<T> values){
		var model = new DefaultComboBoxModel<T>();
		model.addAll(values);
		setModel(model);
		if(values.isEmpty())
			setSelectedItem(null);
		else 
			setSelectedIndex(0);
	}
}
