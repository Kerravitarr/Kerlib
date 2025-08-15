/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
//Версия от 5 опреля 2023
package kerlib.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *Опции командной строки
 * 
 * @author zeus
 */
public class CMDOptions implements java.lang.Iterable{
	/**Все опции командной строки*/
	private Map<String,String> soptions; 
	/**Объекты опций*/
	private List<Option> opts; 	//Опции
	
	public CMDOptions(String[] args){
		soptions = new HashMap<>();
		var sb = new StringBuilder();
		for(var i : args)
			sb.append(i);
		for(var arg : sb.toString().split("-")){
			if(arg.isEmpty()) continue;
			if(arg.charAt(0) == '-'){//Длинный аргумент
				var iV = arg.indexOf('=');
				var key = arg.substring(1, iV);
				soptions.put(key	, arg.length() > (iV+1) ? arg.substring(iV+1) : null);
			} else {
				soptions.put(String.valueOf(arg.charAt(0))	, arg.length() > 1 ? arg.substring(1) : null);
			}
		}
		opts = new ArrayList<>();
	}

	/**
	 * Позволяет получить опции командной строки
	 * @return карта, где ключ - ключ, а значение - значение
	 */
	public Map<String,String> getCmdParams(){return soptions;}
	/**
	 * Добавляет опцию
	 * @param opt опция, которую надо добавить
	 * @throws IllegalArgumentException, если такая опция уже существует
	 */
	public void add(Option opt){
		if(contain(opt.name)){
			throw new IllegalArgumentException(String.format("Добавить опцию '%c' невозможно, так как она уже находится в карте!",opt.name));
		}else{
			opt._status = state.add;
			opts.add(opt);
		}
	}
	/**
	 * Удаляет опцию
	 * @param symbol опция, которую надо удалить
	 * @throws IllegalArgumentException, если такая опция уже существует
	 */
	public void remove(char symbol){
		if (contain(symbol) && get(symbol)._status != state.remove) {
			get(symbol)._status = state.remove;
		} else {
			throw new IllegalArgumentException(String.format("Опция '%c' несуществует!", symbol));
		}
	}
	/**
	 * Обновляет существующую опцию
	 * Если у новой опции не задано описание и тот-же масштаб, то
	 * опция считается не обновлённой, так как по факту мы просто обновили минимум-максимум
	 * @param opt опция, которую надо обновить
	 * @throws IllegalArgumentException, если такая опция уже существует
	 */
	public void update(Option opt){
		if(!contain(opt.name) && get(opt.name)._status != state.remove){
			throw new IllegalArgumentException(String.format("Опция '%c' несуществует!", opt.name));
		}else{
			kerlib.cmd.Option erase = opts.stream().filter(test -> (test.getName() == opt.name)).findFirst().get();
			var index = opts.indexOf(erase);
			String help = opt.help == null ? erase.help : opt.help;
			opts.set(index, opt);
			opt.help = help;
			if(opt.help != null) //Обновляется, по настоящему, опция только тогда, когда её описание меняется. Всё остальное - детали
				opt._status = state.update;
		}
	}
	/**
	 * Сохраняет новое значение для опции.
	 * @param symbol опция
	 * @param val её значение
	 */
	public void setValue(char symbol, String val){
		for(kerlib.cmd.Option i : opts)
			i.isSetVal = false;
		soptions.put(String.valueOf(symbol), val);
	}
	/**
	 * Проверяет, существует-ли такая опция?
	 * @param symbol опция, которую ищем
	 * @return true, если такая опция нам известна
	 */
	public boolean contain(char symbol){
		return opts.stream().filter(test -> (test.getName() == symbol)).findFirst().orElse(null) != null;
	}
	/**
	 * Возвращает опцию по её символу
	 * @param symbol символ опции
	 * @return опция, которую ищем
	 * @throws IllegalArgumentException, если такая опция уже существует
	 */
	public Option get(char symbol){
		kerlib.cmd.Option find = opts.stream().filter(test -> (test.getName() == symbol)).findFirst().orElse(null);
		if (find != null) {
			if(!find.isSetVal){
				if(soptions.containsKey(String.valueOf(symbol)))
					find.setValue(soptions.get(String.valueOf(symbol)));
				find.isSetVal = true;
			}
			return find;
		} else {
			throw new IllegalArgumentException(String.format("Опция '%c' несуществует!", symbol));
		}
	}
	/**
	 * Обновляет все значения всех опций и сохраняет их
	 */
	private void updateAll(){
		for(kerlib.cmd.Option i : opts){
			if(!i.isSetVal){
				if(soptions.containsKey(String.valueOf(i.name)))
					i.setValue(soptions.get(String.valueOf(i.name)));
				i.isSetVal = true;
			}
		}
	}

	@Override
	public Iterator<Option> iterator() {updateAll(); return opts.iterator();}
}
