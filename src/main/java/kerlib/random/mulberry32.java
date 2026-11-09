///
/// Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
/// Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
///

package kerlib.random;

import java.util.random.RandomGenerator;

///Детерминированный  ГСЧ (mulberry32).
///Этот генератор используется, как правило, в JS и TS
///
/// @author Ilia Pushkin (github.com/Kerravitarr)
public class mulberry32 implements RandomGenerator{
	private int s;
    
    public mulberry32(int seed) {
		this.s = seed ^ 0x9e3779b9;
		nextLong();
		nextLong(); // прогрев
	}

    @Override
    public long nextLong() {
        int t = (s += 0x6d2b79f5);
		t = (t ^ (t >>> 15)) * (t | 1);
		t ^= t + (t ^ (t >>> 7)) * (t | 61);
		return ((t ^ (t >>> 14)) & 0xFFFFFFFFL);
    }
}
