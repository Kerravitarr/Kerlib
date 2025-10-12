package kerlib.graphs;

import java.util.Date;

public class AxisDate extends Axis<Date>{
    public AxisDate(String name, String unit) {
        super(name, unit);
    }
    @Override
    protected double transformLocal(Date v) {
        return v.getTime();
    }    
}
